import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        // Database connection details
        String url = "jdbc:mysql://localhost:3306/Group5"; // Replace 'Group5' with your database name
        String user = "root"; // Replace with your MySQL username
        String password = "mert1234"; // Replace with your MySQL password

        try {
            // Load the MySQL JDBC Driver explicitly
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver Loaded!");

            // Establish the connection
            Connection connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the database successfully!");

            // Close the connection
            connection.close();

        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Include it in your library path.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            e.printStackTrace();
        }
    }
}
