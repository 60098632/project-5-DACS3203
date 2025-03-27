package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import service.AuthService;
import java.util.regex.Pattern;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class RegisterController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML
    private void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        Pattern namePattern = Pattern.compile("^[A-Za-z ]{3,}$");
        Pattern emailPattern = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
        Pattern passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[A-Za-z])(?=.*[!@#$%^&*]).{8,}$");

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Please fill in all fields.");
            return;
        }
        if (!namePattern.matcher(name).matches()) {
            showAlert("Invalid name. Use at least 3 letters (letters and spaces only).");
            return;
        }
        if (!emailPattern.matcher(email).matches()) {
            showAlert("Invalid email format.");
            return;
        }
        if (!passwordPattern.matcher(password).matches()) {
            showAlert("Password must be at least 8 characters and include a number and a special character.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showAlert("Passwords do not match.");
            return;
        }

        String generatedId = AuthService.register(name, email, password);
        if (generatedId != null) {
            // Copy assigned student ID to clipboard and inform the user
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(generatedId);
            clipboard.setContent(content);
            showAlert("✅ Registration successful!\nYour University ID is: " + generatedId +
                    "\n(It has been copied to the clipboard.)");
            goToLogin();  // redirect to login on success
        } else {
            showAlert("❌ Registration failed. The email might already be taken.");
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