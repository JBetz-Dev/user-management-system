import java.sql.SQLException;
import java.util.List;

/**
 * Service layer for user management operations containing business logic and validation.
 * Coordinates between UserRequestHandler and UserDAO while enforcing business rules.
 * <p>
 * Service errors generate custom checked exceptions which must be handled by the calling method.
 * <p>
 * Responsibilities:
 * - Validate business rules (authentication, authorization, uniqueness)
 * - Coordinate database operations through UserDAO
 * - Generate meaningful exceptions to indicate business error states
 * - Maintain data consistency by fetching fresh user data for operations
 *
 * @see UserDAO
 * @see UserRequestHandler
 * @see UserValidationUtil
 */
public class UserService {
    private final UserDAO userDAO = new UserDAO();

    public User getUserById(int id) throws SQLException {
        return userDAO.getUserById(id);
    }

    public User getUserByUsername(String username) throws SQLException {
        return userDAO.getUserByUsername(username);
    }

    public User getUserByEmail(String email) throws SQLException {
        return userDAO.getUserByEmail(email);
    }

    public List<User> getAllUsers() throws SQLException {
        return userDAO.getAllUsers();
    }

    public boolean removeUserById(int id) throws SQLException {
        return userDAO.deleteUserById(id);
    }

    public User registerNewUser(String username, String email, String password) throws SQLException,
            UserAlreadyExistsException, EmailAlreadyExistsException, ValidationException {
        String validatedUsername = UserValidationUtil.validateUsername(username);
        String validatedEmail = UserValidationUtil.validateEmail(email);

        if (getUserByUsername(validatedUsername) != null) {
            throw new UserAlreadyExistsException("User already exists with the requested username");
        }

        if (getUserByEmail(validatedEmail) != null) {
            throw new EmailAlreadyExistsException("User already exists with the requested email");
        }

        return userDAO.insertUser(new User(
                validatedUsername,
                validatedEmail,
                UserValidationUtil.validatePassword(password))
        );
    }

    public User authenticateUser(String username, String password)
            throws SQLException, UserAuthenticationException {
        User user = getUserByUsername(username);

        if (user == null || !user.verifyPassword(password)) {
            throw new UserAuthenticationException("User authentication failed");
        }

        return user;
    }

    public User changeUsername(int userId, String username, String password) throws SQLException,
            UserAuthenticationException, UserAlreadyExistsException, ValidationException {
        String validatedUsername = UserValidationUtil.validateUsername(username);
        User user = getUserById(userId);

        if (user == null || !user.verifyPassword(password)) {
            throw new UserAuthenticationException("User authentication failed");
        }

        if (getUserByUsername(validatedUsername) != null) {
            throw new UserAlreadyExistsException("User already exists with the requested username");
        }

        if (!userDAO.updateUsername(userId, validatedUsername)) {
            throw new SQLException("Failed to update username");
        }

        user.setUsername(validatedUsername);
        return user;
    }

    public User changePassword(int userId, String oldPassword, String newPassword)
            throws SQLException, UserAuthenticationException, ValidationException {
        User user = getUserById(userId);

        if (user == null || !user.verifyPassword(oldPassword)) {
            throw new UserAuthenticationException("User authentication failed");
        }

        String hashedPassword = PasswordUtil.hashPassword(
                UserValidationUtil.validatePassword(newPassword));

        if (!userDAO.updatePassword(userId, hashedPassword)) {
            throw new SQLException("Failed to update password");
        }

        user.setPasswordHash(hashedPassword);
        return user;
    }

    public User changeEmail(int userId, String email, String password) throws SQLException,
            UserAuthenticationException, EmailAlreadyExistsException, ValidationException {
        String validatedEmail = UserValidationUtil.validateEmail(email);
        User user = getUserById(userId);

        if (user == null || !user.verifyPassword(password)) {
            throw new UserAuthenticationException("User authentication failed");
        }

        if (getUserByEmail(validatedEmail) != null) {
            throw new EmailAlreadyExistsException("User already exists with the requested email");
        }

        if (!userDAO.updateEmail(userId, validatedEmail)) {
            throw new SQLException("Failed to update email");
        }

        user.setEmail(validatedEmail);
        return user;
    }

    public static class UserAuthenticationException extends Exception {
        UserAuthenticationException(String message) {
            super(message);
        }
    }

    public static class UserAlreadyExistsException extends Exception {
        UserAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class EmailAlreadyExistsException extends Exception {
        EmailAlreadyExistsException(String message) {
            super(message);
        }
    }
}