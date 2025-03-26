package ui;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import java.util.regex.Pattern;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import service.AuthService;

public class RegisterController {


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
            return;
        }

        // Validate email
        if (!emailPattern.matcher(email).matches()) {
            showAlert("Invalid email format.");
            return;
        }

        // Validate password strength
        if (!passwordPattern.matcher(password).matches()) {
            showAlert("Password must be at least 8 characters, include a number and a special character.");
            return;
        }

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Passwords do not match.");
            return;
        }

        String generatedId = AuthService.register(name, email, password);

        if (generatedId != null) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(generatedId);
            clipboard.setContent(clipboardContent);
            showAlert("✅ Registration successful!\nYour University ID is: " + generatedId + "\n(Your student ID has been copied to clipboard.)");
            // TODO: Redirect back to login screen
        } else {
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