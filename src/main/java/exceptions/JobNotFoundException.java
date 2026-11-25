package exceptions;

/**
 * Custom Exception thrown when a requested Job ID cannot be found.
 */
public class JobNotFoundException extends Exception {
    public JobNotFoundException(String message) {
        super(message);
    }
}
