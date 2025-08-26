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
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class StaffDashboardController implements Initializable {

    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button devicesBtn;
    @FXML private Button historyBtn;
    @FXML private Button usageBtn;
    @FXML private Button profileBtn;
    @FXML private Button signOutBtn;
    @FXML private Button reportIssueBtn;
    @FXML
    private Label welcomeLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupButtonActions();
        setupButtonStyles();
    }

    public void setUsername(String username) {
        welcomeLabel.setText("Hello, " + username);
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

    // Navigation button handlers
    private void handleDashboardClick() {
        setActiveNavButton(dashboardBtn);
        System.out.println("Dashboard selected");
        // Stay on current page since this is the dashboard
    }

    private void handleDevicesClick() {
        setActiveNavButton(devicesBtn);
        System.out.println("Devices selected");
        // Add navigation logic here
        showInfoDialog("Devices", "Navigate to Devices page");
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
        // Add navigation logic here
        showInfoDialog("Profile", "Navigate to Profile page");
    }

    private void handleSignOutClick() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Sign Out");
        alert.setHeaderText("Are you sure you want to sign out?");
        alert.setContentText("You will be logged out of the application.");

        alert.showAndWait().ifPresent(response -> {
            if (response.getButtonData().isDefaultButton()) {
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

        System.out.println("Report issue clicked");
        // Add issue reporting logic here
    }

    // Device status methods (can be called from other parts of the application)
    public void updatePrinterStatus(String status) {
        System.out.println("Printer status updated to: " + status);
        // Update printer status in the UI
    }

    public void updateMonitorStatus(String status) {
        System.out.println("Monitor status updated to: " + status);
        // Update monitor status in the UI
    }

    public void updateDeskLampStatus(String status) {
        System.out.println("Desk lamp status updated to: " + status);
        // Update desk lamp status in the UI
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

        // Example: Update statuses periodically
        // Timer timer = new Timer(true);
        // timer.scheduleAtFixedRate(new TimerTask() {
        //     @Override
        //     public void run() {
        //         Platform.runLater(() -> {
        //             // Update UI elements here
        //         });
        //     }
        // }, 0, 30000); // Update every 30 seconds
    }
}