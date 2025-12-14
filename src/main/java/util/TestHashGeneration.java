package util;

/**
 * Test hash generation and verification
 * This will generate a hash using SecurityUtils (password4j) and verify it works
 */
public class TestHashGeneration {
    public static void main(String[] args) {
        String password = "PhishNetAdmin124";
        
        System.out.println("=== Testing Hash Generation and Verification ===\n");
        System.out.println("Password: " + password + "\n");
        
        // Generate hash using SecurityUtils (password4j)
        System.out.println("1. Generating hash using SecurityUtils.hashPassword()...");
        String hash = SecurityUtils.hashPassword(password);
        
        if (hash == null) {
            System.err.println("   ✗ Hash generation FAILED!");
            return;
        }
        
        System.out.println("   ✓ Hash generated successfully");
        System.out.println("   Hash: " + hash);
        System.out.println("   Hash length: " + hash.length());
        System.out.println("   Hash format: " + (hash.startsWith("$argon2id$") ? "Valid Argon2id" : "Invalid"));
        System.out.println();
        
        // Verify the hash
        System.out.println("2. Verifying hash with SecurityUtils.verifyPassword()...");
        boolean verified = SecurityUtils.verifyPassword(password, hash);
        
        if (verified) {
            System.out.println("   ✓ Password verification SUCCESSFUL");
            System.out.println();
            System.out.println("=== Use this hash in your database ===");
            System.out.println(hash);
            System.out.println();
            System.out.println("SQL UPDATE command:");
            System.out.println("UPDATE Administrators SET PasswordHash = '" + hash + "' WHERE ContactEmail = 'admin@phishnet.com';");
        } else {
            System.err.println("   ✗ Password verification FAILED!");
            System.err.println("   This indicates a problem with the Argon2 implementation");
        }
    }
}

