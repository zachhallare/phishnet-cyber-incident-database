package model;

import java.time.LocalDateTime;

/**
 * Model class representing an Administrator account in the PhishNet system.
 *
 * Maps directly to the Administrators table in the database.
 * Stores sensitive data (password hash) and audit information (date assigned).
 * Used throughout the application for authentication, session management,
 * and admin user management.
 */
public class Administrator {
    
    private int adminID;                    // Primary key
    private String name;                    // Full name of the administrator
    private String role;                    // e.g., "System Admin", "Cybersecurity Staff"
    private String contactEmail;            // Login email + contact address
    private String passwordHash;            // Argon2id hash of the password (never store plaintext!)
    private LocalDateTime dateAssigned;     // When this admin account was created/assigned

    /** Default constructor — required for DAO operations and JSON/FXML binding */
    public Administrator() {}

    /**
     * Convenience constructor for creating a new admin (e.g. during seeding or registration)
     */
    public Administrator(String name, String role, String contactEmail, String passwordHash) {
        this.name = name;
        this.role = role;
        this.contactEmail = contactEmail;
        this.passwordHash = passwordHash;
    }

    // === Getters and Setters ===
    public int getAdminID() {
        return adminID;
    }

    public void setAdminID(int adminID) {
        this.adminID = adminID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

    public LocalDateTime getDateAssigned() {
        return dateAssigned;
    }

    public void setDateAssigned(LocalDateTime dateAssigned) {
        this.dateAssigned = dateAssigned;
    }

    /**
     * Human-readable representation, useful for logging and debugging
     */
    @Override
    public String toString() {
        return String.format("AdminID: %d, Name: %s, Role: %s, Email: %s",
                adminID, name, role, contactEmail);
    }
}

