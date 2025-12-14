package util;

import dao.AdministratorDAOImpl;
import model.Administrator;
import service.AdminAuthenticationService;

import java.sql.SQLException;

/**
 * Diagnostic utility to test admin login functionality
 * Usage: mvn compile exec:java -Dexec.mainClass="util.LoginDiagnostic"
 */
public class LoginDiagnostic {
    public static void main(String[] args) {
        System.out.println("=== Admin Login Diagnostic Tool ===\n");
        
        String testEmail = "admin@phishnet.com";
        String testPassword = "PhishNetAdmin124";
        
        // Test 1: Database Connection
        System.out.println("1. Testing database connection...");
        if (DatabaseConnection.testConnection()) {
            System.out.println("   ✓ Database connection successful\n");
        } else {
            System.out.println("   ✗ Database connection FAILED\n");
            System.out.println("   Fix: Check DatabaseConnection.java settings");
            return;
        }
        
        // Test 2: Find Admin by Email
        System.out.println("2. Testing admin lookup by email: " + testEmail);
        AdministratorDAOImpl adminDAO = new AdministratorDAOImpl();
        try {
            Administrator admin = adminDAO.findByEmail(testEmail);
            if (admin != null) {
                System.out.println("   ✓ Admin found!");
                System.out.println("   - Name: " + admin.getName());
                System.out.println("   - Role: " + admin.getRole());
                System.out.println("   - Email: " + admin.getContactEmail());
                System.out.println("   - Hash length: " + admin.getPasswordHash().length());
                System.out.println("   - Hash format: " + 
                    (admin.getPasswordHash().startsWith("$argon2id$") ? "Valid Argon2id" : "Invalid"));
                System.out.println("   - Hash preview: " + admin.getPasswordHash().substring(0, Math.min(50, admin.getPasswordHash().length())) + "...\n");
                
                // Test 3: Password Verification
                System.out.println("3. Testing password verification...");
                System.out.println("   Password: " + testPassword);
                boolean verified = SecurityUtils.verifyPassword(testPassword, admin.getPasswordHash());
                if (verified) {
                    System.out.println("   ✓ Password verification SUCCESSFUL\n");
                } else {
                    System.out.println("   ✗ Password verification FAILED\n");
                    System.out.println("   Possible issues:");
                    System.out.println("   - Wrong password");
                    System.out.println("   - Hash mismatch");
                    System.out.println("   - Argon2 library issue");
                }
                
            } else {
                System.out.println("   ✗ Admin NOT FOUND in database\n");
                System.out.println("   Fix: Run PhishNet-inserts.sql to populate admin accounts");
                System.out.println("   SQL: INSERT INTO Administrators (Name, Role, ContactEmail, PasswordHash) VALUES ...");
            }
        } catch (SQLException e) {
            System.out.println("   ✗ Database error: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        // Test 4: Full Authentication Service Test
        System.out.println("4. Testing full authentication service...");
        AdminAuthenticationService authService = new AdminAuthenticationService();
        try {
            Administrator authenticatedAdmin = authService.authenticate(testEmail, testPassword);
            if (authenticatedAdmin != null) {
                System.out.println("   ✓ Full authentication SUCCESSFUL!");
                System.out.println("   - Authenticated as: " + authenticatedAdmin.getName());
            } else {
                System.out.println("   ✗ Full authentication FAILED");
                System.out.println("   Check console output above for details");
            }
        } catch (Exception e) {
            System.out.println("   ✗ Authentication error: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== Diagnostic Complete ===");
    }
}

