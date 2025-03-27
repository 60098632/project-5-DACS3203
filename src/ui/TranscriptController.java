package ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import util.SessionManager;
import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLSyntaxErrorException;

public class TranscriptController {
    private String studentId;

    @FXML
    private TextArea transcriptArea;

    // Called from DashboardController to initialize the transcript for a student
    public void setStudentId(String studentId) {
        this.studentId = studentId;
        loadTranscript();
    }

    private void loadTranscript() {
        String sql = "SELECT c.course_code, c.course_name, c.credit_hours, e.grade " +
                "FROM enrollments e JOIN courses c ON e.course_code = c.course_code " +
                "WHERE e.student_id = ?";
        StringBuilder sb = new StringBuilder("Transcript for " + SessionManager.getUserName() + ":\n\n");
        double totalGradePoints = 0.0;
        int totalCredits = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            sb.append(String.format("%-10s %-25s %-10s %-10s\n", "Code", "Course Name", "Credits", "Grade"));
            sb.append("---------------------------------------------------------------\n");
            while (rs.next()) {
                String courseCode = rs.getString("course_code");
                String courseName = rs.getString("course_name");
                int credits = rs.getInt("credit_hours");
                String grade = rs.getString("grade");
                if (grade != null && !grade.trim().isEmpty()) {
                    double gradePoints = convertGradeToPoints(grade.trim());
                    totalGradePoints += gradePoints * credits;
                    totalCredits += credits;
                }
                sb.append(String.format("%-10s %-25s %-10d %-10s\n",
                        courseCode,
                        courseName,
                        credits,
                        (grade == null || grade.isEmpty()) ? "In Progress" : grade));
            }
            if (totalCredits > 0) {
                double gpa = totalGradePoints / totalCredits;
                sb.append("\nTotal Credits: ").append(totalCredits);
                sb.append("\nGPA: ").append(String.format("%.2f", gpa));
            } else {
                sb.append("\nNo completed courses to calculate GPA.");
            }
            transcriptArea.setText(sb.toString());
        } catch (SQLSyntaxErrorException ex) {
            // Likely the grade column is missing
            showAlert("Database error: The 'grade' column is missing from the 'enrollments' table.\n" +
                    "Please update your database schema using:\n" +
                    "ALTER TABLE enrollments ADD COLUMN grade VARCHAR(10) DEFAULT NULL;");
        } catch (Exception e) {
            showAlert("Unable to load transcript.");
            e.printStackTrace();
        }
    }

    // Converts letter grade to numeric grade points.
    // Returns 0.0 if the grade is not recognized.
    private double convertGradeToPoints(String grade) {
        switch (grade.toUpperCase()) {
            case "A":  return 4.0;
            case "A-": return 3.7;
            case "B+": return 3.3;
            case "B":  return 3.0;
            case "B-": return 2.7;
            case "C+": return 2.3;
            case "C":  return 2.0;
            case "C-": return 1.7;
            case "D+": return 1.3;
            case "D":  return 1.0;
            case "F":  return 0.0;
            default:   return 0.0;
        }
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Dashboard.fxml"));
            transcriptArea.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Unable to return to dashboard.");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}