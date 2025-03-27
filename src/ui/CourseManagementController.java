package ui;

import config.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Course;
import java.sql.*;
import java.util.logging.Logger;

public class CourseManagementController {
    @FXML private TableView<Course> courseTable;
    @FXML private TableColumn<Course, String> courseCodeColumn;
    @FXML private TableColumn<Course, String> courseNameColumn;
    @FXML private TableColumn<Course, Integer> creditHoursColumn;
    @FXML private TableColumn<Course, String> instructorColumn;
    @FXML private TableColumn<Course, String> descriptionColumn;
    @FXML private TextField courseCodeField;
    @FXML private TextField courseNameField;
    @FXML private TextField creditHoursField;
    @FXML private TextField instructorField;
    @FXML private TextArea descriptionField;
    @FXML private Button addButton;
    @FXML private Button deleteButton;
    @FXML private Button goBackButton;

    private static final Logger logger = Logger.getLogger(CourseManagementController.class.getName());

    @FXML
    public void initialize() {
        // Link table columns with Course properties
        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        creditHoursColumn.setCellValueFactory(new PropertyValueFactory<>("creditHours"));
        instructorColumn.setCellValueFactory(new PropertyValueFactory<>("instructorName"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("courseDescription"));
        loadCourses();
    }

    private void loadCourses() {
        ObservableList<Course> courses = FXCollections.observableArrayList();
        String sql = "SELECT * FROM courses";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                courses.add(new Course(
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getInt("credit_hours"),
                        rs.getString("instructor_name"),
                        rs.getString("course_description")
                ));
            }
            courseTable.setItems(courses);
        } catch (SQLException e) {
            logger.severe("Error loading courses: " + e.getMessage());
            showAlert("Failed to load courses.");
        }
    }

    @FXML
    private void handleAddCourse() {
        String code = courseCodeField.getText().trim();
        String name = courseNameField.getText().trim();
        String hoursStr = creditHoursField.getText().trim();
        String instructor = instructorField.getText().trim();
        String description = descriptionField.getText().trim();

        if (code.isEmpty() || name.isEmpty() || hoursStr.isEmpty() || instructor.isEmpty()) {
            showAlert("All fields except description are required.");
            return;
        }
        int hours;
        try {
            hours = Integer.parseInt(hoursStr);
        } catch (NumberFormatException e) {
            showAlert("Credit hours must be a number.");
            return;
        }
        String sql = "INSERT INTO courses (course_code, course_name, credit_hours, instructor_name, course_description) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            stmt.setString(2, name);
            stmt.setInt(3, hours);
            stmt.setString(4, instructor);
            stmt.setString(5, description);
            stmt.executeUpdate();
            showAlert("Course added successfully.");
            clearFields();
            loadCourses();
        } catch (SQLException e) {
            logger.severe("Failed to add course: " + e.getMessage());
            showAlert("Error adding course.");
        }
    }

    @FXML
    private void handleDeleteCourse() {
        Course selected = courseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a course to delete.");
            return;
        }
        // Prevent deletion if students are enrolled in this course
        String checkSql = "SELECT COUNT(*) FROM enrollments WHERE course_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, selected.getCourseCode());
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                showAlert("Cannot delete course \"" + selected.getCourseName()
                        + "\" because students are enrolled in it.");
                return;
            }
        } catch (SQLException e) {
            logger.warning("Enrollment check failed: " + e.getMessage());
        }
        // Confirm deletion
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete course \"" + selected.getCourseName() + "\"?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText("Confirm Deletion");
        confirm.showAndWait();
        if (confirm.getResult() != ButtonType.OK) {
            return;
        }
        // Proceed with deletion
        String sql = "DELETE FROM courses WHERE course_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, selected.getCourseCode());
            stmt.executeUpdate();
            showAlert("Course deleted successfully.");
            loadCourses();
        } catch (SQLException e) {
            logger.severe("Failed to delete course: " + e.getMessage());
            showAlert("Error deleting course.");
        }
    }

    @FXML
    private void handleGoBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Dashboard.fxml"));
            goBackButton.getScene().setRoot(root);
        } catch (Exception e) {
            logger.severe("Error returning to dashboard: " + e.getMessage());
            showAlert("Could not return to dashboard.");
        }
    }

    private void clearFields() {
        courseCodeField.clear();
        courseNameField.clear();
        creditHoursField.clear();
        instructorField.clear();
        descriptionField.clear();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}