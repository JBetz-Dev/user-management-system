import java.util.List;

public class UserService {
    private final UserDAO userDAO = new UserDAO();

    public User registerNewUser(String username, String email, String password) {
        User user = new User(username, email, password);
        return userDAO.insertUser(user);
    }

    public boolean changeUsername(User user, String username, String password) {
        if (user.verifyPassword(password)) {
            user.setUsername(username);
            return userDAO.updateUsername(user, username);
        } else {
            return false;
        }
    }

    public boolean changePassword(User user, String oldPassword, String newPassword) {
        if (user.verifyPassword(oldPassword)) {
            user.setPasswordHash(PasswordUtil.hashPassword(newPassword));
            return userDAO.updatePassword(user, newPassword);
        } else {
            return false;
        }
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public User getUserById(int id) {
        return userDAO.getUserById(id);
    }

    public User getUserByUsername(String username) {
        return userDAO.getUserByUsername(username);
    }

    public boolean removeUserById(int id) {
        return userDAO.deleteUserById(id);
    }
}