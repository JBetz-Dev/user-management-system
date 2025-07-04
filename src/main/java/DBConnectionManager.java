import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionManager {

    public Connection getConnection() throws SQLException {
        Dotenv dotenv = Dotenv.load();

        String connectionPath = String.format("jdbc:postgresql://%s:%s/%s",
                dotenv.get("DB_URL"), dotenv.get("DB_PORT"), dotenv.get("DB_NAME")
        );

        return DriverManager.getConnection(
                connectionPath, dotenv.get("DB_USER"), dotenv.get("DB_PASSWORD")
        );
    }

}
