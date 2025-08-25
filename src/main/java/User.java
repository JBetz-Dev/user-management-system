/**
 * Domain model representing a user entity with authentication and JSON serialization capabilities.
 * <p>
 * Provides two constructors for different creation contexts:
 * - Protected constructor for new user registration (auto-hashes plaintext password)
 * - Public constructor for database reconstruction (uses existing password hash)
 * <p>
 * Additional Considerations:
 * - Protected password hash access to prevent external hash manipulation
 * - Password hashing handled automatically in constructors and setters
 * - Custom JSON serialization excludes sensitive password hash and provides control over output
 *
 * @see PasswordUtil
 * @see UserService
 * @see JsonUtil
 */
public class User {

    private int id;
    private String username;
    private String email;
    private String passwordHash;

    // Used for creating new Users prior to DB save
    protected User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        passwordHash = PasswordUtil.hashPassword(password);
    }

    // Used to reinstantiate existing Users from the DB
    public User(int id, String username, String email, String passwordHash) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String password) {
        this.passwordHash = PasswordUtil.hashPassword(password);
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    protected String getPasswordHash() {
        return passwordHash;
    }

    public boolean verifyPassword(String password) {
        return PasswordUtil.verifyPassword(password, passwordHash);
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(JsonUtil.escapeJson(String.valueOf(id))).append(",");
        sb.append("\"username\":\"").append(JsonUtil.escapeJson(username)).append("\",");
        sb.append("\"email\":\"").append(JsonUtil.escapeJson(email)).append("\"");
        sb.append("}");

        return sb.toString();
    }
}