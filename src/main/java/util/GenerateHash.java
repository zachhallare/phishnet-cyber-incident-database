package util;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Utility to generate Argon2 hash for SQL inserts
 * Usage: mvn compile exec:java -Dexec.mainClass="util.GenerateHash"
 */
public class GenerateHash {
    public static void main(String[] args) {
        String password = "PhishNetAdmin124";
        
        // Generate one hash (we'll use the same hash for all admins for consistency)
        String hash = SecurityUtils.hashPassword(password);
        
        if (hash != null) {
            // Write to file in project root
            try {
                String projectRoot = System.getProperty("user.dir");
                String filePath = Paths.get(projectRoot, "admin_hash_output.txt").toString();
                FileWriter writer = new FileWriter(filePath);
                writer.write(hash);
                writer.close();
                System.out.println("Hash written to: " + filePath);
                System.out.println("Hash: " + hash);
            } catch (IOException e) {
                System.err.println("Could not write to file: " + e.getMessage());
                System.out.println("Hash: " + hash);
            }
        } else {
            System.err.println("Failed to generate hash!");
            System.exit(1);
        }
    }
}

