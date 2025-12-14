package dao;

import model.AttackType;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete DAO implementation for AttackType entity.
 *
 * Manages access to the AttackTypes reference table — a controlled list of cyberattack
 * categories used throughout the system. Provides robust, reusable query logic via
 * a functional interface to reduce duplication in single-row lookups.
 *
 * All operations are safe from SQL injection due to exclusive use of PreparedStatements.
 */
public class AttackTypeDAOImpl implements AttackTypeDAO {

    /**
     * Retrieves a specific attack type by its primary key.
     */
    @Override
    public AttackType findById(int attackTypeID) throws SQLException {
        String sql = "SELECT * FROM AttackTypes WHERE AttackTypeID = ?";
        return executeQuery(sql, stmt -> stmt.setInt(1, attackTypeID));
    }

    /**
     * Finds an attack type by exact name match.
     * Used during incident submission to ensure consistent categorization.
     */
    @Override
    public AttackType findByName(String attackName) throws SQLException {
        String sql = "SELECT * FROM AttackTypes WHERE AttackName = ?";
        return executeQuery(sql, stmt -> stmt.setString(1, attackName));
    }

    /**
     * Returns all attack types, ordered by severity (High → Medium → Low) then alphabetically.
     * Used to populate dropdowns in report forms and admin management screens.
     */
    @Override
    public List<AttackType> findAll() throws SQLException {
        List<AttackType> list = new ArrayList<>();
        String sql = "SELECT * FROM AttackTypes ORDER BY SeverityLevel DESC, AttackName";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToAttackType(rs));
            }
        }
        return list;
    }

    /**
     * Creates a new attack type entry.
     * Populates the generated AttackTypeID back into the passed object.
     */
    @Override
    public boolean create(AttackType attackType) throws SQLException {
        String sql = "INSERT INTO AttackTypes (AttackName, Description, SeverityLevel) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, attackType.getAttackName());
            stmt.setString(2, attackType.getDescription());
            stmt.setString(3, attackType.getSeverityLevel());

            if (stmt.executeUpdate() > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        attackType.setAttackTypeID(keys.getInt(1)); // Return generated ID
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Updates an existing attack type's name, description, or severity level.
     */
    @Override
    public boolean update(AttackType attackType) throws SQLException {
        String sql = "UPDATE AttackTypes SET AttackName = ?, Description = ?, SeverityLevel = ? WHERE AttackTypeID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, attackType.getAttackName());
            stmt.setString(2, attackType.getDescription());
            stmt.setString(3, attackType.getSeverityLevel());
            stmt.setInt(4, attackType.getAttackTypeID());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Permanently removes an attack type.
     * Warning: May cause foreign key violations if incidents still reference it.
     */
    @Override
    public boolean delete(int attackTypeID) throws SQLException {
        String sql = "DELETE FROM AttackTypes WHERE AttackTypeID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attackTypeID);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Reusable helper for single-row SELECT queries.
     * Reduces code duplication between findById() and findByName().
     */
    private AttackType executeQuery(String sql, PreparedStatementSetter setter) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            setter.setValues(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttackType(rs);
                }
            }
        }
        return null;
    }

    /**
     * Maps a ResultSet row to a fully populated AttackType object.
     * Centralizes mapping logic for consistency.
     */
    private AttackType mapResultSetToAttackType(ResultSet rs) throws SQLException {
        AttackType at = new AttackType();
        at.setAttackTypeID(rs.getInt("AttackTypeID"));
        at.setAttackName(rs.getString("AttackName"));
        at.setDescription(rs.getString("Description"));
        at.setSeverityLevel(rs.getString("SeverityLevel"));
        return at;
    }

    /**
     * Functional interface used by executeQuery() to set parameters dynamically.
     * Enables clean, reusable query execution for different criteria.
     */
    @FunctionalInterface
    private interface PreparedStatementSetter {
        void setValues(PreparedStatement stmt) throws SQLException;
    }
}