package controller;

import dao.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import model.Administrator;
import model.Perpetrator;
import model.ThreatLevelLog;

import java.util.List;

/**
 * Perpetrator Threat Level Update (Admin).
 *
 * This controller handles viewing all perpetrators, manually escalating
 * threat levels, auto-highlighting high-risk perpetrators, and logging changes.
 *
 * Core Records Used:
 * - Perpetrators Record Management
 * - Incident Reports Transaction
 * - Administrators Record Management
 *
 * Transaction Record: ThreatLevelLog
 * Attributes: LogID, PerpetratorID, OldThreatLevel, NewThreatLevel, ChangeDate, AdminID
 *
 * Services/Operations:
 * - Read perpetrator's record
 * - Count unique victims (optional auto-escalation)
 * - Update ThreatLevel if threshold met
 * - Insert log entry
 * - Notify staff
 */
public class ReviewThreatLevelController {

    // UI elements
    @FXML private TableView<Perpetrator> perpTable;
    @FXML private TableColumn<Perpetrator, String> idCol;
    @FXML private TableColumn<Perpetrator, String> typeCol;
    @FXML private TableColumn<Perpetrator, String> nameCol;
    @FXML private TableColumn<Perpetrator, String> levelCol;
    @FXML private Button escalateButton;
    @FXML private ComboBox<String> newLevelCombo;

    // DAO objects for database operations
    private final PerpetratorDAO perpDAO = new PerpetratorDAOImpl();
    private final ThreatLevelLogDAO logDAO = new ThreatLevelLogDAOImpl();
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();

    // Currently logged-in admin
    private Administrator currentAdmin;

    /**
     * Initializes the controller after the FXML is loaded.
     * - Populates combo box with threat levels
     * - Disables escalate button initially
     * - Sets up listener for table selection changes
     * - Loads all perpetrators into the table
     */
    @FXML
    private void initialize() {
        // Populate the threat level combo box
        newLevelCombo.setItems(FXCollections.observableArrayList(
                "UnderReview", "Suspected", "Malicious", "Cleared"
        ));

        // Disable escalate button until a perpetrator is selected
        escalateButton.setDisable(true);

        // Listen for table row selection changes
        perpTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            escalateButton.setDisable(newVal == null); // Disable button when no row is selected, enable when a row is selected
            if (newVal != null) {
                newLevelCombo.setValue(newVal.getThreatLevel()); // Pre-select current threat level
            }
        });

        // Load all perpetrators from the database
        loadPerpetrators();
    }

    /**
     * Set the currently logged-in administrator.
     *
     * @param admin The admin object from AdminDashboard
     */
    public void setCurrentAdmin(Administrator admin) {
        this.currentAdmin = admin;
    }

    /**
     * Load all perpetrators from the database and bind table columns to their properties.
     * Also highlights high-risk perpetrators automatically.
     */
    private void loadPerpetrators() {
        try {
            List<Perpetrator> list = perpDAO.findAll();
            perpTable.setItems(FXCollections.observableArrayList(list));

            // Bind table columns to Perpetrator properties
            idCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdentifier()));
            typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdentifierType()));
            nameCol.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getAssociatedName() != null ? d.getValue().getAssociatedName() : ""
            ));
            levelCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getThreatLevel()));

            // Highlight high-risk perpetrators based on recent incidents
            highlightHighRiskPerpetrators();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Failed to load perpetrators: " + e.getMessage());
        }
    }

    /**
     * Auto-detect and visually highlight perpetrators with ≥3 unique victims in the last 7 days.
     * Highlights rows in red and adds a tooltip with the victim count.
     */
    private void highlightHighRiskPerpetrators() {
        perpTable.setRowFactory(tv -> new TableRow<Perpetrator>() {
            @Override
            protected void updateItem(Perpetrator item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    // Reset style for empty rows
                    setStyle("");
                    setTooltip(null);
                } else {
                    try {
                        // Count unique victims in last 7 days for this perpetrator
                        int victimCount = incidentDAO.countUniqueVictimsLast7Days(item.getPerpetratorID());

                        if (victimCount >= 3 && !"Malicious".equals(item.getThreatLevel())) {
                            // Highlight row as high risk
                            setStyle("-fx-background-color: #ffcccc; -fx-font-weight: bold;");
                            setTooltip(new Tooltip("HIGH RISK: " + victimCount + " victims in last 7 days"));
                        } else {
                            // Reset normal style
                            setStyle("");
                            setTooltip(null);
                        }
                    } catch (Exception e) {
                        // In case of error, reset style
                        setStyle("");
                        setTooltip(null);
                    }
                }
            }
        });
    }

    /**
     * Handles manual threat level escalation when the button is clicked.
     * Validates selection, updates the perpetrator, logs the change, and refreshes the table.
     */
    @FXML
    private void handleEscalate() {
        Perpetrator selected = perpTable.getSelectionModel().getSelectedItem();
        String newLevel = newLevelCombo.getValue();

        // Validate selections
        if (selected == null || newLevel == null || currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "Invalid Action", "Select a perpetrator and threat level.");
            return;
        }

        String oldLevel = selected.getThreatLevel();

        // Check if threat level is actually changing
        if (oldLevel.equals(newLevel)) {
            showAlert(Alert.AlertType.INFORMATION, "No Change", "Threat level is already " + newLevel);
            return;
        }

        try {
            // Update perpetrator threat level
            selected.setThreatLevel(newLevel);
            perpDAO.update(selected);

            // Create a log entry for this change
            ThreatLevelLog log = new ThreatLevelLog();
            log.setPerpetratorID(selected.getPerpetratorID());
            log.setOldThreatLevel(oldLevel);
            log.setNewThreatLevel(newLevel);
            log.setAdminID(currentAdmin.getAdminID());
            logDAO.insert(log);

            // Show confirmation to admin
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Threat level updated: " + oldLevel + " → " + newLevel + "\n" +
                            "Logged by: " + currentAdmin.getName());

            // Refresh table to reflect changes
            loadPerpetrators();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Update Failed",
                    "Could not update threat level: " + e.getMessage());
        }
    }

    /**
     * Display an alert dialog to the user.
     *
     * @param type  The type of alert (ERROR, INFORMATION, WARNING)
     * @param title The alert title
     * @param msg   The content message
     */
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}