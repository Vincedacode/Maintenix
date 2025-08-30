package org.example.Maintenix.Utils;

/**
 * Singleton class to manage admin session data across the application
 */
public class AdminSession {
    private static AdminSession instance;

    // Admin session data
    private boolean isLoggedIn = false;
    private String currentAdminEmail;
    private String currentAdminName;
    private String currentAdminId;

    // Private constructor for singleton
    private AdminSession() {}

    // Get singleton instance
    public static AdminSession getInstance() {
        if (instance == null) {
            instance = new AdminSession();
        }
        return instance;
    }

    // Set admin session after successful login
    public void setAdminSession(String email, String name, String adminId) {
        this.currentAdminEmail = email;
        this.currentAdminName = name;
        this.currentAdminId = adminId;
        this.isLoggedIn = true;

        System.out.println("Admin session created for: " + name + " (" + email + ")");
    }

    // Clear admin session on logout
    public void clearSession() {
        this.currentAdminEmail = null;
        this.currentAdminName = null;
        this.currentAdminId = null;
        this.isLoggedIn = false;

        System.out.println("Admin session cleared");
    }

    // Getters
    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public String getCurrentAdminEmail() {
        return currentAdminEmail;
    }

    public String getCurrentAdminName() {
        return currentAdminName != null ? currentAdminName : "Admin";
    }

    public String getCurrentAdminId() {
        return currentAdminId;
    }

    // Validation method
    public boolean isValidSession() {
        return isLoggedIn && currentAdminEmail != null && currentAdminName != null;
    }

    // Debug info
    public void printSessionInfo() {
        System.out.println("=== Admin Session Info ===");
        System.out.println("Logged In: " + isLoggedIn);
        System.out.println("Email: " + currentAdminEmail);
        System.out.println("Name: " + currentAdminName);
        System.out.println("ID: " + currentAdminId);
        System.out.println("========================");
    }
}