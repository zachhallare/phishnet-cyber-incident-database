package controller;

import dao.AttackTypeDAO;
import dao.AttackTypeDAOImpl;
import dao.EvidenceDAO;
import dao.EvidenceDAOImpl;
import dao.IncidentReportDAO;
import dao.IncidentReportDAOImpl;
import dao.PerpetratorDAO;
import dao.PerpetratorDAOImpl;
import dao.RecycleBinDAO;
import dao.RecycleBinDAOImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.AttackType;
import model.Evidence;
import model.IncidentReport;
import model.Perpetrator;
import model.RecycleBinEvidence;
import model.Victim;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the "View My Reports" page.
 * Handles displaying all incident reports associated with the currently logged-in victim.
 */
public class ViewMyReportsController {

    @FXML private TableView<IncidentReport> reportsTable; // Table to display incident reports
    @FXML private TableColumn<IncidentReport, Integer> idCol; // Column for incident ID
    @FXML private TableColumn<IncidentReport, String> dateCol; // Column for date reported
    @FXML private TableColumn<IncidentReport, String> attackTypeCol; // Column for attack type
    @FXML private TableColumn<IncidentReport, String> perpetratorCol; // Column for perpetrator identifier
    @FXML private TableColumn<IncidentReport, String> statusCol; // Column for report status
    @FXML private TableColumn<IncidentReport, String> evidenceStatusCol; // Column for evidence status summary
    @FXML private TableColumn<IncidentReport, String> descriptionCol; // Column for report description
    @FXML private TableColumn<IncidentReport, String> reviewedByCol; // Column for administrator who reviewed

    // Currently logged-in victim
    private Victim currentVictim;

    // DAO objects for accessing database
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();
    private final AttackTypeDAO attackDAO = new AttackTypeDAOImpl();
    private final PerpetratorDAO perpDAO = new PerpetratorDAOImpl();
    private final dao.AdministratorDAO adminDAO = new dao.AdministratorDAOImpl();
    private final EvidenceDAO evidenceDAO = new EvidenceDAOImpl();
    private final RecycleBinDAO recycleBinDAO = new RecycleBinDAOImpl();

    /**
     * Initializes the controller and configures the table columns.
     * This method is automatically called by JavaFX after FXML is loaded.
     */
    @FXML
    private void initialize() {
        // Map incident ID to table column
        idCol.setCellValueFactory(new PropertyValueFactory<>("incidentID"));

        // Map date reported to table column and format it as "yyyy-MM-dd HH:mm"
        dateCol.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDateReported();
            String dateStr = date != null ? date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });

        // Map attack type ID to attack type name
        attackTypeCol.setCellValueFactory(cellData -> {
            try {
                int attackTypeID = cellData.getValue().getAttackTypeID();
                AttackType type = attackDAO.findById(attackTypeID);
                String name = type != null ? type.getAttackName() : "Unknown";
                return new javafx.beans.property.SimpleStringProperty(name);
            } catch (Exception e) {
                // Return "Error" if DAO lookup fails
                return new javafx.beans.property.SimpleStringProperty("Error");
            }
        });

        // Map perpetrator ID to identifier string
        perpetratorCol.setCellValueFactory(cellData -> {
            try {
                int perpID = cellData.getValue().getPerpetratorID();
                Perpetrator perp = perpDAO.findById(perpID);
                String identifier = perp != null ? perp.getIdentifier() : "Unknown";
                return new javafx.beans.property.SimpleStringProperty(identifier);
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Error");
            }
        });

        // Map status with default to "Pending"
        statusCol.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getStatus();
            // Make status more visible with styling indication
            String displayStatus = status != null ? status : "Pending";
            return new javafx.beans.property.SimpleStringProperty(displayStatus);
        });

        // Custom cell factory for status to add colors
        statusCol.setCellFactory(column -> new javafx.scene.control.TableCell<IncidentReport, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if ("Validated".equals(status)) {
                        setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                    } else if ("Pending".equals(status)) {
                        setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
                    } else if ("Rejected".equals(status)) {
                        setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Evidence status column - shows summary of all evidence for the incident
        evidenceStatusCol.setCellValueFactory(cellData -> {
            IncidentReport report = cellData.getValue();
            String evidenceStatus = getEvidenceStatusSummary(report.getIncidentID());
            return new javafx.beans.property.SimpleStringProperty(evidenceStatus);
        });

        // Custom cell factory for evidence status to add colors
        evidenceStatusCol.setCellFactory(column -> new TableCell<IncidentReport, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if (status.contains("Verified") && !status.contains("Pending") && !status.contains("Rejected")) {
                        setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                    } else if (status.contains("Rejected")) {
                        setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                    } else if (status.contains("Pending")) {
                        setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
                    } else if ("No Evidence".equals(status)) {
                        setStyle("-fx-text-fill: #6c757d;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Map description directly
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Show who reviewed the report (admin name or "Not reviewed")
        reviewedByCol.setCellValueFactory(cellData -> {
            IncidentReport report = cellData.getValue();
            Integer adminID = report.getAdminID();
            if (adminID != null && adminID > 0) {
                try {
                    model.Administrator admin = adminDAO.findById(adminID);
                    if (admin != null) {
                        return new javafx.beans.property.SimpleStringProperty(admin.getName());
                    }
                } catch (Exception e) {
                    // If can't find admin, show ID
                    return new javafx.beans.property.SimpleStringProperty("Admin #" + adminID);
                }
            }
            return new javafx.beans.property.SimpleStringProperty("Not reviewed");
        });

        // Set column widths
        idCol.setPrefWidth(80);
        dateCol.setPrefWidth(150);
        attackTypeCol.setPrefWidth(150);
        perpetratorCol.setPrefWidth(200);
        statusCol.setPrefWidth(120);
        evidenceStatusCol.setPrefWidth(150);
        descriptionCol.setPrefWidth(250);
        reviewedByCol.setPrefWidth(150);
    }

    /**
     * Get evidence status summary for a report.
     * Returns a string like "No Evidence", "Pending", "2 Verified, 1 Pending", etc.
     * Includes both active evidence and rejected evidence from the recycle bin.
     *
     * @param incidentID The incident ID to check
     * @return A summary string of evidence statuses
     */
    private String getEvidenceStatusSummary(int incidentID) {
        try {
            // Get active evidence from EvidenceUpload table
            List<Evidence> evidenceList = evidenceDAO.findByIncidentID(incidentID);
            
            // Get rejected evidence from RecycleBinEvidence table
            List<RecycleBinEvidence> rejectedEvidenceList = recycleBinDAO.findEvidenceByIncidentID(incidentID);
            
            // If both lists are empty, return "No Evidence"
            if ((evidenceList == null || evidenceList.isEmpty()) && 
                (rejectedEvidenceList == null || rejectedEvidenceList.isEmpty())) {
                return "No Evidence";
            }
            
            int pendingCount = 0;
            int verifiedCount = 0;
            int rejectedCount = 0;
            
            // Count active evidence statuses
            if (evidenceList != null) {
                for (Evidence evidence : evidenceList) {
                    String status = evidence.getVerifiedStatus();
                    if (status == null) {
                        pendingCount++;
                    } else {
                        switch (status) {
                            case "Pending":
                                pendingCount++;
                                break;
                            case "Verified":
                                verifiedCount++;
                                break;
                            case "Rejected":
                                rejectedCount++;
                                break;
                        }
                    }
                }
            }
            
            // Count rejected evidence from recycle bin
            // All evidence in recycle bin is considered rejected (it was archived when rejected)
            if (rejectedEvidenceList != null) {
                rejectedCount += rejectedEvidenceList.size();
            }
            
            // Build summary string
            StringBuilder summary = new StringBuilder();
            if (verifiedCount > 0) {
                summary.append(verifiedCount).append(" Verified");
            }
            if (pendingCount > 0) {
                if (summary.length() > 0) summary.append(", ");
                summary.append(pendingCount).append(" Pending");
            }
            if (rejectedCount > 0) {
                if (summary.length() > 0) summary.append(", ");
                summary.append(rejectedCount).append(" Rejected");
            }
            
            return summary.length() > 0 ? summary.toString() : "Pending";
            
        } catch (Exception e) {
            System.err.println("Error getting evidence status for incident " + incidentID + ": " + e.getMessage());
            e.printStackTrace();
            return "Error";
        }
    }

    /**
     * Sets the current victim and refreshes the reports table.
     *
     * @param victim The victim to set as current
     */
    public void setCurrentVictim(Victim victim) {
        this.currentVictim = victim;
        refreshReports();
    }

    /**
     * Refreshes the reports table by loading all reports for the current victim.
     */
    public void refreshReports() {
        if (currentVictim == null) return;

        try {
            List<IncidentReport> reports = incidentDAO.findByVictimID(currentVictim.getVictimID());
            ObservableList<IncidentReport> observableReports = FXCollections.observableArrayList(reports);
            reportsTable.setItems(observableReports);
        } catch (Exception e) {
            showError("Failed to load reports: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Shows an error alert dialog.
     *
     * @param msg The error message to display
     */
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

