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
 * Generic in-memory repository using a HashMap.
 *
 * @param <ID> the identifier type
 * @param <E> the entity type
 */
public class InMemoryRepository<ID, E extends Entity<ID>>
        implements Repository<ID, E> {

    private final Map<ID, E> entities = new HashMap<>();
    private final Validator<E> validator;

    /**
     * Creates an empty repository.
     *
     * @param validator the validation strategy
     * @throws IllegalArgumentException if validator is null
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
                    "Entity already exists: " + entity.getId()
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
                    "Entity does not exist: " + entity.getId()
            );
        }

        entities.put(entity.getId(), entity);
    }

    /**
     * Checks whether an identifier is valid.
     *
     * @param id the identifier
     * @throws IllegalArgumentException if id is null
     */
    protected void checkId(ID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "Entity ID must not be null."
            );
        }
    }

    /**
     * Checks whether an entity and its ID are valid.
     *
     * @param entity the entity to check
     * @throws IllegalArgumentException if entity or ID is null
     */
    protected void checkEntity(E entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException(
                    "Entity and ID must not be null."
            );
        }
    }
}
