package model;

import java.time.LocalDateTime;

/**
 * Model class representing a registered victim (public user) in the PhishNet system.
 *
 * Maps directly to the Victims table in the database.
 * Stores personal information, secure password hash (Argon2id), account status,
 * and creation timestamp. Used for victim registration, login, and incident reporting.
 */
public class Victim {
    
    private int victimID;                    // Primary key (auto-increment)
    private String name;                     // Full name of the victim
    private String contactEmail;             // Login email and primary contact method
    private String passwordHash;             // Secure Argon2id hash — never store plaintext!
    private String accountStatus;            // e.g., "Active", "Under Investigation", "Resolved"
    private LocalDateTime dateCreated;       // When the account was registered

    /** Default constructor — required for DAO operations and object mapping */
    public Victim() {}

    /**
     * Convenience constructor used during victim registration.
     * Automatically sets account status to "Active".
     */
    public Victim(String name, String contactEmail, String passwordHash) {
        this.name = name;
        this.contactEmail = contactEmail;
        this.passwordHash = passwordHash;
        this.accountStatus = "Active";
    }

    // === Getters and Setters ===
    public int getVictimID() {
        return victimID;
    }

    public void setVictimID(int victimID) {
        this.victimID = victimID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * Human-readable string representation.
     * Useful for logging, debugging, and displaying victim info in admin panels.
     */
    @Override
    public String toString() {
        return String.format("VictimID: %d, Name: %s, Email: %s, Status: %s",
                victimID, name, contactEmail, accountStatus);
    }
}

