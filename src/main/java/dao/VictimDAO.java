package dao;

import model.Victim;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for Victim entity
 * Defines CRUD operations for Victims
 */
public interface VictimDAO {
    /**
     * Find victim by email
     * @param email Victim's email
     * @return Victim object or null if not found
     * @throws SQLException if database error occurs
     */
    Victim findByEmail(String email) throws SQLException;

    /**
     * Find victim by ID
     * @param victimID Victim's ID
     * @return Victim object or null if not found
     * @throws SQLException if database error occurs
     */
    Victim findById(int victimID) throws SQLException;

    /**
     * Get all victims
     * @return List of all victims
     * @throws SQLException if database error occurs
     */
    List<Victim> findAll() throws SQLException;

    /**
     * Create a new victim
     * @param victim Victim object to create
     * @return true if creation successful
     * @throws SQLException if database error occurs
     */
    boolean create(Victim victim) throws SQLException;

    /**
     * Update an existing victim
     * @param victim Victim object with updated data
     * @return true if update successful
     * @throws SQLException if database error occurs
     */
    boolean update(Victim victim) throws SQLException;

    /**
     * Delete a victim
     * @param victimID Victim's ID to delete
     * @return true if deletion successful
     * @throws SQLException if database error occurs
     */
    boolean delete(int victimID) throws SQLException;

    /**
     * Update only the account status for a victim.
     *
     * @param victimID   Victim's ID
     * @param newStatus  New account status
     * @return true if update successful
     * @throws SQLException if database error occurs
     */
    boolean updateAccountStatus(int victimID, String newStatus) throws SQLException;
}

