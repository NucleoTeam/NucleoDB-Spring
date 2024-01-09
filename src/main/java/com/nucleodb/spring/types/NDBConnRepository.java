package com.nucleodb.spring.types;

import com.nucleodb.library.database.tables.connection.Connection;
import com.nucleodb.library.database.tables.connection.ConnectionProjection;
import com.nucleodb.library.database.tables.table.DataEntry;
import com.nucleodb.library.database.utils.Pagination;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface NDBConnRepository<C extends Connection<T, F>, ID extends String, T extends DataEntry, F extends DataEntry> extends CrudRepository<C, ID>{
  @Override
  <S extends C> S save(S entity);

  Set<C> getByTo(T entity);
  Set<C> getByTo(T entity, ConnectionProjection projection);
  Set<C> getByTo(T entity, Pagination pagination);
  Set<C> getByTo(T entity, Predicate<C> filter);
  Set<C> getByTo(T entity, Pagination pagination, Predicate<C> filter);
  Set<F> getFromByTo(T entity);
  Set<F> getFromByTo(T entity, ConnectionProjection projection);
  Set<F> getFromByTo(T entity, Pagination pagination);
  Set<F> getFromByTo(T entity, Predicate<C> filter);
  Set<F> getFromByTo(T entity, Pagination pagination, Predicate<C> filter);
  Set<C> getByFrom(F entity);
  Set<C> getByFrom(F entity, ConnectionProjection projection);
  Set<C> getByFrom(F entity, Predicate<C> filter);
  Set<C> getByFrom(F entity, Pagination pagination);
  Set<C> getByFrom(F entity, Pagination pagination, Predicate<C> filter);
  Set<T> getToByFrom(F entity);
  Set<T> getToByFrom(F entity, ConnectionProjection projection);
  Set<T> getToByFrom(F entity, Pagination pagination);
  Set<T> getToByFrom(F entity, Predicate<C> filter);
  Set<T> getToByFrom(F entity, Pagination pagination, Predicate<C> filter);
  Set<C> getByFromAndTo(F fromEntity, T toEntity);
  Set<C> getByFromAndTo(F fromEntity, T toEntity, ConnectionProjection projection);
  Set<C> getByFromAndTo(F fromEntity, T toEntity, Pagination pagination);
  Set<C> getByFromAndTo(F fromEntity, T toEntity, Predicate<C> filter);
  Set<C> getByFromAndTo(F fromEntity, T toEntity, Pagination pagination, Predicate<C> filter);

  C getById(String uuid);
}
