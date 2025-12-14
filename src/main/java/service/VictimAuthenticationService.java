package service;

import dao.VictimDAO;
import dao.VictimDAOImpl;
import model.Victim;
import util.SecurityUtils;
import util.ValidationUtils;

import java.sql.SQLException;

/**
 * Service layer for victim (public user) authentication and session management.
 *
 * Handles:
 * - Secure registration with Argon2id password hashing
 * - Secure login with proper verification
 * - Simple session tracking (current logged-in victim)
 * - Logout and session state checks
 *
 * This is the central point for all victim-facing authentication logic.
 * Keeps controllers clean and ensures consistent security practices.
 */
public class VictimAuthenticationService {
    
    private final VictimDAO victimDAO;
    private Victim currentVictim;   // Tracks the currently logged-in victim (simple session)

    public VictimAuthenticationService() {
        this.victimDAO = new VictimDAOImpl();
        this.currentVictim = null;
    }

    /**
     * Registers a new victim account (public signup).
     *
     * Performs full input validation, checks for duplicate emails,
     * securely hashes the password using Argon2id, and creates the account.
     *
     * @param name     victim's full name
     * @param email    victim's email (used as username)
     * @param password plaintext password
     * @return true if registration successful, false otherwise
     */
    public boolean register(String name, String email, String password) {
        // Input validation
        if (!ValidationUtils.isNotEmpty(name)) {
            System.err.println("Registration failed: Name is required");
            return false;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            System.err.println("Registration failed: Invalid email format");
            return false;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            System.err.println("Registration failed: Password must be at least 6 characters");
            return false;
        }

        try {
            // Prevent duplicate accounts
            Victim existing = victimDAO.findByEmail(email);
            if (existing != null) {
                System.err.println("Registration failed: Email already registered: " + email);
                return false;
            }

            // Securely hash the password
            String passwordHash = SecurityUtils.hashPassword(password);
            if (passwordHash == null) {
                System.err.println("Failed to hash password. Please try again.");
                return false;
            }

            // Build and save new victim account
            Victim newVictim = new Victim();
            newVictim.setName(name);
            newVictim.setContactEmail(email);
            newVictim.setPasswordHash(passwordHash);
            newVictim.setAccountStatus("Active");

            boolean created = victimDAO.create(newVictim);

            if (created) {
                System.out.println("Registration successful! VictimID: " + newVictim.getVictimID() + ", Email: " + email);
                return true;
            } else {
                System.err.println("Registration failed: victimDAO.create() returned false");
            }

        } catch (SQLException e) {
            System.err.println("Database error during registration: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Unexpected error during registration: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Registration error: " + e.getMessage(), e);
        }

        return false;
    }

    /**
     * Logs in a victim using email and password.
     *
     * @param email    victim's login email
     * @param password plaintext password
     * @return Victim object if login successful, null otherwise
     */
    public Victim login(String email, String password) {
        // Validate input
        if (!ValidationUtils.isValidEmail(email)) {
            System.err.println("Invalid email format");
            return null;
        }

        if (!ValidationUtils.isNotEmpty(password)) {
            System.err.println("Password cannot be empty");
            return null;
        }

        try {
            // Find victim by email
            Victim victim = victimDAO.findByEmail(email);

            if (victim == null) {
                System.err.println("No account found with this email");
                return null;
            }

            // Verify password using secure Argon2id comparison
            try {
                if (SecurityUtils.verifyPassword(password, victim.getPasswordHash())) {
                    currentVictim = victim;
                    System.out.println("Login successful! Welcome, " + victim.getName());
                    return victim;
                } else {
                    System.err.println("Invalid password");
                    return null;
                }
            } catch (Exception e) {
                System.err.println("Password verification error: " + e.getMessage());
                return null;
            }

        } catch (SQLException e) {
            System.err.println("Database error during authentication: " + e.getMessage());
            return null;
        }
    }

    /**
     * Logs out the currently authenticated victim.
     */
    public void logout() {
        if (currentVictim != null) {
            System.out.println("Logging out: " + currentVictim.getName());
            currentVictim = null;
        }
    }

    /**
     * Returns the currently logged-in victim.
     *
     * @return current Victim object, or null if no one is logged in
     */
    public Victim getCurrentVictim() {
        return currentVictim;
    }

    /**
     * Checks whether a victim is currently logged in.
     *
     * @return true if a victim is authenticated, false otherwise
     */
    public boolean isLoggedIn() {
        return currentVictim != null;
    }
}

