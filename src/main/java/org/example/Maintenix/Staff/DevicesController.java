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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DevicesController implements Initializable {

    @FXML
    private TableView<Device> deviceTable;

    @FXML
    private TableColumn<Device, String> deviceIdColumn;

    @FXML
    private TableColumn<Device, String> deviceNameColumn;

    @FXML
    private TableColumn<Device, String> statusColumn;

    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button devicesBtn;
    @FXML private Button historyBtn;
    @FXML private Button profileBtn;
    @FXML private Button signOutBtn;

    private ObservableList<Device> deviceList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize table columns
        deviceIdColumn.setCellValueFactory(new PropertyValueFactory<>("deviceId"));
        deviceNameColumn.setCellValueFactory(new PropertyValueFactory<>("deviceName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Set up the table with empty data for now
        deviceTable.setItems(deviceList);

        // Add some sample data (you can remove this and populate from your data source)
        addSampleData();

        // Setup navigation
        setupButtonActions();
        setupButtonStyles();
    }

    private void addSampleData() {
        // Add some empty rows to match the design
        deviceList.addAll(
                new Device("Hi", "sls", "pending"),
                new Device("", "", ""),
                new Device("", "", ""),
                new Device("", "", "")
        );
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
        System.out.println("Devices selected - already on this page");
        // Optionally refresh device data here
        refreshTable();
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
        showInfoDialog("Profile", "Navigate to Profile page");
    }

    private void handleSignOutClick() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Sign Out");
        alert.setHeaderText("🚪 Are you sure you want to sign out?");
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

    // Utility method for info dialogs
    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to add a new device
    public void addDevice(String deviceId, String deviceName, String status) {
        deviceList.add(new Device(deviceId, deviceName, status));
    }

    // Method to clear all devices
    public void clearDevices() {
        deviceList.clear();
    }

    // Method to refresh the table
    public void refreshTable() {
        deviceTable.refresh();
    }

    // Original event handlers for other buttons (keeping existing functionality)
    @FXML
    private void handleRegisterDevice() {
        System.out.println("Register Device clicked");
        // Implement your register device logic here
    }

    @FXML
    private void handleReportIssue() {
        System.out.println("Report Issue clicked");
        // Implement your report issue logic here
    }

    @FXML
    private void handleOverview() {
        System.out.println("Overview clicked");
        // This can be removed as it's now handled by handleDashboardClick()
        handleDashboardClick();
    }

    @FXML
    private void handleDevices() {
        System.out.println("Devices clicked");
        // This can be removed as it's now handled by handleDevicesClick()
        handleDevicesClick();
    }

    @FXML
    private void handleHistory() {
        System.out.println("History clicked");
        // This can be removed as it's now handled by handleHistoryClick()
        handleHistoryClick();
    }

    @FXML
    private void handleMyProfile() {
        System.out.println("My Profile clicked");
        // This can be removed as it's now handled by handleProfileClick()
        handleProfileClick();
    }

    @FXML
    private void handleSignOut() {
        System.out.println("Sign Out clicked");
        // This can be removed as it's now handled by handleSignOutClick()
        handleSignOutClick();
    }

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
        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}