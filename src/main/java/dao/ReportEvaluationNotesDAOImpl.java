package dao;

import util.DatabaseConnection;
import util.DateUtils;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * DAO Implementation for Report Evaluation Notes
 * Uses a simple table to store evaluation notes linked to reports
 */
public class ReportEvaluationNotesDAOImpl implements ReportEvaluationNotesDAO {
    
    @Override
    public String getNotesForReport(int incidentID) throws SQLException {
        // Check if table exists, if not return null
        if (!tableExists()) {
            return null;
        }
        
        String sql = "SELECT Notes FROM ReportEvaluationNotes WHERE IncidentID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, incidentID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Notes");
                }
            }
        }
        return null;
    }
    
    @Override
    public boolean saveNotesForReport(int incidentID, String notes, Integer adminID) throws SQLException {
        // Create table if it doesn't exist
        createTableIfNotExists();
        
        String sql = """
            INSERT INTO ReportEvaluationNotes (IncidentID, Notes, AdminID, LastUpdated)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE 
                Notes = VALUES(Notes),
                AdminID = VALUES(AdminID),
                LastUpdated = VALUES(LastUpdated)
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, incidentID);
            stmt.setString(2, notes != null ? notes : "");
            setInteger(stmt, 3, adminID);
            stmt.setString(4, DateUtils.toDatabaseFormat(LocalDateTime.now()));
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    private void createTableIfNotExists() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS ReportEvaluationNotes (
                IncidentID INT PRIMARY KEY,
                Notes TEXT,
                AdminID INT,
                LastUpdated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (IncidentID) REFERENCES IncidentReports(IncidentID) ON DELETE CASCADE,
                FOREIGN KEY (AdminID) REFERENCES Administrators(AdminID)
            )
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    private boolean tableExists() throws SQLException {
        String sql = """
            SELECT COUNT(*) 
            FROM information_schema.tables 
            WHERE table_schema = DATABASE() 
            AND table_name = 'ReportEvaluationNotes'
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    private void setInteger(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.INTEGER);
        } else {
            stmt.setInt(index, value);
        }
    }
}

