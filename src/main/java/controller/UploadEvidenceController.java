package controller;

import dao.EvidenceDAO;
import dao.EvidenceDAOImpl;
import dao.IncidentReportDAO;
import dao.IncidentReportDAOImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Evidence;
import model.IncidentReport;
import model.Victim;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the Evidence Upload page (Transaction 2).
 * Handles the UI and logic for uploading evidence files associated with an incident report.
 */
public class UploadEvidenceController {

    // FXML UI components
    @FXML private ComboBox<String> evidenceTypeCombo; // Dropdown for evidence type selection
    @FXML private ComboBox<IncidentReport> incidentCombo; // Dropdown for selecting an incident
    @FXML private TextField filePathField; // Text field displaying selected file path
    @FXML private Button browseButton; // Button to browse for a file
    @FXML private Button uploadButton; // Button to upload selected file
    @FXML private Label statusLabel; // Label to show upload status or messages

    // DAO objects for database operations
    private final EvidenceDAO evidenceDAO = new EvidenceDAOImpl();
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();

    // State variables
    private File selectedFile; // Stores the currently selected file
    private Victim currentVictim; // The victim currently logged in or being processed
    private final DateTimeFormatter incidentFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"); // Format for displaying incident dates

    /**
     * Initializes the controller.
     * Sets up dropdowns, cell factories, and change listeners for validation.
     */
    @FXML
    private void initialize() {
        // Populate evidence type options
        evidenceTypeCombo.setItems(FXCollections.observableArrayList(
                "Screenshot", "Email", "File", "Chat Log"
        ));
        evidenceTypeCombo.setValue("Screenshot"); // Default value

        // Customize how incidents are displayed in the dropdown list
        incidentCombo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(IncidentReport item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(buildIncidentLabel(item));
                }
            }
        });

        // Customize the button display of the ComboBox
        incidentCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(IncidentReport item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select incident");
                } else {
                    setText(buildIncidentLabel(item));
                }
            }
        });

        // Add listeners to validate form whenever inputs change
        filePathField.textProperty().addListener((obs, old, newVal) -> validate());
        evidenceTypeCombo.valueProperty().addListener((obs, old, newVal) -> validate());
        incidentCombo.valueProperty().addListener((obs, old, newVal) -> validate());
    }

    /**
     * Sets the current victim and refreshes the incident list for that victim.
     * @param victim The victim currently being processed
     */
    public void setCurrentVictim(Victim victim) {
        this.currentVictim = victim;
        refreshIncidentList();
    }

    /**
     * Handles the browse button click.
     * Opens a file chooser dialog to select an evidence file.
     */
    @FXML
    private void handleBrowse() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Evidence File");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        selectedFile = chooser.showOpenDialog(null);
        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    /**
     * Handles the upload button click.
     * Copies the selected file to the uploads directory and saves evidence record to database.
     */
    @FXML
    private void handleUpload() {
        if (selectedFile == null) {
            showError("Please select a file");
            return;
        }

        IncidentReport selectedIncident = incidentCombo.getValue();
        if (selectedIncident == null) {
            showError("Please choose an incident");
            return;
        }

        try {
            // Copy to uploads/ folder
            Path uploadDir = Path.of("uploads/evidence");
            Files.createDirectories(uploadDir);
            String fileName = selectedIncident.getIncidentID() + "_" + System.currentTimeMillis() + "_" + selectedFile.getName();
            Path dest = uploadDir.resolve(fileName);
            Files.copy(selectedFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

            // Save to DB
            Evidence ev = new Evidence();
            ev.setIncidentID(selectedIncident.getIncidentID());
            ev.setEvidenceType(evidenceTypeCombo.getValue());
            ev.setFilePath(dest.toString());
            ev.setSubmissionDate(LocalDateTime.now());
            ev.setVerifiedStatus("Pending");

            if (evidenceDAO.upload(ev)) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Evidence uploaded!\nFile: " + fileName + "\nAwaiting admin review.");
                clearForm();
                statusLabel.setText("Pending review for Incident #" + selectedIncident.getIncidentID());
            }
        } catch (Exception e) {
            showError("Upload failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validates the form and enables/disables the upload button accordingly.
     */
    private void validate() {
        uploadButton.setDisable(selectedFile == null || incidentCombo.getValue() == null);
    }

    /**
     * Clears the form fields and resets the selected file.
     */
    private void clearForm() {
        filePathField.clear();
        selectedFile = null;
        statusLabel.setText("");
    }

    /**
     * Refreshes the incident list for the current victim.
     */
    public void refreshIncidentList() {
        if (currentVictim == null) {
            incidentCombo.getItems().clear();
            validate();
            return;
        }
        try {
            List<IncidentReport> incidents = incidentDAO.findByVictimID(currentVictim.getVictimID());
            incidentCombo.setItems(FXCollections.observableArrayList(incidents));
            if (!incidents.isEmpty() && incidentCombo.getSelectionModel().isEmpty()) {
                incidentCombo.getSelectionModel().selectFirst();
            }
            validate();
        } catch (Exception e) {
            showError("Failed to load incidents: " + e.getMessage());
        }
    }

    /**
     * Selects a specific incident in the ComboBox.
     *
     * @param incident The incident to select
     */
    public void selectIncident(IncidentReport incident) {
        if (incident == null) return;
        for (IncidentReport item : incidentCombo.getItems()) {
            if (item.getIncidentID() == incident.getIncidentID()) {
                incidentCombo.getSelectionModel().select(item);
                validate();
                return;
            }
        }
    }

    /**
     * Builds a display label for an incident report.
     *
     * @param incident The incident report
     * @return A formatted string displaying incident ID and date
     */
    private String buildIncidentLabel(IncidentReport incident) {
        String date = incident.getDateReported() != null
                ? incident.getDateReported().format(incidentFormatter)
                : "Unknown date";
        return "Incident #" + incident.getIncidentID() + " • " + date;
    }

    /**
     * Shows an alert dialog.
     *
     * @param type  The type of alert
     * @param title The alert title
     * @param msg   The alert message
     */
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
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
        ((Stage) uploadButton.getScene().getWindow()).close();
    }
}