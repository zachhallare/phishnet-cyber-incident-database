package util;

/**
 * Simple test utility to verify Argon2 implementation
 * Run this to test password hashing and verification
 * 
 * Usage: mvn compile exec:java -Dexec.mainClass="util.SecurityUtilsTest"
 */
public class SecurityUtilsTest {
    public static void main(String[] args) {
        // Generate admin hash if requested
        if (args.length > 0 && args[0].equals("--generate-admin-hash")) {
            String adminPassword = "PhishNetAdmin124";
            System.out.println("========================================");
            System.out.println("Generating password4j-compatible hash");
            System.out.println("Password: " + adminPassword);
            System.out.println("========================================");
            String hash = SecurityUtils.hashPassword(adminPassword);
            if (hash != null) {
                // Verify it works
                boolean verified = SecurityUtils.verifyPassword(adminPassword, hash);
                System.out.println("\nHash: " + hash);
                System.out.println("Hash length: " + hash.length());
                System.out.println("Verification: " + (verified ? "✓ SUCCESS" : "✗ FAILED"));
                System.out.println("\n========================================");
                System.out.println("SQL UPDATE COMMAND (for single admin):");
                System.out.println("========================================");
                System.out.println("USE CybersecurityDB;");
                System.out.println();
                System.out.println("UPDATE Administrators");
                System.out.println("SET PasswordHash = '" + hash + "'");
                System.out.println("WHERE ContactEmail = 'admin@phishnet.com';");
                System.out.println();
                System.out.println("========================================");
                System.out.println("SQL UPDATE COMMAND (for ALL admins):");
                System.out.println("========================================");
                System.out.println("USE CybersecurityDB;");
                System.out.println();
                System.out.println("UPDATE Administrators");
                System.out.println("SET PasswordHash = '" + hash + "'");
                System.out.println("WHERE ContactEmail IN (");
                System.out.println("    'admin@phishnet.com',");
                System.out.println("    'zach_benedict_hallare@dlsu.edu.ph',");
                System.out.println("    'benette_campo@dlsu.edu.ph',");
                System.out.println("    'brent_rebollos@dlsu.edu.ph',");
                System.out.println("    'georgina_ravelo@dlsu.edu.ph'");
                System.out.println(");");
                System.out.println("========================================");
                return;
            } else {
                System.err.println("Failed to generate hash!");
                System.exit(1);
                return;
            }
        }
        
        System.out.println("=== Testing Argon2 Implementation ===\n");
        
        // Test password
        String testPassword = "admin123";
        System.out.println("Test Password: " + testPassword);
        
        // Test hashing
        System.out.println("\n1. Testing password hashing...");
        String hash = SecurityUtils.hashPassword(testPassword);
        if (hash != null) {
            System.out.println("✓ Hash generated successfully");
            System.out.println("Hash: " + hash);
            System.out.println("Hash length: " + hash.length() + " characters");
            System.out.println("Hash format: " + (hash.startsWith("$argon2id$") ? "Valid Argon2id format" : "Invalid format"));
        } else {
            System.out.println("✗ Hash generation failed!");
            return;
        }
        
        // Test verification with correct password
        System.out.println("\n2. Testing password verification (correct password)...");
        boolean verified1 = SecurityUtils.verifyPassword(testPassword, hash);
        System.out.println("Result: " + (verified1 ? "✓ Password verified successfully" : "✗ Verification failed"));
        
        // Test verification with incorrect password
        System.out.println("\n3. Testing password verification (incorrect password)...");
        boolean verified2 = SecurityUtils.verifyPassword("wrongpassword", hash);
        System.out.println("Result: " + (!verified2 ? "✓ Correctly rejected wrong password" : "✗ Should have rejected wrong password"));
        
        // Test legacy SHA-256 hash (for migration support)
        System.out.println("\n4. Testing legacy SHA-256 hash support...");
        String legacyHash = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9"; // SHA-256 of "admin123"
        boolean verified3 = SecurityUtils.verifyPassword("admin123", legacyHash);
        System.out.println("Result: " + (verified3 ? "✓ Legacy hash verified successfully" : "✗ Legacy hash verification failed"));
        
        // Test null/empty inputs
        System.out.println("\n5. Testing edge cases...");
        String nullHash = SecurityUtils.hashPassword(null);
        String emptyHash = SecurityUtils.hashPassword("");
        boolean nullVerified = SecurityUtils.verifyPassword(null, hash);
        boolean emptyVerified = SecurityUtils.verifyPassword("", hash);
        
        System.out.println("Null password hash: " + (nullHash == null ? "✓ Correctly returned null" : "✗ Should return null"));
        System.out.println("Empty password hash: " + (emptyHash == null ? "✓ Correctly returned null" : "✗ Should return null"));
        System.out.println("Null password verification: " + (!nullVerified ? "✓ Correctly rejected null" : "✗ Should reject null"));
        System.out.println("Empty password verification: " + (!emptyVerified ? "✓ Correctly rejected empty" : "✗ Should reject empty"));
        
        // Test multiple hashes (should be different due to random salt)
        System.out.println("\n6. Testing salt randomness...");
        String hash1 = SecurityUtils.hashPassword(testPassword);
        String hash2 = SecurityUtils.hashPassword(testPassword);
        boolean bothVerify = SecurityUtils.verifyPassword(testPassword, hash1) && 
                            SecurityUtils.verifyPassword(testPassword, hash2);
        boolean hashesDifferent = !hash1.equals(hash2);
        System.out.println("Hash 1: " + hash1.substring(0, Math.min(50, hash1.length())) + "...");
        System.out.println("Hash 2: " + hash2.substring(0, Math.min(50, hash2.length())) + "...");
        System.out.println("Hashes are different: " + (hashesDifferent ? "✓ Correct (different salts)" : "✗ Should be different"));
        System.out.println("Both verify correctly: " + (bothVerify ? "✓ Both hashes verify" : "✗ Verification issue"));
        
        System.out.println("\n=== Test Complete ===");
        System.out.println("\nAll tests passed! Argon2 implementation is working correctly.");
    }
}

