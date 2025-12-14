package dao;

import model.VictimStatusLog;
import util.DatabaseConnection;
import util.DateUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO implementation for VictimStatusLog — responsible for creating and retrieving
 * audit trail entries when a victim's AccountStatus is changed.
 *
 * This ensures full traceability of all status modifications (e.g., Active → Under Investigation)
 * as required for compliance, accountability, and incident response transparency.
 */
public class VictimStatusLogDAOImpl implements VictimStatusLogDAO {

    /**
     * Logs a status change for a victim. Called automatically whenever Victim.AccountStatus is updated.
     *
     * @param victimID   the victim whose status changed
     * @param oldStatus  previous status (can be null for new accounts)
     * @param newStatus  the new status after update
     * @param adminID    ID of the admin who made the change (nullable if system-triggered)
     * @return true if log entry was successfully inserted
     * @throws SQLException if database error occurs
     */
    @Override
    public boolean logChange(int victimID, String oldStatus, String newStatus, Integer adminID) throws SQLException {
        String sql = """
            INSERT INTO VictimStatusLog 
            (VictimID, OldStatus, NewStatus, ChangeDate, AdminID) 
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, victimID);
            stmt.setString(2, oldStatus);                    // May be null → DB allows NULL
            stmt.setString(3, newStatus);
            stmt.setString(4, DateUtils.toDatabaseFormat(LocalDateTime.now())); // Always current timestamp
            if (adminID == null) {
                stmt.setNull(5, Types.INTEGER);              // No admin involved (e.g., auto-status)
            } else {
                stmt.setInt(5, adminID);
            }

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Retrieves all status change logs for a specific victim.
     *
     * @param victimID the victim's ID
     * @return ordered list (newest first) of status change events
     * @throws SQLException if database error occurs
     */
    @Override
    public List<VictimStatusLog> findByVictimID(int victimID) throws SQLException {
        return findByColumn("VictimID", victimID);
    }

    /**
     * Returns all victim status change logs in the system.
     * Used in admin audit and compliance reporting.
     *
     * @return list of all log entries, ordered by most recent first
     * @throws SQLException if database error occurs
     */
    @Override
    public List<VictimStatusLog> findAll() throws SQLException {
        List<VictimStatusLog> list = new ArrayList<>();
        String sql = "SELECT * FROM VictimStatusLog ORDER BY ChangeDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToLog(rs));
            }
        }
        return list;
    }

    /**
     * Generic helper to search logs by any integer column (e.g., VictimID, AdminID).
     * Avoids SQL injection by using parameterized query.
     */
    private List<VictimStatusLog> findByColumn(String column, int value) throws SQLException {
        List<VictimStatusLog> list = new ArrayList<>();
        String sql = "SELECT * FROM VictimStatusLog WHERE " + column + " = ? ORDER BY ChangeDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLog(rs));
                }
            }
        }
        return list;
    }

    /**
     * Maps a ResultSet row to a fully populated VictimStatusLog object.
     * Centralizes mapping logic — keeps code DRY and consistent.
     */
    private VictimStatusLog mapResultSetToLog(ResultSet rs) throws SQLException {
        VictimStatusLog log = new VictimStatusLog();
        log.setLogID(rs.getInt("LogID"));
        log.setVictimID(rs.getInt("VictimID"));
        log.setOldStatus(rs.getString("OldStatus"));           // May be null
        log.setNewStatus(rs.getString("NewStatus"));
        log.setChangeDate(DateUtils.fromDatabaseFormat(rs.getString("ChangeDate")));
        
        // Handle possible NULL AdminID from DB
        int adminId = rs.getInt("AdminID");
        log.setAdminID(rs.wasNull() ? null : adminId);

        return log;
    }
}