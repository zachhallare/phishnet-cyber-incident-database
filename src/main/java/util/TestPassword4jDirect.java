package util;

import com.password4j.Password;
import com.password4j.Argon2Function;
import com.password4j.types.Argon2;

/**
 * Direct test of password4j to see if there's an issue with our wrapper
 */
public class TestPassword4jDirect {
    public static void main(String[] args) {
        String password = "PhishNetAdmin124";
        
        System.out.println("=== Direct password4j Test ===\n");
        System.out.println("Password: " + password);
        System.out.println();
        
        // Create Argon2 function with same parameters as SecurityUtils
        Argon2Function argon2 = Argon2Function.getInstance(
            65536,  // Memory cost
            3,      // Iterations
            4,      // Parallelism
            32,     // Hash length
            Argon2.ID
        );
        
        // Generate hash
        System.out.println("1. Generating hash...");
        String hash = Password.hash(password)
                .addRandomSalt(16)
                .with(argon2)
                .getResult();
        
        System.out.println("Hash: " + hash);
        System.out.println("Hash length: " + hash.length());
        System.out.println();
        
        // Try verification method 1: withArgon2() without parameters
        System.out.println("2. Testing verification method 1: Password.check().withArgon2()");
        try {
            boolean verified1 = Password.check(password, hash).withArgon2();
            System.out.println("Result: " + (verified1 ? "✓ SUCCESS" : "✗ FAILED"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Try verification method 2: withArgon2() with explicit function
        System.out.println();
        System.out.println("3. Testing verification method 2: Password.check().with(argon2)");
        try {
            boolean verified2 = Password.check(password, hash).with(argon2);
            System.out.println("Result: " + (verified2 ? "✓ SUCCESS" : "✗ FAILED"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Try verification method 3: Extract parameters from hash
        System.out.println();
        System.out.println("4. Testing verification method 3: Extract and use parameters");
        try {
            // password4j should automatically extract parameters from the hash string
            boolean verified3 = Password.check(password, hash)
                    .withArgon2();
            System.out.println("Result: " + (verified3 ? "✓ SUCCESS" : "✗ FAILED"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

