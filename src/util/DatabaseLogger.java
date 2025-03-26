package util;

import config.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseLogger {

    public static void log(String level, String message, String email) {
        String sql = "INSERT INTO logs (level, message, ip_address) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, level);
            stmt.setString(2, message);
            stmt.setString(3, email);

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Failed to log to database: " + e.getMessage());
        }
    }
}