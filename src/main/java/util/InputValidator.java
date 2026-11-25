package util;

import exceptions.InvalidInputException;

import java.util.regex.Pattern;

/**
 * Utility class for validating user and parsed inputs.
 * Ensures data integrity across email, phone numbers, and numeric values.
 */
public class InputValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(?:\\+?\\d{1,3}[-.\s]?)?\\(?\\d{3}\\)?[-.\s]?\\d{3}[-.\s]?\\d{4}$|^\\d{10}$"
    );

    /**
     * Validates that a string is neither null nor empty.
     */
    public static String validateNonEmpty(String input, String fieldName) throws InvalidInputException {
        if (input == null || input.trim().isEmpty()) {
            throw new InvalidInputException(fieldName + " cannot be empty.");
        }
        return input.trim();
    }

    /**
     * Validates email format.
     */
    public static String validateEmail(String email) throws InvalidInputException {
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new InvalidInputException("Invalid email format: '" + email + "'. Expected format: user@domain.com");
        }
        return email.trim().toLowerCase();
    }

    /**
     * Validates phone number format.
     */
    public static String validatePhone(String phone) throws InvalidInputException {
        if (phone == null || phone.trim().isEmpty()) {
            return ""; // Optional phone
        }
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new InvalidInputException("Invalid phone format: '" + phone + "'. Expected 10 digits or standard international format.");
        }
        return phone.trim();
    }

    /**
     * Validates that experience is a non-negative double.
     */
    public static double validateExperience(double experience) throws InvalidInputException {
        if (experience < 0.0 || experience > 60.0) {
            throw new InvalidInputException("Work experience must be between 0.0 and 60.0 years.");
        }
        return experience;
    }

    /**
     * Parses and validates a positive integer.
     */
    public static int parsePositiveInt(String input, String fieldName) throws InvalidInputException {
        try {
            int val = Integer.parseInt(input.trim());
            if (val <= 0) {
                throw new InvalidInputException(fieldName + " must be a positive integer.");
            }
            return val;
        } catch (NumberFormatException e) {
            throw new InvalidInputException(fieldName + " must be a valid numeric integer.");
        }
    }
}
