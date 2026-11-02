package exceptions;

/**
 * Unchecked exception thrown when domain validation fails.
 */
public class ValidationException extends RuntimeException {

    /**
     * Constructs the exception.
     *
     * @param message explanation of the invalid data
     */
    public ValidationException(String message) {
        super(message);
    }
}
