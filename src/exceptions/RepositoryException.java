package exceptions;

/**
 * Unchecked exception for persistence and repository failures.
 */
public class RepositoryException extends RuntimeException {

    /**
     * Constructs the exception with a message.
     *
     * @param message error explanation
     */
    public RepositoryException(String message) {
        super(message);
    }

    /**
     * Constructs the exception while preserving the underlying cause.
     *
     * @param message error explanation
     * @param cause original exception, e.g. IOException or parse failure
     */
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
