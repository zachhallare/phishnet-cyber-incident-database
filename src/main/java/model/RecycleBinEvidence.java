package model;

import java.time.LocalDateTime;

/**
 * Model class representing a soft-deleted (archived) piece of evidence in the Recycle Bin.
 *
 * This entity maps to the RecycleBinEvidence table and stores a complete snapshot
 * of an evidence file when it is removed from an active incident (e.g., marked as
 * irrelevant, duplicate, or part of a rejected report). Preserves the original file path,
 * submission details, and full audit trail of who archived it and why.
 *
 * Essential for:
 * - Admin "Recycle Bin" recovery functionality
 * - Maintaining evidence integrity and chain of custody
 * - Supporting audit requirements and incident review
 */
public class RecycleBinEvidence {
    
    private int binID;                    // Primary key of the recycle bin entry
    private int evidenceID;               // Original EvidenceID from the main Evidence table
    private Integer incidentID;           // Associated incident (nullable if evidence was standalone)
    private String evidenceType;          // e.g., "Screenshot", "Email Header", "PDF Document"
    private String filePath;              // Full server path to the archived file
    private LocalDateTime submissionDate;// When the evidence was originally uploaded
    private String originalStatus;        // Status before archiving (e.g., "Pending Review")
    private Integer adminAssignedID;      // Admin originally assigned to review this evidence
    private int rejectedByAdminID;        // Admin who moved this evidence to recycle bin
    private String archiveReason;         // Reason for removal (e.g., "Irrelevant", "Duplicate")
    private LocalDateTime archivedAt;     // Timestamp when evidence was archived

    // === Getters and Setters ===

    public int getBinID() {
        return binID;
    }

    public void setBinID(int binID) {
        this.binID = binID;
    }

    public int getEvidenceID() {
        return evidenceID;
    }

    public void setEvidenceID(int evidenceID) {
        this.evidenceID = evidenceID;
    }

    public Integer getIncidentID() {
        return incidentID;
    }

    public void setIncidentID(Integer incidentID) {
        this.incidentID = incidentID;
    }

    public String getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(String evidenceType) {
        this.evidenceType = evidenceType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
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

