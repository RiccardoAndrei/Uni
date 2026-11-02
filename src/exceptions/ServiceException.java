package exceptions;

/**
 * Unchecked exception for violated application business rules.
 */
public class ServiceException extends RuntimeException {

    /**
     * Constructs the exception.
     *
     * @param message error explanation
     */
    public ServiceException(String message) {
        super(message);
    }
}