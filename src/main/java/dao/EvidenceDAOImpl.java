package dao;

import model.Evidence;
import util.DatabaseConnection;
import util.DateUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the EvidenceDAO interface.
 * Provides CRUD operations and business logic for managing evidence uploads
 * related to incidents in the database.
 */
public class EvidenceDAOImpl implements EvidenceDAO {

    /**
     * Uploads a new evidence record to the database.
     * Sets initial verification status to 'Pending' and records current timestamp.
     *
     * @param evidence the Evidence object containing incident ID, type, and file path
     * @return true if upload was successful and ID was generated, false otherwise
     * @throws SQLException if a database access error occurs
     */
    @Override
    public boolean upload(Evidence evidence) throws SQLException {
        String sql = """
            INSERT INTO EvidenceUpload 
            (IncidentID, EvidenceType, FilePath, SubmissionDate, VerifiedStatus, AdminID) 
            VALUES (?, ?, ?, ?, 'Pending', NULL)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set parameters for the new evidence record
            stmt.setInt(1, evidence.getIncidentID());
            stmt.setString(2, evidence.getEvidenceType());
            stmt.setString(3, evidence.getFilePath());
            stmt.setString(4, DateUtils.toDatabaseFormat(LocalDateTime.now()));

            // Execute insert and check if a row was affected
            if (stmt.executeUpdate() > 0) {
                // Retrieve the auto-generated EvidenceID
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        evidence.setEvidenceID(keys.getInt(1));
                        evidence.setSubmissionDate(LocalDateTime.now());
                        evidence.setVerifiedStatus("Pending"); // Initial status
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Finds an evidence record by its unique EvidenceID.
     *
     * @param evidenceID the unique identifier of the evidence
     * @return Evidence object if found, null otherwise
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Evidence findById(int evidenceID) throws SQLException {
        String sql = "SELECT * FROM EvidenceUpload WHERE EvidenceID = ?";
        return executeQuery(sql, stmt -> stmt.setInt(1, evidenceID));
    }

    /**
     * Retrieves all evidence records associated with a specific incident.
     *
     * @param incidentID the ID of the incident
     * @return List of Evidence objects linked to the incident
     * @throws SQLException if a database access error occurs
     */
    @Override
    public List<Evidence> findByIncidentID(int incidentID) throws SQLException {
        return findByColumn("IncidentID", incidentID);
    }

    /**
     * Retrieves all evidence records that are awaiting admin verification.
     * Ordered by submission date (oldest first).
     *
     * @return List of pending Evidence objects
     * @throws SQLException if a database access error occurs
     */
    @Override
    public List<Evidence> findPending() throws SQLException {
        List<Evidence> list = new ArrayList<>();
        String sql = "SELECT * FROM EvidenceUpload WHERE VerifiedStatus = 'Pending' ORDER BY SubmissionDate";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToEvidence(rs));
            }
        }
        return list;
    }

    /**
     * Updates the verification status of an evidence record.
     * Called by admins to approve or reject submitted evidence.
     *
     * @param evidenceID the ID of the evidence to verify
     * @param status     "Verified" or "Rejected"
     * @param adminID    the ID of the admin performing the verification
     * @return true if the update was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    @Override
    public boolean verify(int evidenceID, String status, int adminID) throws SQLException {
        String sql = "UPDATE EvidenceUpload SET VerifiedStatus = ?, AdminID = ? WHERE EvidenceID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);  // 'Verified' or 'Rejected'
            stmt.setInt(2, adminID);    // Record which admin processed it
            stmt.setInt(3, evidenceID);

            return stmt.executeUpdate() > 0; // Returns true if at least one row was updated
        }
    }

    /**
     * Permanently deletes an evidence record from the database.
     * Note: Consider soft delete in production systems.
     *
     * @param evidenceID the ID of the evidence to delete
     * @return true if deletion was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    @Override
    public boolean delete(int evidenceID) throws SQLException {
        String sql = "DELETE FROM EvidenceUpload WHERE EvidenceID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, evidenceID);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Generic helper method to find evidence by any integer column (e.g., IncidentID).
     *
     * @param column the column name to filter by
     * @param value  the value to match
     * @return List of matching Evidence objects, ordered by submission date DESC
     * @throws SQLException if a database access error occurs
     */
    private List<Evidence> findByColumn(String column, int value) throws SQLException {
        List<Evidence> list = new ArrayList<>();
        String sql = "SELECT * FROM EvidenceUpload WHERE " + column + " = ? ORDER BY SubmissionDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEvidence(rs));
                }
            }
        }
        return list;
    }

    /**
     * Executes a query expecting at most one result using a lambda for parameter setting.
     *
     * @param sql    the SQL query string
     * @param setter lambda to set PreparedStatement parameters
     * @return Evidence object if found, null if no match
     * @throws SQLException if a database access error occurs
     */
    private Evidence executeQuery(String sql, PreparedStatementSetter setter) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            setter.setValues(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEvidence(rs);
                }
            }
        }
        return null;
    }

    /**
     * Maps a ResultSet row to an Evidence object.
     *
     * @param rs the ResultSet positioned at a valid row
     * @return fully populated Evidence object
     * @throws SQLException if column access fails
     */
    private Evidence mapResultSetToEvidence(ResultSet rs) throws SQLException {
        Evidence e = new Evidence();
        e.setEvidenceID(rs.getInt("EvidenceID"));
        e.setIncidentID(rs.getInt("IncidentID"));
        e.setEvidenceType(rs.getString("EvidenceType"));
        e.setFilePath(rs.getString("FilePath"));

        String dateStr = rs.getString("SubmissionDate");
        if (dateStr != null) {
            e.setSubmissionDate(DateUtils.fromDatabaseFormat(dateStr));
        }

        e.setVerifiedStatus(rs.getString("VerifiedStatus"));
        e.setAdminID(getInteger(rs, "AdminID")); // Handles NULL values safely

        return e;
    }

    /**
     * Safely retrieves an Integer from ResultSet, converting SQL NULL to Java null.
     *
     * @param rs     the ResultSet
     * @param column column name
     * @return Integer value or null if DB value was NULL
     * @throws SQLException if column access fails
     */
    private Integer getInteger(ResultSet rs, String column) throws SQLException {
        int val = rs.getInt(column);
        return rs.wasNull() ? null : val;
    }

    /**
     * Functional interface to set values on a PreparedStatement.
     * Used for reusable query execution with different parameter setups.
     */
    @FunctionalInterface
    private interface PreparedStatementSetter {
        void setValues(PreparedStatement stmt) throws SQLException;
    }
}