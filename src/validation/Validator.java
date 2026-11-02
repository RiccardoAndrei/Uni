package validation;

import exceptions.ValidationException;

/**
 * Strategy interface for validating domain objects.
 *
 * @param <T> validated object type
 */

@FunctionalInterface
public interface Validator<T> {

    /**
     * Validates a domain object.
     *
     * @param entity object to validate
     * @throws ValidationException if the object violates a validation rule
     */
    void validate(T entity);

    default Validator<T> andThen(Validator<T> other) {

    }
}