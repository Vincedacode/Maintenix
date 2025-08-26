package org.example.Maintenix.Staff;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HistoryController implements Initializable {

    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button devicesBtn;
    @FXML private Button historyBtn;
    @FXML private Button profileBtn;
    @FXML private Button signOutBtn;
    @FXML private Button filterBtn;
    @FXML private Button exportBtn;
    @FXML private VBox historyTableBody;

    // History data structure
    public static class HistoryRecord {
        private String device;
        private String issue;
        private LocalDate date;
        private String status;

        public HistoryRecord(String device, String issue, LocalDate date, String status) {
            this.device = device;
            this.issue = issue;
            this.date = date;
            this.status = status;
        }

        // Getters
        public String getDevice() { return device; }
        public String getIssue() { return issue; }
        public LocalDate getDate() { return date; }
        public String getStatus() { return status; }

        public String getFormattedDate() {
            return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupButtonActions();
        setupButtonStyles();
        loadHistoryData();
    }

    private void setupButtonActions() {
        // Navigation button actions
        dashboardBtn.setOnAction(e -> handleDashboardClick());
        devicesBtn.setOnAction(e -> handleDevicesClick());
        historyBtn.setOnAction(e -> handleHistoryClick());
        profileBtn.setOnAction(e -> handleProfileClick());
        signOutBtn.setOnAction(e -> handleSignOutClick());
        filterBtn.setOnAction(e -> handleFilterClick());
        exportBtn.setOnAction(e -> handleExportClick());
    }

    private void setupButtonStyles() {
        // Set history as active
        setActiveNavButton(historyBtn);
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

    // Load history data from database
    private void loadHistoryData() {
        List<HistoryRecord> historyRecords = fetchHistoryFromDatabase();

        // Clear existing rows
        historyTableBody.getChildren().clear();

        if (historyRecords.isEmpty()) {
            showEmptyState();
        } else {
            for (HistoryRecord record : historyRecords) {
                HBox row = createHistoryRow(record);
                historyTableBody.getChildren().add(row);
            }
        }
    }

    // Simulate database fetch - replace with actual database call
    private List<HistoryRecord> fetchHistoryFromDatabase() {
        List<HistoryRecord> records = new ArrayList<>();

        // Sample data - replace with actual database query
        records.add(new HistoryRecord("Printer", "Paper jam issue",
                LocalDate.of(2025, 8, 20), "Resolved"));
        records.add(new HistoryRecord("Monitor", "Display flickering",
                LocalDate.of(2025, 8, 18), "Resolved"));
        records.add(new HistoryRecord("Keyboard", "Keys not responsive",
                LocalDate.of(2025, 8, 15), "Resolved"));
        records.add(new HistoryRecord("Desk Lamp", "Bulb replacement needed",
                LocalDate.of(2025, 8, 12), "Resolved"));
        records.add(new HistoryRecord("Scanner", "Scanning quality poor",
                LocalDate.of(2025, 8, 10), "Resolved"));
        records.add(new HistoryRecord("Mouse", "Left click not working",
                LocalDate.of(2025, 8, 8), "Pending"));
        records.add(new HistoryRecord("Printer", "Ink cartridge empty",
                LocalDate.of(2025, 8, 5), "Active"));

        return records;
    }

    private HBox createHistoryRow(HistoryRecord record) {
        HBox row = new HBox();
        row.getStyleClass().add("table-row");

        // Device name
        Label deviceLabel = new Label(record.getDevice());
        deviceLabel.getStyleClass().addAll("row-cell", "device-cell");

        // Issue description
        Label issueLabel = new Label(record.getIssue());
        issueLabel.getStyleClass().addAll("row-cell", "issue-cell");

        // Date
        Label dateLabel = new Label(record.getFormattedDate());
        dateLabel.getStyleClass().addAll("row-cell", "date-cell");

        // Status badge
        Label statusLabel = new Label(record.getStatus());
        statusLabel.getStyleClass().addAll("status-cell", getStatusStyle(record.getStatus()));

        // Action button
        Button viewBtn = new Button("View");
        viewBtn.getStyleClass().add("view-btn");
        viewBtn.setOnAction(e -> handleViewRecord(record));

        row.getChildren().addAll(deviceLabel, issueLabel, dateLabel, statusLabel, viewBtn);

        return row;
    }

    private String getStatusStyle(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                return "status-pending";
            case "active":
                return "status-active";
            case "resolved":
                return "status-resolved";
            case "failed":
                return "status-failed";
            default:
                return "";
        }
    }

    private void showEmptyState() {
        VBox emptyState = new VBox();
        emptyState.getStyleClass().add("empty-state");

        ImageView emptyIcon = new ImageView();
        try {
            Image image = new Image(getClass().getResourceAsStream("/images/logo.png"));
            emptyIcon.setImage(image);
        } catch (Exception e) {
            // Handle missing image
        }
        emptyIcon.setFitWidth(80);
        emptyIcon.setFitHeight(80);
        emptyIcon.getStyleClass().add("empty-state-icon");

        Label emptyText = new Label("No History Available");
        emptyText.getStyleClass().add("empty-state-text");

        Label emptySubtext = new Label("Device maintenance history will appear here");
        emptySubtext.getStyleClass().add("empty-state-subtext");

        emptyState.getChildren().addAll(emptyIcon, emptyText, emptySubtext);
        historyTableBody.getChildren().add(emptyState);
    }

    private void handleViewRecord(HistoryRecord record) {
        System.out.println("Viewing record: " + record.getDevice() + " - " + record.getIssue());

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Record Details");
        alert.setHeaderText(record.getDevice() + " Maintenance Record");
        alert.setContentText(
                "Device: " + record.getDevice() + "\n" +
                        "Issue: " + record.getIssue() + "\n" +
                        "Date: " + record.getFormattedDate() + "\n" +
                        "Status: " + record.getStatus()
        );
        alert.showAndWait();
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
        System.out.println("Devices selected");
        showInfoDialog("Devices", "Navigate to Devices page");
    }

    private void handleHistoryClick() {
        setActiveNavButton(historyBtn);
        System.out.println("History selected - already on this page");
        // Refresh history data
        loadHistoryData();
    }

    private void handleProfileClick() {
        setActiveNavButton(profileBtn);
        System.out.println("Profile selected");
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

    private void handleFilterClick() {
        System.out.println("Filter clicked");
        showInfoDialog("Filter", "This would open filter options for history records");
    }

    private void handleExportClick() {
        System.out.println("Export clicked");
        showInfoDialog("Export", "This would export history data to CSV/PDF");
    }

    // Utility methods
    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Public method to refresh history data
    public void refreshHistory() {
        loadHistoryData();
    }

    // Method to add new history record
    public void addHistoryRecord(HistoryRecord record) {
        System.out.println("Adding new history record: " + record.getDevice());
        // This would typically update the database first
        loadHistoryData(); // Refresh display
    }
}