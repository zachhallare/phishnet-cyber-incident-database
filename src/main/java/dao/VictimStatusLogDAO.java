package dao;

import model.VictimStatusLog;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO Interface for Victim Status Change Logging
 */
public interface VictimStatusLogDAO {
    /**
     * Log a victim status change
     */
    boolean logChange(int victimID, String oldStatus, String newStatus, Integer adminID) throws SQLException;

    /**
     * Get all logs for a victim
     */
    List<VictimStatusLog> findByVictimID(int victimID) throws SQLException;

    /**
     * Get all logs
     */
    List<VictimStatusLog> findAll() throws SQLException;
}