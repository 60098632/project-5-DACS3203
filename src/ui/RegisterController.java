package ui;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import service.AuthService;
import java.util.regex.Pattern;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class RegisterController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private VBox cardContainer;  // This is our card container that will fade in

    @FXML
    public void initialize() {
        // Populate role selection
        roleComboBox.getItems().addAll("Student", "Instructor");
        roleComboBox.setValue("Student");

        // Animate the card container to fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), cardContainer);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    @FXML
    private void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleComboBox.getValue().toLowerCase();

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

        String generatedId = AuthService.register(name, email, password, role);
        if (generatedId != null) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(generatedId);
            clipboard.setContent(content);
            showAlert("✅ Registration successful!\nYour University ID is: " + generatedId +
                    "\n(It has been copied to the clipboard.)");
            goToLogin();
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