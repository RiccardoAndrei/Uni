
package validation;

import exceptions.ValidationException;

/**
 * Functional interface used to validate entities.
 *
 * @param <T> the type of entity to validate
 */
@FunctionalInterface
public interface Validator<T> {

    /**
     * Validates an entity.
     *
     * @param entity the entity to validate
     * @throws ValidationException if the entity is invalid
     */
    void validate(T entity);

    /**
     * Combines this validator with another validator.
     * The current validator is executed first.
     *
     * @param other the validator to execute afterwards
     * @return a composed validator
     * @throws IllegalArgumentException if other is null
     */
    default Validator<T> andThen(Validator<T> other) {

        if (other == null) {
            throw new IllegalArgumentException(
                    "Validator must not be null."
            );
        }

        return entity -> {
            this.validate(entity);
            other.validate(entity);
        };
    }
}
