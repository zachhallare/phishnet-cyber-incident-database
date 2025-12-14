package dao;

import model.Perpetrator;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO Interface for Perpetrators
 */
public interface PerpetratorDAO {
    /**
     * Find perpetrator by unique identifier (phone, email, URL, etc.)
     */
    Perpetrator findByIdentifier(String identifier) throws SQLException;

    /**
     * Find perpetrator by ID
     */
    Perpetrator findById(int perpetratorID) throws SQLException;

    /**
     * Get all perpetrators
     */
    List<Perpetrator> findAll() throws SQLException;

    /**
     * Create new perpetrator
     */
    boolean create(Perpetrator perpetrator) throws SQLException;

    /**
     * Update existing perpetrator (e.g., threat level, last incident)
     */
    boolean update(Perpetrator perpetrator) throws SQLException;

    /**
     * Delete perpetrator (use with caution)
     */
    boolean delete(int perpetratorID) throws SQLException;

    /**
     * Create or update: upsert logic for incident reporting
     */
    Perpetrator createOrUpdate(Perpetrator perpetrator) throws SQLException;
}