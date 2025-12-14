package util;

import com.password4j.Password;
import com.password4j.Argon2Function;
import com.password4j.types.Argon2;

/**
 * Debug password verification to find the issue
 */
public class DebugVerification {
    public static void main(String[] args) {
        String password = "PhishNetAdmin124";
        String hash = "$argon2id$v=19$m=65536,t=3,p=4$pq/DXvqbc5I1LyiuD+KyFw$uCZ6pixAem8bg4ecoXkWIEv9QKd9YUcl16DPQuJavCw";
        
        System.out.println("=== Debugging Password Verification ===\n");
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println();
        
        // Test 1: withArgon2() without parameters
        System.out.println("Test 1: Password.check().withArgon2()");
        try {
            boolean result1 = Password.check(password, hash).withArgon2();
            System.out.println("Result: " + result1);
            System.out.println("Status: " + (result1 ? "✓ SUCCESS" : "✗ FAILED"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
        
        // Test 2: withArgon2() with explicit function
        System.out.println("Test 2: Password.check().with(ARGON2_FUNCTION)");
        try {
            Argon2Function argon2 = Argon2Function.getInstance(65536, 3, 4, 32, Argon2.ID);
            boolean result2 = Password.check(password, hash).with(argon2);
            System.out.println("Result: " + result2);
            System.out.println("Status: " + (result2 ? "✓ SUCCESS" : "✗ FAILED"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
        
        // Test 3: Generate new hash and verify immediately
        System.out.println("Test 3: Generate new hash and verify immediately");
        try {
            Argon2Function argon2 = Argon2Function.getInstance(65536, 3, 4, 32, Argon2.ID);
            String newHash = Password.hash(password)
                    .addRandomSalt(16)
                    .with(argon2)
                    .getResult();
            System.out.println("New hash: " + newHash);
            
            boolean result3a = Password.check(password, newHash).withArgon2();
            System.out.println("Verify with withArgon2(): " + result3a);
            
            boolean result3b = Password.check(password, newHash).with(argon2);
            System.out.println("Verify with with(argon2): " + result3b);
        } catch (Exception e) {
            System.err.println("Error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}

