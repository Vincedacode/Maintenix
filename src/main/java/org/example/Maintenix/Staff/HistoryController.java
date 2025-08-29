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
import javafx.geometry.Pos;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.util.Duration;
import org.bson.Document;
import org.example.Maintenix.DAO.equipmentrequestdao;
import org.example.Maintenix.DAO.maintenancereportdao;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
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
    @FXML private Button refreshBtn;

    @FXML private VBox requestsTableBody;
    @FXML private VBox reportsTableBody;
    @FXML private Label requestsCountLabel;
    @FXML private Label reportsCountLabel;

    private static equipmentrequestdao equipmentDAO;
    private static maintenancereportdao reportDAO;

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
        equipmentDAO = new equipmentrequestdao();
        reportDAO = new maintenancereportdao();

        setupButtonActions();
        setupButtonStyles();

        loadAllData();
    }

    private void loadAllData() {
        loadRequestsData();
        loadReportsData();
    }

    private void loadRequestsData() {
        requestsTableBody.getChildren().clear();
        List<Document> requests = equipmentDAO.getAllEquipmentRequests();

        if (requests.isEmpty()) {
            requestsTableBody.getChildren().add(createEmptyRow("No Equipment Requests Found"));
            updateRequestsCount(0);
        } else {
            for (Document doc : requests) {
                HBox row = createRequestRow(doc);
                requestsTableBody.getChildren().add(row);
            }
            updateRequestsCount(requests.size());
        }
    }

    private void loadReportsData() {
        reportsTableBody.getChildren().clear();
        List<Document> reports = reportDAO.getAllMaintenanceRequests();

        if (reports.isEmpty()) {
            reportsTableBody.getChildren().add(createEmptyRow("No Maintenance Reports Found"));
            updateReportsCount(0);
        } else {
            for (Document doc : reports) {
                HBox row = createReportRow(doc);
                reportsTableBody.getChildren().add(row);
            }
            updateReportsCount(reports.size());
        }
    }

    private HBox createRequestRow(Document doc) {
        HBox row = new HBox(15);
        row.getStyleClass().add("table-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 15, 12, 15));

        String itemName = doc.getString("item_name") != null ? doc.getString("item_name") : "N/A";
        String description = doc.getString("description") != null ? doc.getString("description") : "No description";
        String priority = doc.getString("priority") != null ? doc.getString("priority") : "medium";
        String status = doc.getString("status") != null ? doc.getString("status") : "pending";
        Date createdAt = doc.getDate("created_at");
        String formattedDate = createdAt != null ?
                createdAt.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        .format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "Unknown";

        // Create styled labels
        Label itemLabel = createStyledLabel(itemName, "item-name-label");
        itemLabel.setPrefWidth(120);

        Label descLabel = createStyledLabel(truncateText(description, 30), "description-label");
        descLabel.setPrefWidth(200);

        Label dateLabel = createStyledLabel(formattedDate, "date-label");
        dateLabel.setPrefWidth(100);

        // Priority badge
        Label priorityLabel = createPriorityBadge(priority);

        // Status badge
        Label statusLabel = createStatusBadge(status);

        // Action button
        Button viewBtn = createActionButton("👁 View", "view-button");
        viewBtn.setOnAction(e -> showRequestDetails(doc));

        row.getChildren().addAll(itemLabel, descLabel, dateLabel, priorityLabel, statusLabel, viewBtn);
        return row;
    }

    private HBox createReportRow(Document doc) {
        HBox row = new HBox(15);
        row.getStyleClass().add("table-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 15, 12, 15));

        String issue = doc.getString("issue") != null ? doc.getString("issue") : "N/A";
        String location = doc.getString("location") != null ? doc.getString("location") : "Unknown";
        String priority = doc.getString("priority") != null ? doc.getString("priority") : "medium";
        String status = doc.getString("status") != null ? doc.getString("status") : "pending";
        Date createdAt = doc.getDate("created_at");
        String formattedDate = createdAt != null ?
                createdAt.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        .format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "Unknown";

        // Create styled labels
        Label issueLabel = createStyledLabel(truncateText(issue, 25), "issue-label");
        issueLabel.setPrefWidth(150);

        Label locationLabel = createStyledLabel(location, "location-label");
        locationLabel.setPrefWidth(120);

        Label dateLabel = createStyledLabel(formattedDate, "date-label");
        dateLabel.setPrefWidth(100);

        // Priority badge
        Label priorityLabel = createPriorityBadge(priority);

        // Status badge
        Label statusLabel = createStatusBadge(status);

        // Action button
        Button viewBtn = createActionButton("👁 View", "view-button");
        viewBtn.setOnAction(e -> showReportDetails(doc));

        row.getChildren().addAll(issueLabel, locationLabel, dateLabel, priorityLabel, statusLabel, viewBtn);
        return row;
    }

    private Label createStyledLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }

    private Label createStatusBadge(String status) {
        Label badge = new Label(status.toUpperCase());
        badge.getStyleClass().addAll("status-badge", getStatusStyle(status));
        badge.setPrefWidth(80);
        badge.setAlignment(Pos.CENTER);
        return badge;
    }

    private Label createPriorityBadge(String priority) {
        Label badge = new Label(priority.toUpperCase());
        badge.getStyleClass().addAll("priority-badge", getPriorityStyle(priority));
        badge.setPrefWidth(70);
        badge.setAlignment(Pos.CENTER);
        return badge;
    }

    private Button createActionButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        return button;
    }

    private String getStatusStyle(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                return "status-pending";
            case "approved":
            case "active":
            case "resolved":
            case "completed":
                return "status-approved";
            case "cancelled":
            case "rejected":
            case "failed":
                return "status-cancelled";
            case "in progress":
            case "processing":
                return "status-in-progress";
            default:
                return "status-default";
        }
    }

    private String getPriorityStyle(String priority) {
        switch (priority.toLowerCase()) {
            case "high":
            case "urgent":
                return "priority-high";
            case "medium":
                return "priority-medium";
            case "low":
                return "priority-low";
            default:
                return "priority-medium";
        }
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "N/A";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

    private HBox createEmptyRow(String message) {
        HBox row = new HBox();
        row.getStyleClass().add("empty-row");
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(40, 20, 40, 20));

        VBox emptyContent = new VBox(10);
        emptyContent.setAlignment(Pos.CENTER);

        Label emptyIcon = new Label("📋");
        emptyIcon.getStyleClass().add("empty-icon");

        Label emptyText = new Label(message);
        emptyText.getStyleClass().add("empty-text");

        emptyContent.getChildren().addAll(emptyIcon, emptyText);
        row.getChildren().add(emptyContent);

        return row;
    }

    private void updateRequestsCount(int count) {
        if (requestsCountLabel != null) {
            requestsCountLabel.setText("(" + count + ")");
        }
    }

    private void updateReportsCount(int count) {
        if (reportsCountLabel != null) {
            reportsCountLabel.setText("(" + count + ")");
        }
    }

    private void showRequestDetails(Document doc) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Equipment Request Details");
        alert.setHeaderText("🔧 " + doc.getString("item_name"));

        String content = String.format(
                "📝 Description: %s\n\n" +
                        "⚡ Priority: %s\n\n" +
                        "📊 Status: %s\n\n" +
                        "📅 Created: %s",
                doc.getString("description") != null ? doc.getString("description") : "No description",
                doc.getString("priority") != null ? doc.getString("priority") : "Not specified",
                doc.getString("status") != null ? doc.getString("status") : "Unknown",
                doc.getDate("created_at") != null ?
                        doc.getDate("created_at").toInstant().atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "Unknown"
        );

        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showReportDetails(Document doc) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Maintenance Report Details");
        alert.setHeaderText("🔧 " + doc.getString("issue"));

        String content = String.format(
                "📍 Location: %s\n\n" +
                        "⚡ Priority: %s\n\n" +
                        "📊 Status: %s\n\n" +
                        "📅 Created: %s",
                doc.getString("location") != null ? doc.getString("location") : "Not specified",
                doc.getString("priority") != null ? doc.getString("priority") : "Not specified",
                doc.getString("status") != null ? doc.getString("status") : "Unknown",
                doc.getDate("created_at") != null ?
                        doc.getDate("created_at").toInstant().atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "Unknown"
        );

        alert.setContentText(content);
        alert.showAndWait();
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
        refreshBtn.setOnAction(e -> handleRefreshClick());
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

    private void handleRefreshClick() {
        // Add loading animation to refresh button
        RotateTransition rotate = new RotateTransition(Duration.seconds(1), refreshBtn);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setCycleCount(1);
        rotate.play();

        // Add fade animation to content
        FadeTransition fade = new FadeTransition(Duration.millis(300), requestsTableBody.getParent());
        fade.setFromValue(1.0);
        fade.setToValue(0.7);
        fade.setAutoReverse(true);
        fade.setCycleCount(2);
        fade.play();

        // Reload data
        loadAllData();

        // Show success message
        showSuccessDialog("Refresh Complete", "History data has been refreshed successfully!");
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

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Devices.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) historyBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Maintenix - Devices");
        } catch (IOException e) {
            System.err.println("Error loading history page: " + e.getMessage());
            showInfoDialog("Error", "Could not load Devices page");
        }
    }

    private void handleHistoryClick() {
        setActiveNavButton(historyBtn);
        System.out.println("History selected - already on this page");
        handleRefreshClick(); // Refresh when clicking history
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

    private void handleProfileClick() {
        setActiveNavButton(profileBtn);
        System.out.println("Profile selected");
        showInfoDialog("Profile", "Navigate to Profile page");
    }

    private void handleSignOutClick() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
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

    private void handleFilterClick() {
        System.out.println("Filter clicked");
        showInfoDialog("Filter", "🔍 Filter options will allow you to sort by status, priority, and date range");
    }

    private void handleExportClick() {
        System.out.println("Export clicked");
        showSuccessDialog("Export", "📊 History data would be exported to CSV/PDF format");
    }

    // Utility methods
    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessDialog(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("✅ " + title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}