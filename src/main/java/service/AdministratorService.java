package service;

import dao.AdministratorDAO;
import dao.AdministratorDAOImpl;
import model.Administrator;
import util.ValidationUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * Service layer for managing Administrator accounts.
 *
 * Handles all business logic related to administrators:
 * - Retrieval (by ID, email, or all)
 * - Updates (with full input validation)
 * - Deletion
 *
 * Acts as a clean boundary between controllers (UI) and the DAO layer.
 * Centralizes validation, logging, and error handling.
 */
public class AdministratorService {
    
    // Single DAO instance — no need for DI in this small desktop app
    private final AdministratorDAO administratorDAO;
    
    public AdministratorService() {
        this.administratorDAO = new AdministratorDAOImpl();
    }
    
    /**
     * Retrieves an administrator by their numeric ID.
     *
     * @param adminID the unique administrator ID
     * @return Administrator object if found, null if not found or invalid ID
     */
    public Administrator getAdministratorById(int adminID) {
        if (!ValidationUtils.isPositive(adminID)) {
            System.err.println("AdministratorService: Invalid admin ID provided: " + adminID);
            return null;
        }
        
        try {
            return administratorDAO.findById(adminID);
        } catch (SQLException e) {
            System.err.println("Database error while fetching admin ID " + adminID + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Retrieves an administrator by their contact email.
     *
     * @param email the administrator's email address
     * @return Administrator object if found, null if invalid email or not found
     */
    public Administrator getAdministratorByEmail(String email) {
        if (!ValidationUtils.isValidEmail(email)) {
            System.err.println("AdministratorService: Invalid or malformed email: " + email);
            return null;
        }
        
        try {
            return administratorDAO.findByEmail(email.trim());
        } catch (SQLException e) {
            System.err.println("Database error while searching for admin email " + email + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Returns all administrators in the system.
     *
     * @return List of all Administrator objects, or null if database error occurs
     */
    public List<Administrator> getAllAdministrators() {
        try {
            return administratorDAO.findAll();
        } catch (SQLException e) {
            System.err.println("Failed to retrieve all administrators: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Updates an existing administrator's information.
     * Performs full validation before attempting database update.
     *
     * @param admin the Administrator object with updated values
     * @return true if update was successful, false otherwise
     */
    public boolean updateAdministrator(Administrator admin) {
        if (admin == null) {
            System.err.println("AdministratorService: Cannot update null Administrator object");
            return false;
        }
        
        if (!ValidationUtils.isPositive(admin.getAdminID())) {
            System.err.println("AdministratorService: Invalid administrator ID");
            return false;
        }
        
        if (!ValidationUtils.isNotEmpty(admin.getName())) {
            System.err.println("AdministratorService: Name cannot be empty");
            return false;
        }
        
        if (!ValidationUtils.isValidEmail(admin.getContactEmail())) {
            System.err.println("AdministratorService: Invalid email format");
            return false;
        }
        
        try {
            boolean updated = administratorDAO.update(admin);
            if (updated) {
                System.out.println("Administrator updated successfully: " + admin.getName());
                return true;
            } else {
                System.err.println("AdministratorService: Update returned false (no rows affected)");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Database error while updating administrator: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Deletes an administrator from the system.
     * Use with caution — this is a permanent deletion.
     *
     * @param adminID the ID of the administrator to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteAdministrator(int adminID) {
        if (!ValidationUtils.isPositive(adminID)) {
            System.err.println("AdministratorService: Invalid administrator ID");
            return false;
        }
        
        try {
            boolean deleted = administratorDAO.delete(adminID);
            if (deleted) {
                System.out.println("Administrator deleted successfully: ID " + adminID);
                return true;
            } else {
                System.err.println("AdministratorService: Delete returned false (admin ID " + adminID + " not found)");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Database error while deleting administrator: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

