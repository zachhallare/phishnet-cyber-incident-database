package controller;

import dao.*;
import model.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controller class for reporting incidents (Transaction 1).
 * Handles the UI interactions, form validation, and submission logic for incident reports.
 */
public class ReportIncidentController {

    @FXML private TextField identifierField; // Input for perpetrator identifier
    @FXML private ComboBox<String> identifierTypeCombo; // Dropdown for identifier type
    @FXML private TextField associatedNameField; // Input for associated name (optional)
    @FXML private ComboBox<String> attackTypeCombo; // Dropdown for attack type
    @FXML private TextArea descriptionArea; // Text area for incident description
    @FXML private Button submitButton; // Submit button for the form
    @FXML private Label statusLabel; // Label for form validation messages

    private Victim currentVictim; // Currently selected victim
    private final PerpetratorDAO perpDAO = new PerpetratorDAOImpl(); // DAO for perpetrators
    private final AttackTypeDAO attackDAO = new AttackTypeDAOImpl(); // DAO for attack types
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl(); // DAO for incident reports
    private final ThreatLevelLogDAO threatLogDAO = new ThreatLevelLogDAOImpl(); // DAO for logging threat level changes
    private final VictimDAO victimDAO = new VictimDAOImpl(); // DAO for victim accounts
    private final VictimStatusLogDAO victimStatusLogDAO = new VictimStatusLogDAOImpl(); // DAO for logging victim status changes

    private boolean initialized = false; // Prevent multiple initializations
    private Consumer<IncidentReport> incidentCreatedCallback; // Callback after a successful report creation

    private static final int FLAG_THRESHOLD_MONTH = 5; // Threshold for auto-flagging victims (flags when incidents > 5, i.e., 6 or more)
    private static final int MIN_DESCRIPTION_LENGTH = 10; // Minimum length for description

    /**
     * Initializes the controller.
     * Loads identifier types, attack types, sets prompt texts, and adds listeners for real-time validation.
     */
    @FXML
    private void initialize() {
        if (initialized) return; // Prevent duplicate initialization
        initialized = true;

        // Load identifier types into ComboBox
        identifierTypeCombo.getItems().clear();
        identifierTypeCombo.setItems(FXCollections.observableArrayList(
                "Phone Number", "Email Address", "Social Media Account / Username",
                "Website URL / Domain", "IP Address"
        ));
        identifierTypeCombo.setValue("Phone Number");

        // Set initial placeholder text for identifier field
        updateIdentifierPromptText("Phone Number");

        // Update prompt text when identifier type changes
        identifierTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateIdentifierPromptText(newVal);
            }
        });

        // Load attack types from database
        loadAttackTypes();

        // Add listeners for real-time form validation
        identifierField.textProperty().addListener((obs, old, newVal) -> validateForm());
        attackTypeCombo.valueProperty().addListener((obs, old, newVal) -> validateForm());
        descriptionArea.textProperty().addListener((obs, old, newVal) -> validateForm());

        // Initial validation
        validateForm();
    }

    /**
     * Sets the current victim for whom the incident is being reported.
     * @param victim The Victim object
     */
    public void setCurrentVictim(Victim victim) {
        this.currentVictim = victim;
    }

    /**
     * Loads attack types from the database and populates the ComboBox.
     */
    private void loadAttackTypes() {
        try {
            if (attackTypeCombo.getItems() != null) {
                attackTypeCombo.getItems().clear(); // Clear existing items
            }

            List<AttackType> types = attackDAO.findAll();
            List<String> names = types.stream()
                    .map(AttackType::getAttackName)
                    .distinct()
                    .toList(); // Ensure unique names

            attackTypeCombo.setItems(FXCollections.observableArrayList(names));
            if (!names.isEmpty()) attackTypeCombo.getSelectionModel().selectFirst();

            System.out.println("Loaded " + names.size() + " attack types into ComboBox");
            validateForm(); // Re-validate form after loading
        } catch (Exception e) {
            showError("Failed to load attack types: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the submit button click.
     * Validates input, creates or updates perpetrator, logs incident, auto-escalates threat levels, and auto-flags victims.
     */
    @FXML
    private void handleSubmit() {
        if (!validateForm()) return;

        try {
            String identifier = identifierField.getText().trim();
            String type = identifierTypeCombo.getValue();
            String dbIdentifierType = mapIdentifierTypeToDB(type); // Convert to database value
            String name = associatedNameField.getText().trim();
            String attackName = attackTypeCombo.getValue();
            String desc = descriptionArea.getText().trim();

            // 1. Find existing perpetrator or create new
            Perpetrator perp = perpDAO.findByIdentifier(identifier);
            if (perp == null) {
                perp = new Perpetrator();
                perp.setIdentifier(identifier);
                perp.setIdentifierType(dbIdentifierType);
                perp.setAssociatedName(name.isEmpty() ? null : name);
                perp.setThreatLevel("UnderReview");
                perp.setLastIncidentDate(LocalDateTime.now());
                perpDAO.create(perp);
            } else {
                // Update last incident date for existing perpetrator
                perp.setLastIncidentDate(LocalDateTime.now());
                perpDAO.update(perp);
            }

            // 2. Create new incident report
            IncidentReport report = new IncidentReport();
            report.setVictimID(currentVictim.getVictimID());
            report.setPerpetratorID(perp.getPerpetratorID());
            report.setAttackTypeID(attackDAO.findByName(attackName).getAttackTypeID());
            report.setDateReported(LocalDateTime.now());
            report.setDescription(desc);
            report.setStatus("Pending");
            incidentDAO.create(report);

            // 3. Auto-escalate threat level if perpetrator has 3+ victims in 7 days
            int victimCount = incidentDAO.countVictimsLast7Days(perp.getPerpetratorID());
            if (victimCount >= 3 && !"Malicious".equals(perp.getThreatLevel())) {
                String oldLevel = perp.getThreatLevel();
                perp.setThreatLevel("Malicious");
                perpDAO.update(perp);
                threatLogDAO.logChange(perp.getPerpetratorID(), oldLevel, "Malicious", 1); // Admin 1
                showAlert(Alert.AlertType.WARNING, "Perpetrator Escalated",
                        "Identifier: " + identifier + "\nNow marked as MALICIOUS (" + victimCount + " victims in 7 days)");
            }

            // 4. Auto-flag victim if threshold exceeded
            autoFlagVictimIfNeeded(report);

            if (incidentCreatedCallback != null) {
                incidentCreatedCallback.accept(report);
            }

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Incident reported successfully!\nID: " + report.getIncidentID());
            clearForm();

        } catch (Exception e) {
            showError("Submit failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validates the form inputs and updates the UI accordingly.
     * @return true if form is valid, false otherwise
     */
    private boolean validateForm() {
        boolean valid = true;
        String id = identifierField.getText() != null ? identifierField.getText().trim() : "";
        String attack = attackTypeCombo.getValue();
        String desc = descriptionArea.getText() != null ? descriptionArea.getText().trim() : "";

        boolean hasIdentifier = !id.isEmpty();
        boolean hasAttackSelection = attack != null && !attack.isBlank();
        boolean hasDescription = desc.length() >= MIN_DESCRIPTION_LENGTH;

        valid = hasIdentifier && hasAttackSelection && hasDescription;

        if (statusLabel != null) {
            if (valid) {
                statusLabel.setText("");
            } else if (!hasIdentifier) {
                statusLabel.setText("Identifier is required.");
            } else if (!hasAttackSelection) {
                statusLabel.setText("Please choose an attack type.");
            } else if (!hasDescription) {
                statusLabel.setText("Description must be at least " + MIN_DESCRIPTION_LENGTH + " characters.");
            }
        }

        submitButton.setDisable(!valid);
        return valid;
    }

    /**
     * Maps UI identifier type values to database ENUM values.
     *
     * @param uiType The identifier type from the UI
     * @return The corresponding database ENUM value
     */
    private String mapIdentifierTypeToDB(String uiType) {
        if (uiType == null) return null;
        
        switch (uiType) {
            case "Phone Number":
                return "Phone Number";
            case "Email Address":
                return "Email Address";
            case "Social Media Account / Username":
                return "Social Media Account";
            case "Website URL / Domain":
                return "Website URL";
            case "IP Address":
                return "IP Address";
            default:
                return uiType; // Fallback to original value
        }
    }

    /**
     * Updates the prompt text for the identifier field based on the selected identifier type.
     *
     * @param identifierType The selected identifier type
     */
    private void updateIdentifierPromptText(String identifierType) {
        if (identifierField == null) return;
        
        switch (identifierType) {
            case "Phone Number":
                identifierField.setPromptText("+63-912-345-6789");
                break;
            case "Email Address":
                identifierField.setPromptText("example@email.com");
                break;
            case "Social Media Account / Username":
                identifierField.setPromptText("@someone67");
                break;
            case "Website URL / Domain":
                identifierField.setPromptText("fake.website.com");
                break;
            case "IP Address":
                identifierField.setPromptText("162.42.93.207");
                break;
            default:
                identifierField.setPromptText("");
                break;
        }
    }

    /**
     * Clears all form fields and resets to initial state.
     */
    private void clearForm() {
        identifierField.clear();
        associatedNameField.clear();
        descriptionArea.clear();
        if (attackTypeCombo.getItems() != null && !attackTypeCombo.getItems().isEmpty()) {
            attackTypeCombo.getSelectionModel().selectFirst();
        } else {
            attackTypeCombo.setValue(null);
        }
        // Restore prompt text after clearing
        if (identifierTypeCombo.getValue() != null) {
            updateIdentifierPromptText(identifierTypeCombo.getValue());
        }
        statusLabel.setText("");
        validateForm();
    }

    /**
     * Sets a callback to be invoked when an incident is successfully created.
     *
     * @param incidentCreatedCallback The callback function
     */
    public void setIncidentCreatedCallback(Consumer<IncidentReport> incidentCreatedCallback) {
        this.incidentCreatedCallback = incidentCreatedCallback;
    }

    /**
     * Automatically flags a victim account if they exceed the monthly incident threshold.
     *
     * @param report The incident report that was just created
     */
    private void autoFlagVictimIfNeeded(IncidentReport report) {
        if (currentVictim == null) return;
        try {
            int incidentCountThisMonth = incidentDAO.countIncidentsLastMonth(currentVictim.getVictimID());
            if (incidentCountThisMonth > FLAG_THRESHOLD_MONTH && !"Flagged".equals(currentVictim.getAccountStatus())) {
                String oldStatus = currentVictim.getAccountStatus();
                String newStatus = "Flagged";
                victimDAO.updateAccountStatus(currentVictim.getVictimID(), newStatus);
                victimStatusLogDAO.logChange(currentVictim.getVictimID(), oldStatus, newStatus, null);
                currentVictim.setAccountStatus(newStatus);
                showAlert(Alert.AlertType.WARNING, "Account Flagged",
                        "Your account has been flagged for additional support.\n" +
                                "Incidents reported this month: " + incidentCountThisMonth);
            }
        } catch (Exception e) {
            System.err.println("Failed to auto-flag victim: " + e.getMessage());
        }
    }

    /**
     * Shows an alert dialog.
     *
     * @param type    The type of alert
     * @param title   The alert title
     * @param message The alert message
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an error alert.
     *
     * @param msg The error message
     */
    private void showError(String msg) {
        showAlert(Alert.AlertType.ERROR, "Error", msg);
    }

    /**
     * Handles the cancel button click.
     * Closes the current window.
     */
    @FXML private void handleCancel() {
        ((Stage) submitButton.getScene().getWindow()).close();
    }
}