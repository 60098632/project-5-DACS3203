package ui;

import config.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.Course;
import java.sql.*;
import java.util.logging.Logger;

/**
 * EnrollmentController manages the enrollment process for a student.
 * It loads available courses from the database and the courses in which the student is already enrolled.
 * The module uses prepared statements to prevent SQL injection and validates the student session.
 */
public class EnrollmentController {

    // TableView for displaying available courses
    @FXML
    private TableView<Course> availableCoursesTable;

    // TableColumns for available courses
    @FXML
    private TableColumn<Course, String> availableCourseCodeColumn;
    @FXML
    private TableColumn<Course, String> availableCourseNameColumn;
    @FXML
    private TableColumn<Course, String> availableCourseDescriptionColumn;

    // TableView for displaying courses the student is enrolled in
    @FXML
    private TableView<Course> enrolledCoursesTable;

    // TableColumns for enrolled courses
    @FXML
    private TableColumn<Course, String> enrolledCourseCodeColumn;
    @FXML
    private TableColumn<Course, String> enrolledCourseNameColumn;
    @FXML
    private TableColumn<Course, String> enrolledCourseDescriptionColumn;

    // Buttons for enrollment actions
    @FXML
    private Button enrollButton;
    @FXML
    private Button dropButton;

    // The logged-in student's ID; this must be set after a successful login
    private String studentId;

    // Logger for logging enrollment events
    private static final Logger logger = Logger.getLogger(EnrollmentController.class.getName());

    /**
     * Called automatically when the FXML is loaded.
     * Loads available courses and the student's current enrollments.
     */
    @FXML
    public void initialize() {
        loadAvailableCourses();
        loadEnrolledCourses();
    }

    /**
     * Sets the logged-in student's ID.
     * This method should be called from the DashboardController after login.
     * @param studentId The current student's ID.
     */
    public void setStudentId(String studentId) {
        this.studentId = studentId;
        // Refresh enrolled courses after setting the student ID.
        loadEnrolledCourses();
    }

    /**
     * Loads all available courses from the "courses" table and displays them in the availableCoursesTable.
     */
    private void loadAvailableCourses() {
        String sql = "SELECT * FROM courses";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            availableCoursesTable.getItems().clear();

            while(rs.next()) {
                // Updated constructor: course_code, course_name, credit_hours, instructor_name
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
            showAlert("Error loading available courses from the database.");
        }
    }

    /**
     * Loads the courses the current student is enrolled in from the "enrollments" table.
     * This query uses a join between the "courses" and "enrollments" tables.
     */
    private void loadEnrolledCourses() {
        // Check if the studentId has been set.
        if(studentId == null || studentId.isEmpty()) {
            return;
        }
        String sql = "SELECT c.* FROM courses c INNER JOIN enrollments e ON c.course_code = e.course_code WHERE e.student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                enrolledCoursesTable.getItems().clear();
                while(rs.next()) {
                    // Updated constructor to include credit_hours and instructor_name
                    Course course = new Course(
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getInt("credit_hours"),
                            rs.getString("instructor_name"),
                            rs.getString("course_description")

                    );
                    enrolledCoursesTable.getItems().add(course);
                }
            }
        } catch (SQLException e) {
            logger.severe("Error loading enrolled courses: " + e.getMessage());
            showAlert("Error loading your enrolled courses.");
        }
    }

    /**
     * Handles enrolling the student in the selected course from the availableCoursesTable.
     * Inserts a new record into the "enrollments" table.
     */
    @FXML
    private void handleEnroll() {
        Course selected = availableCoursesTable.getSelectionModel().getSelectedItem();
        if(selected == null) {
            showAlert("Please select a course to enroll.");
            return;
        }
        if(studentId == null || studentId.isEmpty()) {
            showAlert("Student ID not set. Please log in again.");
            return;
        }

        String sql = "INSERT INTO enrollments (student_id, course_code) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            stmt.setString(2, selected.getCourseCode());

            int rows = stmt.executeUpdate();
            if(rows > 0) {
                showAlert("Enrolled in course successfully.");
                loadEnrolledCourses();
            } else {
                showAlert("Failed to enroll in course.");
            }
        } catch (SQLException e) {
            logger.severe("Error enrolling in course: " + e.getMessage());
            showAlert("Error enrolling in the course.");
        }
    }

    /**
     * Handles dropping an enrollment for the selected course from the enrolledCoursesTable.
     * Deletes the corresponding record from the "enrollments" table.
     */
    @FXML
    private void handleDropEnrollment() {
        Course selected = enrolledCoursesTable.getSelectionModel().getSelectedItem();
        if(selected == null) {
            showAlert("Please select a course to drop.");
            return;
        }
        if(studentId == null || studentId.isEmpty()) {
            showAlert("Student ID not set. Please log in again.");
            return;
        }

        String sql = "DELETE FROM enrollments WHERE student_id = ? AND course_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            stmt.setString(2, selected.getCourseCode());

            int rows = stmt.executeUpdate();
            if(rows > 0) {
                showAlert("Dropped enrollment successfully.");
                loadEnrolledCourses();
            } else {
                showAlert("Failed to drop enrollment.");
            }
        } catch (SQLException e) {
            logger.severe("Error dropping enrollment: " + e.getMessage());
            showAlert("Error dropping the enrollment from the database.");
        }
    }

    /**
     * Utility method to display an informational alert.
     * @param message The message to display.
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}