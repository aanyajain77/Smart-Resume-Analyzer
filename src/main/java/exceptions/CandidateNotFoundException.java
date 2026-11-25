package exceptions;

/**
 * Custom Exception thrown when a candidate cannot be located in the database or collection.
 */
public class CandidateNotFoundException extends Exception {
    public CandidateNotFoundException(String message) {
        super(message);
    }
}
