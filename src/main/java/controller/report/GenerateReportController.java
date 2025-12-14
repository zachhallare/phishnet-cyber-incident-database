package controller.report;

import javafx.fxml.FXML;
import javafx.scene.control.TabPane;

/**
 * Controller for the Generate Report tab
 * This tab consolidates all report generation features:
 * - Monthly Attack Trends
 * - Top Perpetrators
 * - Victim Activity
 * - Incident Evidence Summary
 */
public class GenerateReportController {

    @FXML
    private TabPane reportTabPane;

    @FXML
    private void initialize() {
        // Initialize the report tab pane
        // Individual report controllers are initialized via their respective FXML files
    }
}

