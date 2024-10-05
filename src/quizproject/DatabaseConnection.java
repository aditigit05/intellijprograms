package quizproject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

    public class DatabaseConnection {
        private static final String URL = "jdbc:postgresql://localhost:5432/quiz_application";
        private static final String USERNAME = "postgres";
        private static final String PASSWORD = "adi93023";

        public static Connection getConnection() {
            Connection connection = null;
            try {
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("Database connection successful!");
            } catch (SQLException e) {
                System.err.println("Connection failed: " + e.getMessage());
            }
            return connection;
        }

        public static void main(String[] args) {
            getConnection();
        }
    }


