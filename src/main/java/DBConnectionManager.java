import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages database connections using environment-based configuration.
 * Loads connection parameters once at startup for performance optimization.
 * <p>
 * Responsibilities:
 * - Load database configuration from environment variables
 * - Provide new database connections on demand
 * - Build PostgreSQL connection URLs from configuration components
 *
 * @see UserDAO
 */
public class DBConnectionManager {
    private static final String CONNECTION_URL;
    private static final String DB_USER;
    private static final String DB_PASSWORD;

    static {
        Dotenv dotenv = Dotenv.load();

        CONNECTION_URL = String.format("jdbc:postgresql://%s:%s/%s",
                dotenv.get("DB_URL"), dotenv.get("DB_PORT"), dotenv.get("DB_NAME")
        );
        DB_USER = dotenv.get("DB_USER");
        DB_PASSWORD = dotenv.get("DB_PASSWORD");
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD);
    }
}
