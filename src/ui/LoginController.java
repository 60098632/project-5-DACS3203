package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.User;
import service.AuthService;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    /**
     * Called when the login button is clicked.
     * Validates the user's email and password.
     */
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Please fill in all fields.");
            return;
        }

        User user = AuthService.login(email, password);

        if (user != null) {
            showAlert("✅ Login successful! Welcome " + user.getName());
            // TODO: Navigate to dashboard screen here
        } else {
            showAlert("❌ Invalid email or password.");
        }
    }

    /**
     * Displays an alert box with the given message.
     * @param message The text to show in the alert
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void goToRegister() {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/view/register.fxml"));
            emailField.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}