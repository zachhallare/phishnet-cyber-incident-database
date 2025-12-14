package dao;

import model.ThreatLevelLog;
import util.DatabaseConnection;
import util.DateUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the ThreatLevelLogDAO interface for interacting with the ThreatLevelLog table in the database.
 * Provides methods to log threat level changes, insert logs, and retrieve logs.
 */
public class ThreatLevelLogDAOImpl implements ThreatLevelLogDAO {

    /**
     * Logs a threat level change for a perpetrator.
     *
     * @param perpetratorID ID of the perpetrator whose threat level is changed.
     * @param oldLevel      The old threat level.
     * @param newLevel      The new threat level.
     * @param adminID       ID of the admin making the change.
     * @return true if the log was successfully inserted, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    @Override
    public boolean logChange(int perpetratorID, String oldLevel, String newLevel, int adminID) throws SQLException {
        String sql = """
            INSERT INTO ThreatLevelLog 
            (PerpetratorID, OldThreatLevel, NewThreatLevel, ChangeDate, AdminID) 
            VALUES (?, ?, ?, ?, ?)
            """;

        // Try-with-resources to automatically close connection and statement
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set parameters for the prepared statement
            stmt.setInt(1, perpetratorID);
            stmt.setString(2, oldLevel);
            stmt.setString(3, newLevel);
            stmt.setString(4, DateUtils.toDatabaseFormat(LocalDateTime.now())); // Current date/time
            stmt.setInt(5, adminID);

            // Execute insert and check if at least one row was affected
            if (stmt.executeUpdate() > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    // Optional: retrieve auto-generated LogID if needed
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Inserts a ThreatLevelLog object into the database.
     * Delegates to logChange() method.
     *
     * @param log ThreatLevelLog object to insert.
     * @return true if the log was successfully inserted, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    @Override
    public boolean insert(ThreatLevelLog log) throws SQLException {
        return logChange(
                log.getPerpetratorID(),
                log.getOldThreatLevel(),
                log.getNewThreatLevel(),
                log.getAdminID()
        );
    }

    /**
     * Finds all threat level logs for a specific perpetrator.
     *
     * @param perpetratorID ID of the perpetrator.
     * @return List of ThreatLevelLog objects associated with the given perpetrator.
     * @throws SQLException if a database access error occurs.
     */
    @Override
    public List<ThreatLevelLog> findByPerpetratorID(int perpetratorID) throws SQLException {
        return findByColumn("PerpetratorID", perpetratorID);
    }

    /**
     * Retrieves all threat level logs from the database, ordered by ChangeDate descending.
     *
     * @return List of all ThreatLevelLog objects.
     * @throws SQLException if a database access error occurs.
     */
    @Override
    public List<ThreatLevelLog> findAll() throws SQLException {
        List<ThreatLevelLog> list = new ArrayList<>();
        String sql = "SELECT * FROM ThreatLevelLog ORDER BY ChangeDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            // Map each row in the result set to a ThreatLevelLog object
            while (rs.next()) {
                list.add(mapResultSetToLog(rs));
            }
        }
        return list;
    }

    /**
     * Finds threat level logs based on a specific column and integer value.
     *
     * @param column Column name to filter by.
     * @param value  Integer value to match in the column.
     * @return List of ThreatLevelLog objects matching the criteria.
     * @throws SQLException if a database access error occurs.
     */
    private List<ThreatLevelLog> findByColumn(String column, int value) throws SQLException {
        List<ThreatLevelLog> list = new ArrayList<>();
        String sql = "SELECT * FROM ThreatLevelLog WHERE " + column + " = ? ORDER BY ChangeDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, value);

            // Execute query and map result set to ThreatLevelLog objects
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLog(rs));
                }
            }
        }
        return list;
    }

    /**
     * Maps a ResultSet row to a ThreatLevelLog object.
     *
     * @param rs ResultSet pointing to the current row.
     * @return ThreatLevelLog object with data from the current row.
     * @throws SQLException if a database access error occurs.
     */
    private ThreatLevelLog mapResultSetToLog(ResultSet rs) throws SQLException {
        ThreatLevelLog log = new ThreatLevelLog();
        log.setLogID(rs.getInt("LogID"));
        log.setPerpetratorID(rs.getInt("PerpetratorID"));
        log.setOldThreatLevel(rs.getString("OldThreatLevel"));
        log.setNewThreatLevel(rs.getString("NewThreatLevel"));
        log.setChangeDate(DateUtils.fromDatabaseFormat(rs.getString("ChangeDate"))); // Convert from DB format
        log.setAdminID(rs.getInt("AdminID"));
        return log;
    }
}