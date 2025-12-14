package model;

import java.time.LocalDateTime;

/**
 * Model class representing an audit trail entry for victim status changes.
 *
 * Each record in the VictimStatusLog table is created whenever an administrator
 * updates a victim's AccountStatus (e.g., Active → Under Investigation → Resolved).
 *
 * This provides full traceability and compliance with data privacy audit requirements
 * (e.g., RA 10173 – Data Privacy Act of 2012 in the Philippines).
 *
 * Fields map directly to the VictimStatusLog table in the database.
 */
public class VictimStatusLog {
    
    private int logID;                    // Primary key (auto-increment)
    private int victimID;                 // Foreign key → Victims.VictimID
    private String oldStatus;             // Previous AccountStatus value
    private String newStatus;             // New AccountStatus value after change
    private LocalDateTime changeDate;     // Timestamp when the change occurred
    private int adminID;                  // Foreign key → Administrators.AdminID (who made the change)

    /** Default constructor required for DAO/result set mapping */
    public VictimStatusLog() {}

    // === Getters and Setters ===
    public int getLogID() { return logID; }
    public void setLogID(int logID) { this.logID = logID; }

    public int getVictimID() { return victimID; }
    public void setVictimID(int victimID) { this.victimID = victimID; }

    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public LocalDateTime getChangeDate() { return changeDate; }
    public void setChangeDate(LocalDateTime changeDate) { this.changeDate = changeDate; }

    public int getAdminID() { return adminID; }
    public void setAdminID(int adminID) { this.adminID = adminID; }

    /**
     * Human-readable string representation.
     * Extremely useful for logging, debugging, and displaying audit trails in the UI.
     */
    @Override
    public String toString() {
        return String.format("StatusLog[%d] Victim:%d %s → %s",
                logID, victimID, oldStatus, newStatus);
    }
}
