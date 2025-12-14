package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Simple utility class that handles MySQL database connections for the whole app.
 * Uses a singleton-style approach - there's only one shared Connection object.
 * (Not thread-safe for heavy use, but fine for this small desktop application.)
 */
public class DatabaseConnection {
    
    // Database credentials - change these if your setup is different
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CybersecurityDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";   // empty password by default on many local installs
    
    // The single connection that every part of the app will reuse
    private static Connection connection = null;
    
    /**
     * Returns a valid database connection.
     * If we don't have one yet or the old one was closed, it creates a new one.
     *
     * @return an active Connection object
     * @throws SQLException if something goes wrong while connecting
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load the MySQL JDBC driver (newer versions need the cj driver)
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // Actually open the connection
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                
            } catch (ClassNotFoundException e) {
                // Driver wasn't found in the classpath
                throw new SQLException("MySQL JDBC Driver not found", e);
            }
        }
        return connection;
    }
    
    /**
     * Closes the shared database connection if it's open.
     * Called when the application shuts down to avoid leaving dangling connections.
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                // No need to set connection = null here - it will be recreated next time
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
    
    /**
     * Quick way to check if the database is reachable.
     * Used at app startup to show a friendly error if MySQL isn't running.
     *
     * @return true if we can successfully get a valid connection, false otherwise
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();   // this will create/reuse the connection
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
}

