package model;

import java.time.LocalDateTime;

/**
 * Model class representing a reported cybersecurity incident in the PhishNet system.
 *
 * This is the core entity that connects:
 * - A Victim (who reported the incident)
 * - A Perpetrator (the attacker/actor)
 * - An AttackType (type of cyberattack)
 *
 * Maps directly to the IncidentReports table and serves as the central record
 * for all victim-submitted reports. Administrators review, validate, assign,
 * and track these incidents throughout their lifecycle.
 *
 * Status values typically include: "Pending", "Validated", "Under Investigation",
 * "Resolved", "Rejected", etc.
 */
public class IncidentReport {
    
    private int incidentID;                    // Primary key (auto-increment)
    private int victimID;                      // Foreign key → Victims.VictimID
    private int perpetratorID;                 // Foreign key → Perpetrators.PerpetratorID
    private int attackTypeID;                  // Foreign key → AttackTypes.AttackTypeID
    private Integer adminID;                   // Nullable → Assigned administrator (if any)
    private LocalDateTime dateReported;        // When the victim submitted the report
    private String description;                // Full details provided by the victim
    private String status;                     // Current workflow status of the incident

    /** Default constructor — required for DAO and ResultSet mapping */
    public IncidentReport() {}

    // === Getters and Setters ===
    public int getIncidentID() { return incidentID; }
    public void setIncidentID(int incidentID) { this.incidentID = incidentID; }

    public int getVictimID() { return victimID; }
    public void setVictimID(int victimID) { this.victimID = victimID; }

    public int getPerpetratorID() { return perpetratorID; }
    public void setPerpetratorID(int perpetratorID) { this.perpetratorID = perpetratorID; }

    public int getAttackTypeID() { return attackTypeID; }
    public void setAttackTypeID(int attackTypeID) { this.attackTypeID = attackTypeID; }

    public Integer getAdminID() { return adminID; }
    public void setAdminID(Integer adminID) { this.adminID = adminID; }

    public LocalDateTime getDateReported() { return dateReported; }
    public void setDateReported(LocalDateTime dateReported) { this.dateReported = dateReported; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /**
     * Comprehensive, human-readable string representation.
     * Extremely useful in admin dashboards, logs, and incident lists.
     */
    @Override
    public String toString() {
        return String.format("Incident[%d] Victim:%d → Perp:%d | %s",
                incidentID, victimID, perpetratorID, status);
    }
}
