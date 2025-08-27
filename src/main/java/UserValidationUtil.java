import java.util.regex.Pattern;

/**
 * Utility class providing server-side validation for user-related request data.
 * Validates request field presence, format compliance, and business rules using regex patterns
 * that mirror frontend validation rules for consistency.
 * <p>
 * All validation methods throw ValidationException and must be handled by the calling method.
 * <p>
 * Validation rules:
 * - Username: 4-25 alphanumeric characters; may contain dots, underscores, and hyphens
 * - Email: Standard email format validation
 * - Password: 8-40 characters with at least one uppercase, lowercase, number, and special character
 *
 * @see UserService
 */
public class UserValidationUtil {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{4,25}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{8,40}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,50}$");

    public static String validateUsername(String username) throws ValidationException {
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new ValidationException("Invalid username format: " + username);
        }

        return username;
    }

    public static String validatePassword(String password) throws ValidationException {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ValidationException("Insufficient password complexity");
        }

        return password;
    }

    public static String validateEmail(String email) throws ValidationException {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format: " + email);
        }

        return email;
    }
}
