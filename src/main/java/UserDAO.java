import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for user persistence operations using JDBC.
 * Provides clean abstraction over database operations with consistent error handling.
 * <p>
 * Responsibilities:
 * - Execute SQL operations with proper resource management
 * - Map ResultSets to User objects
 * - Return null for "not found" cases, throw SQLException for errors
 * - Use prepared statements for security and performance
 * <p>
 * Design decisions:
 * - Null return for normal "not found" cases vs exceptions for errors
 * - RETURNING clause for insert operations to get generated IDs
 * - Validate actual updates by checking affected row counts
 * - Consistent resource management using try-with-resources
 *
 * @see UserService
 */
public class UserDAO {
    private final DBConnectionManager dbc = new DBConnectionManager();

    public User getUserById(int id) throws SQLException {
        try (Connection conn = dbc.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return null;
            }

            return mapResultSetToUser(rs);
        }
    }

    public User getUserByUsername(String username) throws SQLException {
        try (Connection conn = dbc.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return null;
            }

            return mapResultSetToUser(rs);
        }
    }

    public User getUserByEmail(String email) throws SQLException {
        try (Connection conn = dbc.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE email = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return null;
            }

            return mapResultSetToUser(rs);
        }
    }

    public List<User> getAllUsers() throws SQLException {
        try (Connection conn = dbc.getConnection()) {
            List<User> users = new ArrayList<>();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

            return users;
        }
    }

    public boolean deleteUserById(int id) throws SQLException {
        try (Connection conn = dbc.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM users WHERE id = ?"
            );
            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        }
    }

    public User insertUser(User user) throws SQLException {
        try (Connection conn = dbc.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (username, email, password) VALUES (?, ?, ?) RETURNING *"
            );
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Failed to insert user - no data returned");
            }

            return mapResultSetToUser(rs);
        }
    }

    public boolean updateUsername(int userId, String username) throws SQLException {
        try (Connection conn = dbc.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE users SET username = ? WHERE id = ?"
            );
            ps.setString(1, username);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(int userId, String password) throws SQLException {
        try (Connection conn = dbc.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE users SET password = ? WHERE id = ?"
            );
            ps.setString(1, password);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateEmail(int userId, String email) throws SQLException {
        try (Connection conn = dbc.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE users SET email = ? WHERE id = ?"
            );
            ps.setString(1, email);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String password = rs.getString("password");

        return new User(id, username, email, password);
    }
}