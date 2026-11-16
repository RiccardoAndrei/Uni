package repository;

import domain.Entity;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generic repository contract.
 *
 * @param <ID> identifier type
 * @param <E> entity type
 */
public interface Repository<ID, E extends Entity<ID>> {

    /**
     * Saves an entity.
     *
     * @param entity entity to save
     */
    void save(E entity);

    /**
     * Finds an entity by ID.
     *
     * @param id entity identifier
     * @return matching entity or an empty Optional
     */
    Optional<E> findOne(ID id);

    /**
     * Returns all entities.
     *
     * @return list of entities
     */
    List<E> findAll();

    /**
     * Returns entities matching a predicate.
     *
     * @param filter search predicate
     * @return matching entities
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
     * Deletes an entity.
     *
     * @param id entity identifier
     * @return deleted entity or null if not found
     * @throws IllegalArgumentException if id is null
     */
    E delete(ID id);

    /**
     * Updates an existing entity.
     *
     * @param entity replacement entity
     * @return null on success or entity if ID does not exist
     */
    E update(E entity);
}
