/**
 * Domain model representing a user entity with authentication and JSON serialization capabilities.
 * <p>
 * Provides two constructors for different creation contexts:
 * - Protected constructor for new user registration (auto-hashes plaintext password)
 * - Public constructor for database reconstruction (uses existing password hash)
 * <p>
 * Design decisions:
 * - Mutable fields to support service layer updates (username, email, password)
 * - Password hashing handled automatically in constructors and setters
 * - Protected password hash access to prevent external hash manipulation
 * - JSON serialization excludes sensitive password hash
 * - Encapsulated password verification through utility class
 * <p>
 * Security considerations:
 * - Password verification uses secure hashing comparison
 * - Password hash never exposed in JSON output
 * - Hash operations delegated to PasswordUtil for consistency
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