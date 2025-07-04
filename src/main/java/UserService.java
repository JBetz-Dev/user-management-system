import java.util.List;

public class UserService {
    private final UserDAO userDAO = new UserDAO();

    public User registerNewUser(String username, String email, String password) {
        User user = new User(username, email, password);
        return userDAO.insertUser(user);
    }

    public boolean changeUsername(User user, String username) {
        return userDAO.updateUsername(user, username);
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public User getUserById(int id) {
        return userDAO.getUserById(id);
    }

    public boolean removeUserById(int id) {
        return userDAO.deleteUserById(id);
    }
}