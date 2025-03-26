package config;

// Importing necessary classes to work with SQL
// and database connections

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    //URL to connect to my local MySQL database
    private static final String URL = "jdbc:mysql://localhost:3306/simple_auth";
    private static final String USER = "root";
    private static final String PASSWORD = "";


    /**
     * This method returns a Connection object to the database.
     * It uses the URL, USER, and PASSWORD to connect via JDBC.
     * @return Connection object ot interact with the database
     * @throws SQLException if the connection fails.
     */

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
