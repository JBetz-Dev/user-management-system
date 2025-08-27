/**
 * Checked exception thrown when validation of a user field fails due to
 * format or complexity requirement violations.
 * <p>
 * This exception indicates that the field data does not satisfy the expected requirements.
 * <p>
 * Common scenarios:
 * - Invalid username or email format
 * - Password complexity requirements not met
 *
 * @see UserValidationUtil
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
