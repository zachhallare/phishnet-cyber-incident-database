package dao;

import java.sql.SQLException;

/**
 * DAO Interface for Report Evaluation Notes
 */
public interface ReportEvaluationNotesDAO {
    /**
     * Get evaluation notes for a specific report
     */
    String getNotesForReport(int incidentID) throws SQLException;
    
    /**
     * Save evaluation notes for a report
     */
    boolean saveNotesForReport(int incidentID, String notes, Integer adminID) throws SQLException;
}

