import java.sql.*;

public class DatabaseFacade
{
    private static final String URL = "jdbc:mysql://localhost:3306/Group5";
    private static final String USER = "root";
    private static final String PASSWORD = "mert1234";

    public Connection connect() throws SQLException
    {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
