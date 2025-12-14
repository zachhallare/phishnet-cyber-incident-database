package dao;

import model.IncidentReport;
import java.sql.SQLException;
import java.util.List;

public interface IncidentReportDAO {
    boolean create(IncidentReport report) throws SQLException;
    IncidentReport findById(int incidentID) throws SQLException;
    List<IncidentReport> findByVictimID(int victimID) throws SQLException;
    List<IncidentReport> findByPerpetratorID(int perpetratorID) throws SQLException;
    List<IncidentReport> findPending() throws SQLException;
    List<IncidentReport> findAll() throws SQLException;
    boolean updateStatus(int incidentID, String status, Integer adminID) throws SQLException;
    boolean delete(int incidentID) throws SQLException;
    int countVictimsLast7Days(int perpetratorID) throws SQLException;
    int countUniqueVictimsLast7Days(int perpetratorID) throws SQLException; // ADD THIS
    int countIncidentsLastMonth(int victimID) throws SQLException;
}