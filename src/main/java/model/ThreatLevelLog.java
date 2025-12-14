package model;

import java.time.LocalDateTime;

/**
 * Model class representing an audit log entry for changes to a perpetrator's threat level.
 *
 * Each record is automatically created whenever an administrator updates the ThreatLevel
 * of a perpetrator in the system. This ensures full traceability of risk assessment changes
 * and supports compliance with data privacy and cybersecurity incident reporting requirements.
 *
 * Maps directly to the ThreatLevelLog table in the database.
 */
public class ThreatLevelLog {
    
    private int logID;                    // Primary key (auto-increment)
    private int perpetratorID;            // Foreign key → Perpetrators.PerpetratorID
    private String oldThreatLevel;        // Previous threat level (e.g., "Low", "Medium", "High")
    private String newThreatLevel;        // Updated threat level after change
    private LocalDateTime changeDate;     // Timestamp of when the change was made
    private int adminID;                  // Foreign key → Administrators.AdminID (who made the change)

    /** Default constructor — required for DAO and ResultSet mapping */
    public ThreatLevelLog() {}

    // === Getters and Setters ===
    public int getLogID() { return logID; }
    public void setLogID(int logID) { this.logID = logID; }

    public int getPerpetratorID() { return perpetratorID; }
    public void setPerpetratorID(int perpetratorID) { this.perpetratorID = perpetratorID; }

    public String getOldThreatLevel() { return oldThreatLevel; }
    public void setOldThreatLevel(String oldThreatLevel) { this.oldThreatLevel = oldThreatLevel; }

    public String getNewThreatLevel() { return newThreatLevel; }
    public void setNewThreatLevel(String newThreatLevel) { this.newThreatLevel = newThreatLevel; }

    public LocalDateTime getChangeDate() { return changeDate; }
    public void setChangeDate(LocalDateTime changeDate) { this.changeDate = changeDate; }

    public int getAdminID() { return adminID; }
    public void setAdminID(int adminID) { this.adminID = adminID; }

    /**
     * Human-readable string representation for logging, debugging, and audit trail display.
     */
    @Override
    public String toString() {
        return String.format("ThreatLog[%d] Perp:%d %s → %s by Admin:%d",
                logID, perpetratorID, oldThreatLevel, newThreatLevel, adminID);
    }
}