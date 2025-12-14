package controller.report;

import dao.EvidenceDAO;
import dao.EvidenceDAOImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * Report: Incident Evidence Summary
 * Generates a summary report of evidence submissions for a selected month.
 * Shows evidence type, verification status, reviewing admin, and submission date.
 */
public class IncidentEvidenceSummaryReportController {

    @FXML private ComboBox<Integer> yearCombo;
    @FXML private ComboBox<Integer> monthCombo;
    @FXML private TableView<EvidenceSummary> table;
    @FXML private TableColumn<EvidenceSummary, String> typeCol, statusCol, adminCol;
    @FXML private TableColumn<EvidenceSummary, String> dateCol;
    @FXML private Button generateButton;
    @FXML private Button exportButton;

    // DAO instance for evidence database operations
    private final EvidenceDAO evidenceDAO = new EvidenceDAOImpl();

    @FXML
    private void initialize() {
        int year = java.time.Year.now().getValue();
        yearCombo.setItems(FXCollections.observableArrayList(2020, 2021, 2022, 2023, 2024, year));
        yearCombo.setValue(year);
        monthCombo.setItems(FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10,11,12));
        monthCombo.setValue(java.time.LocalDate.now().getMonthValue());

        typeCol.setCellValueFactory(d -> d.getValue().typeProperty());
        statusCol.setCellValueFactory(d -> d.getValue().statusProperty());
        adminCol.setCellValueFactory(d -> d.getValue().adminProperty());
        dateCol.setCellValueFactory(d -> d.getValue().dateProperty());

        validate();
    }

    private void validate() {
        generateButton.setDisable(yearCombo.getValue() == null || monthCombo.getValue() == null);
    }

    @FXML
    private void handleGenerate() {
        int year = yearCombo.getValue();
        int month = monthCombo.getValue();

        try {
            String sql = """
                SELECT e.EvidenceType, e.VerifiedStatus, a.Name as AdminName, e.SubmissionDate
                FROM EvidenceUpload e
                LEFT JOIN Administrators a ON e.AdminID = a.AdminID
                WHERE YEAR(e.SubmissionDate) = ? AND MONTH(e.SubmissionDate) = ?
                ORDER BY e.SubmissionDate DESC
                """;

            System.out.println("IncidentEvidenceSummaryReportController: Generating report for " + year + "-" + month);
            System.out.println("IncidentEvidenceSummaryReportController: Executing SQL: " + sql);

            List<EvidenceSummary> list = new ArrayList<>();
            try (var conn = util.DatabaseConnection.getConnection();
                 var stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, year);
                stmt.setInt(2, month);
                var rs = stmt.executeQuery();

                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                    String submissionDateStr = rs.getString("SubmissionDate");
                    String dateStr = (submissionDateStr != null && submissionDateStr.length() >= 16) 
                            ? submissionDateStr.substring(0, 16) 
                            : submissionDateStr;
                    list.add(new EvidenceSummary(
                            rs.getString("EvidenceType"),
                            rs.getString("VerifiedStatus"),
                            rs.getString("AdminName"),
                            dateStr
                    ));
                }
                System.out.println("IncidentEvidenceSummaryReportController: Query returned " + rowCount + " rows");
            }

            table.setItems(FXCollections.observableArrayList(list));
            exportButton.setDisable(list.isEmpty());
            
            if (list.isEmpty()) {
                showAlert("No evidence records found for " + year + "-" + String.format("%02d", month) + 
                        ". Try selecting a different year/month or check if data exists in the database.");
            } else {
                showAlert(list.size() + " evidence records.");
            }

        } catch (Exception e) {
            System.err.println("IncidentEvidenceSummaryReportController: Error generating report: " + e.getMessage());
            e.printStackTrace();
            showError("Failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        File file = new FileChooser().showSaveDialog(null);
        if (file == null) return;
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("Type,Status,ReviewedBy,Submitted");
            for (var e : table.getItems()) {
                pw.println(e.getType() + "," + e.getStatus() + "," + e.getAdmin() + "," + e.getDate());
            }
            showAlert("Exported");
        } catch (Exception e) {
            showError("Export failed");
        }
    }

    private void showAlert(String msg) { new Alert(Alert.AlertType.INFORMATION, msg).show(); }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).show(); }

    public static class EvidenceSummary {
        private final String type, status, admin, date;
        public EvidenceSummary(String t, String s, String a, String d) { type=t; status=s; admin=a==null?"Pending":a; date=d; }
        public String getType() { return type; }
        public String getStatus() { return status; }
        public String getAdmin() { return admin; }
        public String getDate() { return date; }
        public javafx.beans.property.StringProperty typeProperty() { return new javafx.beans.property.SimpleStringProperty(type); }
        public javafx.beans.property.StringProperty statusProperty() { return new javafx.beans.property.SimpleStringProperty(status); }
        public javafx.beans.property.StringProperty adminProperty() { return new javafx.beans.property.SimpleStringProperty(admin); }
        public javafx.beans.property.StringProperty dateProperty() { return new javafx.beans.property.SimpleStringProperty(date); }
    }
}