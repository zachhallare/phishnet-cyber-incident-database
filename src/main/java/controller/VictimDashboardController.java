package controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import model.Victim;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

/**
 * Controller for the Victim Dashboard.
 * This controller manages the main dashboard for a victim, including three tabs:
 * - Submit Report
 * - Upload Evidence
 * - View Reports
 * It also handles logout functionality and updates tab controllers with the current victim data.
 */
public class VictimDashboardController {

    @FXML private Label victimNameLabel; // Label to display the current victim's name
    @FXML private TabPane tabPane; // TabPane containing the dashboard tabs
    @FXML private ReportIncidentController reportIncidentTabController; // Controller for submitting reports
    @FXML private UploadEvidenceController uploadEvidenceTabController; // Controller for uploading evidence
    @FXML private ViewMyReportsController viewMyReportsTabController; // Controller for viewing reports

    private Victim currentVictim; // The currently logged-in victim

    /**
     * Initializes the controller.
     * This method is called automatically after the FXML is loaded.
     */
    @FXML
    private void initialize() {
        // Currently no initialization logic needed
    }

    /**
     * Returns the currently set victim.
     * @return current Victim
     */
    public Victim getCurrentVictim() {
        return currentVictim;
    }

    /**
     * Sets the current victim and updates the dashboard UI accordingly.
     * Also updates all tab controllers with the current victim.
     *
     * @param victim the Victim object to set as current
     */
    public void setCurrentVictim(Victim victim) {
        this.currentVictim = victim;

        // Update victim name label, truncate if name is too long
        if (victimNameLabel != null) {
            String name = victim.getName();
            if (name != null && name.length() > 30) {
                name = name.substring(0, 27) + "...";
            }
            victimNameLabel.setText("Victim: " + (name != null ? name : "Unknown"));
        }

        // Update Report Incident tab controller
        if (reportIncidentTabController != null) {
            reportIncidentTabController.setCurrentVictim(victim);
            // Callback triggered when a new incident is created
            reportIncidentTabController.setIncidentCreatedCallback(incident -> {
                if (viewMyReportsTabController != null) {
                    viewMyReportsTabController.refreshReports(); // Refresh reports tab
                }
                if (uploadEvidenceTabController != null) {
                    uploadEvidenceTabController.refreshIncidentList(); // Refresh incident list
                    uploadEvidenceTabController.selectIncident(incident); // Select newly created incident
                }
            });
        }

        // Update View My Reports tab controller
        if (viewMyReportsTabController != null) {
            viewMyReportsTabController.setCurrentVictim(victim);
            viewMyReportsTabController.refreshReports(); // Load current reports
        }

        // Update Upload Evidence tab controller
        if (uploadEvidenceTabController != null) {
            uploadEvidenceTabController.setCurrentVictim(victim);
            uploadEvidenceTabController.refreshIncidentList(); // Load current incidents
        }
    }

    /**
     * Handles logout action.
     * Loads the login screen and shows a confirmation alert.
     */
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SceneBuilder/fxml/auth/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) victimNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 450));
            stage.setTitle("PhishNet - Cybersecurity Incident Reporting System");
            stage.centerOnScreen();
            stage.show();

            showAlert("Logged out successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Logout failed: " + e.getMessage());
        }
    }

    /**
     * Shows an information alert.
     *
     * @param msg The message to display
     */
    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.show();
    }

    /**
     * Shows an error alert.
     *
     * @param msg The error message to display
     */
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(msg);
        a.show();
    }
}