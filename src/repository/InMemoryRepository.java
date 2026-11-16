package repository;

import domain.Entity;
import exceptions.RepositoryException;
import validation.Validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory repository backed by a HashMap.
 *
 * @param <ID> identifier type
 * @param <E> entity type
 */
public class InMemoryRepository<ID, E extends Entity<ID>>
        implements Repository<ID, E> {

    private final Map<ID, E> entities = new HashMap<>();
    private final Validator<E> validator;

    /**
     * Creates an empty repository.
     *
     * @param validator entity validator
     */
    public InMemoryRepository(Validator<E> validator) {
        if (validator == null) {
            throw new IllegalArgumentException(
                    "Validator must not be null."
            );
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
                    "Duplicate ID: " + entity.getId()
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
        return entities.values().stream()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /** {@inheritDoc} */
    @Override
    public E delete(ID id) {
        checkId(id);
        return entities.remove(id);
    }

    /** {@inheritDoc} */
    @Override
    public E update(E entity) {
        checkEntity(entity);
        validator.validate(entity);

        if (!entities.containsKey(entity.getId())) {
            return entity;
        }

        entities.put(entity.getId(), entity);
        return null;
    }

    /**
     * Validates an identifier.
     *
     * @param id identifier
     */
    protected void checkId(ID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "ID must not be null."
            );
        }
    }

    /**
     * Validates an entity and its ID.
     *
     * @param entity entity to check
     */
    protected void checkEntity(E entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException(
                    "Entity and ID must not be null."
            );
        }
    }
}
