import java.sql.SQLException;
import java.util.List;

/**
 * Service layer for user management operations containing business logic and validation.
 * Coordinates between UserRequestHandler and UserDAO while enforcing business rules.
 * <p>
 * Responsibilities:
 * - Validate business rules (authentication, authorization, uniqueness)
 * - Coordinate database operations through UserDAO
 * - Generate meaningful exceptions to indicate business error states
 * - Maintain data consistency by fetching fresh user data for operations
 * <p>
 * Design decisions:
 * - Always fetch fresh user data to avoid stale cache issues
 * - Use specific exceptions for different error types
 * - Let SQLException bubble up for infrastructure errors
 * - Return User objects for successful operations to support API responses
 *
 * @see UserDAO
 * @see UserRequestHandler
 */
public class UserService {
    private final UserDAO userDAO = new UserDAO();

    public User getUserById(int id) throws SQLException {
        return userDAO.getUserById(id);
    }

    public User getUserByUsername(String username) throws SQLException {
        return userDAO.getUserByUsername(username);
    }

    public List<User> getAllUsers() throws SQLException {
        return userDAO.getAllUsers();
    }

    public boolean removeUserById(int id) throws SQLException {
        return userDAO.deleteUserById(id);
    }

    public User registerNewUser(String username, String email, String password)
            throws SQLException, UserAlreadyExistsException {
        if (userDAO.getUserByUsername(username) != null) {
            throw new UserAlreadyExistsException("User already exists");
        }

        return userDAO.insertUser(new User(username, email, password));
    }

    public User authenticateUser(String username, String password)
            throws SQLException, UserAuthenticationException {
        User user = getUserByUsername(username);

        if (user == null || !user.verifyPassword(password)) {
            throw new UserAuthenticationException("User authentication failed");
        }

        return user;
    }

    public User changeUsername(int userId, String username, String password)
            throws SQLException, UserAuthenticationException {
        User user = getUserById(userId);

        if (user == null || !user.verifyPassword(password)) {
            throw new UserAuthenticationException("User authentication failed");
        }

        if (!userDAO.updateUsername(userId, username)) {
            throw new SQLException("Failed to update username");
        }

        user.setUsername(username);
        return user;
    }

    public User changePassword(int userId, String oldPassword, String newPassword)
            throws SQLException, UserAuthenticationException {
        User user = getUserById(userId);

        if (user == null || !user.verifyPassword(oldPassword)) {
            throw new UserAuthenticationException("User authentication failed");
        }

        String hashedPassword = PasswordUtil.hashPassword(newPassword);

        if (!userDAO.updatePassword(userId, hashedPassword)) {
            throw new SQLException("Failed to update password");
        }

        user.setPasswordHash(hashedPassword);
        return user;
    }

    public User changeEmail(int userId, String email, String password)
            throws SQLException, UserAuthenticationException {
        User user = getUserById(userId);

        if (user == null || !user.verifyPassword(password)) {
            throw new UserAuthenticationException("User authentication failed");
        }

        if (!userDAO.updateEmail(userId, email)) {
            throw new SQLException("Failed to update email");
        }

        user.setEmail(email);
        return user;
    }

    public static class UserAlreadyExistsException extends Exception {
        UserAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class UserAuthenticationException extends Exception {
        UserAuthenticationException(String message) {
            super(message);
        }
    }
}