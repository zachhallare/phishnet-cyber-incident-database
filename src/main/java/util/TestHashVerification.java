package util;

import com.password4j.Password;
import com.password4j.Argon2Function;
import com.password4j.types.Argon2;

/**
 * Test hash generation and verification to find the issue
 */
public class TestHashVerification {
    public static void main(String[] args) {
        String password = "PhishNetAdmin124";
        
        System.out.println("=== Testing Hash Generation and Verification ===\n");
        System.out.println("Password: " + password);
        System.out.println();
        
        // Create Argon2Function with same parameters as SecurityUtils
        Argon2Function argon2 = Argon2Function.getInstance(65536, 3, 4, 32, Argon2.ID);
        
        // Generate hash
        System.out.println("1. Generating hash...");
        String hash = Password.hash(password)
                .with(argon2)
                .getResult();
        
        System.out.println("Hash: " + hash);
        System.out.println("Hash length: " + hash.length());
        System.out.println();
        
        // Test verification method 1: withArgon2()
        System.out.println("2. Testing verification with withArgon2()...");
        try {
            boolean verified1 = Password.check(password, hash).withArgon2();
            System.out.println("Result: " + verified1);
            System.out.println("Status: " + (verified1 ? "✓ SUCCESS" : "✗ FAILED"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
        
        // Test verification method 2: with(argon2)
        System.out.println("3. Testing verification with with(argon2)...");
        try {
            boolean verified2 = Password.check(password, hash).with(argon2);
            System.out.println("Result: " + verified2);
            System.out.println("Status: " + (verified2 ? "✓ SUCCESS" : "✗ FAILED"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
        
        // Test with wrong password
        System.out.println("4. Testing with wrong password (should fail)...");
        try {
            boolean verified3 = Password.check("wrongpassword", hash).withArgon2();
            System.out.println("Result: " + verified3);
            System.out.println("Status: " + (!verified3 ? "✓ Correctly rejected" : "✗ Should have failed"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}

