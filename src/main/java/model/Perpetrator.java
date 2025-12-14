package model;

import java.time.LocalDateTime;

/**
 * Model class representing a known or suspected cybercrime perpetrator/actor.
 *
 * Maps directly to the Perpetrators table. Used to track repeat offenders,
 * scam phone numbers, fraudulent email addresses, and other malicious entities.
 * Central to threat intelligence and pattern recognition in the PhishNet system.
 *
 * The threat level is regularly updated by administrators and logged in ThreatLevelLog.
 */
public class Perpetrator {
    
    private int perpetratorID;                    // Primary key (auto-increment)
    private String identifier;                    // Core identifier: phone, email, URL, wallet address, etc.
    private String identifierType;                // e.g., "Phone Number", "Email Address", "Website", "Social Media"
    private String associatedName;                // Optional alias or real name if known (e.g., "John Doe Scam Group")
    private String threatLevel;                   // Current risk: "UnderReview", "Suspected", "Malicious", "Cleared"
    private LocalDateTime lastIncidentDate;       // Most recent confirmed incident involving this actor

    /** Default constructor — required for DAO and ResultSet mapping */
    public Perpetrator() {}

    /**
     * Full constructor used when creating or updating perpetrator records.
     */
    public Perpetrator(String identifier, String identifierType, String associatedName,
                       String threatLevel, LocalDateTime lastIncidentDate) {
        this.identifier = identifier;
        this.identifierType = identifierType;
        this.associatedName = associatedName;
        this.threatLevel = threatLevel;
        this.lastIncidentDate = lastIncidentDate;
    }

    // === Getters and Setters ===
    public int getPerpetratorID() { return perpetratorID; }
    public void setPerpetratorID(int perpetratorID) { this.perpetratorID = perpetratorID; }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getIdentifierType() { return identifierType; }
    public void setIdentifierType(String identifierType) { this.identifierType = identifierType; }

    public String getAssociatedName() { return associatedName; }
    public void setAssociatedName(String associatedName) { this.associatedName = associatedName; }

    public String getThreatLevel() { return threatLevel; }
    public void setThreatLevel(String threatLevel) { this.threatLevel = threatLevel; }

    public LocalDateTime getLastIncidentDate() { return lastIncidentDate; }
    public void setLastIncidentDate(LocalDateTime lastIncidentDate) { this.lastIncidentDate = lastIncidentDate; }

    /**
     * Clean, readable representation — ideal for admin dashboards, logs, and dropdowns.
     */
    @Override
    public String toString() {
        return String.format("Perpetrator[%d] %s (%s) - %s",
                perpetratorID, identifier, identifierType, threatLevel);
    }
}
