import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthenticationManager {

    // Database connection details
    private static final String URL = "jdbc:mysql://localhost:3306/Group5"; // Replace 'Group5' with your database name
    private static final String USER = "root"; // Replace with your MySQL username
    private static final String PASSWORD = "mert1234"; // Replace with your MySQL password

    /**
     * Authenticates the user by checking the database for matching credentials.
     *
     * @param username the entered username
     * @param password the entered password
     * @return the role of the authenticated user, or null if authentication fails
     */
    public static String authenticate(String username, String password) {
        String query = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Set parameters
            statement.setString(1, username);
            statement.setString(2, password);

            // Execute query
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                // Return the user's role if credentials match
                return resultSet.getString("role");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Authentication failed
    }
}
