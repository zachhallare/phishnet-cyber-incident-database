package dao;

import model.Perpetrator;
import util.DatabaseConnection;
import util.DateUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of PerpetratorDAO for managing perpetrator records.
 * Provides CRUD operations and lookup utilities used by the incident reporting system.
 */
public class PerpetratorDAOImpl implements PerpetratorDAO {

    /**
     * Finds a perpetrator using a unique identifier (email, phone, link, etc.).
     *
     * @param identifier the unique identifying string
     * @return the matching Perpetrator object or null if not found
     * @throws SQLException if a database error occurs
     */
    @Override
    public Perpetrator findByIdentifier(String identifier) throws SQLException {
        String sql = "SELECT * FROM Perpetrators WHERE Identifier = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, identifier); // Set unique string identifier

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPerpetrator(rs); // Convert row to object
                }
            }
        }
        return null;
    }

    /**
     * Retrieves a perpetrator by their primary key ID.
     *
     * @param perpetratorID the perpetrator's database ID
     * @return the matching Perpetrator or null if not found
     * @throws SQLException if a database error occurs
     */
    @Override
    public Perpetrator findById(int perpetratorID) throws SQLException {
        String sql = "SELECT * FROM Perpetrators WHERE PerpetratorID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, perpetratorID); // Bind ID into query

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPerpetrator(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all perpetrators, ordered by most recent incident.
     *
     * @return a list of all perpetrators
     * @throws SQLException if database access fails
     */
    @Override
    public List<Perpetrator> findAll() throws SQLException {
        List<Perpetrator> list = new ArrayList<>();
        String sql = "SELECT * FROM Perpetrators ORDER BY LastIncidentDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Loop through all rows and map them to Perpetrator objects
            while (rs.next()) {
                list.add(mapResultSetToPerpetrator(rs));
            }
        }
        return list;
    }

    /**
     * Inserts a new perpetrator entry into the database.
     *
     * @param perpetrator the perpetrator object to insert
     * @return true if successfully inserted
     * @throws SQLException if a database error occurs
     */
    @Override
    public boolean create(Perpetrator perpetrator) throws SQLException {
        String sql = "INSERT INTO Perpetrators (Identifier, IdentifierType, AssociatedName, ThreatLevel, LastIncidentDate) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Bind fields to the INSERT statement
            stmt.setString(1, perpetrator.getIdentifier());
            stmt.setString(2, perpetrator.getIdentifierType());
            stmt.setString(3, perpetrator.getAssociatedName());
            stmt.setString(4, perpetrator.getThreatLevel());
            stmt.setString(5, DateUtils.toDatabaseFormat(perpetrator.getLastIncidentDate()));

            if (stmt.executeUpdate() > 0) {
                // Retrieve auto-generated ID
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        perpetrator.setPerpetratorID(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Updates an existing perpetrator entry.
     *
     * @param perpetrator the updated perpetrator data
     * @return true if update was applied
     * @throws SQLException if database error occurs
     */
    @Override
    public boolean update(Perpetrator perpetrator) throws SQLException {
        String sql = "UPDATE Perpetrators SET IdentifierType = ?, AssociatedName = ?, " +
                "ThreatLevel = ?, LastIncidentDate = ? WHERE PerpetratorID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, perpetrator.getIdentifierType());
            stmt.setString(2, perpetrator.getAssociatedName());
            stmt.setString(3, perpetrator.getThreatLevel());
            stmt.setString(4, DateUtils.toDatabaseFormat(perpetrator.getLastIncidentDate()));
            stmt.setInt(5, perpetrator.getPerpetratorID());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a perpetrator record from the database.
     *
     * @param perpetratorID the ID of the perpetrator to delete
     * @return true if row was deleted
     * @throws SQLException if a database error occurs
     */
    @Override
    public boolean delete(int perpetratorID) throws SQLException {
        String sql = "DELETE FROM Perpetrators WHERE PerpetratorID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, perpetratorID); // Bind ID for deletion
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Creates a new perpetrator or updates an existing one.
     * This is "upsert" logic based on the unique identifier.
     *
     * @param perpetrator the perpetrator to create or update
     * @return the final persisted perpetrator object
     * @throws SQLException if a database error occurs
     */
    @Override
    public Perpetrator createOrUpdate(Perpetrator perpetrator) throws SQLException {
        // Check if the perpetrator already exists
        Perpetrator existing = findByIdentifier(perpetrator.getIdentifier());

        if (existing != null) {
            // Update existing perpetrator
            existing.setIdentifierType(perpetrator.getIdentifierType());
            existing.setAssociatedName(perpetrator.getAssociatedName());
            existing.setThreatLevel(perpetrator.getThreatLevel());
            existing.setLastIncidentDate(LocalDateTime.now()); // Refresh timestamp
            update(existing);
            return existing;
        } else {
            // Create a brand new perpetrator
            perpetrator.setLastIncidentDate(LocalDateTime.now());
            create(perpetrator);
            return perpetrator;
        }
    }

    /**
     * Maps a ResultSet row into a Perpetrator model object.
     *
     * @param rs the result set pointing to a row
     * @return a populated Perpetrator object
     * @throws SQLException if accessing result set fails
     */
    private Perpetrator mapResultSetToPerpetrator(ResultSet rs) throws SQLException {
        Perpetrator p = new Perpetrator();

        // Read columns into object fields
        p.setPerpetratorID(rs.getInt("PerpetratorID"));
        p.setIdentifier(rs.getString("Identifier"));
        p.setIdentifierType(rs.getString("IdentifierType"));
        p.setAssociatedName(rs.getString("AssociatedName"));
        p.setThreatLevel(rs.getString("ThreatLevel"));

        // Convert date string into LocalDateTime
        String dateStr = rs.getString("LastIncidentDate");
        if (dateStr != null) {
            p.setLastIncidentDate(DateUtils.fromDatabaseFormat(dateStr));
        }

        return p;
    }
}