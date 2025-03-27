package ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import util.SessionManager;

/**
 * DashboardController handles the main menu/dashboard screen.
 * It displays buttons based on the user's role (student/instructor/admin).
 */
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
    private String currentUserName;
    private String currentUserRole; // e.g., "student", "instructor", or "admin"
    private String currentStudentId; // only used for student-specific screens

    public void initialize() {
        // Pull from SessionManager if values were not passed directly
        if (currentUserName == null) {
            currentUserName = SessionManager.getUserName();
            currentUserRole = SessionManager.getUserRole();
            currentStudentId = SessionManager.getStudentId();
        }

        welcomeLabel.setText("Welcome, " + currentUserName + "!");

        // Hide all role-specific buttons by default
        courseManagementButton.setVisible(false);
        enrollmentButton.setVisible(false);
        transcriptButton.setVisible(false);
        payTuitionButton.setVisible(false);

        // Show buttons based on role
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
                    break;
            }
        }
    }
    public void setCurrentUser(String userName, String userRole, String studentId) {
        this.currentUserName = userName;
        this.currentUserRole = userRole;
        this.currentStudentId = studentId;

        // Store in SessionManager for access after going back
        SessionManager.setCurrentUser(userName, userRole, studentId);

        welcomeLabel.setText("Welcome, " + currentUserName + "!");

        // Show buttons based on role
        switch (userRole.toLowerCase()) {
            case "student":
                enrollmentButton.setVisible(true);
                transcriptButton.setVisible(true);
                payTuitionButton.setVisible(true);
                break;
            case "instructor":
            case "admin":
                courseManagementButton.setVisible(true);
                break;
        }
    }

    @FXML
    private void handleCourseManagement() {
        if (!"admin".equalsIgnoreCase(currentUserRole) && !"instructor".equalsIgnoreCase(currentUserRole)) {
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

            EnrollmentController controller = loader.getController();
            controller.setStudentId(currentStudentId); // set student ID so enrolled courses are fetched

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
            controller.setStudentId(currentStudentId);

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
            controller.setStudentId(currentStudentId);

            payTuitionButton.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading the Payment screen.");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();
            logoutButton.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Logout failed.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleGradeManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GradeManagement.fxml"));
            Parent root = loader.load();

            GradeManagementController controller = loader.getController();
            controller.setInstructorName(SessionManager.getUserName());

            gradeManagementButton.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading Grade Management.");
        }
    }

}