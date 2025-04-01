package ui;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import model.User;
import service.AuthService;
import util.DatabaseLogger;
import util.SessionManager;

public class LoginController {
    @FXML private AnchorPane rootPane;       // The entire AnchorPane
    @FXML private Circle decorCircle;        // The decorative circle
    @FXML private TextField studentIdField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    private static int failedLoginAttempts = 0;

    @FXML
    public void initialize() {
        // Fade in the entire AnchorPane
        FadeTransition fadeAll = new FadeTransition(Duration.millis(1200), rootPane);
        fadeAll.setFromValue(0.0);
        fadeAll.setToValue(1.0);
        fadeAll.play();

        // (Optional) Fade in the circle separately if you want a staggered effect
        FadeTransition fadeCircle = new FadeTransition(Duration.millis(1000), decorCircle);
        fadeCircle.setFromValue(0.0);
        fadeCircle.setToValue(1.0);
        fadeCircle.setDelay(Duration.millis(300)); // start after 300ms
        fadeCircle.play();
    }

    @FXML
    private void handleLogin() {
        String studentId = studentIdField.getText().trim();
        String password = passwordField.getText();

        // Basic input check
        if (studentId.isEmpty() || password.isEmpty()) {
            showAlert("Please fill in all fields.");
            return;
        }

        // Attempt login
        User user = AuthService.login(studentId, password);
        if (user != null) {
            // Success
            failedLoginAttempts = 0;
            SessionManager.setCurrentUser(user.getName(), user.getRole(), user.getId());
            loadDashboard(user);
        } else {
            // Failure
            failedLoginAttempts++;
            if (failedLoginAttempts >= 3) {
                disableLogin();
                showAlert("Too many failed attempts — please try again later.");
            } else {
                showAlert("Invalid username or password.");
            }
        }
    }

    private void disableLogin() {
        loginButton.setDisable(true);
        studentIdField.setDisable(true);
        passwordField.setDisable(true);
    }

    private void loadDashboard(User user) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/Dashboard.fxml"));
            javafx.scene.Parent root = loader.load();
            // Pass user info to Dashboard if needed
            DashboardController controller = loader.getController();
            controller.setCurrentUser(user.getName(), user.getRole(), user.getId());
            studentIdField.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading the dashboard screen.");
        }
    }

    @FXML
    private void goToRegister() {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/view/register.fxml"));
            studentIdField.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}