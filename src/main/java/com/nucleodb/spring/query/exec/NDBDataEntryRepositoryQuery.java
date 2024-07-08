package com.nucleodb.spring.query.exec;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.nucleodb.library.NucleoDB;
import com.nucleodb.library.database.index.IndexWrapper;
import com.nucleodb.library.database.index.TreeIndex;
import com.nucleodb.library.database.tables.table.DataEntry;
import com.nucleodb.library.database.tables.table.DataEntryProjection;
import com.nucleodb.library.database.tables.table.DataTable;
import com.nucleodb.library.database.utils.Pagination;
import com.nucleodb.library.database.utils.Serializer;
import com.nucleodb.library.database.utils.exceptions.InvalidIndexTypeException;
import com.nucleodb.library.database.utils.exceptions.ObjectNotSavedException;
import com.nucleodb.spring.query.QueryParser;
import com.nucleodb.spring.query.common.ConditionOperation;
import com.nucleodb.spring.query.common.LookupOperation;
import com.nucleodb.spring.query.common.Operation;
import com.nucleodb.spring.query.common.OperatorProperty;
import com.nucleodb.spring.query.common.QueryOperation;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NDBDataEntryRepositoryQuery implements RepositoryQuery{
  private final Class classType;
  private final Method method;
  private Class<?> tableClass;
  private DataTable table;

  public NDBDataEntryRepositoryQuery(NucleoDB nucleoDB, Class classType, Method method) {
    this.classType = classType;
    Type[] actualTypeArguments = ((ParameterizedType) classType.getGenericSuperclass()).getActualTypeArguments();
    if (actualTypeArguments.length == 1) {
      this.tableClass = (Class<?>) actualTypeArguments[0];
      this.table = nucleoDB.getTable(this.tableClass);
    }
    this.method = method;
  }

  @Override
  public Object execute(Object[] parameters) {
    QueryOperation query = QueryParser.parse(method);
    Operation tmp = query;
    String currentConditional = null;
    Set<DataEntry> entries = null;
    Optional<Object> first = Arrays.stream(parameters).filter(o -> o instanceof DataEntryProjection).findFirst();
    DataEntryProjection dataEntryProjection = new DataEntryProjection(new Pagination(0, Integer.MAX_VALUE));
    dataEntryProjection.setWritable(false);
    if(first.isPresent()){
      dataEntryProjection = (DataEntryProjection) first.get();
    }

    int i = 0;
    while ((tmp = tmp.getNext()) != null) {
      if (tmp instanceof ConditionOperation) {
        currentConditional = ((ConditionOperation) tmp).getConditional();
      } else if (tmp instanceof LookupOperation) {
        OperatorProperty property = ((LookupOperation) tmp).getProperty();
        String indexKey = property.getPropertyName().replaceAll("_", ".").toLowerCase();
        if (indexKey.equals("key")) {
          indexKey = "id";
        }
        if (!indexKey.equals("id") && !table.getIndexes().keySet().contains(indexKey)) {
          return null; // do not handle non indexed properties
        }
        Set<DataEntry> lookupEntries = null;
        try {
          switch (property.getExpression()) {
            case "=" -> {
              lookupEntries = table.get(indexKey, parameters[i], dataEntryProjection);
            }
            case ">", "]" -> {
              lookupEntries = table.greaterThan(indexKey, parameters[i], dataEntryProjection);
            }
            case "<", "[" -> {
              lookupEntries = table.lessThan(indexKey, parameters[i], dataEntryProjection);
            }
            case "contains" -> {
              lookupEntries = table.search(indexKey, parameters[i], dataEntryProjection);
            }
            case "s%" -> {
              lookupEntries = table.startsWith(indexKey, (String) parameters[i], dataEntryProjection);
            }
            case "%s" -> {
              lookupEntries = table.endsWith(indexKey, (String) parameters[i], dataEntryProjection);
            }
          }
        } catch (InvalidIndexTypeException e) {
          throw new RuntimeException(e);
        }
        i++;
        if (lookupEntries == null)
          continue;
        if (currentConditional != null) {
          switch (currentConditional) {
            case "AND" -> {
              assert entries != null;
              entries.retainAll(lookupEntries);
            }
            case "OR" -> {
              assert entries != null;
              entries.addAll(lookupEntries);
            }
          }
          currentConditional = null;
        } else {
          entries = lookupEntries;
        }
      }
    }
    if(query.getMethod().equals("findBy")){
      Stream<DataEntry> dataEntryStream = entries.stream();
      if(Collection.class.isAssignableFrom(method.getReturnType())) {
        method.getReturnType();
        Type[] actualTypeArguments = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments();
        Class<?> returnClass = DataEntry.class;
        if (actualTypeArguments.length == 1) {
          returnClass = (Class<?>) actualTypeArguments[0];
        }
        if (method.getReturnType() == List.class) {
          if(DataEntry.class.isAssignableFrom(returnClass)){
            return dataEntryStream.collect(Collectors.toList());
          }else{
            if(entries.size()>0){
              Object data = dataEntryStream.findFirst().get().getData();
              if(data.getClass()==returnClass){
                return dataEntryStream.map(e->e.getData()).collect(Collectors.toList());
              }
            }
          }
        }else if (method.getReturnType() == Set.class) {
          if(DataEntry.class.isAssignableFrom(returnClass)){
            return dataEntryStream.collect(Collectors.toSet());
          }else{
            if(entries.size()>0){
              Object data = dataEntryStream.findFirst().get().getData();
              if(data.getClass()==returnClass){
                return dataEntryStream.map(e->e.getData()).collect(Collectors.toSet());
              }
            }
          }
        }
      }else if(DataEntry.class.isAssignableFrom(method.getReturnType())){
        return dataEntryStream.findFirst();
      }else{
        if(entries.size()>0) {
          Object data = dataEntryStream.findFirst().get().getData();
          if(data.getClass() == method.getReturnType()){
            return data;
          }
        }
      }
    }else if(query.getMethod().equals("streamBy")){
      return entries.stream();
    }else if(query.getMethod().equals("deleteBy")){
      CountDownLatch countDownLatch = new CountDownLatch(entries.size());
      entries.stream().map(de -> {
          try {
              return de.copy(table.getConfig().getDataEntryClass(), true);
          } catch (ObjectNotSavedException e) {
              throw new RuntimeException(e);
          }
      }).forEach(e->{
        table.deleteAsync(e, (dataEntry)->{
          countDownLatch.countDown();
        });
      });
      try {
        countDownLatch.await(5, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }else if(query.getMethod().equals("countBy")){
      return entries.size();
    }else if(query.getMethod().equals("existsBy")){
      return entries.size()>0;
    }

    return null;
  }

  @Override
  public QueryMethod getQueryMethod() {
    return null;
  }
}
