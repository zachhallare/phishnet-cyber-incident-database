package model;

/**
 * Model class representing a type of cybersecurity attack.
 *
 * Maps to the AttackTypes table in the database.
 * Used to categorize incidents reported by victims (e.g., Phishing, Ransomware).
 * Includes severity level for prioritization in the admin dashboard.
 */
public class AttackType {
    
    private int attackTypeID;               // Primary key (auto-increment)
    private String attackName;              // e.g., "Phishing", "Malware"
    private String description;             // Detailed explanation of the attack type
    private String severityLevel;           // "Low", "Medium", "High" — for triage

    /** Default constructor required for DAO/result set mapping */
    public AttackType() {}

    /**
     * Convenience constructor for creating new attack types.
     */
    public AttackType(String attackName, String description, String severityLevel) {
        this.attackName = attackName;
        this.description = description;
        this.severityLevel = severityLevel;
    }

    // === Getters and Setters ===
    public int getAttackTypeID() { return attackTypeID; }
    public void setAttackTypeID(int attackTypeID) { this.attackTypeID = attackTypeID; }

    public String getAttackName() { return attackName; }
    public void setAttackName(String attackName) { this.attackName = attackName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(String severityLevel) { this.severityLevel = severityLevel; }

    /**
     * Simple string representation for display in lists/dropdowns.
     */
    @Override
    public String toString() {
        return String.format("%s [%s]", attackName, severityLevel);
    }
}