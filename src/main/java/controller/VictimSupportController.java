package controller;

import dao.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import model.Administrator;
import model.Victim;
import model.VictimStatusLog;

import java.util.List;

/**
 * Controller for Transaction 4: Victim Account Status Update (Admin).
 * Allows administrators to view victim accounts and flag them based on incident reports.
 */
public class VictimSupportController {

    // UI elements from FXML
    @FXML private TableView<Victim> victimTable;      // Table to display victims
    @FXML private TableColumn<Victim, String> nameCol, emailCol, statusCol; // Table columns
    @FXML private Button flagButton;                 // Button to flag a victim
    @FXML private TextArea notesArea;                // Text area for admin notes (currently unused)

    // DAO instances to interact with database
    private final VictimDAO victimDAO = new VictimDAOImpl();
    private final VictimStatusLogDAO logDAO = new VictimStatusLogDAOImpl();
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();

    // Currently logged-in administrator
    private Administrator currentAdmin;

    /**
     * Initializes the controller after the FXML elements are loaded.
     * Sets up selection listener for the victim table and loads victim data.
     */
    @FXML
    private void initialize() {
        // Enable or disable flagButton depending on table selection
        victimTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            flagButton.setDisable(newVal == null);
        });

        // Load all victims from the database
        loadVictims();
    }

    /**
     * Sets the currently logged-in administrator.
     *
     * @param admin the Administrator currently using this controller
     */
    public void setCurrentAdmin(Administrator admin) {
        this.currentAdmin = admin;
    }

    /**
     * Loads all victims from the database and displays them in the table.
     * Sets up table columns to display name, email, and account status.
     */
    private void loadVictims() {
        try {
            // Fetch all victims
            List<Victim> list = victimDAO.findAll();
            victimTable.setItems(FXCollections.observableArrayList(list));

            // Map table columns to victim properties
            nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
            emailCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getContactEmail()));
            statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAccountStatus()));
        } catch (Exception e) {
            // Show error if loading fails
            showError("Load failed: " + e.getMessage());
        }
    }

    /**
     * Handles the action of flagging a selected victim account.
     * Only flags victims with more than 5 incidents in the last month.
     * Logs the status change and updates the table view.
     */
    @FXML
    private void handleFlagVictim() {
        Victim selected = victimTable.getSelectionModel().getSelectedItem();
        if (selected == null) return; // No victim selected

        try {
            // Count incidents for the victim in the last month
            int count = incidentDAO.countIncidentsLastMonth(selected.getVictimID());

            // Check if victim meets criteria to be flagged
            if (count <= 5 && !"Flagged".equals(selected.getAccountStatus())) {
                showAlert("Victim has only " + count + " incidents. Not flagging.");
                return;
            }

            // Save old status for logging
            String oldStatus = selected.getAccountStatus();
            selected.setAccountStatus("Flagged");

            // Update victim in database and log the change
            victimDAO.update(selected);
            logDAO.logChange(selected.getVictimID(), oldStatus, "Flagged", currentAdmin.getAdminID());

            // Inform admin of successful flag
            showAlert(Alert.AlertType.WARNING, "Victim Flagged",
                    selected.getName() + " is now FLAGGED for support.\nIncidents last month: " + count);

            // Refresh table to show updated status
            loadVictims();
        } catch (Exception e) {
            // Show error if flagging fails
            showError("Flag failed: " + e.getMessage());
        }
    }

    /**
     * Shows an alert dialog with a given type, title, and message.
     *
     * @param type the type of alert (INFO, WARNING, ERROR)
     * @param title the title of the alert dialog
     * @param msg the message content
     */
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setContentText(msg);
        a.show();
    }

    /**
     * Convenience method to show an error alert.
     *
     * @param msg the error message
     */
    private void showError(String msg) {
        showAlert(Alert.AlertType.ERROR, "Error", msg);
    }

    /**
     * Convenience method to show an informational alert.
     *
     * @param msg the info message
     */
    private void showAlert(String msg) {
        showAlert(Alert.AlertType.INFORMATION, "Info", msg);
    }
}