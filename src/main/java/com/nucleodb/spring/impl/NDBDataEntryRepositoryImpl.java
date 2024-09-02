package com.nucleodb.spring.impl;

import com.nucleodb.library.NucleoDB;
import com.nucleodb.library.database.tables.table.DataEntry;
import com.nucleodb.library.database.tables.table.DataTable;
import com.nucleodb.library.database.utils.exceptions.IncorrectDataEntryObjectException;
import com.nucleodb.spring.types.NDBDataRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class NDBDataEntryRepositoryImpl<T extends DataEntry, ID> implements NDBDataRepository<T, ID>{
  private @Nullable DataTable table = null;
  private final NucleoDB nucleoDB;
  private final Class<T> classType;
  private @Nullable Class<?> tableClass = null;
  private final ApplicationEventPublisher publisher;

  public NDBDataEntryRepositoryImpl(NucleoDB nucleoDB, Class<T> classType, ApplicationEventPublisher publisher) {
    this.nucleoDB = nucleoDB;
    this.classType = classType;
    this.publisher = publisher;
    Type[] actualTypeArguments = ((ParameterizedType) classType.getGenericSuperclass()).getActualTypeArguments();
    if (actualTypeArguments.length == 1) {
      this.tableClass = (Class<?>) actualTypeArguments[0];
      this.table = nucleoDB.getTable(this.tableClass);

    }
  }

  @Override
  public <S extends T> S save(S entity) {
    AtomicReference<S> returnedVal = new AtomicReference<>();
    try {
      table.saveAsync(entity, (de)->{
        returnedVal.set((S)de);
        synchronized (returnedVal) {
          returnedVal.notify();
        }
      });
      synchronized (returnedVal) {
        returnedVal.wait();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (IncorrectDataEntryObjectException e) {
      throw new RuntimeException(e);
    }
    return returnedVal.get();
  }

  @Override
  public <S extends T> List<S> saveAll(Iterable<S> entities) {
    List<S> items = new LinkedList<>();
    entities.forEach(entity->{
      S savedEntity = save(entity);
      if(savedEntity==null) return;
      items.add(savedEntity);
    });
    return items;
  }

  @Override
  public Optional<T> findById(ID id) {
    Set<DataEntry> dataEntrySet = table.get("id", id);
    if(dataEntrySet!=null && dataEntrySet.size()>0){
      return (Optional<T>) Optional.of(dataEntrySet.iterator().next());
    }
    return Optional.empty();
  }

  @Override
  public boolean existsById(ID id) {
    Set<DataEntry> dataEntrySet = table.get("id", id);
    if(dataEntrySet!=null && dataEntrySet.size()>0){
      return true;
    }
    return false;
  }

  @Override
  public List<T> findAll() {
    return (List<T>) new LinkedList<>(table.getEntries());
  }

  @Override
  public List<T> findAllById(Iterable<ID> iterable) {
    List<T> items = new LinkedList<>();
    for (ID id : iterable) {
      Optional<T> byId = findById(id);
      if(byId.isPresent()) {
        items.add(byId.get());
      }
    }
    return items;
  }

  @Override
  public long count() {
    return table.getSize();
  }

  @Override
  public void deleteById(ID id) {
    Optional<T> byId = findById(id);
    if(!byId.isPresent())
      return;
    delete(byId.get());
  }

  @Override
  public void delete(T entity) {
    try {
      table.deleteSync(entity);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deleteAllById(Iterable<? extends ID> ids) {
    ids.forEach(id->deleteById(id));
  }

  @Override
  public void deleteAll(Iterable<? extends T> entities) {
    entities.forEach(de->delete(de));
  }

  @Override
  public void deleteAll() {
    table.getEntries().forEach(de->delete((T) de));
  }

  @Override
  public List<T> findAll(Sort sort) {
    return null;
  }
}
