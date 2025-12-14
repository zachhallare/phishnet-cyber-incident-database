package util;

import java.util.regex.Pattern;

/**
 * Central place for all input validation logic used throughout the app.
 * Keeps validation rules consistent and makes it easy to tweak them later.
 */
public class ValidationUtils {
    
    // Simple but effective regex for most common email formats
    // Allows: letters, numbers, +._- before @, proper domain with at least one dot and 2+ letter TLD
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    /**
     * Checks if an email address looks valid (proper format, not just empty).
     *
     * @param email the email string to validate
     * @return true if the email matches a reasonable pattern, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Checks that a string is not null, not empty, and not just whitespace.
     * Used for required text fields like names, descriptions, etc.
     *
     * @param str the string to check
     * @return true if the string has actual content
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    /**
     * Simple check to make sure an integer is positive (> 0).
     * Useful for IDs, counts, or any value that shouldn't be zero or negative.
     *
     * @param value the integer to validate
     * @return true if value is greater than zero
     */
    public static boolean isPositive(int value) {
        return value > 0;
    }
    
    /**
     * Case-insensitive check to see if a string matches one of the allowed enum values.
     * Handy when reading user input or database strings that represent enums.
     *
     * @param value        the value to check (e.g. from a form or DB)
     * @param validValues  array of acceptable values (e.g. {"LOW", "MEDIUM", "HIGH"})
     * @return true if the value matches one of the valid ones (ignoring case)
     */
    public static boolean isValidEnumValue(String value, String[] validValues) {
        if (value == null) return false;
        for (String valid : validValues) {
            if (valid.equalsIgnoreCase(value.trim())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Very basic password strength check.
     * Currently only enforces minimum length of 6 characters.
     *
     * @param password the password to validate
     * @return true if password is at least 6 characters long
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        return true;
    }
}

