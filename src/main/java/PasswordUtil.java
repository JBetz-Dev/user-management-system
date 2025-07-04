import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String hashPassword(String ptPassword) {
        return encoder.encode(ptPassword);
    }

    public static boolean verifyPassword(String ptPassword, String hashedPassword) {
        return encoder.matches(ptPassword, hashedPassword);
    }
}
