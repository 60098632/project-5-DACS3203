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

    // Logger for logging exceptions and informational messages
    private static final Logger logger = Logger.getLogger(AuthService.class.getName());

    /**
     * Generates a random salt string using SecureRandom and encodes it in Base64.
     * @return a random salt string
     */
    public static String generateSalt() {
        byte[] salt = new byte[16]; // 128-bit salt
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Generates a unique university ID in the format 60XXXXXX where X is 1-9.
     * @return the generated university ID as a String
     */
    public static String generateUniversityId() {
        StringBuilder id = new StringBuilder("60");
        for (int i = 0; i < 6; i++) {
            int digit = 1 + new SecureRandom().nextInt(9); // 1 to 9
            id.append(digit);
        }
        return id.toString();
    }

    /**
     * Hashes a password with a given salt using SHA-256.
     * @param password The plain password.
     * @param salt The salt.
     * @return SHA-256 hash of (password + salt) as a hex string.
     */
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
            // In production, consider using a more secure algorithm like PBKDF2, bcrypt, or scrypt.
            throw new RuntimeException("SHA-256 algorithm not found.", e);
        }
    }

    /**
     * Registers a user with a salted and hashed password.
     * @param name The user's name.
     * @param email The user's email address.
     * @param password The user's plain password.
     * @return The generated university ID if registration is successful, or null if it fails.
     */
    public static String register(String name, String email, String password) {
        String sql = "INSERT INTO users (id, name, email, password, salt) VALUES (?, ?, ?, ?, ?)";

        // Generate custom university ID, salt, and hashed password
        String universityId = generateUniversityId();
        String salt = generateSalt();
        String hashed = hashPasswordWithSalt(password, salt);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, universityId);  // Custom ID stored as a String
            stmt.setString(2, name);
            stmt.setString(3, email);
            stmt.setString(4, hashed);
            stmt.setString(5, salt);

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
     * Authenticates a user by comparing the hashed (input + salt) password to the stored hash.
     * @param studentId The custom university ID provided by the user.
     * @param password The plain password provided by the user.
     * @return A User object if authentication is successful, or null if it fails.
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
                    // Create a User object using retrieved data.
                    User user = new User(
                            rs.getString("name"),
                            rs.getString("email"),
                            storedHash
                    );
                    // Convert the custom university ID (stored as a String) to an int.
                    // Make sure that your university IDs are valid integers.
                    user.setId(Integer.parseInt(rs.getString("id")));
                    return user;
                }
            }

        } catch (SQLException e) {
            logger.severe("SQL Exception in login: " + e.getMessage());
        }

        return null;
    }
}