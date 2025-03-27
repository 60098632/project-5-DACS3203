package service;

import config.DBConnection;
import model.User;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;
import java.util.logging.Logger;

public class AuthService {
    private static final Logger logger = Logger.getLogger(AuthService.class.getName());

    /** Generates a random salt string (Base64 encoded). */
    public static String generateSalt() {
        byte[] salt = new byte[16];  // 128-bit salt
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /** Generates a unique university ID in the format "60XXXXXX" (X = 1-9). */
    public static String generateUniversityId() {
        SecureRandom random = new SecureRandom();
        StringBuilder id = new StringBuilder("60");
        for (int i = 0; i < 6; i++) {
            int digit = 1 + random.nextInt(9);  // 1 to 9
            id.append(digit);
        }
        return id.toString();
    }

    /** Hashes a password with the given salt using SHA-256. */
    public static String hashPasswordWithSalt(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String salted = password + salt;
            byte[] hash = md.digest(salted.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // Consider stronger algorithms (PBKDF2, bcrypt, scrypt) in production
            throw new RuntimeException("SHA-256 algorithm not found.", e);
        }
    }

    /**
     * Registers a new user (as student) with salted & hashed password.
     * @return the generated university ID if successful, or null if failed.
     */

    public static String register(String name, String email, String password, String role) {
        String sql = "INSERT INTO users (id, name, email, password, salt, role) VALUES (?, ?, ?, ?, ?, ?)";
        String universityId = generateUniversityId();
        String salt = generateSalt();
        String hashed = hashPasswordWithSalt(password, salt);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, universityId);
            stmt.setString(2, name);
            stmt.setString(3, email);
            stmt.setString(4, hashed);
            stmt.setString(5, salt);
            stmt.setString(6, role);  // Pass the role value here
            stmt.executeUpdate();
            return universityId;
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("❌ Email already exists.");
            return null;
        } catch (SQLException e) {
            logger.severe("SQL Exception in register: " + e.getMessage());
            return null;
        }
    }

    /**
     * Authenticates a user by ID and password.
     * @return a User object if authentication succeeds, or null if it fails.
     */
    public static User login(String studentId, String password) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String salt = rs.getString("salt");
                String storedHash = rs.getString("password");
                String inputHash = hashPasswordWithSalt(password, salt);
                if (storedHash.equals(inputHash)) {
                    // Successful authentication: create User object
                    return new User(rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("role"),
                            storedHash);
                }
            }
        } catch (SQLException e) {
            logger.severe("SQL Exception in login: " + e.getMessage());
        }
        return null;
    }
}