package ui;

import config.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Course;
import java.sql.*;
import java.util.logging.Logger;

/**
 * This controller allows a student to view all available courses,
 * view their enrolled courses, register for new ones (if under 18 credits),
 * and drop courses they've already enrolled in.
 */
public class StudentCourseRegistrationController {

    @FXML
    private TableView<Course> availableCoursesTable;
    @FXML
    private TableColumn<Course, String> availableCourseCodeCol;
    @FXML
    private TableColumn<Course, String> availableCourseNameCol;
    @FXML
    private TableColumn<Course, Integer> availableCreditHoursCol;
    @FXML
    private TableColumn<Course, String> availableInstructorCol;

    @FXML
    private TableView<Course> enrolledCoursesTable;
    @FXML
    private TableColumn<Course, String> enrolledCourseCodeCol;
    @FXML
    private TableColumn<Course, String> enrolledCourseNameCol;
    @FXML
    private TableColumn<Course, Integer> enrolledCreditHoursCol;
    @FXML
    private TableColumn<Course, String> enrolledInstructorCol;

    @FXML
    private Button registerButton;
    @FXML
    private Button dropButton;

    // Logger for logging events
    private static final Logger logger = Logger.getLogger(StudentCourseRegistrationController.class.getName());

    // The current student's ID (set after login or via dashboard)
    private String studentId;

    @FXML
    public void initialize() {
        // Set up the cell value factories for the TableView columns
        availableCourseCodeCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        availableCourseNameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        availableCreditHoursCol.setCellValueFactory(new PropertyValueFactory<>("creditHours"));
        availableInstructorCol.setCellValueFactory(new PropertyValueFactory<>("instructorName"));

        enrolledCourseCodeCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        enrolledCourseNameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        enrolledCreditHoursCol.setCellValueFactory(new PropertyValueFactory<>("creditHours"));
        enrolledInstructorCol.setCellValueFactory(new PropertyValueFactory<>("instructorName"));

        loadAvailableCourses();
        // enrolled courses will be loaded once we have the studentId
    }

    /**
     * This method is called by the Dashboard (or login flow) to provide the current student's ID.
     * Once set, we can load the student's enrolled courses.
     */
    public void setStudentId(String studentId) {
        this.studentId = studentId;
        loadEnrolledCourses();
    }

    /**
     * Loads all courses from the 'courses' table into the availableCoursesTable.
     */
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
            showAlert("Error loading available courses from the database.");
        }
    }

    /**
     * Loads all courses the student is enrolled in by joining 'enrollments' and 'courses'.
     */
    private void loadEnrolledCourses() {
        if (studentId == null || studentId.isEmpty()) {
            return; // We can't load enrollments without a valid student ID
        }
        String sql = "SELECT c.* FROM courses c " +
                "JOIN enrollments e ON c.course_code = e.course_code " +
                "WHERE e.student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
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
            }
        } catch (SQLException e) {
            logger.severe("Error loading enrolled courses: " + e.getMessage());
            showAlert("Error loading enrolled courses.");
        }
    }

    /**
     * Handles registering the student for the selected course from the availableCoursesTable,
     * ensuring we do not exceed 18 credits or duplicate enrollments.
     */
    @FXML
    private void handleRegister() {
        Course selected = availableCoursesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a course to register.");
            return;
        }
        if (studentId == null || studentId.isEmpty()) {
            showAlert("No student ID found. Please log in again.");
            return;
        }

        // Check if already enrolled in this course
        if (isAlreadyEnrolled(selected.getCourseCode())) {
            showAlert("You are already enrolled in " + selected.getCourseName() + ".");
            return;
        }

        // Check total credits
        int currentCredits = getTotalEnrolledCredits();
        if (currentCredits + selected.getCreditHours() > 18) {
            showAlert("Cannot register. You will exceed the 18-credit limit.");
            return;
        }

        String sql = "INSERT INTO enrollments (student_id, course_code) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            stmt.setString(2, selected.getCourseCode());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                showAlert("Successfully registered for " + selected.getCourseName() + ".");
                loadEnrolledCourses();
            } else {
                showAlert("Failed to register for the course.");
            }
        } catch (SQLException e) {
            logger.severe("Error registering course: " + e.getMessage());
            showAlert("Error registering for the course.");
        }
    }

    /**
     * Handles dropping an enrolled course from the enrolledCoursesTable.
     */
    @FXML
    private void handleDrop() {
        Course selected = enrolledCoursesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a course to drop.");
            return;
        }
        if (studentId == null || studentId.isEmpty()) {
            showAlert("No student ID found. Please log in again.");
            return;
        }

        String sql = "DELETE FROM enrollments WHERE student_id = ? AND course_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            stmt.setString(2, selected.getCourseCode());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                showAlert("Dropped " + selected.getCourseName() + " successfully.");
                loadEnrolledCourses();
            } else {
                showAlert("Failed to drop the course.");
            }
        } catch (SQLException e) {
            logger.severe("Error dropping course: " + e.getMessage());
            showAlert("Error dropping the course from your enrollment.");
        }
    }

    /**
     * Checks if the student is already enrolled in the given course_code.
     * @param courseCode The course code to check.
     * @return true if enrolled, false otherwise.
     */
    private boolean isAlreadyEnrolled(String courseCode) {
        String sql = "SELECT 1 FROM enrollments WHERE student_id = ? AND course_code = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            stmt.setString(2, courseCode);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // If there's a row, we are already enrolled
            }
        } catch (SQLException e) {
            logger.severe("Error checking enrollment: " + e.getMessage());
        }
        return false;
    }

    /**
     * Computes the total credit hours for all courses in which the student is enrolled.
     * @return the total enrolled credit hours.
     */
    private int getTotalEnrolledCredits() {
        String sql = "SELECT SUM(c.credit_hours) AS total_credits " +
                "FROM enrollments e " +
                "JOIN courses c ON e.course_code = c.course_code " +
                "WHERE e.student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_credits");
                }
            }
        } catch (SQLException e) {
            logger.severe("Error computing total enrolled credits: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Utility method to display an informational alert.
     * @param message The message to display in the alert.
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}