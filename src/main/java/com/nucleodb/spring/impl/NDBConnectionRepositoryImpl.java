package com.nucleodb.spring.impl;

import com.nucleodb.library.NucleoDB;
import com.nucleodb.library.database.tables.connection.Connection;
import com.nucleodb.library.database.tables.connection.ConnectionHandler;
import com.nucleodb.library.database.tables.connection.ConnectionProjection;
import com.nucleodb.library.database.tables.table.DataEntry;
import com.nucleodb.library.database.utils.InvalidConnectionException;
import com.nucleodb.library.database.utils.Pagination;
import com.nucleodb.library.database.utils.TreeSetExt;
import com.nucleodb.spring.types.NDBConnRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class NDBConnectionRepositoryImpl<C extends Connection<F, T>, ID extends String, T extends DataEntry, F extends DataEntry> implements NDBConnRepository<C, ID, F, T>{
  private @Nullable ConnectionHandler connectionHandler = null;
  private final NucleoDB nucleoDB;
  private final Class<C> classType;
  private final ApplicationEventPublisher publisher;
  public NDBConnectionRepositoryImpl(NucleoDB nucleoDB, Class<C> classType, ApplicationEventPublisher publisher) {
    this.nucleoDB = nucleoDB;
    this.classType = classType;
    this.connectionHandler = nucleoDB.getConnectionHandler(classType);
    this.publisher = publisher;
  }

  @Override
  public Set<C> getByTo(T entity) {
    return getByTo(entity, null, null);
  }

  @Override
  public Set<C> getByTo(T entity, Pagination pagination) {
    return getByTo(entity, pagination, null);
  }

  @Override
  public Set<C> getByTo(T entity, Predicate<C> filter) {
    return getByTo(entity, null, filter);
  }

  @Override
  public Set<C> getByTo(T entity, Pagination pagination, Predicate<C> filter) {
    ConnectionProjection connectionProjection = new ConnectionProjection();
    if(pagination!=null){
      connectionProjection.setPagination(pagination);
    }
    if(filter!=null){
      connectionProjection.setFilter((Predicate) filter);
    }
    return getByTo(entity, connectionProjection);
  }

  @Override
  public Set<F> getFromByTo(T entity) {
    return getByTo(entity).stream().map(c->c.fromEntry()).collect(Collectors.toSet());
  }
  @Override
  public Set<F> getFromByTo(T entity, Pagination pagination) {
    return getByTo(entity, pagination, null).stream().map(c->c.fromEntry()).collect(Collectors.toSet());
  }

  @Override
  public Set<F> getFromByTo(T entity, Predicate<C> filter) {
    return getByTo(entity, null, filter).stream().map(c->c.fromEntry()).collect(Collectors.toSet());
  }

  @Override
  public Set<F> getFromByTo(T entity, Pagination pagination, Predicate<C> filter) {
    return getByTo(entity, pagination, filter).stream().map(c->c.fromEntry()).collect(Collectors.toSet());
  }


  @Override
  public Set<C> getByFrom(F entity) {
    return getByFrom(entity, null, null);
  }

  @Override
  public Set<C> getByFrom(F entity, Predicate<C> filter) {
    return getByFrom(entity, null, filter);
  }

  @Override
  public Set<C> getByFrom(F entity, Pagination pagination) {
    return getByFrom(entity, pagination, null);
  }
  @Override
  public Set<C> getByFrom(F entity, Pagination pagination, Predicate<C> filter) {
    ConnectionProjection connectionProjection = new ConnectionProjection();
    if(pagination!=null){
      connectionProjection.setPagination(pagination);
    }
    if(filter!=null){
      connectionProjection.setFilter((Predicate) filter);
    }
    return getByFrom(entity, connectionProjection);
  }

  @Override
  public Set<T> getToByFrom(F entity) {
    return getByFrom(entity, null, null).stream().map(c->c.toEntry()).collect(Collectors.toSet());
  }
  @Override
  public Set<T> getToByFrom(F entity, Pagination pagination) {
    return getByFrom(entity, pagination, null).stream().map(c->c.toEntry()).collect(Collectors.toSet());
  }

  @Override
  public Set<T> getToByFrom(F entity, Predicate<C> filter) {
    return getByFrom(entity, null, filter).stream().map(c->c.toEntry()).collect(Collectors.toSet());
  }

  @Override
  public Set<T> getToByFrom(F entity, Pagination pagination, Predicate<C> filter) {
    return getByFrom(entity, pagination, filter).stream().map(c->c.toEntry()).collect(Collectors.toSet());
  }

  @Override
  public Set<C> getByFromAndTo(F fromEntity, T toEntity) {
    return getByFromAndTo(fromEntity, toEntity, null, null);
  }
  @Override
  public Set<C> getByFromAndTo(F fromEntity, T toEntity, Pagination pagination) {
    return getByFromAndTo(fromEntity, toEntity, pagination, null);
  }

  @Override
  public Set<C> getByFromAndTo(F fromEntity, T toEntity, Predicate<C> filter) {
    return getByFromAndTo(fromEntity, toEntity, null, filter);
  }

  @Override
  public Set<C> getByFromAndTo(F fromEntity, T toEntity, Pagination pagination, Predicate<C> filter) {
    ConnectionProjection connectionProjection = new ConnectionProjection();
    if(pagination!=null){
      connectionProjection.setPagination(pagination);
    }
    if(filter!=null){
      connectionProjection.setFilter((Predicate) filter);
    }
    return getByFromAndTo(fromEntity, toEntity, connectionProjection);
  }

  @Override
  public <S extends C> S save(S entity) {
    AtomicReference<S> returnedVal = new AtomicReference<>();
    try {
      connectionHandler.saveAsync(entity, (connection)->{
        returnedVal.set((S)connection);
        synchronized (returnedVal) {
          returnedVal.notify();
        }
      });
      synchronized (returnedVal) {
        returnedVal.wait();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (InvalidConnectionException e) {
      throw new RuntimeException(e);
    }
    return returnedVal.get();
  }

  @Override
  public <S extends C> Set<S> saveAll(Iterable<S> entities) {
    AtomicReference<Set<S>> returnedVal = new AtomicReference<>(new TreeSetExt<>());
    CountDownLatch countDownLatch = new CountDownLatch(Long.valueOf(StreamSupport.stream(entities.spliterator(), false).count()).intValue());
    StreamSupport.stream(entities.spliterator(), true).forEach(entity-> {
      try {
        connectionHandler.saveAsync(entity, (connection) -> {
          returnedVal.getAcquire().add((S) connection);
          countDownLatch.countDown();
        });
      } catch (InvalidConnectionException e) {
        countDownLatch.countDown();
        throw new RuntimeException(e);
      }
    });
    try {
      countDownLatch.await(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return returnedVal.get();
  }

  @Override
  public Optional<C> findById(ID id) {
    return (Optional) Optional.ofNullable(connectionHandler.getConnectionByUUID().get(id));
  }

  @Override
  public boolean existsById(ID id) {
    return findById(id).isPresent();
  }

  @Override
  public Set<C> findAll() {
    return (Set<C>) connectionHandler.getConnections();
  }

  @Override
  public Set<C> findAllById(Iterable<ID> ids) {
    return StreamSupport
        .stream(ids.spliterator(), true)
        .map(id->findById(id))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());
  }

  @Override
  public long count() {
    return connectionHandler.getConnections().size();
  }

  @Override
  public void deleteById(ID id) {
    Optional<C> byId = findById(id);
    if(byId.isPresent()){
      delete(byId.get());
    }
  }

  @Override
  public void delete(C entity) {
    try {
      connectionHandler.deleteSync(entity.copy(classType, true));
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deleteAllById(Iterable<? extends ID> ids) {
    StreamSupport
        .stream(ids.spliterator(), true)
        .forEach(id->deleteById(id));
  }

  @Override
  public void deleteAll(Iterable<? extends C> entities) {
    StreamSupport
        .stream(entities.spliterator(), true)
        .forEach(entity->delete(entity));
  }

  @Override
  public void deleteAll() {
    // not implemented unsafe!
  }

  @Override
  public C getById(String uuid) {
    return (C) connectionHandler.getConnectionByUUID().get(uuid);
  }

  @Override
  public Set<C> getByTo(T entity, ConnectionProjection projection) {
    return connectionHandler.getReverseByTo(entity, projection);
  }

  @Override
  public Set<F> getFromByTo(T entity, ConnectionProjection projection) {
    return getByTo(entity, projection).stream().map(c->c.fromEntry()).collect(Collectors.toSet());
  }

  @Override
  public Set<C> getByFrom(F entity, ConnectionProjection projection) {
    return connectionHandler.getByFrom(entity, projection);
  }

  @Override
  public Set<T> getToByFrom(F entity, ConnectionProjection projection) {
    return getByFrom(entity, projection).stream().map(c->c.toEntry()).collect(Collectors.toSet());
  }

  @Override
  public Set<C> getByFromAndTo(F fromEntity, T toEntity, ConnectionProjection projection) {
    return connectionHandler.getByFromAndTo(fromEntity, toEntity, projection);
  }
}
