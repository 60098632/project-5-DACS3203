package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.User;
import service.AuthService;
import util.DatabaseLogger;
import util.SessionManager;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class LoginController {

    @FXML private TextField studentIdField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    private static int failedLoginAttempts = 0;

    @FXML
    private void handleLogin() {
        String ipAddress = getIpAddress();

        if (failedLoginAttempts >= 3) {
            disableLogin();
            showAlert("Too many failed attempts — please try again later.");
            return;
        }

        String studentId = studentIdField.getText().trim();
        String password = passwordField.getText();

        if (studentId.isEmpty() || password.isEmpty()) {
            DatabaseLogger.log("WARNING", "Login attempt with empty fields", ipAddress);
            showAlert("Please fill in all fields.");
            return;
        }

        User user = AuthService.login(studentId, password);
        if (user != null) {
            DatabaseLogger.log("INFO", "Successful login for user", ipAddress);
            System.out.println("DEBUG: Logged in user ID=" + user.getId() + ", role=" + user.getRole());
            failedLoginAttempts = 0;
            SessionManager.setCurrentUser(user.getName(), user.getRole(), user.getId());
            loadDashboard(user);
        } else {
            failedLoginAttempts++;
            DatabaseLogger.log("WARNING", "Failed login attempt (attempt " + failedLoginAttempts + ")", ipAddress);
            if (failedLoginAttempts >= 3) {
                disableLogin();
                showAlert("Too many failed attempts — please try again later.");
            } else {
                showAlert("Invalid student ID or password.");
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
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
            javafx.scene.Parent root = loader.load();
            DashboardController controller = loader.getController();
            controller.setCurrentUser(user.getName(), user.getRole(), user.getId());
            studentIdField.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading the dashboard screen.");
        }
    }

    private String getIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goTaoRegister() {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/view/register.fxml"));
            studentIdField.getScene().setRoot(root);
        } catch (Exception e) {
            DatabaseLogger.log("SEVERE", "Error navigating to registration screen: " + e.getMessage(), getIpAddress());
            e.printStackTrace();
        }
    }
}