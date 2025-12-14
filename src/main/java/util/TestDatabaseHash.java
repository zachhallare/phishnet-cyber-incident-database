package util;

import dao.AdministratorDAOImpl;
import model.Administrator;

import java.sql.SQLException;

/**
 * Test utility to verify a hash from the database works with the current verification code
 * Usage: mvn compile exec:java -Dexec.mainClass="util.TestDatabaseHash" -Dexec.args="benette_campo@dlsu.edu.ph PhishNetAdmin124"
 */
public class TestDatabaseHash {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: TestDatabaseHash <email> <password>");
            System.err.println("Example: TestDatabaseHash benette_campo@dlsu.edu.ph PhishNetAdmin124");
            System.exit(1);
        }
        
        String email = args[0];
        String password = args[1];
        
        System.out.println("========================================");
        System.out.println("Testing Database Hash");
        System.out.println("========================================");
        System.out.println("Email: " + email);
        System.out.println("Password: " + password);
        System.out.println();
        
        try {
            AdministratorDAOImpl adminDAO = new AdministratorDAOImpl();
            Administrator admin = adminDAO.findByEmail(email);
            
            if (admin == null) {
                System.err.println("✗ Admin not found in database!");
                return;
            }
            
            String hashFromDB = admin.getPasswordHash();
            System.out.println("✓ Admin found: " + admin.getName());
            System.out.println("Hash from DB:");
            System.out.println("  Length: " + hashFromDB.length());
            System.out.println("  Preview: " + (hashFromDB.length() > 60 ? hashFromDB.substring(0, 60) + "..." : hashFromDB));
            System.out.println("  Full hash: " + hashFromDB);
            System.out.println();
            
            // Test verification
            System.out.println("Testing verification...");
            boolean verified = SecurityUtils.verifyPassword(password, hashFromDB);
            
            if (verified) {
                System.out.println("✓ VERIFICATION SUCCESSFUL!");
            } else {
                System.out.println("✗ VERIFICATION FAILED!");
                System.out.println();
                System.out.println("Trying to generate a new hash for comparison...");
                String newHash = SecurityUtils.hashPassword(password);
                if (newHash != null) {
                    System.out.println("New hash generated:");
                    System.out.println("  Length: " + newHash.length());
                    System.out.println("  Preview: " + (newHash.length() > 60 ? newHash.substring(0, 60) + "..." : newHash));
                    System.out.println();
                    System.out.println("Testing new hash verification...");
                    boolean newHashVerified = SecurityUtils.verifyPassword(password, newHash);
                    System.out.println("New hash verification: " + (newHashVerified ? "✓ SUCCESS" : "✗ FAILED"));
                    System.out.println();
                    System.out.println("SQL to update database:");
                    System.out.println("UPDATE Administrators");
                    System.out.println("SET PasswordHash = '" + newHash + "'");
                    System.out.println("WHERE ContactEmail = '" + email + "';");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

