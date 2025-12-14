package dao;

import model.IncidentReport;
import util.DatabaseConnection;
import util.DateUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the IncidentReportDAO interface.
 * Handles all database operations related to IncidentReports, including
 * creation, retrieval, updating status, deletion, and analytical queries.
 */
public class IncidentReportDAOImpl implements IncidentReportDAO {

    /**
     * Creates a new incident report record in the database.
     *
     * @param report The IncidentReport object to insert.
     * @return true if insertion succeeded, false otherwise.
     * @throws SQLException if any database error occurs.
     */
    @Override
    public boolean create(IncidentReport report) throws SQLException {
        String sql = "INSERT INTO IncidentReports (VictimID, PerpetratorID, AttackTypeID, AdminID, " +
                "DateReported, Description, Status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        // Auto-close connection and statement using try-with-resources
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set required fields
            stmt.setInt(1, report.getVictimID());
            stmt.setInt(2, report.getPerpetratorID());
            stmt.setInt(3, report.getAttackTypeID());
            setInteger(stmt, 4, report.getAdminID()); // Nullable integer
            stmt.setString(5, DateUtils.toDatabaseFormat(LocalDateTime.now())); // Auto timestamp
            stmt.setString(6, report.getDescription());
            stmt.setString(7, report.getStatus());

            // Execute insert
            if (stmt.executeUpdate() > 0) {
                // Retrieve and store generated primary key
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        report.setIncidentID(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a report by its unique incidentID.
     *
     * @param incidentID ID of the incident.
     * @return IncidentReport if found, else null.
     * @throws SQLException if any DB error occurs.
     */
    @Override
    public IncidentReport findById(int incidentID) throws SQLException {
        String sql = "SELECT * FROM IncidentReports WHERE IncidentID = ?";
        return executeQuery(sql, stmt -> stmt.setInt(1, incidentID));
    }

    /**
     * Retrieves all reports where the victimID matches.
     *
     * @param victimID Victim ID.
     * @return List of matching reports.
     * @throws SQLException DB errors.
     */
    @Override
    public List<IncidentReport> findByVictimID(int victimID) throws SQLException {
        return findByColumn("VictimID", victimID);
    }

    /**
     * Retrieves all reports where the perpetratorID matches.
     *
     * @param perpetratorID Perpetrator ID.
     * @return List of matching reports.
     * @throws SQLException DB errors.
     */
    @Override
    public List<IncidentReport> findByPerpetratorID(int perpetratorID) throws SQLException {
        return findByColumn("PerpetratorID", perpetratorID);
    }

    /**
     * Retrieves all reports with status = 'Pending'.
     *
     * @return List of pending incident reports.
     * @throws SQLException DB errors.
     */
    @Override
    public List<IncidentReport> findPending() throws SQLException {
        String sql = "SELECT * FROM IncidentReports WHERE Status = 'Pending' ORDER BY DateReported";
        List<IncidentReport> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Add each row as an IncidentReport object
            while (rs.next()) {
                list.add(mapResultSetToIncidentReport(rs));
            }
        }
        return list;
    }

    /**
     * Retrieves all incident reports, ordered by most recent first.
     *
     * @return List of all incident reports.
     * @throws SQLException if database access fails.
     */
    @Override
    public List<IncidentReport> findAll() throws SQLException {
        String sql = "SELECT * FROM IncidentReports ORDER BY DateReported DESC";
        List<IncidentReport> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToIncidentReport(rs));
            }
        }
        return list;
    }

    /**
     * Updates the status of a report and assigns an admin ID.
     *
     * @param incidentID Report identifier.
     * @param status New status string.
     * @param adminID Admin responsible (nullable).
     * @return true if update succeeded.
     * @throws SQLException DB errors.
     */
    @Override
    public boolean updateStatus(int incidentID, String status, Integer adminID) throws SQLException {
        String sql = "UPDATE IncidentReports SET Status = ?, AdminID = ? WHERE IncidentID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            setInteger(stmt, 2, adminID); // Nullable admin
            stmt.setInt(3, incidentID);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes an incident report by ID.
     *
     * @param incidentID ID to delete.
     * @return true if deletion succeeded.
     * @throws SQLException DB errors.
     */
    @Override
    public boolean delete(int incidentID) throws SQLException {
        String sql = "DELETE FROM IncidentReports WHERE IncidentID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, incidentID);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Counts distinct victims a perpetrator has attacked in the last 7 days.
     *
     * @param perpetratorID The perpetrator ID.
     * @return Count of unique victims.
     * @throws SQLException DB errors.
     */
    @Override
    public int countVictimsLast7Days(int perpetratorID) throws SQLException {
        String sql = """
            SELECT COUNT(DISTINCT VictimID) 
            FROM IncidentReports 
            WHERE PerpetratorID = ? 
              AND DateReported >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, perpetratorID);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0; // Return count
            }
        }
    }

    /**
     * Duplicate of countVictimsLast7Days (intentionally preserved).
     *
     * @param perpetratorID The perpetrator ID.
     * @return Number of unique victims.
     * @throws SQLException DB errors.
     */
    @Override
    public int countUniqueVictimsLast7Days(int perpetratorID) throws SQLException {
        String sql = """
        SELECT COUNT(DISTINCT VictimID) 
        FROM IncidentReports 
        WHERE PerpetratorID = ? 
          AND DateReported >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, perpetratorID);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Counts number of incidents a victim has reported this month.
     *
     * @param victimID Victim ID.
     * @return Count of reports.
     * @throws SQLException DB errors.
     */
    @Override
    public int countIncidentsLastMonth(int victimID) throws SQLException {
        String sql = """
            SELECT COUNT(*) 
            FROM IncidentReports 
            WHERE VictimID = ? 
              AND YEAR(DateReported) = YEAR(CURDATE()) 
              AND MONTH(DateReported) = MONTH(CURDATE())
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, victimID);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Helper method to retrieve reports by a specific column.
     *
     * @param column Column name.
     * @param value Value to match.
     * @return List of reports.
     * @throws SQLException DB errors.
     */
    private List<IncidentReport> findByColumn(String column, int value) throws SQLException {
        List<IncidentReport> list = new ArrayList<>();
        String sql = "SELECT * FROM IncidentReports WHERE " + column + " = ? ORDER BY DateReported DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, value);

            try (ResultSet rs = stmt.executeQuery()) {
                // Map results to objects
                while (rs.next()) {
                    list.add(mapResultSetToIncidentReport(rs));
                }
            }
        }
        return list;
    }

    /**
     * Executes a query that expects a single result (or none).
     *
     * @param sql SQL query.
     * @param setter Lambda to set prepared-statement values.
     * @return IncidentReport if found, else null.
     * @throws SQLException DB errors.
     */
    private IncidentReport executeQuery(String sql, PreparedStatementSetter setter) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Apply prepared-statement parameters
            setter.setValues(stmt);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToIncidentReport(rs);
                }
            }
        }
        return null;
    }

    /**
     * Maps a ResultSet row into an IncidentReport object.
     *
     * @param rs ResultSet pointing to a row.
     * @return Fully populated IncidentReport object.
     * @throws SQLException DB errors.
     */
    private IncidentReport mapResultSetToIncidentReport(ResultSet rs) throws SQLException {
        IncidentReport ir = new IncidentReport();
        ir.setIncidentID(rs.getInt("IncidentID"));
        ir.setVictimID(rs.getInt("VictimID"));
        ir.setPerpetratorID(rs.getInt("PerpetratorID"));
        ir.setAttackTypeID(rs.getInt("AttackTypeID"));
        ir.setAdminID(getInteger(rs, "AdminID")); // Nullable
        ir.setDateReported(DateUtils.fromDatabaseFormat(rs.getString("DateReported")));
        ir.setDescription(rs.getString("Description"));
        ir.setStatus(rs.getString("Status"));
        return ir;
    }

    /**
     * Helper for setting nullable Integer values in PreparedStatements.
     */
    private void setInteger(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.INTEGER);
        } else {
            stmt.setInt(index, value);
        }
    }

    /**
     * Retrieves nullable integer values from a ResultSet.
     */
    private Integer getInteger(ResultSet rs, String column) throws SQLException {
        int val = rs.getInt(column);
        return rs.wasNull() ? null : val;
    }

    /**
     * Functional interface used for generalized prepared-statement parameter setting.
     */
    @FunctionalInterface
    private interface PreparedStatementSetter {
        void setValues(PreparedStatement stmt) throws SQLException;
    }
}