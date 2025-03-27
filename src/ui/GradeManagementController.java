package ui;

import config.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import model.Enrollment;
import util.SessionManager;
import java.sql.*;

public class GradeManagementController {
    private String instructorName;

    @FXML private ComboBox<String> courseSelector;
    @FXML private TableView<Enrollment> gradeTable;
    @FXML private TableColumn<Enrollment, String> studentIdColumn;
    @FXML private TableColumn<Enrollment, String> gradeColumn;
    @FXML private TextField gradeInput;
    @FXML private Button updateGradeButton;
    @FXML private Button backButton;

    private ObservableList<Enrollment> enrollments = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up columns for Enrollment properties
        studentIdColumn.setCellValueFactory(data -> data.getValue().studentIdProperty());
        gradeColumn.setCellValueFactory(data -> data.getValue().gradeProperty());
        loadInstructorCourses();
        // When a course is selected, load its students
        courseSelector.setOnAction(e -> loadEnrolledStudents());
    }

    private void loadInstructorCourses() {
        // Use the instructor's name (from session or passed in)
        String instructor = (instructorName != null) ? instructorName : SessionManager.getUserName();
        String sql = "SELECT course_code FROM courses WHERE instructor_name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, instructor);
            ResultSet rs = stmt.executeQuery();
            ObservableList<String> courses = FXCollections.observableArrayList();
            while (rs.next()) {
                courses.add(rs.getString("course_code"));
            }
            courseSelector.setItems(courses);
        } catch (SQLException e) {
            showAlert("Error loading courses: " + e.getMessage());
        }
    }

    private void loadEnrolledStudents() {
        enrollments.clear();
        String selectedCourse = courseSelector.getValue();
        if (selectedCourse == null) return;
        String sql = "SELECT * FROM enrollments WHERE course_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, selectedCourse);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                // Create Enrollment object for each student in the course
                Enrollment enrollment = new Enrollment(
                        rs.getString("student_id"),
                        rs.getString("course_code"),
                        rs.getString("semester"),
                        rs.getString("grade")
                );
                enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
                enrollments.add(enrollment);
            }
            gradeTable.setItems(enrollments);
        } catch (SQLException e) {
            showAlert("Error loading enrolled students: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateGrade() {
        Enrollment selected = gradeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a student to update the grade.");
            return;
        }
        String newGrade = gradeInput.getText().trim();
        if (newGrade.isEmpty()) {
            showAlert("Please enter a grade.");
            return;
        }
        String sql = "UPDATE enrollments SET grade = ? WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newGrade);
            stmt.setInt(2, selected.getEnrollmentId());
            stmt.executeUpdate();
            showAlert("Grade updated successfully.");
            loadEnrolledStudents();  // Refresh the table
        } catch (SQLException e) {
            showAlert("Failed to update grade: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Dashboard.fxml"));
            backButton.getScene().setRoot(root);
        } catch (Exception e) {
            showAlert("Failed to return to dashboard.");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /** Called by DashboardController to set the instructor's name (if needed). */
    public void setInstructorName(String name) {
        this.instructorName = name;
    }
}