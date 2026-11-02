package repository;

import domain.Entity;
import exceptions.RepositoryException;
import validation.Validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * HashMap-backed generic repository, with no on-disk persistence.
 *
 * @param <ID> identifier type
 * @param <E> entity type
 */
public class InMemoryRepository<ID, E extends Entity<ID>>
        implements Repository<ID, E> {

    private final Map<ID, E> entities = new HashMap<ID, E>();
    private final Validator<E> validator;

    /**
     * Constructs an empty repository with an injected validation strategy.
     *
     * @param validator validation strategy for entities
     * @throws IllegalArgumentException if validator is null
     */
    public InMemoryRepository(Validator<E> validator) {
        if (validator == null) {
            throw new IllegalArgumentException("Validator must not be null.");
        }

        this.validator = validator;
    }

    /** {@inheritDoc} */
    @Override
    public void save(E entity) {
        checkEntity(entity);
        validator.validate(entity);

        if (entities.containsKey(entity.getId())) {
            throw new RepositoryException(
                    "An entity with this ID already exists: " + entity.getId()
            );
        }

        entities.put(entity.getId(), entity);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<E> findOne(ID id) {
        checkId(id);
        return Optional.ofNullable(entities.get(id));
    }

    /** {@inheritDoc} */
    @Override
    public List<E> findAll() {
        return new ArrayList<E>(entities.values());
    }

    /** {@inheritDoc} */
    @Override
    public boolean delete(ID id) {
        checkId(id);
        return entities.remove(id) != null;
    }

    /** {@inheritDoc} */
    @Override
    public void update(E entity) {
        checkEntity(entity);
        validator.validate(entity);

        if (!entities.containsKey(entity.getId())) {
            throw new RepositoryException(
                    "Entity not found for update: " + entity.getId()
            );
        }

        entities.put(entity.getId(), entity);
    }

    /**
     * Validates the identifier according to the Repository contract.
     *
     * @param id identifier to check
     * @throws IllegalArgumentException for a null identifier
     */
    protected void checkId(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("Entity ID must not be null.");
        }
    }

    /**
     * Validates the entity and identifier according to the Repository contract.
     *
     * @param entity entity to check
     * @throws IllegalArgumentException for a null entity or identifier
     */
    protected void checkEntity(E entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException(
                    "Entity and entity ID must not be null."
            );
        }
    }
}
