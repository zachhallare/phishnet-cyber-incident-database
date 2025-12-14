package util;

import com.password4j.Password;

/**
 * Direct test of password verification to debug the issue
 */
public class TestDirectVerification {
    public static void main(String[] args) {
        String password = "PhishNetAdmin124";
        String hash = "$argon2id$v=19$m=65536,t=3,p=4$OC12rXF4qT5BVigv61onEQ$wy1XG9i6aAB47LZN2RmsD7QOZfYxR7Ssz2QZ6QUAmRM";
        
        System.out.println("=== Direct Verification Test ===\n");
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println();
        
        // Test 1: Direct password4j verification
        System.out.println("Test 1: Direct Password.check().withArgon2()");
        try {
            boolean result = Password.check(password, hash).withArgon2();
            System.out.println("Result: " + result);
            System.out.println("Status: " + (result ? "✓ SUCCESS" : "✗ FAILED"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
        
        // Test 2: Using SecurityUtils
        System.out.println("Test 2: Using SecurityUtils.verifyPassword()");
        try {
            boolean result = SecurityUtils.verifyPassword(password, hash);
            System.out.println("Result: " + result);
            System.out.println("Status: " + (result ? "✓ SUCCESS" : "✗ FAILED"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
        
        // Test 3: Generate new hash and verify immediately
        System.out.println("Test 3: Generate new hash and verify immediately");
        try {
            String newHash = SecurityUtils.hashPassword(password);
            System.out.println("New hash: " + newHash);
            boolean result = SecurityUtils.verifyPassword(password, newHash);
            System.out.println("Verification: " + (result ? "✓ SUCCESS" : "✗ FAILED"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}

