package util;

import dao.AdministratorDAOImpl;
import model.Administrator;

import java.sql.SQLException;

/**
 * Generate a password4j-compatible hash and optionally update the database
 * Usage: mvn compile exec:java -Dexec.mainClass="util.GenerateAndUpdateHash"
 */
public class GenerateAndUpdateHash {
    public static void main(String[] args) {
        String password = "PhishNetAdmin124";
        String email = "admin@phishnet.com";
        
        System.out.println("=== Generating Password4j-Compatible Hash ===\n");
        System.out.println("Password: " + password);
        System.out.println("Email: " + email);
        System.out.println();
        
        // Generate hash using SecurityUtils (password4j)
        System.out.println("1. Generating hash using password4j...");
        String hash = SecurityUtils.hashPassword(password);
        
        if (hash == null) {
            System.err.println("   ✗ Hash generation FAILED!");
            return;
        }
        
        System.out.println("   ✓ Hash generated successfully");
        System.out.println("   Hash: " + hash);
        System.out.println("   Hash length: " + hash.length());
        System.out.println();
        
        // Verify the hash works
        System.out.println("2. Verifying hash...");
        boolean verified = SecurityUtils.verifyPassword(password, hash);
        
        if (!verified) {
            System.err.println("   ✗ Hash verification FAILED!");
            System.err.println("   This hash will NOT work for login!");
            return;
        }
        
        System.out.println("   ✓ Hash verification SUCCESSFUL");
        System.out.println();
        
        // Test with database
        System.out.println("3. Testing with database...");
        try {
            AdministratorDAOImpl adminDAO = new AdministratorDAOImpl();
            Administrator admin = adminDAO.findByEmail(email);
            
            if (admin == null) {
                System.err.println("   ✗ Admin account not found in database");
                System.err.println("   Run PhishNet-inserts.sql first");
            } else {
                System.out.println("   ✓ Admin account found: " + admin.getName());
                System.out.println("   Current hash length: " + admin.getPasswordHash().length());
                
                // Test verification with current hash
                System.out.println();
                System.out.println("4. Testing current database hash...");
                boolean currentHashWorks = SecurityUtils.verifyPassword(password, admin.getPasswordHash());
                System.out.println("   Current hash verification: " + (currentHashWorks ? "✓ WORKS" : "✗ FAILS"));
                
                if (!currentHashWorks) {
                    System.out.println();
                    System.out.println("5. Generating NEW hash to replace the current one...");
                    System.out.println("   New hash: " + hash);
                    System.out.println();
                    System.out.println("   SQL UPDATE command to fix this:");
                    System.out.println("   USE CybersecurityDB;");
                    System.out.println("   UPDATE Administrators SET PasswordHash = '" + hash + "' WHERE ContactEmail = '" + email + "';");
                    System.out.println();
                    System.out.println("   After running this SQL, the password 'PhishNetAdmin124' will work.");
                } else {
                    System.out.println("   ✓ Current hash works! No update needed.");
                }
            }
        } catch (SQLException e) {
            System.err.println("   Database error: " + e.getMessage());
        }
        
        System.out.println();
        System.out.println("=== Complete ===");
        System.out.println("Copy the hash above and use it in your SQL UPDATE statement");
    }
}

