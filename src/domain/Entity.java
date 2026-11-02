package domain;

import java.util.Objects;

/**
 * Base class for domain entities with a typed identifier.
 *
 * @param <ID> identifier type
 */
public class Entity<ID> {
    private ID id;

    /**
     * Constructs an entity with the given identifier.
     *
     * @param id entity identifier
     */
    public Entity(ID id) {
        this.id = id;
    }

    /**
     * Returns the identifier.
     *
     * @return entity identifier
     */
    public ID getId() {
        return id;
    }

    /**
     * Sets the identifier.
     *
     * @param id new identifier
     */
    public void setId(ID id) {
        this.id = id;
    }
    /**
     * Compares entities by their class and identifier.
     *
     * @param obj object to compare
     * @return true if the entities are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Entity<?> other = (Entity<?>) obj;
        return Objects.equals(id, other.id);
    }

    /**
     * Computes the hash code based on the class and identifier.
     *
     * @return hash code of the entity
     */
    @Override
    public int hashCode() {
        return Objects.hash(getClass(), id);
    }
}

