package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.User;
import service.AuthService;
import util.DatabaseLogger;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class handles the login functionality for the application.
 * It includes database logging for failed and successful login attempts.
 * The IP address of the client machine is logged instead of the student ID.
 */
public class LoginController {

    @FXML
    private TextField studentIdField;

    @FXML
    private PasswordField passwordField;

    // Counter for tracking consecutive failed login attempts
    private static int failedLoginAttempts = 0;

    /**
     * Called when the login button is clicked.
     * Validates the user's student ID and password.
     */
    @FXML
    private void handleLogin() {
        String studentId = studentIdField.getText().trim();
        String password = passwordField.getText();

        // Retrieve the client's IP address
        String ipAddress = getIpAddress();

        // Check if required fields are filled
        if (studentId.isEmpty() || password.isEmpty()) {
            DatabaseLogger.log("WARNING", "Login attempt with empty fields", ipAddress);
            showAlert("Please fill in all fields.");
            return;
        }

        // Attempt to authenticate the user
        User user = AuthService.login(studentId, password);

        if (user != null) {
            // Reset the failed login counter on successful login
            DatabaseLogger.log("INFO", "Successful login for user", ipAddress);
            failedLoginAttempts = 0;
            showAlert("Login successful! Welcome " + user.getName());
            // TODO: Navigate to dashboard screen here
        } else {
            // Increment the counter on failed login
            failedLoginAttempts++;
            DatabaseLogger.log("WARNING", "Failed login attempt for user (attempt " + failedLoginAttempts + ")", ipAddress);
            if (failedLoginAttempts >= 3) {
                DatabaseLogger.log("WARNING", "User has failed to login 3 or more times", ipAddress);
                // Additional actions can be taken here, such as account lockout or alerting an administrator.
            }
            showAlert("Invalid student ID or password.");
        }
    }

    /**
     * Displays an alert box with the given message.
     * @param message The text to show in the alert.
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Navigates to the registration screen.
     */
    @FXML
    private void goToRegister() {
        String ipAddress = getIpAddress();
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/view/register.fxml"));
            studentIdField.getScene().setRoot(root);
        } catch (Exception e) {
            DatabaseLogger.log("SEVERE", "Error navigating to registration screen: " + e.getMessage(), ipAddress);
            e.printStackTrace();
        }
    }

    /**
     * Helper method to retrieve the local IP address.
     * @return The IP address as a String, or "unknown" if it cannot be determined.
     */
    private String getIpAddress() {
        try {
            // Get the local host IP address
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}