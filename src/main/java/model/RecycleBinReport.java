package model;

import java.time.LocalDateTime;

/**
 * Model class representing a deleted/archived incident report in the "Recycle Bin".
 *
 * This entity maps to the RecycleBinReports table and stores a full snapshot
 * of an incident report when it is soft-deleted (e.g., rejected, marked as duplicate,
 * or cleaned up). It preserves all original data along with audit information
 * about who archived it and why.
 *
 * Used in the admin "Recycle Bin" feature to allow review, restoration,
 * or "deletion" of previously removed reports.
 */
public class RecycleBinReport {
    
    private int binID;                    // Primary key of the recycle bin entry
    private int incidentID;               // Original IncidentID from the main reports table
    private Integer victimID;             // Nullable — victim who reported the incident
    private Integer perpetratorID;        // Nullable — known/suspected attacker
    private Integer attackTypeID;         // Nullable — type of cyberattack
    private LocalDateTime dateReported;   // When the incident was originally reported
    private String description;           // Full incident description
    private String originalStatus;        // Status before archiving (e.g., "Pending", "Under Investigation")
    private Integer adminAssignedID;      // Admin originally assigned to handle the case
    private int rejectedByAdminID;        // Admin who moved the report to recycle bin
    private String archiveReason;         // Reason for archiving (e.g., "False Positive", "Duplicate")
    private LocalDateTime archivedAt;     // Timestamp when moved to recycle bin

    // === Getters and Setters ===

    public int getBinID() {
        return binID;
    }

    public void setBinID(int binID) {
        this.binID = binID;
    }

    public int getIncidentID() {
        return incidentID;
    }

    public void setIncidentID(int incidentID) {
        this.incidentID = incidentID;
    }

    public Integer getVictimID() {
        return victimID;
    }

    public void setVictimID(Integer victimID) {
        this.victimID = victimID;
    }

    public Integer getPerpetratorID() {
        return perpetratorID;
    }

    public void setPerpetratorID(Integer perpetratorID) {
        this.perpetratorID = perpetratorID;
    }

    public Integer getAttackTypeID() {
        return attackTypeID;
    }

    public void setAttackTypeID(Integer attackTypeID) {
        this.attackTypeID = attackTypeID;
    }

    public LocalDateTime getDateReported() {
        return dateReported;
    }

    public void setDateReported(LocalDateTime dateReported) {
        this.dateReported = dateReported;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOriginalStatus() {
        return originalStatus;
    }

    public void setOriginalStatus(String originalStatus) {
        this.originalStatus = originalStatus;
    }

    public Integer getAdminAssignedID() {
        return adminAssignedID;
    }

    public void setAdminAssignedID(Integer adminAssignedID) {
        this.adminAssignedID = adminAssignedID;
    }

    public int getRejectedByAdminID() {
        return rejectedByAdminID;
    }

    public void setRejectedByAdminID(int rejectedByAdminID) {
        this.rejectedByAdminID = rejectedByAdminID;
    }

    public String getArchiveReason() {
        return archiveReason;
    }

    public void setArchiveReason(String archiveReason) {
        this.archiveReason = archiveReason;
    }

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(LocalDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }
}

