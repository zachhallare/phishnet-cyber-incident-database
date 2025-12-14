package dao;

import model.Victim;
import util.DatabaseConnection;
import util.DateUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link VictimDAO} interface.
 * Provides database operations (CRUD) for the Victim entity.
 */
public class VictimDAOImpl implements VictimDAO {

    /**
     * Finds a victim by their email address.
     *
     * @param email the email of the victim to search for
     * @return the {@link Victim} object if found, otherwise null
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Victim findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM Victims WHERE ContactEmail = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email); // Set the email parameter in the query

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVictim(rs); // Map result set to Victim object
                }
            }
        }

        return null; // Return null if no victim found
    }

    /**
     * Finds a victim by their unique ID.
     *
     * @param victimID the ID of the victim
     * @return the {@link Victim} object if found, otherwise null
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Victim findById(int victimID) throws SQLException {
        String sql = "SELECT * FROM Victims WHERE VictimID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, victimID); // Set the VictimID parameter

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVictim(rs); // Map result set to Victim object
                }
            }
        }

        return null; // Return null if no victim found
    }

    /**
     * Retrieves all victims from the database.
     *
     * @return a list of {@link Victim} objects
     * @throws SQLException if a database access error occurs
     */
    @Override
    public List<Victim> findAll() throws SQLException {
        List<Victim> victims = new ArrayList<>();
        String sql = "SELECT * FROM Victims ORDER BY VictimID";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                victims.add(mapResultSetToVictim(rs)); // Add each victim to the list
            }
        }

        return victims;
    }

    /**
     * Creates a new victim record in the database.
     *
     * @param victim the {@link Victim} object to insert
     * @return true if insertion was successful, false otherwise
     * @throws SQLException if a database access error occurs or validation fails
     */
    @Override
    public boolean create(Victim victim) throws SQLException {
        String sql = "INSERT INTO Victims (Name, ContactEmail, PasswordHash, AccountStatus) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Validate required fields
            if (victim.getName() == null || victim.getName().trim().isEmpty()) {
                throw new SQLException("Name cannot be null or empty");
            }
            if (victim.getContactEmail() == null || victim.getContactEmail().trim().isEmpty()) {
                throw new SQLException("ContactEmail cannot be null or empty");
            }
            if (victim.getPasswordHash() == null || victim.getPasswordHash().trim().isEmpty()) {
                throw new SQLException("PasswordHash cannot be null or empty");
            }
            if (victim.getAccountStatus() == null || victim.getAccountStatus().trim().isEmpty()) {
                victim.setAccountStatus("Active"); // Set default account status if not provided
            }

            // Set parameters for the INSERT statement
            stmt.setString(1, victim.getName().trim());
            stmt.setString(2, victim.getContactEmail().trim().toLowerCase());
            stmt.setString(3, victim.getPasswordHash());
            stmt.setString(4, victim.getAccountStatus());

            System.out.println("Executing INSERT: Name=" + victim.getName() + ", Email=" + victim.getContactEmail());

            int rowsAffected = stmt.executeUpdate(); // Execute the insert
            System.out.println("Rows affected: " + rowsAffected);

            if (rowsAffected > 0) {
                // Retrieve the generated VictimID
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedID = generatedKeys.getInt(1);
                        victim.setVictimID(generatedID); // Set the ID in the object
                        System.out.println("Generated VictimID: " + generatedID);
                    } else {
                        System.err.println("Warning: No generated keys returned");
                    }
                }
                return true;
            } else {
                System.err.println("Warning: INSERT executed but no rows affected");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in VictimDAOImpl.create(): " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            throw e; // Re-throw exception to caller
        }
    }

    /**
     * Updates an existing victim record in the database.
     *
     * @param victim the {@link Victim} object containing updated data
     * @return true if update was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    @Override
    public boolean update(Victim victim) throws SQLException {
        String sql = "UPDATE Victims SET Name = ?, ContactEmail = ?, PasswordHash = ?, AccountStatus = ? WHERE VictimID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameters for the UPDATE statement
            stmt.setString(1, victim.getName());
            stmt.setString(2, victim.getContactEmail());
            stmt.setString(3, victim.getPasswordHash());
            stmt.setString(4, victim.getAccountStatus());
            stmt.setInt(5, victim.getVictimID());

            int rowsAffected = stmt.executeUpdate(); // Execute update
            return rowsAffected > 0; // Return true if at least one row updated
        }
    }

    /**
     * Deletes a victim record from the database by ID.
     *
     * @param victimID the ID of the victim to delete
     * @return true if deletion was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    @Override
    public boolean delete(int victimID) throws SQLException {
        String sql = "DELETE FROM Victims WHERE VictimID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, victimID); // Set ID for deletion

            int rowsAffected = stmt.executeUpdate(); // Execute deletion
            return rowsAffected > 0; // Return true if row deleted
        }
    }

    /**
     * Updates the account status of a victim.
     *
     * @param victimID  the ID of the victim
     * @param newStatus the new account status to set
     * @return true if update was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    @Override
    public boolean updateAccountStatus(int victimID, String newStatus) throws SQLException {
        String sql = "UPDATE Victims SET AccountStatus = ? WHERE VictimID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus); // Set new account status
            stmt.setInt(2, victimID);     // Set victim ID

            return stmt.executeUpdate() > 0; // Return true if update successful
        }
    }

    /**
     * Maps a {@link ResultSet} row to a {@link Victim} object.
     *
     * @param rs the result set from a query
     * @return a Victim object populated with data from the current row
     * @throws SQLException if a database access error occurs
     */
    private Victim mapResultSetToVictim(ResultSet rs) throws SQLException {
        Victim victim = new Victim();
        victim.setVictimID(rs.getInt("VictimID"));
        victim.setName(rs.getString("Name"));
        victim.setContactEmail(rs.getString("ContactEmail"));
        victim.setPasswordHash(rs.getString("PasswordHash"));
        victim.setAccountStatus(rs.getString("AccountStatus"));

        // Convert DateCreated string from database to LocalDateTime
        String dateStr = rs.getString("DateCreated");
        if (dateStr != null) {
            victim.setDateCreated(DateUtils.fromDatabaseFormat(dateStr));
        }

        return victim;
    }
}

