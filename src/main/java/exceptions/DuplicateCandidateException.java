package exceptions;

/**
 * Custom Exception thrown when attempting to register a candidate with an email
 * that is already registered in the system.
 */
public class DuplicateCandidateException extends Exception {
    public DuplicateCandidateException(String message) {
        super(message);
    }
}
