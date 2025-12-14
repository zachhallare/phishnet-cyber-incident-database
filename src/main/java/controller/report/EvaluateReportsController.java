package controller.report;

import dao.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Administrator;
import model.IncidentReport;
import model.Perpetrator;
import model.AttackType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Evaluate Reports Controller
 * Allows admins to evaluate reports, manage perpetrators, set threat levels, and add notes.
 * 
 * This controller provides administrators with tools to:
 * - View all incident reports in a table
 * - Select a report for detailed evaluation
 * - View and update perpetrator information associated with the report
 * - Set or change threat levels for perpetrators
 * - Add evaluation notes for each report
 * - Save evaluation results
 */
public class EvaluateReportsController {

    // Reports table and columns
    @FXML private TableView<IncidentReport> reportsTable;
    @FXML private TableColumn<IncidentReport, Integer> reportIdCol;
    @FXML private TableColumn<IncidentReport, String> reportDateCol;
    @FXML private TableColumn<IncidentReport, String> reportDescriptionCol;
    @FXML private TableColumn<IncidentReport, String> reportStatusCol;
    @FXML private TableColumn<IncidentReport, String> reportAttackTypeCol;
    
    // Evaluation form components
    @FXML private Label selectedReportLabel;
    @FXML private TextField perpetratorIdentifierField;
    @FXML private ComboBox<String> identifierTypeCombo;
    @FXML private TextField associatedNameField;
    @FXML private ComboBox<String> threatLevelCombo;
    @FXML private TextArea evaluationNotesArea;
    @FXML private Button saveEvaluationButton;
    @FXML private Button updatePerpetratorButton;
    @FXML private Label reportsCountLabel;
    
    // DAO instances for database operations
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();
    private final PerpetratorDAO perpDAO = new PerpetratorDAOImpl();
    private final AttackTypeDAO attackDAO = new AttackTypeDAOImpl();
    private final ThreatLevelLogDAO threatLogDAO = new ThreatLevelLogDAOImpl();
    private final ReportEvaluationNotesDAO notesDAO = new ReportEvaluationNotesDAOImpl();
    
    // Currently logged-in administrator
    private Administrator currentAdmin;
    
    // Currently selected report and associated perpetrator
    private IncidentReport selectedReport;
    private Perpetrator currentPerpetrator;
    
    // Date formatter for displaying dates consistently
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    @FXML
    private void initialize() {
        System.out.println("EvaluateReportsController: Initializing...");
        setupReportsTable();
        setupComboBoxes();
        setupEventHandlers();
        refreshReports();
    }
    
    private void setupReportsTable() {
        reportIdCol.setCellValueFactory(new PropertyValueFactory<>("incidentID"));
        reportDateCol.setCellValueFactory(cellData -> {
            IncidentReport report = cellData.getValue();
            if (report.getDateReported() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    report.getDateReported().format(dateFormatter)
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        reportDescriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        reportStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        reportAttackTypeCol.setCellValueFactory(cellData -> {
            IncidentReport report = cellData.getValue();
            try {
                AttackType attackType = attackDAO.findById(report.getAttackTypeID());
                return new javafx.beans.property.SimpleStringProperty(
                    attackType != null ? attackType.getAttackName() : "Unknown"
                );
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Unknown");
            }
        });
        
        // Set column widths
        reportIdCol.setPrefWidth(80);
        reportDateCol.setPrefWidth(150);
        reportDescriptionCol.setPrefWidth(400);
        reportStatusCol.setPrefWidth(100);
        reportAttackTypeCol.setPrefWidth(150);
    }
    
    private void setupComboBoxes() {
        // Identifier types
        identifierTypeCombo.setItems(FXCollections.observableArrayList(
            "Phone Number",
            "Email Address",
            "Social Media Account",
            "Website URL",
            "IP Address"
        ));
        
        // Threat levels
        threatLevelCombo.setItems(FXCollections.observableArrayList(
            "UnderReview",
            "Suspected",
            "Malicious",
            "Cleared"
        ));
    }
    
    private void setupEventHandlers() {
        // Handle report selection
        reportsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldReport, newReport) -> {
            if (newReport != null) {
                loadReportForEvaluation(newReport);
            } else {
                clearEvaluationForm();
            }
        });
    }
    
    private void loadReportForEvaluation(IncidentReport report) {
        try {
            selectedReport = report;
            selectedReportLabel.setText("Selected Report ID: " + report.getIncidentID());
            
            // Load perpetrator information
            currentPerpetrator = perpDAO.findById(report.getPerpetratorID());
            if (currentPerpetrator != null) {
                perpetratorIdentifierField.setText(currentPerpetrator.getIdentifier());
                identifierTypeCombo.setValue(currentPerpetrator.getIdentifierType());
                associatedNameField.setText(
                    currentPerpetrator.getAssociatedName() != null ? 
                    currentPerpetrator.getAssociatedName() : ""
                );
                threatLevelCombo.setValue(currentPerpetrator.getThreatLevel());
                updatePerpetratorButton.setDisable(false);
            } else {
                // Perpetrator not found
                clearPerpetratorFields();
                updatePerpetratorButton.setDisable(true);
            }
            
            // Load evaluation notes if they exist
            String notes = notesDAO.getNotesForReport(report.getIncidentID());
            evaluationNotesArea.setText(notes != null ? notes : "");
            
        } catch (Exception e) {
            showError("Failed to load report: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void clearEvaluationForm() {
        selectedReport = null;
        selectedReportLabel.setText("No report selected");
        clearPerpetratorFields();
        evaluationNotesArea.clear();
        updatePerpetratorButton.setDisable(true);
    }
    
    private void clearPerpetratorFields() {
        perpetratorIdentifierField.clear();
        identifierTypeCombo.setValue(null);
        associatedNameField.clear();
        threatLevelCombo.setValue(null);
    }
    
    @FXML
    private void handleUpdatePerpetrator() {
        if (selectedReport == null || currentPerpetrator == null) {
            showError("Please select a report with an existing perpetrator.");
            return;
        }
        
        if (!validatePerpetratorFields()) {
            return;
        }
        
        try {
            String oldThreatLevel = currentPerpetrator.getThreatLevel();
            
            currentPerpetrator.setIdentifier(perpetratorIdentifierField.getText().trim());
            currentPerpetrator.setIdentifierType(identifierTypeCombo.getValue());
            currentPerpetrator.setAssociatedName(
                associatedNameField.getText().trim().isEmpty() ? 
                null : associatedNameField.getText().trim()
            );
            
            String newThreatLevel = threatLevelCombo.getValue() != null ? 
                threatLevelCombo.getValue() : "UnderReview";
            currentPerpetrator.setThreatLevel(newThreatLevel);
            currentPerpetrator.setLastIncidentDate(LocalDateTime.now());
            
            perpDAO.update(currentPerpetrator);
            
            // Log threat level change if it changed
            if (!oldThreatLevel.equals(newThreatLevel) && currentAdmin != null) {
                threatLogDAO.logChange(
                    currentPerpetrator.getPerpetratorID(),
                    oldThreatLevel,
                    newThreatLevel,
                    currentAdmin.getAdminID()
                );
            }
            
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                "Perpetrator updated successfully!");
            refreshReports();
            
        } catch (Exception e) {
            showError("Failed to update perpetrator: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleSaveEvaluation() {
        if (selectedReport == null) {
            showError("Please select a report first.");
            return;
        }
        
        try {
            String notes = evaluationNotesArea.getText().trim();
            notesDAO.saveNotesForReport(selectedReport.getIncidentID(), notes, 
                currentAdmin != null ? currentAdmin.getAdminID() : null);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                "Evaluation notes saved successfully!");
            
        } catch (Exception e) {
            showError("Failed to save evaluation notes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean validatePerpetratorFields() {
        if (perpetratorIdentifierField.getText().trim().isEmpty()) {
            showError("Perpetrator identifier is required.");
            return false;
        }
        if (identifierTypeCombo.getValue() == null) {
            showError("Please select an identifier type.");
            return false;
        }
        return true;
    }
    
    public void refreshReports() {
        try {
            List<IncidentReport> allReports = incidentDAO.findAll();
            ObservableList<IncidentReport> reportList = FXCollections.observableArrayList(allReports);
            reportsTable.setItems(reportList);
            
            reportsCountLabel.setText("Total Reports: " + allReports.size());
            
        } catch (Exception e) {
            showError("Failed to load reports: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void setCurrentAdmin(Administrator admin) {
        this.currentAdmin = admin;
        System.out.println("EvaluateReportsController: Admin set to " + 
            (admin != null ? admin.getName() : "null"));
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Error", message);
    }
}

