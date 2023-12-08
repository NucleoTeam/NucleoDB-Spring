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
import java.util.List;
import java.util.Set;
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
    DataEntryProjection dataEntryProjection = new DataEntryProjection(new Pagination(0, Integer.MAX_VALUE));
    dataEntryProjection.setWritable(false);
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
    return entries;
  }

  @Override
  public QueryMethod getQueryMethod() {
    return null;
  }
}
