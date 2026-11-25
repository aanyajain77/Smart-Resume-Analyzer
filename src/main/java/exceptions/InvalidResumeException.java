package exceptions;

/**
 * Custom Exception thrown when an uploaded resume file is invalid,
 * unreadable, or contains corrupt data.
 */
public class InvalidResumeException extends Exception {
    public InvalidResumeException(String message) {
        super(message);
    }

    public InvalidResumeException(String message, Throwable cause) {
        super(message, cause);
    }
}
