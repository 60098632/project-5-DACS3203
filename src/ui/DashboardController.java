package ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import util.SessionManager;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button courseManagementButton;
    @FXML
    private Button enrollmentButton;
    @FXML
    private Button transcriptButton;
    @FXML
    private Button payTuitionButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button gradeManagementButton;

    /**
     * This method is called automatically after the FXML is loaded.
     * It initializes the dashboard UI based on the current session.
     */
    public void initialize() {
        // If session data exists in SessionManager, update the UI accordingly.
        updateUI();
    }

    /**
     * Sets the current user in the SessionManager and updates the UI.
     * This method can be called from the login controller.
     */
    public void setCurrentUser(String userName, String userRole, String studentId) {
        SessionManager.setCurrentUser(userName, userRole, studentId);
        updateUI();
    }

    /**
     * Updates the welcome label and button visibility based on the user's role.
     */
    private void updateUI() {
        String currentUserName = SessionManager.getUserName();
        String currentUserRole = SessionManager.getUserRole();

        if (currentUserName != null) {
            welcomeLabel.setText("Welcome, " + currentUserName + "!");
        } else {
            welcomeLabel.setText("Welcome!");
        }

        // Hide all role-specific buttons by default.
        courseManagementButton.setVisible(false);
        enrollmentButton.setVisible(false);
        transcriptButton.setVisible(false);
        payTuitionButton.setVisible(false);
        gradeManagementButton.setVisible(false);

        // Show buttons based on role.
        if (currentUserRole != null) {
            switch (currentUserRole.toLowerCase()) {
                case "student":
                    enrollmentButton.setVisible(true);
                    transcriptButton.setVisible(true);
                    payTuitionButton.setVisible(true);
                    break;
                case "instructor":
                case "admin":
                    courseManagementButton.setVisible(true);
                    gradeManagementButton.setVisible(true);
                    break;
            }
        }
    }

    @FXML
    private void handleCourseManagement() {
        // Only instructors and admins can access course management.
        String role = SessionManager.getUserRole();
        if (role == null || (!role.equalsIgnoreCase("admin") && !role.equalsIgnoreCase("instructor"))) {
            showAlert("Access denied: Course Management is only for instructors or admins.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CourseManagement.fxml"));
            Parent root = loader.load();
            welcomeLabel.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading the Course Management module.");
        }
    }

    @FXML
    private void handleEnrollment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Enrollment.fxml"));
            Parent root = loader.load();
            // Use SessionManager to set the student ID for the enrollment screen.
            EnrollmentController controller = loader.getController();
            controller.setStudentId(SessionManager.getStudentId());
            enrollmentButton.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading the Enrollment module.");
        }
    }

    @FXML
    private void handleTranscript() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Transcript.fxml"));
            Parent root = loader.load();
            TranscriptController controller = loader.getController();
            controller.setStudentId(SessionManager.getStudentId());
            transcriptButton.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading the Transcript module.");
        }
    }

    @FXML
    private void handlePayTuition() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Payment.fxml"));
            Parent root = loader.load();
            PaymentController controller = loader.getController();
            controller.setStudentId(SessionManager.getStudentId());
            payTuitionButton.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading the Payment screen.");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            // Clear the session data on logout.
            SessionManager.clearSession();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();
            logoutButton.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Logout failed.");
        }
    }

    @FXML
    private void handleGradeManagement() {
        // Only instructors and admins can access grade management.
        String role = SessionManager.getUserRole();
        if (role == null || (!role.equalsIgnoreCase("admin") && !role.equalsIgnoreCase("instructor"))) {
            showAlert("Access denied: Grade Management is only for instructors or admins.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GradeManagement.fxml"));
            Parent root = loader.load();
            GradeManagementController controller = loader.getController();
            // Pass the instructor's name if needed.
            controller.setInstructorName(SessionManager.getUserName());
            gradeManagementButton.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading Grade Management.");
        }
    }

    /**
     * Displays an alert with the provided message.
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}