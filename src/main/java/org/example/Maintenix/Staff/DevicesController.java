package org.example.Maintenix.Staff;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.Maintenix.DAO.equipmentrequestdao;
import org.example.Maintenix.DAO.maintenancereportdao;
import org.example.Maintenix.Utils.UserSession;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class DevicesController implements Initializable {

    @FXML private TableView<Device> deviceTable;
    @FXML private TableColumn<Device, String> deviceIdColumn;
    @FXML private TableColumn<Device, String> deviceNameColumn;
    @FXML private TableColumn<Device, String> statusColumn;

    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button devicesBtn;
    @FXML private Button historyBtn;
    @FXML private Button profileBtn;
    @FXML private Button signOutBtn;

    // Status labels
    @FXML private Label activeStatusLabel;
    @FXML private Label pendingStatusLabel;
    @FXML private Label welcomeLabel;

    private ObservableList<Device> deviceList = FXCollections.observableArrayList();
    private equipmentrequestdao equipmentDAO;
    private maintenancereportdao maintenanceDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize DAOs
        equipmentDAO = new equipmentrequestdao();
        maintenanceDAO = new maintenancereportdao();

        // Initialize table columns
        deviceIdColumn.setCellValueFactory(new PropertyValueFactory<>("deviceId"));
        deviceNameColumn.setCellValueFactory(new PropertyValueFactory<>("deviceName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Set up the table
        deviceTable.setItems(deviceList);

        // Load user data and populate table
        loadUserData();

        // Setup navigation
        setupButtonActions();
        setupButtonStyles();
    }

    private void loadUserData() {
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            String username = session.getCurrentUsername();

            // Set welcome message
            if (welcomeLabel != null) {
                welcomeLabel.setText("Welcome, " + username + "!");
            }

            // Load user's equipment requests
            loadEquipmentRequests(username);

            // Update status counts
            updateStatusCounts();
        }
    }

    private void loadEquipmentRequests(String username) {
        try {
            // Clear existing data
            deviceList.clear();

            // Get equipment requests for this user
            List<Document> equipmentRequests = equipmentDAO.getEquipmentRequestsByUsername(username);

            // Convert to Device objects and add to table
            for (Document request : equipmentRequests) {
                String deviceId = request.getObjectId("_id").toString().substring(18, 24); // Short ID
                String itemName = request.getString("item_name");
                String status = request.getString("status");
                String type = request.getString("type");

                // Create display name combining type and item name
                String displayName = (type != null ? type + " - " : "") + (itemName != null ? itemName : "Unknown Item");

                Device device = new Device(deviceId, displayName, status != null ? status : "unknown");
                deviceList.add(device);
            }

            System.out.println("Loaded " + deviceList.size() + " equipment requests for user: " + username);

        } catch (Exception e) {
            System.err.println("Error loading equipment requests: " + e.getMessage());
            e.printStackTrace();
            showInfoDialog("Error", "Failed to load your equipment requests.");
        }
    }

    private void updateStatusCounts() {
        int activeCount = 0;
        int pendingCount = 0;

        for (Device device : deviceList) {
            String status = device.getStatus().toLowerCase();
            switch (status) {
                case "active":
                case "approved":
                case "completed":
                    activeCount++;
                    break;
                case "pending":
                case "submitted":
                    pendingCount++;
                    break;
            }
        }

        // Update status labels if they exist
        if (activeStatusLabel != null) {
            activeStatusLabel.setText(String.valueOf(activeCount));
        }
        if (pendingStatusLabel != null) {
            pendingStatusLabel.setText(String.valueOf(pendingCount));
        }

        // If labels don't exist in FXML, print to console
        System.out.println("Status counts - Active: " + activeCount + ", Pending: " + pendingCount);
    }

    private void setupButtonActions() {
        // Navigation button actions
        dashboardBtn.setOnAction(e -> handleDashboardClick());
        devicesBtn.setOnAction(e -> handleDevicesClick());
        historyBtn.setOnAction(e -> handleHistoryClick());
        profileBtn.setOnAction(e -> handleProfileClick());
        signOutBtn.setOnAction(e -> handleSignOutClick());
    }

    private void setupButtonStyles() {
        // Set devices as active since we're on the devices page
        setActiveNavButton(devicesBtn);
    }

    private void setActiveNavButton(Button activeButton) {
        // Remove active class from all nav buttons
        Button[] navButtons = {dashboardBtn, devicesBtn, historyBtn, profileBtn, signOutBtn};

        for (Button button : navButtons) {
            button.getStyleClass().removeAll("nav-active");
        }

        // Add active class to the selected button
        if (!activeButton.getStyleClass().contains("nav-active")) {
            activeButton.getStyleClass().add("nav-active");
        }
    }

    // Navigation handlers
    private void handleDashboardClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/StaffDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) dashboardBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
        }
    }

    private void handleDevicesClick() {
        setActiveNavButton(devicesBtn);
        System.out.println("Devices selected - refreshing data");
        // Refresh data for current page
        loadUserData();
    }

    private void handleHistoryClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/HistoryPage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) historyBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Error loading history: " + e.getMessage());
        }
    }

    private void handleProfileClick() {
        setActiveNavButton(profileBtn);
        System.out.println("Profile selected");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Profile.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) historyBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Error loading history: " + e.getMessage());
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

    // Utility method for info dialogs
    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to refresh the table
    public void refreshTable() {
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            loadEquipmentRequests(session.getCurrentUsername());
            updateStatusCounts();
        }
        deviceTable.refresh();
    }

    // Method to add a new device (for external calls)
    public void addDevice(String deviceId, String deviceName, String status) {
        deviceList.add(new Device(deviceId, deviceName, status));
        updateStatusCounts();
    }

    // Method to clear all devices
    public void clearDevices() {
        deviceList.clear();
        updateStatusCounts();
    }

    // Original event handlers for backward compatibility
    @FXML
    private void handleRegisterDevice() {
        System.out.println("Register Device clicked - redirecting to equipment request");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/EquipmentRequest.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) devicesBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Error loading equipment request page: " + e.getMessage());
        }
    }

    @FXML
    private void handleReportIssue() {
        System.out.println("Report Issue clicked");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/MaintenanceReport.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) devicesBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Error loading maintenance report page: " + e.getMessage());
        }
    }

    // Legacy methods (can be removed if not used elsewhere)
    @FXML private void handleOverview() { handleDashboardClick(); }
    @FXML private void handleDevices() { handleDevicesClick(); }
    @FXML private void handleHistory() { handleHistoryClick(); }
    @FXML private void handleMyProfile() { handleProfileClick(); }
    @FXML private void handleSignOut() { handleSignOutClick(); }

    // Inner class for Device data model
    public static class Device {
        private String deviceId;
        private String deviceName;
        private String status;

        public Device(String deviceId, String deviceName, String status) {
            this.deviceId = deviceId;
            this.deviceName = deviceName;
            this.status = status;
        }

        // Getters and setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}