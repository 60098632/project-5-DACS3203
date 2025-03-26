package service;

import config.DBConnection;
import model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;

public class AuthService {

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
     * @return the generated university ID
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
     * @param password The plain password
     * @param salt The salt
     * @return SHA-256 hash of (password + salt)
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
            throw new RuntimeException("SHA-256 algorithm not found.");
        }
    }

    /**
     * Registers a user with a salted + hashed password.
     */
    public static boolean register(String name, String email, String password) {
        String sql = "INSERT INTO users (id, name, email, password, salt) VALUES (?, ?, ?, ?, ?)";

        String universityId = generateUniversityId();
        String salt = generateSalt();
        String hashed = hashPasswordWithSalt(password, salt);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, universityId);  // Custom ID
            stmt.setString(2, name);
            stmt.setString(3, email);
            stmt.setString(4, hashed);
            stmt.setString(5, salt);

            stmt.executeUpdate();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("❌ Email already exists.");
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Authenticates a user by comparing hashed (input + salt) to stored password.
     */
    public static User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String salt = rs.getString("salt");
                String storedHash = rs.getString("password");
                String inputHash = hashPasswordWithSalt(password, salt);

                if (storedHash.equals(inputHash)) {
                    User user = new User(
                            rs.getString("name"),
                            rs.getString("email"),
                            storedHash
                    );
                    user.setId(rs.getInt("id"));
                    return user;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}