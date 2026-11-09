
package repository;

import domain.Entity;
import exceptions.RepositoryException;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generic repository interface for storing domain entities.
 *
 * @param <ID> the type of the entity identifier
 * @param <E> the type of stored entity
 */
public interface Repository<ID, E extends Entity<ID>> {

    /**
     * Saves a new entity.
     *
     * @param entity the entity to save
     * @throws IllegalArgumentException if entity or its ID is null
     * @throws RepositoryException if the entity already exists
     *         or persistence fails
     */
    void save(E entity);

    /**
     * Finds an entity by its identifier.
     *
     * @param id the identifier
     * @return an Optional containing the entity if found
     * @throws IllegalArgumentException if id is null
     */
    Optional<E> findOne(ID id);

    /**
     * Returns all stored entities.
     *
     * @return a list of entities
     */
    List<E> findAll();

    /**
     * Returns entities matching a predicate.
     *
     * @param filter the filtering predicate
     * @return a list of matching entities
     * @throws IllegalArgumentException if filter is null
     */
    default List<E> findAll(Predicate<E> filter) {

        if (filter == null) {
            throw new IllegalArgumentException(
                    "Filter must not be null."
            );
        }

        return findAll().stream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    /**
     * Deletes an entity by its identifier.
     *
     * @param id the entity identifier
     * @return true if the entity was deleted, false otherwise
     * @throws IllegalArgumentException if id is null
     * @throws RepositoryException if persistence fails
     */
    boolean delete(ID id);

    /**
     * Updates an existing entity.
     *
     * @param entity the updated entity
     * @throws IllegalArgumentException if entity or its ID is null
     * @throws RepositoryException if the entity does not exist
     *         or persistence fails
     */
    void update(E entity);
}

