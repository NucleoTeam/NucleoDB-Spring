package com.nucleodb.spring.types;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;


@NoRepositoryBean
public interface NDBDataRepository<T, ID> extends Repository<T, ID> {

  List<T> saveAll(Iterable<T> entities);

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.CrudRepository#findAll()
   */

  List<T> findAll();

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.CrudRepository#findAllById(java.lang.Iterable)
   */

  List<T> findAllById(Iterable<ID> iterable);

  void saveForget(T entity);
  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.PagingAndSortingRepository#findAll(org.springframework.data.domain.Sort)
   */
  List<T> findAll(Sort sort);

  T findById(ID id);



  /**
   * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
   * entity instance completely.
   *
   * @param entity must not be {@literal null}.
   * @return the saved entity; will never be {@literal null}.
   * @throws IllegalArgumentException in case the given {@literal entity} is {@literal null}.
   * @throws OptimisticLockingFailureException when the entity uses optimistic locking and has a version attribute with
   *           a different value from that found in the persistence store. Also thrown if the entity is assumed to be
   *           present but does not exist in the database.
   */
  T save(T entity);


  /**
   * Returns whether an entity with the given id exists.
   *
   * @param id must not be {@literal null}.
   * @return {@literal true} if an entity with the given id exists, {@literal false} otherwise.
   * @throws IllegalArgumentException if {@literal id} is {@literal null}.
   */
  boolean existsById(ID id);

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
   * @param id must not be {@literal null}.
   * @throws IllegalArgumentException in case the given {@literal id} is {@literal null}
   */
  void deleteById(ID id);

  /**
   * Deletes a given entity.
   *
   * @param entity must not be {@literal null}.
   * @throws IllegalArgumentException in case the given entity is {@literal null}.
   * @throws OptimisticLockingFailureException when the entity uses optimistic locking and has a version attribute with
   *           a different value from that found in the persistence store. Also thrown if the entity is assumed to be
   *           present but does not exist in the database.
   */
  void delete(T entity);

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
  void deleteAll(Iterable<? extends T> entities);

  /**
   * Deletes all entities managed by the repository.
   */
  void deleteAll();
}