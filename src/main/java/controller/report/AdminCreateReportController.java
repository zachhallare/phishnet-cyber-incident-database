package controller.report;

import controller.ReportIncidentController;
import controller.UploadEvidenceController;
import controller.ViewMyReportsController;
import dao.VictimDAO;
import dao.VictimDAOImpl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import model.Administrator;
import model.Victim;
import util.SecurityUtils;

/**
 * Admin Manual Report Controller
 * Allows admins to manually create reports using the same interface as victims
 * Uses a special admin victim account for report creation
 */
public class AdminCreateReportController {

    @FXML private TabPane tabPane;
    @FXML private ReportIncidentController reportIncidentTabController;
    @FXML private UploadEvidenceController uploadEvidenceTabController;
    @FXML private ViewMyReportsController viewMyReportsTabController;

    private Administrator currentAdmin;
    private Victim adminVictimAccount;
    private final VictimDAO victimDAO = new VictimDAOImpl();

    @FXML
    private void initialize() {
        System.out.println("AdminCreateReportController: Initializing...");
        
        // Try to find controllers after scene is ready
        Platform.runLater(() -> {
            setupAdminVictimAccount();
        });
    }

    /**
     * Set the currently logged-in administrator
     */
    public void setCurrentAdmin(Administrator admin) {
        this.currentAdmin = admin;
        System.out.println("AdminCreateReportController: Admin set to " + 
            (admin != null ? admin.getName() : "null"));
        
        Platform.runLater(() -> {
            setupAdminVictimAccount();
        });
    }

    /**
     * Setup or find the admin victim account for creating reports
     * Admins need a victim account to create reports since reports are linked to victims
     */
    private void setupAdminVictimAccount() {
        if (currentAdmin == null) {
            return;
        }

        try {
            // Try to find existing admin victim account by email
            String adminEmail = currentAdmin.getContactEmail();
            adminVictimAccount = victimDAO.findByEmail(adminEmail);

            if (adminVictimAccount == null) {
                // Create a new admin victim account if it doesn't exist
                adminVictimAccount = new Victim();
                adminVictimAccount.setName("Admin: " + currentAdmin.getName());
                adminVictimAccount.setContactEmail(adminEmail);
                // Generate a secure password hash (admins won't use this account for login)
                // Using a random secure password since this account is only for report creation
                String randomPassword = "AdminReportAccount" + System.currentTimeMillis();
                adminVictimAccount.setPasswordHash(SecurityUtils.hashPassword(randomPassword));
                adminVictimAccount.setAccountStatus("Active");
                
                victimDAO.create(adminVictimAccount);
                System.out.println("AdminCreateReportController: Created admin victim account with ID: " + 
                    adminVictimAccount.getVictimID());
            } else {
                System.out.println("AdminCreateReportController: Found existing admin victim account with ID: " + 
                    adminVictimAccount.getVictimID());
            }

            // Setup controllers with the admin victim account
            if (reportIncidentTabController != null) {
                reportIncidentTabController.setCurrentVictim(adminVictimAccount);
                reportIncidentTabController.setIncidentCreatedCallback(incident -> {
                    if (viewMyReportsTabController != null) {
                        viewMyReportsTabController.refreshReports();
                    }
                    if (uploadEvidenceTabController != null) {
                        uploadEvidenceTabController.refreshIncidentList();
                        uploadEvidenceTabController.selectIncident(incident);
                    }
                });
            }

            if (viewMyReportsTabController != null) {
                viewMyReportsTabController.setCurrentVictim(adminVictimAccount);
                viewMyReportsTabController.refreshReports();
            }

            if (uploadEvidenceTabController != null) {
                uploadEvidenceTabController.setCurrentVictim(adminVictimAccount);
                uploadEvidenceTabController.refreshIncidentList();
            }

        } catch (Exception e) {
            System.err.println("AdminCreateReportController: Failed to setup admin victim account: " + 
                e.getMessage());
            e.printStackTrace();
        }
    }
}

