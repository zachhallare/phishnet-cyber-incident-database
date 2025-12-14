package dao;

import model.Evidence;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO Interface for Evidence Upload Transaction
 */
public interface EvidenceDAO {
    /**
     * Upload new evidence
     */
    boolean upload(Evidence evidence) throws SQLException;

    /**
     * Find evidence by ID
     */
    Evidence findById(int evidenceID) throws SQLException;

    /**
     * Find all evidence for an incident
     */
    List<Evidence> findByIncidentID(int incidentID) throws SQLException;

    /**
     * Get all pending evidence for admin review
     */
    List<Evidence> findPending() throws SQLException;

    /**
     * Update verification status (Verified / Rejected)
     */
    boolean verify(int evidenceID, String status, int adminID) throws SQLException;

    /**
     * Delete evidence (admin only)
     */
    boolean delete(int evidenceID) throws SQLException;
}