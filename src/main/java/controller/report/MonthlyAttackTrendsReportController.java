package controller.report;

import dao.AttackTypeDAO;
import dao.AttackTypeDAOImpl;
import dao.IncidentReportDAO;
import dao.IncidentReportDAOImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.time.YearMonth;
import java.util.*;

/**
 * Report: Monthly Attack Trends
 * Shows incidents by attack type and time-of-day pattern for a selected month.
 * Displays data in a bar chart format for visual analysis.
 */
public class MonthlyAttackTrendsReportController {

    @FXML private ComboBox<Integer> yearCombo;
    @FXML private ComboBox<Integer> monthCombo;
    @FXML private BarChart<String, Number> chart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Button generateButton;
    @FXML private Button exportButton;

    // DAO instances for database operations
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();
    private final AttackTypeDAO attackDAO = new AttackTypeDAOImpl();

    @FXML
    private void initialize() {
        // Years: 2020 to current
        int currentYear = YearMonth.now().getYear();
        List<Integer> years = new ArrayList<>();
        for (int y = 2020; y <= currentYear; y++) years.add(y);
        yearCombo.setItems(FXCollections.observableArrayList(years));
        yearCombo.setValue(currentYear);

        // Months 1-12
        List<Integer> months = new ArrayList<>();
        for (int m = 1; m <= 12; m++) months.add(m);
        monthCombo.setItems(FXCollections.observableArrayList(months));
        monthCombo.setValue(YearMonth.now().getMonthValue());

        xAxis.setLabel("Hour of Day (0-23)");
        yAxis.setLabel("Number of Incidents");
        chart.setTitle("Attack Trends by Hour");

        yearCombo.valueProperty().addListener((obs, old, val) -> validate());
        monthCombo.valueProperty().addListener((obs, old, val) -> validate());
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
            Map<String, int[]> hourlyData = new HashMap<>();
            List<model.AttackType> types = attackDAO.findAll();

            System.out.println("MonthlyAttackTrendsReportController: Generating report for " + year + "-" + month);
            System.out.println("MonthlyAttackTrendsReportController: Found " + types.size() + " attack types");

            // Initialize
            for (model.AttackType type : types) {
                hourlyData.put(type.getAttackName(), new int[24]);
            }

            // Query DB
            String sql = """
                SELECT a.AttackName, HOUR(i.DateReported) as hour
                FROM IncidentReports i
                JOIN AttackTypes a ON i.AttackTypeID = a.AttackTypeID
                WHERE YEAR(i.DateReported) = ? AND MONTH(i.DateReported) = ?
                """;

            System.out.println("MonthlyAttackTrendsReportController: Executing SQL: " + sql);
            int totalRows = 0;
            try (var conn = util.DatabaseConnection.getConnection();
                 var stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, year);
                stmt.setInt(2, month);
                var rs = stmt.executeQuery();

                while (rs.next()) {
                    totalRows++;
                    String attack = rs.getString("AttackName");
                    int hour = rs.getInt("hour");
                    if (hourlyData.containsKey(attack)) {
                        hourlyData.get(attack)[hour]++;
                    }
                }
                System.out.println("MonthlyAttackTrendsReportController: Query returned " + totalRows + " rows");
            }

            // Build chart
            chart.getData().clear();
            int seriesCount = 0;
            for (Map.Entry<String, int[]> entry : hourlyData.entrySet()) {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName(entry.getKey());
                boolean hasData = false;
                for (int h = 0; h < 24; h++) {
                    if (entry.getValue()[h] > 0) {
                        series.getData().add(new XYChart.Data<>(String.valueOf(h), entry.getValue()[h]));
                        hasData = true;
                    }
                }
                if (hasData) {
                    chart.getData().add(series);
                    seriesCount++;
                }
            }

            exportButton.setDisable(seriesCount == 0);
            
            if (totalRows == 0) {
                showAlert("No incidents found for " + YearMonth.of(year, month) + 
                        ". Try selecting a different year/month or check if data exists in the database.");
            } else {
                showAlert("Report generated for " + YearMonth.of(year, month) + 
                        ". Showing " + seriesCount + " attack type(s) with data.");
            }

        } catch (Exception e) {
            System.err.println("MonthlyAttackTrendsReportController: Error generating report: " + e.getMessage());
            e.printStackTrace();
            showError("Generate failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = chooser.showSaveDialog(null);
        if (file == null) return;

        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("AttackType,Hour0,Hour1,...,Hour23");
            for (var series : chart.getData()) {
                String name = series.getName();
                StringBuilder row = new StringBuilder(name);
                for (int h = 0; h < 24; h++) {
                    row.append(",").append(getCount(series, h));
                }
                pw.println(row);
            }
            showAlert("Exported to " + file.getName());
        } catch (Exception e) {
            showError("Export failed: " + e.getMessage());
        }
    }

    private int getCount(XYChart.Series<String, Number> series, int hour) {
        for (var data : series.getData()) {
            if (data.getXValue().equals(String.valueOf(hour))) {
                return data.getYValue().intValue();
            }
        }
        return 0;
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.show();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.show();
    }
}

