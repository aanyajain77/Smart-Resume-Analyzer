package exceptions;

/**
 * Custom Exception thrown when user input violates validation criteria
 * (e.g. invalid email format, negative experience, empty fields).
 */
public class InvalidInputException extends Exception {
    public InvalidInputException(String message) {
        super(message);
    }
}
