package repository;

import domain.Entity;
import exceptions.RepositoryException;

import java.util.List;
import java.util.Optional;

/**
 * Generic storage contract for domain entities.
 * Implementations must reject null identifiers and duplicate saves.
 *
 * @param <ID> identifier type
 * @param <E> entity type
 */
public interface Repository<ID, E extends Entity<ID>> {

    /**
     * Saves a new entity.
     *
     * @param entity entity to save
     * @throws IllegalArgumentException if entity or its identifier is null
     * @throws RepositoryException if the identifier already exists or persistence fails
     */
    void save(E entity);

    /**
     * Finds an entity by ID.
     *
     * @param id entity identifier
     * @return entity wrapped in Optional, or Optional.empty()
     * @throws IllegalArgumentException if id is null
     * @throws RepositoryException if reading from persistent storage fails
     */
    Optional<E> findOne(ID id);

    /**
     * Returns a snapshot list of entities.
     *
     * @return all stored entities
     * @throws RepositoryException if reading from persistent storage fails
     */
    List<E> findAll();

    /**
     * Deletes an entity by ID.
     *
     * @param id entity identifier
     * @return true if the entity existed and was deleted; false otherwise
     * @throws IllegalArgumentException if id is null
     * @throws RepositoryException if persistence fails
     */
    boolean delete(ID id);

    /**
     * Replaces an existing entity (does not insert new identifiers).
     *
     * @param entity updated entity
     * @throws IllegalArgumentException if entity or its identifier is null
     * @throws RepositoryException if the entity does not exist or persistence fails
     */
    void update(E entity);
}
