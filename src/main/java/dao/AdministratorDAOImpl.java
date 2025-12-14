package dao;

import dao.AdministratorDAO;
import model.Administrator;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete DAO implementation for Administrator entity.
 *
 * Handles all direct database interactions with the Administrators table using
 * prepared statements for security and performance. Follows best practices:
 * - Try-with-resources for automatic resource cleanup
 * - Parameterized queries to prevent SQL injection
 * - Case-insensitive + trimmed email lookup for robust login
 * - Proper handling of auto-generated AdminID on insert
 * - Central mapping method to keep code DRY
 */
public class AdministratorDAOImpl implements AdministratorDAO {

    /**
     * Finds an administrator by email with robust matching.
     * Uses LOWER(TRIM(...)) to handle whitespace and case variations.
     */
    @Override
    public Administrator findByEmail(String email) throws SQLException {
        // Trim email and use case-insensitive comparison for better compatibility
        String query = "SELECT * FROM Administrators WHERE LOWER(TRIM(ContactEmail)) = LOWER(TRIM(?))";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAdministrator(rs);
                }
            }
        }
        return null;
    }

    /** Retrieves administrator by primary key (fast, exact match) */
    @Override
    public Administrator findById(int adminID) throws SQLException {
        String query = "SELECT * FROM Administrators WHERE AdminID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, adminID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAdministrator(rs);
                }
            }
        }
        return null;
    }

    /** Returns all administrators — used in admin management and audit views */
    @Override
    public List<Administrator> findAll() throws SQLException {
        List<Administrator> admins = new ArrayList<>();
        String query = "SELECT * FROM Administrators ORDER BY Name";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                admins.add(mapResultSetToAdministrator(rs));
            }
        }
        return admins;
    }

    /**
     * Creates a new administrator account.
     * Returns generated AdminID in the passed object.
     * Password must already be hashed using SecurityUtils.hashPassword().
     */
    @Override
    public boolean create(Administrator admin) throws SQLException {
        String query = "INSERT INTO Administrators (Name, Role, ContactEmail, PasswordHash) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, admin.getName());
            stmt.setString(2, admin.getRole());
            stmt.setString(3, admin.getContactEmail());
            stmt.setString(4, admin.getPasswordHash());

            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        admin.setAdminID(rs.getInt(1));  // Populate the object with new ID
                    }
                }
                return true;
            }
        }
        return false;
    }

    /** Updates name, role, email, or password (if re-hashed) */
    @Override
    public boolean update(Administrator admin) throws SQLException {
        String query = "UPDATE Administrators SET Name = ?, Role = ?, ContactEmail = ?, PasswordHash = ? WHERE AdminID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, admin.getName());
            stmt.setString(2, admin.getRole());
            stmt.setString(3, admin.getContactEmail());
            stmt.setString(4, admin.getPasswordHash());
            stmt.setInt(5, admin.getAdminID());

            return stmt.executeUpdate() > 0;
        }
    }

    /** Permanently deletes an administrator — use with extreme caution */
    @Override
    public boolean delete(int adminID) throws SQLException {
        String query = "DELETE FROM Administrators WHERE AdminID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, adminID);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Central mapping method — converts a ResultSet row into a fully populated Administrator object.
     * Keeps mapping logic consistent across all queries.
     */
    private Administrator mapResultSetToAdministrator(ResultSet rs) throws SQLException {
        Administrator admin = new Administrator();
        admin.setAdminID(rs.getInt("AdminID"));
        admin.setName(rs.getString("Name"));
        admin.setRole(rs.getString("Role"));
        admin.setContactEmail(rs.getString("ContactEmail"));
        admin.setPasswordHash(rs.getString("PasswordHash"));

        Timestamp timestamp = rs.getTimestamp("DateAssigned");
        if (timestamp != null) {
            admin.setDateAssigned(timestamp.toLocalDateTime());
        }
        return admin;
    }
}

