import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class for secure password hashing and verification operations.
 * Uses industry-standard BCrypt algorithm for password security.
 * <p>
 * Responsibilities:
 * - Hash plaintext passwords using BCrypt algorithm
 * - Verify plaintext passwords against stored hashes
 * - Provide consistent password security across the application
 *
 * @see User
 * @see UserService
 */
public class PasswordUtil {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String hashPassword(String ptPassword) {
        return encoder.encode(ptPassword);
    }

    public static boolean verifyPassword(String ptPassword, String hashedPassword) {
        return encoder.matches(ptPassword, hashedPassword);
    }
}
