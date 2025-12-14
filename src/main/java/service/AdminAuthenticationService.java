package service;

import dao.AdministratorDAO;
import dao.AdministratorDAOImpl;
import model.Administrator;
import util.SecurityUtils;

import java.sql.SQLException;

/**
 * Service layer responsible for administrator login/authentication.
 *
 * Centralizes all login logic:
 * - Finds admin by email
 * - Securely verifies password using Argon2id via SecurityUtils
 * - Returns the full Administrator object on success
 *
 * All admin login attempts (from FXML controllers, API, etc.) should go through
 * this class. Keeps security logic in one place and makes debugging easier.
 */
public class AdminAuthenticationService {

    // Single DAO instance — lightweight, no need for dependency injection in this small app
    private final AdministratorDAO adminDAO = new AdministratorDAOImpl();

    /**
     * Attempts to authenticate an administrator using email and password.
     *
     * @param email    the administrator's contact email (case-sensitive, trimmed)
     * @param password the plaintext password entered by the user
     * @return the fully populated Administrator object if login succeeds, null otherwise
     * @throws Exception if a database or unexpected error occurs during lookup
     */
    public Administrator authenticate(String email, String password) throws Exception {
        // Clean up input and prevent issues from copy-paste spaces
        email = email != null ? email.trim() : "";
        
        if (email.isEmpty()) {
            System.err.println("Admin authentication failed: Email is empty or null");
            return null;
        }
        
        // Step 1: Look up the admin account by email
        Administrator admin = adminDAO.findByEmail(email);

        if (admin == null) {
            System.err.println("Admin authentication failed: No account found with email: " + email);
            System.err.println("Hint: Run PhishNet-inserts.sql to create admin accounts, or check spelling/casing");
            return null;
        }

        // Optional debug output but extremely helpful when login mysteriously fails
        String hashFromDB = admin.getPasswordHash();
        System.out.println("DEBUG: Attempting login for: " + admin.getName() + " (" + email + ")");
        System.out.println("DEBUG: Stored hash length: " + (hashFromDB != null ? hashFromDB.length() : "null"));
        System.out.println("DEBUG: Hash starts with $argon2id$: " + (hashFromDB != null && hashFromDB.startsWith("$argon2id$")));
        System.out.println("DEBUG: Hash preview: " + (hashFromDB != null && hashFromDB.length() > 50 
                ? hashFromDB.substring(0, 50) + "..." : hashFromDB));

        try {
            // Step 2: Verify the password using our secure Argon2id implementation
            boolean passwordCorrect = SecurityUtils.verifyPassword(password, hashFromDB);

            if (passwordCorrect) {
                System.out.println("Admin login SUCCESSFUL: " + admin.getName() + " (" + email + ")");
                return admin;   // Success = return the admin object for session use
            } else {
                System.err.println("Admin login FAILED: Invalid password for " + email);
                System.err.println("Provided password length: " + (password != null ? password.length() : "null"));
                return null;
            }

        } catch (Exception e) {
            System.err.println("Critical error during password verification for: " + email);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

