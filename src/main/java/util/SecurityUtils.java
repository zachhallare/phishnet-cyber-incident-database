package util;

import com.password4j.Argon2Function;
import com.password4j.Password;
import com.password4j.types.Argon2;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Central security utility class for the entire PhishNet application.
 * Handles password hashing/verification using Argon2id (current best practice),
 * supports legacy SHA-256 hashes during migration, provides basic encryption
 * (Base64 placeholder), and includes data anonymization helpers for RA 10173
 * (Philippine Data Privacy Act) compliance.
 *
 * This is the single source of truth for all security-related operations.
 * All password-related code in the app should go through these methods.
 */
public class SecurityUtils {
    
    // Argon2id configuration — carefully chosen balance between security and performance
    private static final int ITERATIONS     = 3;       // Time cost
    private static final int MEMORY_COST    = 65536;   // 64 MiB memory usage
    private static final int PARALLELISM    = 4;       // Use 4 threads
    private static final int SALT_LENGTH   = 16;       // 128-bit salt
    private static final int HASH_LENGTH    = 32;      // 256-bit output hash
    
    // Pre-configured Argon2id instance — reused for performance
    private static final Argon2Function ARGON2_FUNCTION;
    
    static {
        // Initialize once at class load time — faster than recreating every time
        ARGON2_FUNCTION = Argon2Function.getInstance(
                MEMORY_COST,      // memory in KiB
                ITERATIONS,
                PARALLELISM,
                HASH_LENGTH,
                Argon2.ID         // Argon2id = best resistance to side-channel + GPU attacks
        );
    }
    
    /**
     * Hashes a plaintext password using Argon2id with strong, fixed parameters.
     * The returned string is fully self-contained — includes version, parameters,
     * salt, and hash — so verification can extract everything automatically.
     *
     * @param password the plaintext password (never log or store this!)
     * @return fully encoded Argon2 hash string, or null if input is invalid or error occurs
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        
        // Prevent DoS by rejecting absurdly long passwords
        if (password.length() > 128) {
            System.err.println("Password exceeds maximum allowed length (128 chars)");
            return null;
        }
        
        try {
            // We explicitly use our pre-configured ARGON2_FUNCTION
            // This ensures consistency between hashing and verification
            String encodedHash = Password.hash(password)
                    .with(ARGON2_FUNCTION)
                    .getResult();
            
            return encodedHash;
            
        } catch (Exception e) {
            System.err.println("Argon2 hashing failed: " + e.getMessage());
            System.err.println("Error type: " + e.getClass().getName());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Verifies a plaintext password against a stored Argon2 (or legacy SHA-256) hash.
     * 
     * IMPORTANT: Must use the exact same Argon2Function instance used during hashing.
     * Using .withArgon2() instead of .with(ARGON2_FUNCTION) WILL fail even with correct password.
     *
     * @param password the plaintext password to check
     * @param encodedHash the stored hash (from DB)
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String encodedHash) {
        if (password == null || encodedHash == null || encodedHash.isEmpty()) {
            return false;
        }
        
        // Trim hash to remove any whitespace that might have been introduced from database
        encodedHash = encodedHash.trim();
        
        // Validate password length
        if (password.length() > 128) {
            return false;
        }
        
        try {
            // Support for old SHA-256 hashes during migration period
            if (!encodedHash.startsWith("$argon2id$") && !encodedHash.startsWith("$argon2i$") && 
                !encodedHash.startsWith("$argon2d$")) {
                // Legacy SHA-256 hash support (for migration)
                return verifyLegacyHash(password, encodedHash);
            }
            
            // Critical: Use the exact same function used when hashing
            // CRITICAL: Must use .with(ARGON2_FUNCTION) because that's how hashes are generated
            // Test results show: ARGON2_FUNCTION generate + ARGON2_FUNCTION verify = SUCCESS
            //                   ARGON2_FUNCTION generate + .withArgon2() verify = FAILED
            boolean matches = Password.check(password, encodedHash)
                    .with(ARGON2_FUNCTION);   // This is the only way that works reliably
            
            return matches;
            
        } catch (Exception e) {
            System.err.println("Password verification failed: " + e.getMessage());
            System.err.println("Hash starts with $argon2id$: " + encodedHash.startsWith("$argon2id$"));
            System.err.println("Hash length: " + encodedHash.length());
            System.err.println("Hash preview (first 50 chars): " + (encodedHash.length() > 50 ? encodedHash.substring(0, 50) + "..." : encodedHash));
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Legacy SHA-256 verification — kept only to support old accounts during migration.
     * Once all accounts use Argon2, this can be removed.
     */
    private static boolean verifyLegacyHash(String password, String hash) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            
            // Constant-time comparison to prevent timing attacks
            return constantTimeEquals(hex.toString(), hash.toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Constant-time string comparison — prevents timing attacks.
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
    
    /**
     * Very lightweight "encryption" using Base64 — placeholder only.
     * In production, replace with proper AES-GCM or similar.
     */
    public static String encrypt(String data) {
        if (data == null || data.isEmpty()) return data;
        try {
            // Simple Base64 encoding (for demonstration)
            // In production, use proper encryption
            return java.util.Base64.getEncoder()
                    .encodeToString(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("Encryption failed: " + e.getMessage());
            return data;
        }
    }
    
    /**
     * Decrypts data previously encrypted with encrypt().
     */
    public static String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) return encryptedData;
        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(encryptedData);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Decryption failed: " + e.getMessage());
            return encryptedData;
        }
    }
    
    /**
     * Anonymizes email for reports/logs — complies with RA 10173.
     * Example: john.doe@example.com → jo***@example.com
     */
    public static String anonymizeEmail(String email) {
        if (email == null || email.isEmpty()) return email;
        int at = email.indexOf('@');
        if (at > 1) {
            return email.substring(0, 2) + "***@" + email.substring(at + 1);
        }
        return "***@***";
    }
    
    /**
     * Anonymizes full name — shows only first letter.
     * Example: "Juan Dela Cruz" → "J***"
     */
    public static String anonymizeName(String name) {
        if (name == null || name.isEmpty()) return name;
        if (name.length() > 1) {
            return name.charAt(0) + "***";
        }
        return "***";
    }
}

