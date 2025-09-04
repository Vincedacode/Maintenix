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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.animation.RotateTransition;
import javafx.util.Duration;
import org.bson.Document;
import org.example.Maintenix.DAO.equipmentrequestdao;
import org.example.Maintenix.DAO.maintenancereportdao;
import org.example.Maintenix.DAO.staffdao;
import org.example.Maintenix.Utils.UserSession;

import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.Optional;
import java.io.File;

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
    @FXML private Label welcomeLabel;

    private equipmentrequestdao equipmentDAO;
    private maintenancereportdao reportDAO;
    private staffdao staffDAO;

    // Filter state
    private FilterCriteria currentFilter = new FilterCriteria();
    private List<Document> allRequests;
    private List<Document> allReports;

    // Inner class for filter criteria
    private static class FilterCriteria {
        String status = "All";
        String priority = "All";
        LocalDate startDate = null;
        LocalDate endDate = null;
        String type = "All"; // "All", "Requests", "Reports"

        public void reset() {
            status = "All";
            priority = "All";
            startDate = null;
            endDate = null;
            type = "All";
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        equipmentDAO = new equipmentrequestdao();
        reportDAO = new maintenancereportdao();
        staffDAO = new staffdao();

        setupButtonActions();
        setupButtonStyles();

        // Load data from user session
        loadUserData();
    }

    /**
     * Load data for the currently logged-in user
     */
    private void loadUserData() {
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            String username = session.getCurrentUsername();
            String fullName = session.getCurrentUserFullName();

            System.out.println("History Controller: Loading data for username " + username);

            // Update welcome message
            if (welcomeLabel != null) {
                welcomeLabel.setText("History for " + fullName + " (@" + username + ")");
            }

            // Load all data first, then apply current filter
            loadAllUserData(username);
            applyCurrentFilter();
        } else {
            System.err.println("No user logged in - showing empty state");
            showEmptyState();
        }
    }

    /**
     * Load all data for the user (unfiltered)
     */
    private void loadAllUserData(String username) {
        if (username == null || username.trim().isEmpty()) {
            System.err.println("No username provided - cannot load data");
            showEmptyState();
            return;
        }

        try {
            // Load all requests and reports
            allRequests = equipmentDAO.getEquipmentRequestsByUsername(username);
            allReports = reportDAO.getMaintenanceRequestsByUsername(username);

            System.out.println("Loaded " + allRequests.size() + " requests and " + allReports.size() + " reports");

        } catch (Exception e) {
            System.err.println("Error loading user data: " + e.getMessage());
            allRequests = List.of();
            allReports = List.of();
        }
    }

    /**
     * Apply current filter criteria to loaded data
     */
    private void applyCurrentFilter() {
        // Filter requests
        List<Document> filteredRequests = filterDocuments(allRequests, true);

        // Filter reports
        List<Document> filteredReports = filterDocuments(allReports, false);

        // Update UI based on type filter
        if ("Reports".equals(currentFilter.type)) {
            displayRequests(List.of()); // Empty requests
            displayReports(filteredReports);
        } else if ("Requests".equals(currentFilter.type)) {
            displayRequests(filteredRequests);
            displayReports(List.of()); // Empty reports
        } else { // "All"
            displayRequests(filteredRequests);
            displayReports(filteredReports);
        }
    }

    /**
     * Filter documents based on current criteria
     */
    private List<Document> filterDocuments(List<Document> documents, boolean isRequest) {
        return documents.stream().filter(doc -> {
            // Status filter
            if (!"All".equals(currentFilter.status)) {
                String status = doc.getString("status");
                if (status == null || !status.equalsIgnoreCase(currentFilter.status)) {
                    return false;
                }
            }

            // Priority filter
            if (!"All".equals(currentFilter.priority)) {
                String priority = doc.getString("priority");
                if (priority == null || !priority.equalsIgnoreCase(currentFilter.priority)) {
                    return false;
                }
            }

            // Date filter
            Date docDate = doc.getDate("created_at");
            if (docDate != null) {
                LocalDate docLocalDate = docDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

                if (currentFilter.startDate != null && docLocalDate.isBefore(currentFilter.startDate)) {
                    return false;
                }

                if (currentFilter.endDate != null && docLocalDate.isAfter(currentFilter.endDate)) {
                    return false;
                }
            }

            return true;
        }).collect(Collectors.toList());
    }

    /**
     * Display filtered requests in the UI
     */
    private void displayRequests(List<Document> requests) {
        requestsTableBody.getChildren().clear();

        if (requests.isEmpty()) {
            requestsTableBody.getChildren().add(createEmptyRow("No Equipment Requests Found"));
            updateRequestsCount(0);
        } else {
            // Sort requests by creation date (most recent first)
            List<Document> sortedRequests = requests.stream()
                    .sorted((doc1, doc2) -> {
                        Date date1 = doc1.getDate("created_at");
                        Date date2 = doc2.getDate("created_at");

                        // Handle null dates - put them at the end
                        if (date1 == null && date2 == null) return 0;
                        if (date1 == null) return 1;
                        if (date2 == null) return -1;

                        // Sort in descending order (most recent first)
                        return date2.compareTo(date1);
                    })
                    .collect(Collectors.toList());

            for (Document doc : sortedRequests) {
                HBox row = createRequestRow(doc);
                requestsTableBody.getChildren().add(row);
            }
            updateRequestsCount(sortedRequests.size());
        }
    }

    /**
     * Display filtered reports in the UI
     */
    private void displayReports(List<Document> reports) {
        reportsTableBody.getChildren().clear();

        if (reports.isEmpty()) {
            reportsTableBody.getChildren().add(createEmptyRow("No Maintenance Reports Found"));
            updateReportsCount(0);
        } else {
            // Sort reports by creation date (most recent first)
            List<Document> sortedReports = reports.stream()
                    .sorted((doc1, doc2) -> {
                        Date date1 = doc1.getDate("created_at");
                        Date date2 = doc2.getDate("created_at");

                        // Handle null dates - put them at the end
                        if (date1 == null && date2 == null) return 0;
                        if (date1 == null) return 1;
                        if (date2 == null) return -1;

                        // Sort in descending order (most recent first)
                        return date2.compareTo(date1);
                    })
                    .collect(Collectors.toList());

            for (Document doc : sortedReports) {
                HBox row = createReportRow(doc);
                reportsTableBody.getChildren().add(row);
            }
            updateReportsCount(sortedReports.size());
        }
    }

    /**
     * Show filter dialog
     */
    private void handleFilterClick() {
        Dialog<FilterCriteria> dialog = new Dialog<>();
        dialog.setTitle("Filter History");
        dialog.setHeaderText("Filter your maintenance history");

        // Create filter form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Type filter
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("All", "Requests", "Reports");
        typeBox.setValue(currentFilter.type);

        // Status filter
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("All", "Pending", "Approved", "Cancelled", "In Progress", "Completed", "Rejected");
        statusBox.setValue(currentFilter.status);

        // Priority filter
        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("All", "Low", "Medium", "High", "Urgent");
        priorityBox.setValue(currentFilter.priority);

        // Date filters
        DatePicker startDatePicker = new DatePicker(currentFilter.startDate);
        DatePicker endDatePicker = new DatePicker(currentFilter.endDate);

        // Add to grid
        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeBox, 1, 0);
        grid.add(new Label("Status:"), 0, 1);
        grid.add(statusBox, 1, 1);
        grid.add(new Label("Priority:"), 0, 2);
        grid.add(priorityBox, 1, 2);
        grid.add(new Label("Start Date:"), 0, 3);
        grid.add(startDatePicker, 1, 3);
        grid.add(new Label("End Date:"), 0, 4);
        grid.add(endDatePicker, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Add buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Add reset button
        ButtonType resetButtonType = new ButtonType("Reset");
        dialog.getDialogPane().getButtonTypes().add(resetButtonType);

        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                FilterCriteria criteria = new FilterCriteria();
                criteria.type = typeBox.getValue();
                criteria.status = statusBox.getValue();
                criteria.priority = priorityBox.getValue();
                criteria.startDate = startDatePicker.getValue();
                criteria.endDate = endDatePicker.getValue();
                return criteria;
            } else if (dialogButton == resetButtonType) {
                FilterCriteria criteria = new FilterCriteria();
                criteria.reset();
                return criteria;
            }
            return null;
        });

        // Show dialog and handle result
        Optional<FilterCriteria> result = dialog.showAndWait();
        result.ifPresent(criteria -> {
            currentFilter = criteria;
            applyCurrentFilter();

            // Show filter status
            String filterStatus = buildFilterStatusMessage();
            showInfoDialog("Filter Applied", filterStatus);
        });
    }

    /**
     * Build filter status message
     */
    private String buildFilterStatusMessage() {
        StringBuilder sb = new StringBuilder("Active filters: ");
        boolean hasFilters = false;

        if (!"All".equals(currentFilter.type)) {
            sb.append("Type: ").append(currentFilter.type).append(", ");
            hasFilters = true;
        }
        if (!"All".equals(currentFilter.status)) {
            sb.append("Status: ").append(currentFilter.status).append(", ");
            hasFilters = true;
        }
        if (!"All".equals(currentFilter.priority)) {
            sb.append("Priority: ").append(currentFilter.priority).append(", ");
            hasFilters = true;
        }
        if (currentFilter.startDate != null) {
            sb.append("From: ").append(currentFilter.startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append(", ");
            hasFilters = true;
        }
        if (currentFilter.endDate != null) {
            sb.append("To: ").append(currentFilter.endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append(", ");
            hasFilters = true;
        }

        if (!hasFilters) {
            return "No filters applied - showing all data";
        }

        // Remove trailing comma and space
        String result = sb.toString();
        if (result.endsWith(", ")) {
            result = result.substring(0, result.length() - 2);
        }

        return result;
    }

    /**
     * Handle export functionality
     */
    private void handleExportClick() {
        // Get current filtered data
        List<Document> filteredRequests = filterDocuments(allRequests, true);
        List<Document> filteredReports = filterDocuments(allReports, false);

        // Apply type filter
        if ("Reports".equals(currentFilter.type)) {
            filteredRequests = List.of();
        } else if ("Requests".equals(currentFilter.type)) {
            filteredReports = List.of();
        }

        if (filteredRequests.isEmpty() && filteredReports.isEmpty()) {
            showInfoDialog("Export Error", "No data to export. Please adjust your filters or refresh the data.");
            return;
        }

        // Show export format dialog
        Dialog<String> formatDialog = new Dialog<>();
        formatDialog.setTitle("Export Format");
        formatDialog.setHeaderText("Choose export format");

        VBox formatBox = new VBox(10);
        formatBox.setPadding(new Insets(20));

        ComboBox<String> formatCombo = new ComboBox<>();
        formatCombo.getItems().addAll("CSV", "JSON", "TXT");
        formatCombo.setValue("CSV");

        formatBox.getChildren().addAll(new Label("Export Format:"), formatCombo);
        formatDialog.getDialogPane().setContent(formatBox);
        formatDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        formatDialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return formatCombo.getValue();
            }
            return null;
        });

        Optional<String> formatResult = formatDialog.showAndWait();
        List<Document> finalFilteredRequests = filteredRequests;
        List<Document> finalFilteredReports = filteredReports;
        formatResult.ifPresent(format -> {
            // File chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Export File");
            fileChooser.setInitialFileName("maintenix_history_export." + format.toLowerCase());

            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                    format + " files (*." + format.toLowerCase() + ")", "*." + format.toLowerCase());
            fileChooser.getExtensionFilters().add(extFilter);

            Stage stage = (Stage) exportBtn.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                try {
                    exportData(file, format, finalFilteredRequests, finalFilteredReports);
                    showSuccessDialog("Export Successful",
                            "Data exported successfully to: " + file.getAbsolutePath());
                } catch (IOException e) {
                    showErrorDialog("Export Failed",
                            "Failed to export data: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Export data to file
     */
    private void exportData(File file, String format, List<Document> requests, List<Document> reports) throws IOException {
        UserSession session = UserSession.getInstance();
        String username = session.getCurrentUsername();
        String fullName = session.getCurrentUserFullName();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            switch (format.toUpperCase()) {
                case "CSV":
                    exportToCSV(writer, requests, reports, username, fullName);
                    break;
                case "JSON":
                    exportToJSON(writer, requests, reports, username, fullName);
                    break;
                case "TXT":
                    exportToTXT(writer, requests, reports, username, fullName);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported format: " + format);
            }
        }
    }

    private void exportToCSV(BufferedWriter writer, List<Document> requests, List<Document> reports,
                             String username, String fullName) throws IOException {
        // Header
        writer.write("# Maintenix History Export\n");
        writer.write("# User: " + fullName + " (" + username + ")\n");
        writer.write("# Export Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + "\n");
        writer.write("# Filters: " + buildFilterStatusMessage() + "\n\n");

        if (!requests.isEmpty()) {
            writer.write("Equipment Requests\n");
            writer.write("Item Name,Description,Priority,Status,Created Date\n");

            for (Document doc : requests) {
                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        escapeCSV(doc.getString("item_name")),
                        escapeCSV(doc.getString("description")),
                        escapeCSV(doc.getString("priority")),
                        escapeCSV(doc.getString("status")),
                        formatDate(doc.getDate("created_at"))
                ));
            }
            writer.write("\n");
        }

        if (!reports.isEmpty()) {
            writer.write("Maintenance Reports\n");
            writer.write("Issue,Location,Priority,Status,Created Date\n");

            for (Document doc : reports) {
                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        escapeCSV(doc.getString("issue")),
                        escapeCSV(doc.getString("location")),
                        escapeCSV(doc.getString("priority")),
                        escapeCSV(doc.getString("status")),
                        formatDate(doc.getDate("created_at"))
                ));
            }
        }
    }

    private void exportToJSON(BufferedWriter writer, List<Document> requests, List<Document> reports,
                              String username, String fullName) throws IOException {
        writer.write("{\n");
        writer.write("  \"export_info\": {\n");
        writer.write("    \"user\": \"" + fullName + " (" + username + ")\",\n");
        writer.write("    \"export_date\": \"" + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + "\",\n");
        writer.write("    \"filters\": \"" + buildFilterStatusMessage() + "\"\n");
        writer.write("  },\n");

        writer.write("  \"equipment_requests\": [\n");
        for (int i = 0; i < requests.size(); i++) {
            Document doc = requests.get(i);
            writer.write("    {\n");
            writer.write("      \"item_name\": \"" + escapeJSON(doc.getString("item_name")) + "\",\n");
            writer.write("      \"description\": \"" + escapeJSON(doc.getString("description")) + "\",\n");
            writer.write("      \"priority\": \"" + escapeJSON(doc.getString("priority")) + "\",\n");
            writer.write("      \"status\": \"" + escapeJSON(doc.getString("status")) + "\",\n");
            writer.write("      \"created_date\": \"" + formatDate(doc.getDate("created_at")) + "\"\n");
            writer.write("    }");
            if (i < requests.size() - 1) writer.write(",");
            writer.write("\n");
        }
        writer.write("  ],\n");

        writer.write("  \"maintenance_reports\": [\n");
        for (int i = 0; i < reports.size(); i++) {
            Document doc = reports.get(i);
            writer.write("    {\n");
            writer.write("      \"issue\": \"" + escapeJSON(doc.getString("issue")) + "\",\n");
            writer.write("      \"location\": \"" + escapeJSON(doc.getString("location")) + "\",\n");
            writer.write("      \"priority\": \"" + escapeJSON(doc.getString("priority")) + "\",\n");
            writer.write("      \"status\": \"" + escapeJSON(doc.getString("status")) + "\",\n");
            writer.write("      \"created_date\": \"" + formatDate(doc.getDate("created_at")) + "\"\n");
            writer.write("    }");
            if (i < reports.size() - 1) writer.write(",");
            writer.write("\n");
        }
        writer.write("  ]\n");
        writer.write("}\n");
    }

    private void exportToTXT(BufferedWriter writer, List<Document> requests, List<Document> reports,
                             String username, String fullName) throws IOException {
        writer.write("MAINTENIX HISTORY EXPORT\n");
        writer.write("========================\n\n");
        writer.write("User: " + fullName + " (" + username + ")\n");
        writer.write("Export Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + "\n");
        writer.write("Filters: " + buildFilterStatusMessage() + "\n\n");

        if (!requests.isEmpty()) {
            writer.write("EQUIPMENT REQUESTS (" + requests.size() + ")\n");
            writer.write("==================\n\n");

            for (int i = 0; i < requests.size(); i++) {
                Document doc = requests.get(i);
                writer.write((i + 1) + ". " + doc.getString("item_name") + "\n");
                writer.write("   Description: " + doc.getString("description") + "\n");
                writer.write("   Priority: " + doc.getString("priority") + "\n");
                writer.write("   Status: " + doc.getString("status") + "\n");
                writer.write("   Created: " + formatDate(doc.getDate("created_at")) + "\n\n");
            }
        }

        if (!reports.isEmpty()) {
            writer.write("MAINTENANCE REPORTS (" + reports.size() + ")\n");
            writer.write("===================\n\n");

            for (int i = 0; i < reports.size(); i++) {
                Document doc = reports.get(i);
                writer.write((i + 1) + ". " + doc.getString("issue") + "\n");
                writer.write("   Location: " + doc.getString("location") + "\n");
                writer.write("   Priority: " + doc.getString("priority") + "\n");
                writer.write("   Status: " + doc.getString("status") + "\n");
                writer.write("   Created: " + formatDate(doc.getDate("created_at")) + "\n\n");
            }
        }
    }

    // Utility methods for export
    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    private String escapeJSON(String value) {
        if (value == null) return "";
        return value.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private String formatDate(Date date) {
        if (date == null) return "Unknown";
        return date.toInstant().atZone(java.time.ZoneId.systemDefault())
                .toLocalDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }

    // [Rest of the existing methods remain the same...]

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
        Button viewBtn = createActionButton("View", "view-button");
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
        Button viewBtn = createActionButton("View", "view-button");
        viewBtn.setOnAction(e -> showReportDetails(doc));

        row.getChildren().addAll(issueLabel, locationLabel, dateLabel, priorityLabel, statusLabel, viewBtn);
        return row;
    }

    // Helper methods for creating UI elements
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
        UserSession session = UserSession.getInstance();
        String currentUsername = session.getCurrentUsername();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Equipment Request Details");
        alert.setHeaderText("Equipment: " + doc.getString("item_name"));

        String content = String.format(
                "Description: %s\n\n" +
                        "Priority: %s\n\n" +
                        "Status: %s\n\n" +
                        "Created: %s\n\n" +
                        "Requested by: %s",
                doc.getString("description") != null ? doc.getString("description") : "No description",
                doc.getString("priority") != null ? doc.getString("priority") : "Not specified",
                doc.getString("status") != null ? doc.getString("status") : "Unknown",
                doc.getDate("created_at") != null ?
                        doc.getDate("created_at").toInstant().atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "Unknown",
                currentUsername
        );

        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showReportDetails(Document doc) {
        UserSession session = UserSession.getInstance();
        String currentUsername = session.getCurrentUsername();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Maintenance Report Details");
        alert.setHeaderText("Issue: " + doc.getString("issue"));

        String content = String.format(
                "Location: %s\n\n" +
                        "Priority: %s\n\n" +
                        "Status: %s\n\n" +
                        "Created: %s\n\n" +
                        "Reported by: %s",
                doc.getString("location") != null ? doc.getString("location") : "Not specified",
                doc.getString("priority") != null ? doc.getString("priority") : "Not specified",
                doc.getString("status") != null ? doc.getString("status") : "Unknown",
                doc.getDate("created_at") != null ?
                        doc.getDate("created_at").toInstant().atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "Unknown",
                currentUsername
        );

        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showEmptyState() {
        requestsTableBody.getChildren().clear();
        reportsTableBody.getChildren().clear();
        requestsTableBody.getChildren().add(createEmptyRow("Please log in to view your history"));
        reportsTableBody.getChildren().add(createEmptyRow("Please log in to view your history"));
        updateRequestsCount(0);
        updateReportsCount(0);
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

        // Reset filters and reload all data
        currentFilter.reset();
        loadUserData();

        // Show success message
        showSuccessDialog("Refresh Complete", "Your history data has been refreshed and filters cleared!");
    }

    // Navigation handlers - All use UserSession now
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Devices.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) devicesBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Maintenix - Devices");
        } catch (IOException e) {
            System.err.println("Error loading devices page: " + e.getMessage());
            showInfoDialog("Error", "Could not load Devices page");
        }
    }

    private void handleHistoryClick() {
        setActiveNavButton(historyBtn);
        System.out.println("History selected - refreshing current page");
        handleRefreshClick(); // Just refresh the current page
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
            System.err.println("Error loading profile: " + e.getMessage());
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

    // Utility methods for dialogs
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

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("❌ " + title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}