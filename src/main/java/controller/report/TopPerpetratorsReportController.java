package controller.report;

import dao.IncidentReportDAO;
import dao.IncidentReportDAOImpl;
import dao.PerpetratorDAO;
import dao.PerpetratorDAOImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * Report: Top Perpetrators
 * Shows perpetrators with the most incidents in a selected month.
 * Displays identifier, type, associated name, and incident count.
 */
public class TopPerpetratorsReportController {

    @FXML private ComboBox<Integer> yearCombo;
    @FXML private ComboBox<Integer> monthCombo;
    @FXML private TableView<TopPerp> table;
    @FXML private TableColumn<TopPerp, String> idCol;
    @FXML private TableColumn<TopPerp, String> typeCol;
    @FXML private TableColumn<TopPerp, String> nameCol;
    @FXML private TableColumn<TopPerp, Number> attacksCol;
    @FXML private Button generateButton;
    @FXML private Button exportButton;

    // DAO instances for database operations
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();
    private final PerpetratorDAO perpDAO = new PerpetratorDAOImpl();

    @FXML
    private void initialize() {
        int year = java.time.Year.now().getValue();
        yearCombo.setItems(FXCollections.observableArrayList(2020, 2021, 2022, 2023, 2024, year));
        yearCombo.setValue(year);
        monthCombo.setItems(FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10,11,12));
        monthCombo.setValue(java.time.Month.from(java.time.LocalDate.now()).getValue());

        idCol.setCellValueFactory(d -> d.getValue().identifierProperty());
        typeCol.setCellValueFactory(d -> d.getValue().typeProperty());
        nameCol.setCellValueFactory(d -> d.getValue().nameProperty());
        attacksCol.setCellValueFactory(d -> d.getValue().countProperty());

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
                SELECT p.Identifier, p.IdentifierType, p.AssociatedName, COUNT(*) as cnt
                FROM IncidentReports i
                JOIN Perpetrators p ON i.PerpetratorID = p.PerpetratorID
                WHERE YEAR(i.DateReported) = ? AND MONTH(i.DateReported) = ?
                GROUP BY p.PerpetratorID
                ORDER BY cnt DESC LIMIT 10
                """;

            System.out.println("TopPerpetratorsReportController: Generating report for " + year + "-" + month);
            System.out.println("TopPerpetratorsReportController: Executing SQL: " + sql);

            List<TopPerp> list = new ArrayList<>();
            try (var conn = util.DatabaseConnection.getConnection();
                 var stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, year);
                stmt.setInt(2, month);
                var rs = stmt.executeQuery();

                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                    list.add(new TopPerp(
                            rs.getString("Identifier"),
                            rs.getString("IdentifierType"),
                            rs.getString("AssociatedName"),
                            rs.getInt("cnt")
                    ));
                }
                System.out.println("TopPerpetratorsReportController: Query returned " + rowCount + " rows");
            }

            table.setItems(FXCollections.observableArrayList(list));
            exportButton.setDisable(list.isEmpty());
            
            if (list.isEmpty()) {
                showAlert("No perpetrators found for " + year + "-" + String.format("%02d", month) + 
                        ". Try selecting a different year/month or check if data exists in the database.");
            } else {
                showAlert("Top " + list.size() + " Perpetrators loaded for " + year + "-" + String.format("%02d", month));
            }

        } catch (Exception e) {
            System.err.println("TopPerpetratorsReportController: Error generating report: " + e.getMessage());
            e.printStackTrace();
            showError("Failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        File file = new FileChooser().showSaveDialog(null);
        if (file == null) return;

        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("Identifier,Type,Name,IncidentCount");
            for (var row : table.getItems()) {
                pw.println(row.getIdentifier() + "," + row.getType() + "," + row.getName() + "," + row.getCount());
            }
            showAlert("Exported to " + file.getName());
        } catch (Exception e) {
            showError("Export failed");
        }
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).show();
    }

    public static class TopPerp {
        private final SimpleStringProperty identifier;
        private final SimpleStringProperty type;
        private final SimpleStringProperty name;
        private final SimpleIntegerProperty count;

        public TopPerp(String i, String t, String n, int c) {
            this.identifier = new SimpleStringProperty(i);
            this.type = new SimpleStringProperty(t);
            this.name = new SimpleStringProperty(n == null ? "" : n);
            this.count = new SimpleIntegerProperty(c);
        }

        public String getIdentifier() { return identifier.get(); }
        public String getType() { return type.get(); }
        public String getName() { return name.get(); }
        public int getCount() { return count.get(); }

        public SimpleStringProperty identifierProperty() { return identifier; }
        public SimpleStringProperty typeProperty() { return type; }
        public SimpleStringProperty nameProperty() { return name; }
        public ObservableValue<Number> countProperty() { return count; }
    }
}