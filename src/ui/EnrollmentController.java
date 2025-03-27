package ui;

import config.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Course;
import java.sql.*;
import java.util.logging.Logger;

public class EnrollmentController {

    @FXML
    private TableView<Course> availableCoursesTable;
    @FXML
    private TableColumn<Course, String> availableCourseCodeColumn;
    @FXML
    private TableColumn<Course, String> availableCourseNameColumn;
    @FXML
    private TableColumn<Course, Integer> availableCreditHoursColumn;
    @FXML
    private TableColumn<Course, String> availableCourseDescriptionColumn;
    @FXML
    private TableColumn<Course, String> availableInstructorColumn;

    @FXML
    private TableView<Course> enrolledCoursesTable;
    @FXML
    private TableColumn<Course, String> enrolledCourseCodeColumn;
    @FXML
    private TableColumn<Course, String> enrolledCourseNameColumn;
    @FXML
    private TableColumn<Course, Integer> enrolledCreditHoursColumn;
    @FXML
    private TableColumn<Course, String> enrolledCourseDescriptionColumn;
    @FXML
    private TableColumn<Course, String> enrolledInstructorColumn;

    @FXML
    private Button enrollButton;
    @FXML
    private Button dropButton;
    @FXML
    private Button goBackButton;

    private String studentId;
    private String userName;
    private String userRole;
    @FXML
    private Button gradeManagementButton;
    private static final Logger logger = Logger.getLogger(EnrollmentController.class.getName());

    @FXML
    public void initialize() {
        availableCourseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        availableCourseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        availableCreditHoursColumn.setCellValueFactory(new PropertyValueFactory<>("creditHours"));
        availableCourseDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("courseDescription"));
        availableInstructorColumn.setCellValueFactory(new PropertyValueFactory<>("instructorName"));

        enrolledCourseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        enrolledCourseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        enrolledCreditHoursColumn.setCellValueFactory(new PropertyValueFactory<>("creditHours"));
        enrolledCourseDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("courseDescription"));
        enrolledInstructorColumn.setCellValueFactory(new PropertyValueFactory<>("instructorName"));

        loadAvailableCourses();
    }

    public void setCurrentUser(String name, String role, String id) {
        this.userName = name;
        this.userRole = role;
        this.studentId = id;
        loadEnrolledCourses();
    }

    private void loadAvailableCourses() {
        String sql = "SELECT * FROM courses";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            availableCoursesTable.getItems().clear();
            while (rs.next()) {
                Course course = new Course(
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getInt("credit_hours"),
                        rs.getString("instructor_name"),
                        rs.getString("course_description")
                );
                availableCoursesTable.getItems().add(course);
            }
        } catch (SQLException e) {
            logger.severe("Error loading available courses: " + e.getMessage());
            showAlert("Error loading available courses.");
        }
    }

    private void loadEnrolledCourses() {
        if (studentId == null || studentId.isEmpty()) return;

        String sql = "SELECT c.* FROM courses c " +
                "JOIN enrollments e ON c.course_code = e.course_code " +
                "WHERE e.student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();

            enrolledCoursesTable.getItems().clear();
            while (rs.next()) {
                Course course = new Course(
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getInt("credit_hours"),
                        rs.getString("instructor_name"),
                        rs.getString("course_description")
                );
                enrolledCoursesTable.getItems().add(course);
            }
        } catch (SQLException e) {
            logger.severe("Error loading enrolled courses: " + e.getMessage());
            showAlert("Error loading enrolled courses.");
        }
    }

    @FXML
    private void handleEnroll() {
        Course selected = availableCoursesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a course.");
            return;
        }

        if (studentId == null || studentId.isEmpty()) {
            showAlert("No student ID. Please log in again.");
            return;
        }

        String sql = "INSERT INTO enrollments (student_id, course_code) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            stmt.setString(2, selected.getCourseCode());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                showAlert("Enrollment successful.");
                loadEnrolledCourses();
            } else {
                showAlert("Failed to enroll.");
            }
        } catch (SQLException e) {
            logger.severe("Enrollment error: " + e.getMessage());
            showAlert("Error enrolling in course.");
        }
    }

    @FXML
    private void handleDropEnrollment() {
        Course selected = enrolledCoursesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a course to drop.");
            return;
        }

        String sql = "DELETE FROM enrollments WHERE student_id = ? AND course_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            stmt.setString(2, selected.getCourseCode());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                showAlert("Dropped course.");
                loadEnrolledCourses();
            } else {
                showAlert("Failed to drop course.");
            }
        } catch (SQLException e) {
            logger.severe("Drop error: " + e.getMessage());
            showAlert("Error dropping course.");
        }
    }

    @FXML
    private void handleGoBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setCurrentUser("User", "student", studentId); // TODO: Replace "User" and "student" with actual values if you have them saved

            goBackButton.getScene().setRoot(root);
        } catch (Exception e) {
            logger.severe("Failed to go back: " + e.getMessage());
            showAlert("Unable to return to dashboard.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void setStudentId(String studentId) {
        this.studentId = studentId;
        loadEnrolledCourses();
    }
}