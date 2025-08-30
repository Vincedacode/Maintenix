package org.example.Maintenix.Staff;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

// Add these imports to your existing StaffDashboardController
import org.example.Maintenix.Utils.UserSession;
import org.example.Maintenix.DAO.maintenancereportdao;
import org.bson.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class StaffDashboardController implements Initializable {

    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button devicesBtn;
    @FXML private Button historyBtn;
    @FXML private Button usageBtn;
    @FXML private Button profileBtn;
    @FXML private Button signOutBtn;
    @FXML private Button reportIssueBtn, requestBtn;
    @FXML private Label welcomeLabel;

    // Card elements for the three device status cards
    @FXML private Label printerTitleLabel;
    @FXML private Label printerStatusLabel;
    @FXML private ImageView printerImageView;

    @FXML private Label monitorTitleLabel;
    @FXML private Label monitorStatusLabel;
    @FXML private ImageView monitorImageView;

    @FXML private Label deskLampTitleLabel;
    @FXML private Label deskLampStatusLabel;
    @FXML private ImageView deskLampImageView;

    // Store the current username for passing to other controllers
    private String currentUsername;

    // DAO for database operations
    private maintenancereportdao maintenanceDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize DAO
        maintenanceDAO = new maintenancereportdao();

        setupButtonActions();
        setupButtonStyles();

        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            welcomeLabel.setText("Hello, " + session.getCurrentUsername());
            // Load maintenance reports data
            loadLatestMaintenanceReports(session.getCurrentUsername());
        }
    }

    /**
     * Load the latest 3 maintenance reports and populate the device cards
     */
    private void loadLatestMaintenanceReports(String username) {
        try {
            // Get maintenance reports for current user
            List<Document> reports = maintenanceDAO.getMaintenanceRequestsByUsername(username);

            // Sort by creation date (most recent first) and take only first 3
            reports.sort((r1, r2) -> {
                Date date1 = r1.getDate("created_at");
                Date date2 = r2.getDate("created_at");
                if (date1 == null && date2 == null) return 0;
                if (date1 == null) return 1;
                if (date2 == null) return -1;
                return date2.compareTo(date1); // Descending order
            });

            // Take only first 3 reports
            List<Document> latestReports = reports.subList(0, Math.min(3, reports.size()));

            // Populate cards with report data
            populateDeviceCards(latestReports);

        } catch (Exception e) {
            System.err.println("Error loading maintenance reports: " + e.getMessage());
            e.printStackTrace();
            // Show default data if there's an error
            setDefaultCardData();
        }
    }

    /**
     * Populate the three device cards with maintenance report data
     */
    private void populateDeviceCards(List<Document> reports) {
        // Card elements arrays for easier iteration
        Label[] titleLabels = {printerTitleLabel, monitorTitleLabel, deskLampTitleLabel};
        Label[] statusLabels = {printerStatusLabel, monitorStatusLabel, deskLampStatusLabel};
        ImageView[] imageViews = {printerImageView, monitorImageView, deskLampImageView};

        for (int i = 0; i < 3; i++) {
            if (i < reports.size()) {
                // Populate with report data
                Document report = reports.get(i);

                // Set title from issue or location
                String title = report.getString("issue");
                if (title == null || title.trim().isEmpty()) {
                    title = report.getString("location");
                }
                if (title == null || title.trim().isEmpty()) {
                    title = "Report";
                }
                titleLabels[i].setText(title);

                // Set status
                String status = report.getString("status");
                if (status == null || status.trim().isEmpty()) {
                    status = "Pending";
                }
                // Capitalize first letter
                status = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
                statusLabels[i].setText(status);

                // Set image if available
                Document imageDoc = (Document) report.get("image");
                if (imageDoc != null) {
                    String base64Data = imageDoc.getString("data");
                    if (base64Data != null && !base64Data.trim().isEmpty()) {
                        try {
                            // Decode base64 image
                            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                            Image image = new Image(bis);
                            imageViews[i].setImage(image);
                        } catch (Exception e) {
                            System.err.println("Error decoding image for report " + i + ": " + e.getMessage());
                            // Keep default image
                        }
                    }
                }

            } else {
                // Use placeholder data for remaining cards
                titleLabels[i].setText("Report");
                statusLabels[i].setText("No status");
                // Keep default images
            }
        }
    }

    /**
     * Set default data for cards when no reports are available
     */
    private void setDefaultCardData() {
        // This method sets placeholder values when no reports are found
        if (printerTitleLabel != null) printerTitleLabel.setText("Report");
        if (printerStatusLabel != null) printerStatusLabel.setText("No status");

        if (monitorTitleLabel != null) monitorTitleLabel.setText("Report");
        if (monitorStatusLabel != null) monitorStatusLabel.setText("No status");

        if (deskLampTitleLabel != null) deskLampTitleLabel.setText("Report");
        if (deskLampStatusLabel != null) deskLampStatusLabel.setText("No status");
    }

    public void setUsername(String username) {
        this.currentUsername = username;
        welcomeLabel.setText("Hello, " + username);
        // Reload maintenance reports when username is set
        loadLatestMaintenanceReports(username);
    }

    private void setupButtonActions() {
        // Navigation button actions
        dashboardBtn.setOnAction(e -> handleDashboardClick());
        devicesBtn.setOnAction(e -> handleDevicesClick());
        historyBtn.setOnAction(e -> handleHistoryClick());
        usageBtn.setOnAction(e -> handleUsageClick());
        profileBtn.setOnAction(e -> handleProfileClick());
        signOutBtn.setOnAction(e -> handleSignOutClick());
        reportIssueBtn.setOnAction(e -> handleReportIssueClick());
    }

    private void setupButtonStyles() {
        // Set dashboard as active by default
        setActiveNavButton(dashboardBtn);
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

    public void requestDevice(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/EquipmentRequest.fxml"));
            Parent root = loader.load();
            // If EquipmentRequest controller needs username, pass it here too
            Stage stage = (Stage) requestBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Navigation button handlers
    private void handleDashboardClick() {
        setActiveNavButton(dashboardBtn);
        System.out.println("Dashboard selected");
        // Refresh data when returning to dashboard
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            loadLatestMaintenanceReports(session.getCurrentUsername());
        }
    }

    private void handleDevicesClick() {
        setActiveNavButton(devicesBtn);
        System.out.println("Devices selected");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Devices.fxml"));
            Parent root = loader.load();
            // If Devices controller needs username, pass it here
            Stage stage = (Stage) devicesBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Maintenix - Devices");
        } catch (IOException e) {
            System.err.println("Error loading devices page: " + e.getMessage());
            showInfoDialog("Error", "Could not load Devices page");
        }
    }

    private void handleHistoryClick() {
        System.out.println("History selected - navigating to History page");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/HistoryPage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) historyBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Maintenix - History");
        } catch (IOException e) {
            System.err.println("Error loading history page: " + e.getMessage());
            showInfoDialog("Error", "Could not load History page");
        }
    }

    private void handleUsageClick() {
        setActiveNavButton(usageBtn);
        System.out.println("Usage selected");
        // Add navigation logic here
        showInfoDialog("Usage", "Navigate to Usage page");
    }

    private void handleProfileClick() {
        setActiveNavButton(profileBtn);
        System.out.println("Profile selected");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Profile.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) signOutBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleSignOutClick() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Sign Out");
        alert.setHeaderText("Are you sure you want to sign out?");
        alert.setContentText("You will be logged out of the application.");

        alert.showAndWait().ifPresent(response -> {
            if (response.getButtonData().isDefaultButton()) {
                // Clear user session
                UserSession.getInstance().clearSession();

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/View.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) signOutBtn.getScene().getWindow();
                    stage.setScene(new Scene(root));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void handleReportIssueClick() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Report Issue");
        alert.setHeaderText("Report an Issue");
        alert.setContentText("This would open the issue reporting form.");
        alert.showAndWait();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/MaintenanceReport.fxml"));
            Parent root = loader.load();
            // If MaintenanceReport controller needs username, pass it here
            Stage stage = (Stage) reportIssueBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Refresh dashboard data - can be called from other controllers
     */
    public void refreshDashboardData() {
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            loadLatestMaintenanceReports(session.getCurrentUsername());
        }
    }

    // Device status methods (can be called from other parts of the application)
    public void updatePrinterStatus(String status) {
        System.out.println("Printer status updated to: " + status);
        if (printerStatusLabel != null) {
            printerStatusLabel.setText(status);
        }
    }

    public void updateMonitorStatus(String status) {
        System.out.println("Monitor status updated to: " + status);
        if (monitorStatusLabel != null) {
            monitorStatusLabel.setText(status);
        }
    }

    public void updateDeskLampStatus(String status) {
        System.out.println("Desk lamp status updated to: " + status);
        if (deskLampStatusLabel != null) {
            deskLampStatusLabel.setText(status);
        }
    }

    public void toggleAirConditioner() {
        System.out.println("Air conditioner toggled");
        // Add AC toggle logic here
        showInfoDialog("Air Conditioner", "Air conditioner toggled");
    }

    public void toggleElectricFan() {
        System.out.println("Electric fan toggled");
        // Add fan toggle logic here
        showInfoDialog("Electric Fan", "Electric fan toggled");
    }

    public void triggerSystemUpdate() {
        System.out.println("System update triggered");
        // Add system update logic here
        showInfoDialog("System Update", "System update has been triggered. Reboot scheduled for 12:00 AM");
    }

    // Utility methods
    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to simulate real-time updates
    public void startStatusUpdates() {
        // This could be used to start a background thread that updates device statuses
        System.out.println("Starting real-time status updates");
    }
}