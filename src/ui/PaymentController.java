package ui;

import config.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.PaymentEntry;
import util.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentController {
    @FXML
    private TableView<PaymentEntry> coursesTable;
    @FXML
    private TableColumn<PaymentEntry, String> courseCodeColumn;
    @FXML
    private TableColumn<PaymentEntry, Integer> creditHoursColumn;
    @FXML
    private TableColumn<PaymentEntry, Double> costColumn;

    @FXML
    private Label totalCostLabel;
    @FXML
    private Label totalPaidLabel;
    @FXML
    private Label outstandingLabel;

    @FXML
    private TextField paymentAmountField;
    @FXML
    private Button payButton;
    @FXML
    private Button backButton;

    private String studentId;
    private double totalCost = 0.0;
    private double totalPaid = 0.0;
    private double outstanding = 0.0;

    private ObservableList<PaymentEntry> paymentEntries = FXCollections.observableArrayList();

    /**
     * Called by the DashboardController to set the current student's ID.
     */
    public void setStudentId(String studentId) {
        this.studentId = studentId;
        initializeTableColumns();
        loadData();
    }

    /**
     * Sets up table columns to map PaymentEntry properties.
     */
    private void initializeTableColumns() {
        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        creditHoursColumn.setCellValueFactory(new PropertyValueFactory<>("creditHours"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));
    }

    /**
     * Loads all the student's enrolled courses, calculates the cost,
     * then loads total payments to find the outstanding balance.
     */
    private void loadData() {
        paymentEntries.clear();
        totalCost = 0.0;
        totalPaid = 0.0;

        if (studentId == null || studentId.isEmpty()) {
            showAlert("No student ID. Please log in again.");
            return;
        }

        // 1) Load enrolled courses from the database
        String enrollmentsSql =
                "SELECT c.course_code, c.credit_hours " +
                        "FROM enrollments e " +
                        "JOIN courses c ON e.course_code = c.course_code " +
                        "WHERE e.student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(enrollmentsSql)) {
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String code = rs.getString("course_code");
                int hours = rs.getInt("credit_hours");
                double cost = hours * 975.0;  // 975 QAR per credit hour
                totalCost += cost;
                paymentEntries.add(new PaymentEntry(code, hours, cost));
            }
        } catch (SQLException e) {
            showAlert("Error loading enrolled courses: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // 2) Load total payments from the database
        String paymentsSql =
                "SELECT IFNULL(SUM(amount), 0) AS total_paid " +
                        "FROM payments " +
                        "WHERE student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(paymentsSql)) {
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                totalPaid = rs.getDouble("total_paid");
            }
        } catch (SQLException e) {
            showAlert("Error loading payments: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // 3) Calculate outstanding
        outstanding = totalCost - totalPaid;

        // Update the UI
        coursesTable.setItems(paymentEntries);
        totalCostLabel.setText(String.format("Total Cost: %.2f QAR", totalCost));
        totalPaidLabel.setText(String.format("Total Paid: %.2f QAR", totalPaid));
        outstandingLabel.setText(String.format("Outstanding: %.2f QAR", outstanding));
    }

    /**
     * Handles the "Pay" button. Inserts a new row in 'payments' if the amount is valid.
     */
    @FXML
    private void handlePay() {
        String input = paymentAmountField.getText().trim();
        if (input.isEmpty()) {
            showAlert("Please enter a payment amount.");
            return;
        }
        double paymentAmount;
        try {
            paymentAmount = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            showAlert("Invalid amount. Please enter a valid number.");
            return;
        }
        if (paymentAmount <= 0) {
            showAlert("Payment amount must be greater than 0.");
            return;
        }
        if (paymentAmount > outstanding) {
            showAlert("Payment exceeds the outstanding balance. Please enter a smaller amount.");
            return;
        }

        // Insert a new payment record
        String insertSql = "INSERT INTO payments (student_id, amount) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setString(1, studentId);
            stmt.setDouble(2, paymentAmount);
            stmt.executeUpdate();
            showAlert(String.format("Payment of %.2f QAR successful!", paymentAmount));
            paymentAmountField.clear();
            // Reload data to update totalPaid and outstanding
            loadData();
        } catch (SQLException e) {
            showAlert("Payment failed due to a database error.");
            e.printStackTrace();
        }
    }

    /**
     * Returns to the dashboard.
     */
    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Dashboard.fxml"));
            backButton.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
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