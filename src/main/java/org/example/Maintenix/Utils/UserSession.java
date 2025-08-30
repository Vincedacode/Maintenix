package org.example.Maintenix.Utils;

/**
 * Singleton class to maintain user session across controllers
 */
public class UserSession {
    private static UserSession instance;
    private String currentUsername;
    private String currentUserFullName;

    private UserSession() {
        // Private constructor for singleton
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setCurrentUser(String username, String fullName) {
        this.currentUsername = username;
        this.currentUserFullName = fullName;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public String getCurrentUserFullName() {
        return currentUserFullName;
    }

    public void clearSession() {
        this.currentUsername = null;
        this.currentUserFullName = null;
    }

    public boolean isLoggedIn() {
        return currentUsername != null && !currentUsername.isEmpty();
    }
}