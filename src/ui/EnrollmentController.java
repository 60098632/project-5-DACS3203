package ui;

import config.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Course;
import util.SessionManager;
import java.sql.*;
import java.util.logging.Logger;

public class EnrollmentController {
    @FXML private TableView<Course> availableCoursesTable;
    @FXML private TableColumn<Course, String> availableCourseCodeColumn;
    @FXML private TableColumn<Course, String> availableCourseNameColumn;
    @FXML private TableColumn<Course, Integer> availableCreditHoursColumn;
    @FXML private TableColumn<Course, String> availableCourseDescriptionColumn;
    @FXML private TableColumn<Course, String> availableInstructorColumn;
    @FXML private TableView<Course> enrolledCoursesTable;
    @FXML private TableColumn<Course, String> enrolledCourseCodeColumn;
    @FXML private TableColumn<Course, String> enrolledCourseNameColumn;
    @FXML private TableColumn<Course, Integer> enrolledCreditHoursColumn;
    @FXML private TableColumn<Course, String> enrolledCourseDescriptionColumn;
    @FXML private TableColumn<Course, String> enrolledInstructorColumn;
    @FXML private Button enrollButton;
    @FXML private Button dropButton;
    @FXML private Button goBackButton;

    private static final Logger logger = Logger.getLogger(EnrollmentController.class.getName());

    @FXML
    public void initialize() {
        // Set up table columns
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

    // Called from DashboardController to set current student ID
    public void setStudentId(String studentId) {
        // Update session's student ID (name/role remain same)
        SessionManager.setCurrentUser(SessionManager.getUserName(), SessionManager.getUserRole(), studentId);
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
        String studentId = SessionManager.getStudentId();
        if (studentId == null || studentId.isEmpty()) return;
        String sql = "SELECT c.* FROM courses c JOIN enrollments e ON c.course_code = e.course_code WHERE e.student_id = ?";
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
        String studentId = SessionManager.getStudentId();
        if (studentId == null || studentId.isEmpty()) {
            showAlert("No student ID. Please log in again.");
            return;
        }
        // Check for duplicate enrollment
        String dupCheckSql = "SELECT 1 FROM enrollments WHERE student_id = ? AND course_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(dupCheckSql)) {
            ps.setString(1, studentId);
            ps.setString(2, selected.getCourseCode());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                showAlert("You are already enrolled in " + selected.getCourseName() + ".");
                return;
            }
        } catch (SQLException e) {
            logger.warning("Dup check failed: " + e.getMessage());
        }
        // Check total enrolled credits
        String sumSql = "SELECT SUM(c.credit_hours) AS total_credits "
                + "FROM enrollments e JOIN courses c ON e.course_code = c.course_code "
                + "WHERE e.student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sumSql)) {
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            int currentCredits = 0;
            if (rs.next()) {
                currentCredits = rs.getInt("total_credits");
            }
            if (currentCredits + selected.getCreditHours() > 18) {
                showAlert("Cannot enroll in " + selected.getCourseName()
                        + " because it would exceed the 18-credit limit.");
                return;
            }
        } catch (SQLException e) {
            logger.warning("Failed to check credit total: " + e.getMessage());
        }
        // Insert new enrollment record
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
        String studentId = SessionManager.getStudentId();
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
            // Restore DashboardController state with current session info
            DashboardController controller = loader.getController();
            controller.setCurrentUser(SessionManager.getUserName(), SessionManager.getUserRole(), SessionManager.getStudentId());
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
}