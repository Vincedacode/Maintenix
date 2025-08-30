package org.example.Maintenix.Staff;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.bson.Document;
import org.example.Maintenix.DAO.staffdao;
import org.example.Maintenix.Utils.UserSession;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button devicesBtn;
    @FXML private Button historyBtn;
    @FXML private Button usageBtn;
    @FXML private Button profileBtn;
    @FXML private Button signOutBtn;

    // Profile information labels
    @FXML private Label usernameLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label departmentLabel;
    @FXML private Label welcomeLabel;
    @FXML private Label profileInitialsLabel;
    @FXML private Label memberSinceLabel;

    // DAO for database operations
    private staffdao staffDAO;
    private String currentUsername;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize DAO
        staffDAO = new staffdao();

        setupButtonActions();
        setupButtonStyles();

        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            currentUsername = session.getCurrentUsername();
            welcomeLabel.setText("Profile - " + session.getCurrentUserFullName());
            loadUserProfile(currentUsername);
        } else {
            // Handle case where user is not logged in
            showErrorAndRedirect("User not logged in", "Please log in to view profile");
        }
    }

    /**
     * Load user profile data from database
     */
    private void loadUserProfile(String username) {
        try {
            Document staffDocument = staffDAO.getStaffByUsername(username);

            if (staffDocument != null) {
                // Populate profile fields
                String fullName = staffDocument.getString("Fullname");
                String email = staffDocument.getString("Email");
                String department = staffDocument.getString("Department");
                String usernameFromDB = staffDocument.getString("Username");

                // Set labels
                fullNameLabel.setText(fullName != null ? fullName : "N/A");
                emailLabel.setText(email != null ? email : "N/A");
                departmentLabel.setText(department != null ? department : "N/A");
                usernameLabel.setText(usernameFromDB != null ? usernameFromDB : "N/A");

                // Set member since date (assuming you have a created_at field or similar)
                // You can modify this based on your actual field name in the staff collection
                Object createdAt = staffDocument.get("created_at");
                if (createdAt != null) {
                    // If you have a Date field
                    if (createdAt instanceof java.util.Date) {
                        java.util.Date memberDate = (java.util.Date) createdAt;
                        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("MMMM yyyy");
                        memberSinceLabel.setText(formatter.format(memberDate));
                    } else {
                        // If it's a string or other format, you can handle it accordingly
                        memberSinceLabel.setText(createdAt.toString());
                    }
                } else {
                    // Fallback if no creation date is available
                    memberSinceLabel.setText("January 2024");
                }

                // Set profile initials
                setProfileInitials(fullName);

            } else {
                showErrorDialog("Profile Error", "Could not load profile information");
            }

        } catch (Exception e) {
            System.err.println("Error loading user profile: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Database Error", "Failed to retrieve profile data");
        }
    }

    /**
     * Generate and set profile initials from full name
     */
    private void setProfileInitials(String fullName) {
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] nameParts = fullName.trim().split("\\s+");
            StringBuilder initials = new StringBuilder();

            for (String part : nameParts) {
                if (!part.isEmpty()) {
                    initials.append(part.charAt(0));
                    if (initials.length() >= 2) break; // Limit to 2 initials
                }
            }

            profileInitialsLabel.setText(initials.toString().toUpperCase());
        } else {
            profileInitialsLabel.setText("U");
        }
    }

    private void setupButtonActions() {
        // Navigation button actions
        dashboardBtn.setOnAction(e -> handleDashboardClick());
        devicesBtn.setOnAction(e -> handleDevicesClick());
        historyBtn.setOnAction(e -> handleHistoryClick());
        usageBtn.setOnAction(e -> handleUsageClick());
        profileBtn.setOnAction(e -> handleProfileClick());
        signOutBtn.setOnAction(e -> handleSignOutClick());
    }

    private void setupButtonStyles() {
        // Set profile as active since we're on profile page
        setActiveNavButton(profileBtn);
    }

    private void setActiveNavButton(Button activeButton) {
        // Remove active class from all nav buttons
        Button[] navButtons = {dashboardBtn, devicesBtn, historyBtn, usageBtn, profileBtn, signOutBtn};

        for (Button button : navButtons) {
            button.getStyleClass().removeAll("nav-active");
        }

        // Add active class to the selected button
        if (!activeButton.getStyleClass().contains("nav-active")) {
            activeButton.getStyleClass().add("nav-active");
        }
    }

    // Navigation button handlers
    private void handleDashboardClick() {
        setActiveNavButton(dashboardBtn);
        navigateToPage("/FXML/StaffDashboard.fxml", "Maintenix - Dashboard");
    }

    private void handleDevicesClick() {
        setActiveNavButton(devicesBtn);
        navigateToPage("/FXML/Devices.fxml", "Maintenix - Devices");
    }

    private void handleHistoryClick() {
        setActiveNavButton(historyBtn);
        navigateToPage("/FXML/HistoryPage.fxml", "Maintenix - History");
    }

    private void handleUsageClick() {
        setActiveNavButton(usageBtn);
        System.out.println("Usage selected");
        showInfoDialog("Usage", "Navigate to Usage page");
    }

    private void handleProfileClick() {
        setActiveNavButton(profileBtn);
        System.out.println("Profile selected - already on profile page");
        // Already on profile page, just refresh data
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            loadUserProfile(session.getCurrentUsername());
        }
    }

    private void handleSignOutClick() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Sign Out");
        alert.setHeaderText("Are you sure you want to sign out?");
        alert.setContentText("You will be logged out of the application.");

        alert.showAndWait().ifPresent(response -> {
            if (response.getButtonData().isDefaultButton()) {
                // Clear user session
                UserSession.getInstance().clearSession();
                navigateToPage("/FXML/View.fxml", "Maintenix - Login");
            }
        });
    }

    /**
     * Navigate to a different page
     */
    private void navigateToPage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) profileBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setMaximized(true);
        } catch (IOException e) {
            System.err.println("Error loading page " + fxmlPath + ": " + e.getMessage());
            showErrorDialog("Navigation Error", "Could not load the requested page");
        }
    }

    /**
     * Show info dialog
     */
    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show error dialog
     */
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show error and redirect to login
     */
    private void showErrorAndRedirect(String title, String message) {
        showErrorDialog(title, message);
        navigateToPage("/FXML/View.fxml", "Maintenix - Login");
    }

    /**
     * Refresh profile data - can be called from external controllers
     */
    public void refreshProfile() {
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            loadUserProfile(session.getCurrentUsername());
        }
    }
}