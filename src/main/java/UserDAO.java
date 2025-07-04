import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final DBConnectionManager dbc = new DBConnectionManager();

    public User insertUser(User user) {
        try (Connection conn = dbc.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (username, email, password) VALUES (?, ?, ?) RETURNING *"
            );
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int userId =  rs.getInt("id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String password = rs.getString("password");
                return new User(userId, username, email, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try (Connection conn = dbc.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int userId =  rs.getInt("id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String password = rs.getString("password");
                users.add(new User(userId, username, email, password));
            }
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserById(int id) {
        try (Connection conn = dbc.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int userId =  rs.getInt("id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String password = rs.getString("password");
                return new User(userId, username, email, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Updating just username for simplicity
    public boolean updateUsername(User user, String username) {
        try (Connection conn = dbc.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE users SET username = ? WHERE id = ?"
            );
            ps.setString(1, username);
            ps.setInt(2, user.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteUserById(int id) {
        try (Connection conn = dbc.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM users WHERE id = ?"
            );
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}