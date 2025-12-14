package controller.report;

import dao.IncidentReportDAO;
import dao.IncidentReportDAOImpl;
import dao.VictimDAO;
import dao.VictimDAOImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * Report: Victim Activity
 * Shows victims with more than 3 incidents in a selected month.
 * Displays victim name, email, and incident count for identifying high-activity accounts.
 */
public class VictimActivityReportController {

    @FXML private ComboBox<Integer> yearCombo;
    @FXML private ComboBox<Integer> monthCombo;
    @FXML private TableView<VictimActivity> table;
    @FXML private TableColumn<VictimActivity, String> nameCol, emailCol;
    @FXML private TableColumn<VictimActivity, Integer> countCol;
    @FXML private Button generateButton;
    @FXML private Button exportButton;

    // DAO instances for database operations
    private final VictimDAO victimDAO = new VictimDAOImpl();
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();

    @FXML
    private void initialize() {
        int year = java.time.Year.now().getValue();
        yearCombo.setItems(FXCollections.observableArrayList(2020, 2021, 2022, 2023, 2024, year));
        yearCombo.setValue(year);
        monthCombo.setItems(FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10,11,12));
        monthCombo.setValue(java.time.LocalDate.now().getMonthValue());

        nameCol.setCellValueFactory(d -> d.getValue().nameProperty());
        emailCol.setCellValueFactory(d -> d.getValue().emailProperty());
        countCol.setCellValueFactory(d -> d.getValue().countProperty().asObject());

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
                SELECT v.Name, v.ContactEmail, COUNT(*) as cnt
                FROM IncidentReports i
                JOIN Victims v ON i.VictimID = v.VictimID
                WHERE YEAR(i.DateReported) = ? AND MONTH(i.DateReported) = ?
                GROUP BY v.VictimID
                HAVING cnt > 3
                ORDER BY cnt DESC
                """;

            System.out.println("VictimActivityReportController: Generating report for " + year + "-" + month);
            System.out.println("VictimActivityReportController: Executing SQL: " + sql);

            List<VictimActivity> list = new ArrayList<>();
            try (var conn = util.DatabaseConnection.getConnection();
                 var stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, year);
                stmt.setInt(2, month);
                var rs = stmt.executeQuery();

                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                    list.add(new VictimActivity(
                            rs.getString("Name"),
                            rs.getString("ContactEmail"),
                            rs.getInt("cnt")
                    ));
                }
                System.out.println("VictimActivityReportController: Query returned " + rowCount + " rows");
            }

            table.setItems(FXCollections.observableArrayList(list));
            exportButton.setDisable(list.isEmpty());
            
            if (list.isEmpty()) {
                showAlert("No high-risk victims found for " + year + "-" + String.format("%02d", month) + 
                        ". (Victims with >3 incidents in the selected month)\n" +
                        "Try selecting a different year/month or check if data exists in the database.");
            } else {
                showAlert(list.size() + " high-risk victims found.");
            }

        } catch (Exception e) {
            System.err.println("VictimActivityReportController: Error generating report: " + e.getMessage());
            e.printStackTrace();
            showError("Failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        File file = new FileChooser().showSaveDialog(null);
        if (file == null) return;
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("Name,Email,IncidentCount");
            for (var v : table.getItems()) {
                pw.println(v.getName() + "," + v.getEmail() + "," + v.getCount());
            }
            showAlert("Exported");
        } catch (Exception e) {
            showError("Export failed");
        }
    }

    private void showAlert(String msg) { new Alert(Alert.AlertType.INFORMATION, msg).show(); }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).show(); }

    public static class VictimActivity {
        private final String name, email;
        private final int count;
        public VictimActivity(String n, String e, int c) { name=n; email=e; count=c; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public int getCount() { return count; }
        public javafx.beans.property.StringProperty nameProperty() { return new javafx.beans.property.SimpleStringProperty(name); }
        public javafx.beans.property.StringProperty emailProperty() { return new javafx.beans.property.SimpleStringProperty(email); }
        public javafx.beans.property.IntegerProperty countProperty() { return new javafx.beans.property.SimpleIntegerProperty(count); }
    }
}