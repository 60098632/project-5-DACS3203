package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // URL to connect to the local MySQL database
    private static final String URL = "jdbc:mysql://localhost:3306/simple_auth";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    /**
     * Returns a Connection object to the database.
     * @throws SQLException if the connection fails.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}