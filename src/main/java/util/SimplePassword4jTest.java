package util;

import com.password4j.Password;
import com.password4j.Argon2Function;
import com.password4j.types.Argon2;

/**
 * Simple test to verify password4j basic functionality
 */
public class SimplePassword4jTest {
    public static void main(String[] args) {
        String password = "PhishNetAdmin124";
        
        System.out.println("========================================");
        System.out.println("Simple Password4j Test");
        System.out.println("========================================");
        System.out.println("Password: " + password);
        System.out.println();
        
        // Test 1: Generate with .withArgon2() and verify with .withArgon2()
        System.out.println("Test 1: Generate with .withArgon2(), verify with .withArgon2()");
        try {
            String hash1 = Password.hash(password).withArgon2().getResult();
            System.out.println("Hash generated: " + hash1.substring(0, Math.min(60, hash1.length())) + "...");
            boolean verified1 = Password.check(password, hash1).withArgon2();
            System.out.println("Verification: " + (verified1 ? "✓ SUCCESS" : "✗ FAILED"));
        } catch (Exception e) {
            System.err.println("Test 1 failed: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
        
        // Test 2: Generate with ARGON2_FUNCTION and verify with ARGON2_FUNCTION
        System.out.println("Test 2: Generate with ARGON2_FUNCTION, verify with ARGON2_FUNCTION");
        try {
            Argon2Function func = Argon2Function.getInstance(65536, 3, 4, 32, Argon2.ID);
            String hash2 = Password.hash(password).with(func).getResult();
            System.out.println("Hash generated: " + hash2.substring(0, Math.min(60, hash2.length())) + "...");
            boolean verified2 = Password.check(password, hash2).with(func);
            System.out.println("Verification: " + (verified2 ? "✓ SUCCESS" : "✗ FAILED"));
        } catch (Exception e) {
            System.err.println("Test 2 failed: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
        
        // Test 3: Generate with ARGON2_FUNCTION and verify with .withArgon2()
        System.out.println("Test 3: Generate with ARGON2_FUNCTION, verify with .withArgon2()");
        try {
            Argon2Function func = Argon2Function.getInstance(65536, 3, 4, 32, Argon2.ID);
            String hash3 = Password.hash(password).with(func).getResult();
            System.out.println("Hash generated: " + hash3.substring(0, Math.min(60, hash3.length())) + "...");
            boolean verified3 = Password.check(password, hash3).withArgon2();
            System.out.println("Verification: " + (verified3 ? "✓ SUCCESS" : "✗ FAILED"));
        } catch (Exception e) {
            System.err.println("Test 3 failed: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
        
        // Test 4: Generate with .withArgon2() and verify with ARGON2_FUNCTION
        System.out.println("Test 4: Generate with .withArgon2(), verify with ARGON2_FUNCTION");
        try {
            Argon2Function func = Argon2Function.getInstance(65536, 3, 4, 32, Argon2.ID);
            String hash4 = Password.hash(password).withArgon2().getResult();
            System.out.println("Hash generated: " + hash4.substring(0, Math.min(60, hash4.length())) + "...");
            boolean verified4 = Password.check(password, hash4).with(func);
            System.out.println("Verification: " + (verified4 ? "✓ SUCCESS" : "✗ FAILED"));
        } catch (Exception e) {
            System.err.println("Test 4 failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

