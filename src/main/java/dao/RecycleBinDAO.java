package dao;

import model.Evidence;
import model.IncidentReport;
import model.RecycleBinEvidence;
import model.RecycleBinReport;

import java.sql.SQLException;
import java.util.List;

/**
 * DAO for archiving and restoring records via the recycle bin tables.
 */
public interface RecycleBinDAO {

    boolean archiveIncidentReport(IncidentReport report, int rejectedByAdminId, String reason) throws SQLException;

    boolean archiveEvidence(Evidence evidence, int rejectedByAdminId, String reason) throws SQLException;

    List<RecycleBinReport> findAllReports() throws SQLException;

    List<RecycleBinEvidence> findAllEvidence() throws SQLException;

    List<RecycleBinEvidence> findEvidenceByIncidentID(int incidentID) throws SQLException;

    boolean restoreIncidentReport(RecycleBinReport archivedReport) throws SQLException;

    boolean restoreEvidence(RecycleBinEvidence archivedEvidence) throws SQLException;
}
