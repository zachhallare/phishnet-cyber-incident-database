package util;

/**
 * Generate a password4j-compatible hash for PhishNetAdmin124
 * This ensures 100% compatibility with the login system
 */
public class GetWorkingHash {
    public static void main(String[] args) {
        String password = "PhishNetAdmin124";
        
        System.out.println("========================================");
        System.out.println("Generating password4j-compatible hash");
        System.out.println("Password: " + password);
        System.out.println("========================================");
        System.out.println();
        
        // Generate hash
        String hash = SecurityUtils.hashPassword(password);
        
        if (hash == null) {
            System.err.println("ERROR: Failed to generate hash!");
            System.exit(1);
        }
        
        // Verify it works
        boolean verified = SecurityUtils.verifyPassword(password, hash);
        
        if (!verified) {
            System.err.println("ERROR: Generated hash doesn't verify!");
            System.exit(1);
        }
        
        System.out.println("SUCCESS! Hash generated and verified.");
        System.out.println();
        System.out.println("Hash: " + hash);
        System.out.println();
        System.out.println("========================================");
        System.out.println("SQL UPDATE COMMAND:");
        System.out.println("========================================");
        System.out.println();
        System.out.println("USE CybersecurityDB;");
        System.out.println();
        System.out.println("UPDATE Administrators");
        System.out.println("SET PasswordHash = '" + hash + "'");
        System.out.println("WHERE ContactEmail = 'admin@phishnet.com';");
        System.out.println();
        System.out.println("========================================");
    }
}

