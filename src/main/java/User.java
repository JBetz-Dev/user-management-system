public class User {

    private int id;
    private String username;
    private String email;
    private String passwordHash;

    // Used for creating new Users prior to DB save
    public User(String username, String email, String password) {
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
        if (verifyPassword(password)) {
            this.passwordHash = PasswordUtil.hashPassword(password);
        }
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
}