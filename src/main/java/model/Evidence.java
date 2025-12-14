package model;

import java.time.LocalDateTime;

/**
 * Model class representing a piece of evidence uploaded by a victim as part of an incident report.
 *
 * Maps directly to the Evidence table. Each evidence file (screenshot, email, document, etc.)
 * is linked to a specific IncidentReport and goes through a verification workflow
 * by administrators.
 *
 * Critical for maintaining chain of custody and supporting incident validation.
 */
public class Evidence {
    
    private int evidenceID;                    // Primary key (auto-increment)
    private int incidentID;                    // Foreign key → IncidentReports.IncidentID
    private String evidenceType;               // e.g., "Screenshot", "Email Header", "PDF Document", "Chat Log"
    private String filePath;                   // Server-side path to the uploaded file
    private LocalDateTime submissionDate;      // When the victim uploaded the evidence
    private String verifiedStatus;             // "Pending", "Verified", "Rejected"
    private Integer adminID;                   // Nullable → Administrator who reviewed/verified (if any)

    /** Default constructor — required for DAO and ResultSet mapping */
    public Evidence() {}

    // === Getters and Setters ===
    public int getEvidenceID() { return evidenceID; }
    public void setEvidenceID(int evidenceID) { this.evidenceID = evidenceID; }

    public int getIncidentID() { return incidentID; }
    public void setIncidentID(int incidentID) { this.incidentID = incidentID; }

    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDateTime submissionDate) { this.submissionDate = submissionDate; }

    public String getVerifiedStatus() { return verifiedStatus; }
    public void setVerifiedStatus(String verifiedStatus) { this.verifiedStatus = verifiedStatus; }

    public Integer getAdminID() { return adminID; }
    public void setAdminID(Integer adminID) { this.adminID = adminID; }

    /**
     * Human-readable representation — ideal for admin evidence review panels and logs.
     */
    @Override
    public String toString() {
        return String.format("Evidence[%d] for Incident %d | %s",
                evidenceID, incidentID, verifiedStatus);
    }
}
