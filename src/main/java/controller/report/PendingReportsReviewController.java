package controller.report;

import dao.EvidenceDAO;
import dao.EvidenceDAOImpl;
import dao.IncidentReportDAO;
import dao.IncidentReportDAOImpl;
import dao.RecycleBinDAO;
import dao.RecycleBinDAOImpl;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Administrator;
import model.Evidence;
import model.IncidentReport;
import model.RecycleBinEvidence;
import model.RecycleBinReport;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pending Reports Review Controller
 * 
 * This controller manages the review and approval workflow for pending incident reports
 * and evidence submissions. It provides administrators with the ability to:
 * - View and validate pending incident reports
 * - Verify or reject submitted evidence
 * - Manage archived items in the recycle bin
 * - Preview evidence files (especially images)
 * 
 * The interface is organized into three main tabs:
 * 1. Pending Reports - Shows unvalidated incident reports awaiting admin approval
 * 2. Pending Evidence - Displays unverified evidence submissions
 * 3. Recycle Bin - Contains rejected/archived reports and evidence that can be restored
 */
public class PendingReportsReviewController {

    // Tab components for organizing the review interface
    @FXML private TabPane reviewTabs;
    @FXML private Tab pendingReportsTab;
    @FXML private Tab pendingEvidenceTab;
    @FXML private Tab recycleBinTab;
    
    // Pending Reports Table components
    @FXML private TableView<IncidentReport> reportsTable;
    @FXML private TableColumn<IncidentReport, Boolean> reportSelectCol;
    @FXML private TableColumn<IncidentReport, Integer> reportIdCol;
    @FXML private TableColumn<IncidentReport, String> reportDateCol;
    @FXML private TableColumn<IncidentReport, String> reportDescriptionCol;
    @FXML private TableColumn<IncidentReport, String> reportStatusCol;
    @FXML private Button validateReportButton;
    @FXML private Button rejectReportButton;
    
    // Map to track which reports have been selected via checkboxes
    private final Map<IncidentReport, Boolean> selectedReports = new HashMap<>();
    
    // Pending Evidence Table components
    @FXML private TableView<Evidence> evidenceTable;
    @FXML private TableColumn<Evidence, Boolean> evidenceSelectCol;
    @FXML private TableColumn<Evidence, Integer> evidenceIdCol;
    @FXML private TableColumn<Evidence, Integer> evidenceIncidentCol;
    @FXML private TableColumn<Evidence, String> evidenceTypeCol;
    @FXML private TableColumn<Evidence, String> evidenceDateCol;
    @FXML private TableColumn<Evidence, String> evidenceStatusCol;
    @FXML private Button verifyEvidenceButton;
    @FXML private Button rejectEvidenceButton;
    
    // Labels showing counts of items in each section
    @FXML private Label reportsCountLabel;
    @FXML private Label evidenceCountLabel;
    @FXML private Label recycleReportsCountLabel;
    @FXML private Label recycleEvidenceCountLabel;
    
    // Evidence preview components
    @FXML private ImageView evidencePreviewImage;
    @FXML private Label previewPlaceholderLabel;
    @FXML private Label previewMetadataLabel;
    @FXML private Label previewFilePathLabel;
    @FXML private Hyperlink openFileLocationLink;

    // Recycle Bin - Reports Table components
    @FXML private TableView<RecycleBinReport> recycleReportsTable;
    @FXML private TableColumn<RecycleBinReport, Boolean> recycleReportSelectCol;
    @FXML private TableColumn<RecycleBinReport, Integer> recycleReportIdCol;
    @FXML private TableColumn<RecycleBinReport, String> recycleReportReasonCol;
    @FXML private TableColumn<RecycleBinReport, String> recycleReportArchivedCol;
    @FXML private Button restoreReportButton;

    // Recycle Bin - Evidence Table components
    @FXML private TableView<RecycleBinEvidence> recycleEvidenceTable;
    @FXML private TableColumn<RecycleBinEvidence, Boolean> recycleEvidenceSelectCol;
    @FXML private TableColumn<RecycleBinEvidence, Integer> recycleEvidenceIdCol;
    @FXML private TableColumn<RecycleBinEvidence, Integer> recycleEvidenceIncidentCol;
    @FXML private TableColumn<RecycleBinEvidence, String> recycleEvidenceTypeCol;
    @FXML private TableColumn<RecycleBinEvidence, String> recycleEvidenceArchivedCol;
    @FXML private Button restoreEvidenceButton;

    // DAO instances for database operations
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();
    private final EvidenceDAO evidenceDAO = new EvidenceDAOImpl();
    private final RecycleBinDAO recycleBinDAO = new RecycleBinDAOImpl();
    
    // Currently logged-in administrator
    private Administrator currentAdmin;
    
    // Date formatter for displaying dates consistently
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // Maps to track selected items via checkboxes
    private final Map<Evidence, Boolean> selectedEvidence = new HashMap<>();
    private final Map<RecycleBinReport, Boolean> recycleReportSelections = new HashMap<>();
    private final Map<RecycleBinEvidence, Boolean> recycleEvidenceSelections = new HashMap<>();
    
    // Constants for rejection reasons
    private static final String REPORT_REJECTION_REASON = "Rejected from Pending Reports Review";
    private static final String EVIDENCE_REJECTION_REASON = "Rejected from Pending Evidence Review";
    
    // Supported image file extensions for preview
    private static final String[] IMAGE_EXTENSIONS = {"png", "jpg", "jpeg", "gif", "bmp", "webp"};
    
    // Currently previewed evidence item
    private Evidence previewedEvidence;

    @FXML
    private void initialize() {
        System.out.println("PendingReportsReviewController: Initializing...");
        setupReportsTable();
        setupEvidenceTable();
        setupRecycleBinTables();
        showEvidencePreview(null);
        
        // Setup button handlers (already set in FXML, but can be set here too)
        
        // Refresh when tab is selected - load data regardless of admin (pending items are visible to all admins)
        if (pendingReportsTab != null) {
            pendingReportsTab.setOnSelectionChanged(e -> {
                if (pendingReportsTab.isSelected()) {
                    System.out.println("PendingReportsReviewController: Pending Reports tab selected, refreshing...");
                    refreshPendingReports();
                }
            });
        }
        
        if (pendingEvidenceTab != null) {
            pendingEvidenceTab.setOnSelectionChanged(e -> {
                if (pendingEvidenceTab.isSelected()) {
                    System.out.println("PendingReportsReviewController: Pending Evidence tab selected, refreshing...");
                    refreshPendingEvidence();
                }
            });
        }
        
        if (recycleBinTab != null) {
            recycleBinTab.setOnSelectionChanged(e -> {
                if (recycleBinTab.isSelected()) {
                    System.out.println("PendingReportsReviewController: Recycle Bin tab selected, refreshing...");
                    refreshRecycleBin();
                }
            });
        }
        
        // Load data initially when first tab is selected
        if (reviewTabs != null && reviewTabs.getTabs().size() > 0) {
            Tab selectedTab = reviewTabs.getSelectionModel().getSelectedItem();
            if (selectedTab == pendingReportsTab) {
                System.out.println("PendingReportsReviewController: Initial load - Pending Reports tab");
                refreshPendingReports();
            } else if (selectedTab == pendingEvidenceTab) {
                System.out.println("PendingReportsReviewController: Initial load - Pending Evidence tab");
                refreshPendingEvidence();
            } else if (selectedTab == recycleBinTab) {
                System.out.println("PendingReportsReviewController: Initial load - Recycle Bin tab");
                refreshRecycleBin();
            } else if (selectedTab == null && pendingReportsTab != null) {
                // If no tab selected, select and load the first one (Pending Reports)
                reviewTabs.getSelectionModel().select(pendingReportsTab);
            }
        }
        
        System.out.println("PendingReportsReviewController: Initialization complete");
    }

    private void setupReportsTable() {
        // Checkbox column for selection
        reportSelectCol.setCellValueFactory(cellData -> {
            IncidentReport report = cellData.getValue();
            Boolean selected = selectedReports.getOrDefault(report, false);
            return new SimpleBooleanProperty(selected);
        });
        
        reportSelectCol.setCellFactory(column -> new javafx.scene.control.TableCell<IncidentReport, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            private IncidentReport currentReport;
            
            {
                checkBox.setOnAction(e -> {
                    if (currentReport != null) {
                        selectedReports.put(currentReport, checkBox.isSelected());
                        updateButtonStates();
                    }
                });
            }
            
            @Override
            protected void updateItem(Boolean selected, boolean empty) {
                super.updateItem(selected, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    currentReport = null;
                } else {
                    currentReport = getTableRow().getItem();
                    boolean isSelected = selectedReports.getOrDefault(currentReport, false);
                    checkBox.setSelected(isSelected);
                    setGraphic(checkBox);
                }
            }
        });
        
        reportIdCol.setCellValueFactory(new PropertyValueFactory<>("incidentID"));
        reportDateCol.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDateReported();
            String dateStr = date != null ? date.format(dateFormatter) : "";
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });
        reportDescriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        reportStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        reportSelectCol.setPrefWidth(60);
        reportIdCol.setPrefWidth(80);
        reportDateCol.setPrefWidth(150);
        reportDescriptionCol.setPrefWidth(350);
        reportStatusCol.setPrefWidth(100);
        
        // Enable/disable buttons based on checkbox selection
        updateButtonStates();
    }
    
    private void updateButtonStates() {
        long selectedCount = selectedReports.values().stream().filter(Boolean::booleanValue).count();
        boolean hasSelection = selectedCount > 0;
        validateReportButton.setDisable(!hasSelection);
        rejectReportButton.setDisable(!hasSelection);
    }

    private void updateEvidenceButtonStates() {
        long selectedCount = selectedEvidence.values().stream().filter(Boolean::booleanValue).count();
        boolean hasSelection = selectedCount > 0;
        if (verifyEvidenceButton != null) {
            verifyEvidenceButton.setDisable(!hasSelection);
        }
        if (rejectEvidenceButton != null) {
            rejectEvidenceButton.setDisable(!hasSelection);
        }
    }

    private void updateRecycleReportButtonState() {
        boolean hasSelection = recycleReportSelections.values().stream().anyMatch(Boolean::booleanValue);
        if (restoreReportButton != null) {
            restoreReportButton.setDisable(!hasSelection);
        }
    }

    private void updateRecycleEvidenceButtonState() {
        boolean hasSelection = recycleEvidenceSelections.values().stream().anyMatch(Boolean::booleanValue);
        if (restoreEvidenceButton != null) {
            restoreEvidenceButton.setDisable(!hasSelection);
        }
    }

    private void setupEvidenceTable() {
        evidenceSelectCol.setCellValueFactory(cellData -> {
            Evidence evidence = cellData.getValue();
            Boolean selected = selectedEvidence.getOrDefault(evidence, false);
            return new SimpleBooleanProperty(selected);
        });

        evidenceSelectCol.setCellFactory(column -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();
            private Evidence currentEvidence;

            {
                checkBox.setOnAction(e -> {
                    if (currentEvidence != null) {
                        selectedEvidence.put(currentEvidence, checkBox.isSelected());
                        updateEvidenceButtonStates();
                    }
                });
            }

            @Override
            protected void updateItem(Boolean selected, boolean empty) {
                super.updateItem(selected, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    currentEvidence = null;
                } else {
                    currentEvidence = getTableRow().getItem();
                    boolean isSelected = selectedEvidence.getOrDefault(currentEvidence, false);
                    checkBox.setSelected(isSelected);
                    setGraphic(checkBox);
                }
            }
        });

        evidenceIdCol.setCellValueFactory(new PropertyValueFactory<>("evidenceID"));
        evidenceIncidentCol.setCellValueFactory(new PropertyValueFactory<>("incidentID"));
        evidenceTypeCol.setCellValueFactory(new PropertyValueFactory<>("evidenceType"));
        evidenceDateCol.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getSubmissionDate();
            String dateStr = date != null ? date.format(dateFormatter) : "";
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });
        evidenceStatusCol.setCellValueFactory(new PropertyValueFactory<>("verifiedStatus"));
        
        evidenceSelectCol.setPrefWidth(60);
        evidenceIdCol.setPrefWidth(80);
        evidenceIncidentCol.setPrefWidth(100);
        evidenceTypeCol.setPrefWidth(150);
        evidenceDateCol.setPrefWidth(150);
        evidenceStatusCol.setPrefWidth(120);

        if (evidenceTable != null) {
            evidenceTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> showEvidencePreview(newSelection));
        }

        updateEvidenceButtonStates();
    }

    private void setupRecycleBinTables() {
        if (recycleReportSelectCol != null) {
            recycleReportSelectCol.setCellValueFactory(cellData -> {
                RecycleBinReport report = cellData.getValue();
                Boolean selected = recycleReportSelections.getOrDefault(report, false);
                return new SimpleBooleanProperty(selected);
            });

            recycleReportSelectCol.setCellFactory(column -> new TableCell<>() {
                private final CheckBox checkBox = new CheckBox();
                private RecycleBinReport currentReport;

                {
                    checkBox.setOnAction(e -> {
                        if (currentReport != null) {
                            recycleReportSelections.put(currentReport, checkBox.isSelected());
                            updateRecycleReportButtonState();
                        }
                    });
                }

                @Override
                protected void updateItem(Boolean selected, boolean empty) {
                    super.updateItem(selected, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                        currentReport = null;
                    } else {
                        currentReport = getTableRow().getItem();
                        boolean isSelected = recycleReportSelections.getOrDefault(currentReport, false);
                        checkBox.setSelected(isSelected);
                        setGraphic(checkBox);
                    }
                }
            });

            recycleReportIdCol.setCellValueFactory(new PropertyValueFactory<>("incidentID"));
            recycleReportReasonCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getArchiveReason() != null
                            ? cellData.getValue().getArchiveReason() : "N/A"));
            recycleReportArchivedCol.setCellValueFactory(cellData -> {
                LocalDateTime archivedAt = cellData.getValue().getArchivedAt();
                String dateStr = archivedAt != null ? archivedAt.format(dateFormatter) : "";
                return new SimpleStringProperty(dateStr);
            });
        }

        if (recycleEvidenceSelectCol != null) {
            recycleEvidenceSelectCol.setCellValueFactory(cellData -> {
                RecycleBinEvidence evidence = cellData.getValue();
                Boolean selected = recycleEvidenceSelections.getOrDefault(evidence, false);
                return new SimpleBooleanProperty(selected);
            });

            recycleEvidenceSelectCol.setCellFactory(column -> new TableCell<>() {
                private final CheckBox checkBox = new CheckBox();
                private RecycleBinEvidence currentEvidence;

                {
                    checkBox.setOnAction(e -> {
                        if (currentEvidence != null) {
                            recycleEvidenceSelections.put(currentEvidence, checkBox.isSelected());
                            updateRecycleEvidenceButtonState();
                        }
                    });
                }

                @Override
                protected void updateItem(Boolean selected, boolean empty) {
                    super.updateItem(selected, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                        currentEvidence = null;
                    } else {
                        currentEvidence = getTableRow().getItem();
                        boolean isSelected = recycleEvidenceSelections.getOrDefault(currentEvidence, false);
                        checkBox.setSelected(isSelected);
                        setGraphic(checkBox);
                    }
                }
            });

            recycleEvidenceIdCol.setCellValueFactory(new PropertyValueFactory<>("evidenceID"));
            recycleEvidenceIncidentCol.setCellValueFactory(new PropertyValueFactory<>("incidentID"));
            recycleEvidenceTypeCol.setCellValueFactory(new PropertyValueFactory<>("evidenceType"));
            recycleEvidenceArchivedCol.setCellValueFactory(cellData -> {
                LocalDateTime archivedAt = cellData.getValue().getArchivedAt();
                String dateStr = archivedAt != null ? archivedAt.format(dateFormatter) : "";
                return new SimpleStringProperty(dateStr);
            });
        }

        updateRecycleReportButtonState();
        updateRecycleEvidenceButtonState();
    }

    public void setCurrentAdmin(Administrator admin) {
        this.currentAdmin = admin;
        // Load data when admin is set - refresh the currently visible tab
        if (reviewTabs != null) {
            Tab selectedTab = reviewTabs.getSelectionModel().getSelectedItem();
            if (selectedTab == pendingReportsTab) {
                refreshPendingReports();
            } else if (selectedTab == pendingEvidenceTab) {
                refreshPendingEvidence();
            } else if (selectedTab == recycleBinTab) {
                refreshRecycleBin();
            }
        }
    }

    public void refreshPendingReports() {
        try {
            System.out.println("PendingReportsReviewController: Loading pending reports...");
            List<IncidentReport> pending = incidentDAO.findPending();
            System.out.println("PendingReportsReviewController: Found " + pending.size() + " pending reports");
            
            // Clear selections for reports that no longer exist
            selectedReports.keySet().removeIf(report -> !pending.contains(report));
            
            ObservableList<IncidentReport> reports = FXCollections.observableArrayList(pending);
            if (reportsTable != null) {
                reportsTable.setItems(reports);
            }
            if (reportsCountLabel != null) {
                reportsCountLabel.setText("Pending Reports: " + pending.size());
            }
            updateButtonStates();
            System.out.println("PendingReportsReviewController: Reports loaded successfully");
        } catch (Exception e) {
            System.err.println("PendingReportsReviewController: Failed to load pending reports: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load pending reports: " + e.getMessage());
        }
    }

    public void refreshPendingEvidence() {
        try {
            System.out.println("PendingReportsReviewController: Loading pending evidence...");
            List<Evidence> pending = evidenceDAO.findPending();
            System.out.println("PendingReportsReviewController: Found " + pending.size() + " pending evidence");

            selectedEvidence.clear();

            ObservableList<Evidence> evidence = FXCollections.observableArrayList(pending);
            if (evidenceTable != null) {
                evidenceTable.setItems(evidence);
                if (!evidence.isEmpty()) {
                    evidenceTable.getSelectionModel().selectFirst();
                } else {
                    showEvidencePreview(null);
                }
            } else {
                showEvidencePreview(null);
            }
            if (evidenceCountLabel != null) {
                evidenceCountLabel.setText("Pending Evidence: " + pending.size());
            }
            updateEvidenceButtonStates();
            System.out.println("PendingReportsReviewController: Evidence loaded successfully");
        } catch (Exception e) {
            System.err.println("PendingReportsReviewController: Failed to load pending evidence: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load pending evidence: " + e.getMessage());
        }
    }

    public void refreshRecycleBin() {
        refreshRecycleReports();
        refreshRecycleEvidence();
    }

    private void refreshRecycleReports() {
        try {
            List<RecycleBinReport> archived = recycleBinDAO.findAllReports();
            recycleReportSelections.keySet().removeIf(report -> !archived.contains(report));

            ObservableList<RecycleBinReport> data = FXCollections.observableArrayList(archived);
            if (recycleReportsTable != null) {
                recycleReportsTable.setItems(data);
            }
            if (recycleReportsCountLabel != null) {
                recycleReportsCountLabel.setText("Archived Reports: " + archived.size());
            }
            updateRecycleReportButtonState();
            System.out.println("PendingReportsReviewController: Recycle bin reports loaded.");
        } catch (Exception e) {
            System.err.println("PendingReportsReviewController: Failed to load recycle bin reports: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load recycled reports: " + e.getMessage());
        }
    }

    private void refreshRecycleEvidence() {
        try {
            List<RecycleBinEvidence> archived = recycleBinDAO.findAllEvidence();
            recycleEvidenceSelections.keySet().removeIf(item -> !archived.contains(item));

            ObservableList<RecycleBinEvidence> data = FXCollections.observableArrayList(archived);
            if (recycleEvidenceTable != null) {
                recycleEvidenceTable.setItems(data);
            }
            if (recycleEvidenceCountLabel != null) {
                recycleEvidenceCountLabel.setText("Archived Evidence: " + archived.size());
            }
            updateRecycleEvidenceButtonState();
            System.out.println("PendingReportsReviewController: Recycle bin evidence loaded.");
        } catch (Exception e) {
            System.err.println("PendingReportsReviewController: Failed to load recycle bin evidence: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load recycled evidence: " + e.getMessage());
        }
    }

    @FXML
    private void handleValidateReport() {
        if (currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Admin", "Admin not set. Please log out and log back in.");
            return;
        }

        // Get all selected reports
        List<IncidentReport> toValidate = getSelectedIncidentReports();

        if (toValidate.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one report to validate.");
            return;
        }

        try {
            int successCount = 0;
            int failCount = 0;
            
            for (IncidentReport report : toValidate) {
                try {
                    if (incidentDAO.updateStatus(report.getIncidentID(), "Validated", currentAdmin.getAdminID())) {
                        successCount++;
                        selectedReports.remove(report); // Remove from selection after validation
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    System.err.println("Error validating report #" + report.getIncidentID() + ": " + e.getMessage());
                    failCount++;
                }
            }

            String message = String.format("Validated %d report(s).", successCount);
            if (failCount > 0) {
                message += String.format(" %d report(s) failed to validate.", failCount);
            }
            
            showAlert(Alert.AlertType.INFORMATION, "Validation Complete", message);
            refreshPendingReports();
        } catch (Exception e) {
            showError("Error validating reports: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRejectReport() {
        if (currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Admin", "Admin not set. Please log out and log back in.");
            return;
        }

        List<IncidentReport> selected = getSelectedIncidentReports();
        if (selected.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one report to reject.");
            return;
        }

        for (IncidentReport report : selected) {
            try {
                // Try to archive to recycle bin for audit purposes (non-critical)
                try {
                    recycleBinDAO.archiveIncidentReport(report, currentAdmin.getAdminID(), REPORT_REJECTION_REASON);
                } catch (Exception archiveEx) {
                    System.err.println("Error archiving report #" + report.getIncidentID() + ": " + archiveEx.getMessage());
                    archiveEx.printStackTrace();
                }

                // Always update status to "Rejected" so victim can see it (critical operation)
                try {
                    if (incidentDAO.updateStatus(report.getIncidentID(), "Rejected", currentAdmin.getAdminID())) {
                        selectedReports.remove(report);
                    }
                } catch (Exception statusEx) {
                    System.err.println("Error updating status for report #" + report.getIncidentID() + ": " + statusEx.getMessage());
                    statusEx.printStackTrace();
                    // Check if it's a schema issue
                    if (statusEx.getMessage() != null && statusEx.getMessage().contains("Data truncated")) {
                        System.err.println("NOTE: Database schema may need to be updated. Run: ALTER TABLE IncidentReports MODIFY COLUMN Status ENUM('Pending', 'Validated', 'Rejected') DEFAULT 'Pending';");
                    }
                }
            } catch (Exception ex) {
                System.err.println("Unexpected error rejecting report #" + report.getIncidentID() + ": " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        String message = "Reports rejected successfully.";

        showAlert(Alert.AlertType.INFORMATION, "Rejection Complete", message);
        refreshPendingReports();
        refreshPendingEvidence(); // cascade delete may remove related evidence
        refreshRecycleReports();
    }

    @FXML
    private void handleVerifyEvidence() {
        if (currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Admin", "Admin not set. Please log out and log back in.");
            return;
        }

        List<Evidence> toVerify = getSelectedEvidenceItems();
        if (toVerify.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one piece of evidence to verify.");
            return;
        }

        try {
            int successCount = 0;
            int failCount = 0;

            for (Evidence evidence : toVerify) {
                try {
                    if (evidenceDAO.verify(evidence.getEvidenceID(), "Verified", currentAdmin.getAdminID())) {
                        successCount++;
                        selectedEvidence.remove(evidence);
                    } else {
                        failCount++;
                    }
                } catch (Exception ex) {
                    System.err.println("Error verifying evidence #" + evidence.getEvidenceID() + ": " + ex.getMessage());
                    failCount++;
                }
            }

            String message = String.format("Verified %d evidence item(s).", successCount);
            if (failCount > 0) {
                message += String.format(" %d item(s) failed to verify.", failCount);
            }

            showAlert(Alert.AlertType.INFORMATION, "Verification Complete", message);
            refreshPendingEvidence();
        } catch (Exception e) {
            showError("Error verifying evidence: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRejectEvidence() {
        if (currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Admin", "Admin not set. Please log out and log back in.");
            return;
        }

        List<Evidence> toReject = getSelectedEvidenceItems();
        if (toReject.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one piece of evidence to reject.");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (Evidence evidence : toReject) {
            try {
                boolean archived = recycleBinDAO.archiveEvidence(evidence, currentAdmin.getAdminID(), EVIDENCE_REJECTION_REASON);
                if (!archived) {
                    failCount++;
                    continue;
                }

                if (evidenceDAO.delete(evidence.getEvidenceID())) {
                    selectedEvidence.remove(evidence);
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception ex) {
                System.err.println("Error rejecting evidence #" + evidence.getEvidenceID() + ": " + ex.getMessage());
                failCount++;
            }
        }

        String message = String.format("Rejected %d evidence item(s).", successCount);
        if (failCount > 0) {
            message += String.format(" %d item(s) failed to archive.", failCount);
        }

        showAlert(Alert.AlertType.INFORMATION, "Rejection Complete", message);
        refreshPendingEvidence();
        refreshRecycleEvidence();
    }

    @FXML
    private void handleRestoreReports() {
        if (currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Admin", "Admin not set. Please log out and log back in.");
            return;
        }

        List<RecycleBinReport> toRestore = getSelectedRecycleReports();
        if (toRestore.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one archived report to restore.");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (RecycleBinReport report : toRestore) {
            try {
                if (recycleBinDAO.restoreIncidentReport(report)) {
                    recycleReportSelections.remove(report);
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception ex) {
                System.err.println("Error restoring report #" + report.getIncidentID() + ": " + ex.getMessage());
                failCount++;
            }
        }

        String message = String.format("Restored %d report(s).", successCount);
        if (failCount > 0) {
            message += String.format(" %d report(s) failed to restore.", failCount);
        }

        showAlert(Alert.AlertType.INFORMATION, "Restore Complete", message);
        refreshRecycleReports();
        refreshPendingReports();
    }

    @FXML
    private void handleRestoreEvidence() {
        if (currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Admin", "Admin not set. Please log out and log back in.");
            return;
        }

        List<RecycleBinEvidence> toRestore = getSelectedRecycleEvidence();
        if (toRestore.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one archived evidence item to restore.");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (RecycleBinEvidence evidence : toRestore) {
            try {
                if (recycleBinDAO.restoreEvidence(evidence)) {
                    recycleEvidenceSelections.remove(evidence);
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception ex) {
                System.err.println("Error restoring evidence #" + evidence.getEvidenceID() + ": " + ex.getMessage());
                failCount++;
            }
        }

        String message = String.format("Restored %d evidence item(s).", successCount);
        if (failCount > 0) {
            message += String.format(" %d item(s) failed to restore.", failCount);
        }

        showAlert(Alert.AlertType.INFORMATION, "Restore Complete", message);
        refreshRecycleEvidence();
        refreshPendingEvidence();
    }

    @FXML
    private void handleOpenFileLocation() {
        if (previewedEvidence == null) {
            showAlert(Alert.AlertType.INFORMATION, "No Selection", "Select an evidence item to open its file location.");
            return;
        }

        String filePath = previewedEvidence.getFilePath();
        if (filePath == null || filePath.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Missing File", "This evidence record does not have a saved file path.");
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            showAlert(Alert.AlertType.ERROR, "File Not Found",
                    "The referenced file could not be found:\n" + file.getAbsolutePath());
            return;
        }

        if (!Desktop.isDesktopSupported()) {
            showAlert(Alert.AlertType.WARNING, "Not Supported",
                    "Opening files is not supported on this device. Please access the file manually:\n" + file.getAbsolutePath());
            return;
        }

        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Unable to Open File",
                    "An error occurred while opening the file:\n" + e.getMessage());
        }
    }

    private List<IncidentReport> getSelectedIncidentReports() {
        return selectedReports.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .toList();
    }

    private List<Evidence> getSelectedEvidenceItems() {
        return selectedEvidence.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .toList();
    }

    private List<RecycleBinReport> getSelectedRecycleReports() {
        return recycleReportSelections.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .toList();
    }

    private List<RecycleBinEvidence> getSelectedRecycleEvidence() {
        return recycleEvidenceSelections.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .toList();
    }

    private void showEvidencePreview(Evidence evidence) {
        previewedEvidence = evidence;

        if (previewMetadataLabel == null || previewPlaceholderLabel == null) {
            return;
        }

        if (evidence == null) {
            previewMetadataLabel.setText("Select an evidence item to preview.");
            if (previewFilePathLabel != null) {
                previewFilePathLabel.setText("");
            }
            setPreviewImageVisible(false);
            togglePlaceholder(true, "Select an evidence item to preview.");
            toggleOpenFileLink(false);
            return;
        }

        String submitted = evidence.getSubmissionDate() != null
                ? evidence.getSubmissionDate().format(dateFormatter)
                : "N/A";
        String status = safeValue(evidence.getVerifiedStatus());
        String type = safeValue(evidence.getEvidenceType());

        previewMetadataLabel.setText(String.format(
                "Evidence #%d | Incident #%d%nType: %s%nStatus: %s%nSubmitted: %s",
                evidence.getEvidenceID(),
                evidence.getIncidentID(),
                type,
                status,
                submitted));

        String filePath = evidence.getFilePath();
        if (filePath == null || filePath.isBlank()) {
            if (previewFilePathLabel != null) {
                previewFilePathLabel.setText("File: (no file path provided)");
            }
            setPreviewImageVisible(false);
            togglePlaceholder(true, "No file path is associated with this evidence.");
            toggleOpenFileLink(false);
            return;
        }

        File file = new File(filePath);
        boolean exists = file.exists();
        if (previewFilePathLabel != null) {
            String labelText = exists ? "File: " + file.getAbsolutePath()
                    : "File: " + file.getAbsolutePath() + " (not found)";
            previewFilePathLabel.setText(labelText);
        }

        if (!exists) {
            setPreviewImageVisible(false);
            togglePlaceholder(true, "The referenced file could not be found.");
            toggleOpenFileLink(false);
            return;
        }

        boolean isImage = isImageFile(filePath);
        if (isImage) {
            try {
                Image image = new Image(file.toURI().toString());
                if (!image.isError() && image.getWidth() > 1 && image.getHeight() > 1) {
                    if (evidencePreviewImage != null) {
                        evidencePreviewImage.setImage(image);
                    }
                    setPreviewImageVisible(true);
                    togglePlaceholder(false, null);
                    toggleOpenFileLink(shouldOfferFullView(image));
                    return;
                }
            } catch (Exception ex) {
                System.err.println("PendingReportsReviewController: Failed to display image preview for evidence "
                        + evidence.getEvidenceID() + ": " + ex.getMessage());
            }
            setPreviewImageVisible(false);
            togglePlaceholder(true, "Unable to render the image preview. Use the link below to open the original file.");
            toggleOpenFileLink(true);
            return;
        }

        setPreviewImageVisible(false);
        togglePlaceholder(true, "Preview available for image uploads only. Use the link below to open the file.");
        toggleOpenFileLink(true);
    }

    private void setPreviewImageVisible(boolean visible) {
        if (evidencePreviewImage == null) {
            return;
        }
        evidencePreviewImage.setVisible(visible);
        evidencePreviewImage.setManaged(visible);
        if (!visible) {
            evidencePreviewImage.setImage(null);
        }
    }

    private void togglePlaceholder(boolean show, String message) {
        if (previewPlaceholderLabel == null) {
            return;
        }
        previewPlaceholderLabel.setVisible(show);
        previewPlaceholderLabel.setManaged(show);
        if (message != null && !message.isBlank()) {
            previewPlaceholderLabel.setText(message);
        } else if (!show && previewPlaceholderLabel.getText().isBlank()) {
            previewPlaceholderLabel.setText("Select an evidence item to preview.");
        }
    }

    private void toggleOpenFileLink(boolean show) {
        if (openFileLocationLink == null) {
            return;
        }
        openFileLocationLink.setVisible(show);
        openFileLocationLink.setManaged(show);
    }

    private boolean isImageFile(String filePath) {
        if (filePath == null) {
            return false;
        }
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filePath.length() - 1) {
            return false;
        }
        String extension = filePath.substring(dotIndex + 1).toLowerCase();
        for (String allowed : IMAGE_EXTENSIONS) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldOfferFullView(Image image) {
        if (image == null || evidencePreviewImage == null) {
            return false;
        }
        double fitWidth = evidencePreviewImage.getFitWidth();
        double fitHeight = evidencePreviewImage.getFitHeight();
        if (fitWidth <= 0 || fitHeight <= 0) {
            return false;
        }
        return image.getWidth() > fitWidth || image.getHeight() > fitHeight;
    }

    private String safeValue(String value) {
        return (value == null || value.isBlank()) ? "N/A" : value;
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

