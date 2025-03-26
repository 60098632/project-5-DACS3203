package ui;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import service.AuthService;
import util.DatabaseLogger;

public class RegisterController {

    private static final Logger logger = Logger.getLogger(RegisterController.class.getName());

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Regex patterns for validation
        Pattern emailPattern = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
        Pattern namePattern = Pattern.compile("^[A-Za-z ]{3,}$"); // At least 3 characters
        Pattern passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#\\$%^&*]).{8,}$");

        // Validate name
        if (!namePattern.matcher(name).matches()) {
            showAlert("Invalid name. Use at least 3 letters and only letters/spaces.");
            logger.warning("Invalid name input attempt: " + name);
            DatabaseLogger.log("WARNING", "Invalid name input attempt", email);
            return;
        }

        // Validate email
        if (!emailPattern.matcher(email).matches()) {
            showAlert("Invalid email format.");
            logger.warning("Invalid email input attempt: " + email);
            DatabaseLogger.log("WARNING", "Invalid email input attempt", email);
            return;
        }

        // Validate password strength
        if (!passwordPattern.matcher(password).matches()) {
            showAlert("Password must be at least 8 characters, include a number and a special character.");
            logger.warning("Weak password input attempt for email: " + email);
            DatabaseLogger.log("WARNING", "Weak password input attempt", email);
            return;
        }

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            logger.warning("Password mismatch during registration attempt for: " + email);
            DatabaseLogger.log("WARNING", "Password mismatch during registration", email);
            showAlert("Passwords do not match.");
            return;
        }

        boolean success = AuthService.register(name, email, password);

        if (success) {
            String generatedId = AuthService.generateUniversityId();
            logger.info("New user registered: " + email);
            DatabaseLogger.log("INFO", "New user registered", email);
            showAlert("✅ Registration successful!\nYour University ID is: " + generatedId);
            // TODO: Redirect back to login screen
        } else {
            logger.warning("Registration failed for: " + email);
            DatabaseLogger.log("WARNING", "Registration failed", email);
            showAlert("❌ Registration failed. Email might already be taken.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void goToLogin() {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            nameField.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}