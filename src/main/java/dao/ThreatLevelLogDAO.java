package dao;

import model.ThreatLevelLog;
import java.sql.SQLException;
import java.util.List;

public interface ThreatLevelLogDAO {
    boolean logChange(int perpetratorID, String oldLevel, String newLevel, int adminID) throws SQLException;
    boolean insert(ThreatLevelLog log) throws SQLException; // ADD THIS
    List<ThreatLevelLog> findByPerpetratorID(int perpetratorID) throws SQLException;
    List<ThreatLevelLog> findAll() throws SQLException;
}
