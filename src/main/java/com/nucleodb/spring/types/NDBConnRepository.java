package com.nucleodb.spring.types;

import com.nucleodb.library.database.tables.connection.Connection;
import com.nucleodb.library.database.tables.connection.ConnectionProjection;
import com.nucleodb.library.database.tables.table.DataEntry;
import com.nucleodb.library.database.utils.Pagination;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface NDBConnRepository<C extends Connection<F, T>, ID extends String, F extends DataEntry, T extends DataEntry> extends Repository<C, ID> {

  <S extends C> S save(S entity);
  <S extends C> Iterable<S> saveAll(Iterable<S> entities);

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

  Set<C> findAll();

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.CrudRepository#findAllById(java.lang.Iterable)
   */

  Set<C> findAllById(Iterable<ID> iterable);

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.PagingAndSortingRepository#findAll(org.springframework.data.domain.Sort)
   */
  Set<C> findAll(Sort sort);

  C findById(ID uuid);



  /**
   * Returns whether an entity with the given id exists.
   *
   * @param uuid must not be {@literal null}.
   * @return {@literal true} if an entity with the given id exists, {@literal false} otherwise.
   * @throws IllegalArgumentException if {@literal id} is {@literal null}.
   */
  boolean existsById(ID uuid);

  /**
   * Returns the number of entities available.
   *
   * @return the number of entities.
   */
  long count();

  /**
   * Deletes the entity with the given id.
   * <p>
   * If the entity is not found in the persistence store it is silently ignored.
   *
   * @param uuid must not be {@literal null}.
   * @throws IllegalArgumentException in case the given {@literal id} is {@literal null}
   */
  void deleteById(ID uuid);

  /**
   * Deletes a given entity.
   *
   * @param entity must not be {@literal null}.
   * @throws IllegalArgumentException in case the given entity is {@literal null}.
   * @throws OptimisticLockingFailureException when the entity uses optimistic locking and has a version attribute with
   *           a different value from that found in the persistence store. Also thrown if the entity is assumed to be
   *           present but does not exist in the database.
   */
  void delete(C entity);

  /**
   * Deletes all instances of the type {@code T} with the given IDs.
   * <p>
   * Entities that aren't found in the persistence store are silently ignored.
   *
   * @param ids must not be {@literal null}. Must not contain {@literal null} elements.
   * @throws IllegalArgumentException in case the given {@literal ids} or one of its elements is {@literal null}.
   * @since 2.5
   */
  void deleteAllById(Iterable<? extends ID> ids);

  /**
   * Deletes the given entities.
   *
   * @param entities must not be {@literal null}. Must not contain {@literal null} elements.
   * @throws IllegalArgumentException in case the given {@literal entities} or one of its entities is {@literal null}.
   * @throws OptimisticLockingFailureException when at least one entity uses optimistic locking and has a version
   *           attribute with a different value from that found in the persistence store. Also thrown if at least one
   *           entity is assumed to be present but does not exist in the database.
   */
  void deleteAll(Iterable<? extends C> entities);

  /**
   * Deletes all entities managed by the repository.
   */
  void deleteAll();
}
