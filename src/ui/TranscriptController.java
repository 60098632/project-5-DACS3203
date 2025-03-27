package ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import util.SessionManager;
import config.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TranscriptController {
    private String studentId;

    @FXML private TextArea transcriptArea;

    public void setStudentId(String studentId) {
        this.studentId = studentId;
        loadTranscript();
    }

    private void loadTranscript() {
        String sql = "SELECT c.course_code, c.course_name, e.grade "
                + "FROM enrollments e JOIN courses c ON e.course_code = c.course_code "
                + "WHERE e.student_id = ?";
        StringBuilder sb = new StringBuilder("Transcript for " + SessionManager.getUserName() + ":\n\n");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String grade = rs.getString("grade");
                sb.append(rs.getString("course_code"))
                        .append(" - ").append(rs.getString("course_name"))
                        .append(": ").append(grade == null ? "In Progress" : grade)
                        .append("\n");
            }
            transcriptArea.setText(sb.toString());

        } catch (Exception e) {
            showAlert("Unable to load transcript.");
            e.printStackTrace();
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
        new Alert(AlertType.ERROR, msg).showAndWait();
    }
}