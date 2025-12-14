package dao;

import model.Administrator;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for Administrator entity
 * Defines CRUD operations for Administrators
 */
public interface AdministratorDAO {
    /**
     * Find administrator by email
     * @param email Administrator's email
     * @return Administrator object or null if not found
     * @throws SQLException if database error occurs
     */
    Administrator findByEmail(String email) throws SQLException;
    
    /**
     * Find administrator by ID
     * @param adminID Administrator's ID
     * @return Administrator object or null if not found
     * @throws SQLException if database error occurs
     */
    Administrator findById(int adminID) throws SQLException;
    
    /**
     * Get all administrators
     * @return List of all administrators
     * @throws SQLException if database error occurs
     */
    List<Administrator> findAll() throws SQLException;
    
    /**
     * Create a new administrator
     * @param admin Administrator object to create
     * @return true if creation successful
     * @throws SQLException if database error occurs
     */
    boolean create(Administrator admin) throws SQLException;
    
    /**
     * Update an existing administrator
     * @param admin Administrator object with updated data
     * @return true if update successful
     * @throws SQLException if database error occurs
     */
    boolean update(Administrator admin) throws SQLException;
    
    /**
     * Delete an administrator
     * @param adminID Administrator's ID to delete
     * @return true if deletion successful
     * @throws SQLException if database error occurs
     */
    boolean delete(int adminID) throws SQLException;
}

