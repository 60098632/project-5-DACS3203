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
        String ipAddress = getIpAddress();

        if (studentId.isEmpty() || password.isEmpty()) {
            DatabaseLogger.log("WARNING", "Login attempt with empty fields", ipAddress);
            showAlert("Please fill in all fields.");
            return;
        }

        User user = AuthService.login(studentId, password);

        if (user != null) {
            DatabaseLogger.log("INFO", "Successful login for user", ipAddress);
            failedLoginAttempts = 0;

            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
                javafx.scene.Parent root = loader.load();

                // Securely pass user details to Dashboard
                DashboardController controller = loader.getController();
                controller.setCurrentUser(
                        user.getName(),
                        user.getRole(),     // role needed to control access
                        user.getId()        // studentId needed for enrollment/payment
                );

                studentIdField.getScene().setRoot(root);

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error loading the dashboard screen.");
            }

        } else {
            failedLoginAttempts++;
            DatabaseLogger.log("WARNING", "Failed login attempt (attempt " + failedLoginAttempts + ")", ipAddress);
            if (failedLoginAttempts >= 3) {
                DatabaseLogger.log("WARNING", "User failed to login 3 or more times", ipAddress);
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