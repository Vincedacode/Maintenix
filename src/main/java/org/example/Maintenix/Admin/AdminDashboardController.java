package org.example.Maintenix.Admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.scene.input.MouseEvent;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.Maintenix.DAO.equipmentrequestdao;
import org.example.Maintenix.DAO.maintenancereportdao;
import org.example.Maintenix.DAO.staffdao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.github.cdimascio.dotenv.Dotenv;
import org.example.Maintenix.DBConnection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.text.SimpleDateFormat;

public class AdminDashboardController implements Initializable {

    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button statisticsBtn;
    @FXML private Button notificationsBtn;
    @FXML private Button logoutBtn;

    // Device Request Table
    @FXML private TableView<DeviceRequest> deviceRequestTable;
    @FXML private TableColumn<DeviceRequest, String> deviceCol;
    @FXML private TableColumn<DeviceRequest, String> dateCol;
    @FXML private TableColumn<DeviceRequest, String> priorityCol;

    // Maintenance Request Table
    @FXML private TableView<MaintenanceRequest> maintenanceRequestTable;
    @FXML private TableColumn<MaintenanceRequest, String> mainDeviceCol;
    @FXML private TableColumn<MaintenanceRequest, String> mainDateCol;
    @FXML private TableColumn<MaintenanceRequest, String> mainPriorityCol;
    @FXML private TableColumn<MaintenanceRequest, String> actionCol;

    // Action buttons
    @FXML private Button printBtn1;
    @FXML private Button shareBtn1;
    @FXML private Button printBtn2;
    @FXML private Button shareBtn2;
    @FXML private Button historyBtn;
    @FXML private Button addUpdateBtn;
    @FXML private Hyperlink showAllLink;

    private ObservableList<DeviceRequest> deviceRequests;
    private ObservableList<MaintenanceRequest> maintenanceRequests;

    // DAO objects
    private equipmentrequestdao equipmentDAO;
    private maintenancereportdao maintenanceDAO;
    private staffdao staffDAO;

    // MongoDB collections for direct updates
    private MongoCollection<Document> requestsCollection;
    private MongoCollection<Document> reportsCollection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeDAOs();
        loadDataFromDatabase();
        setupDeviceRequestTable();
        setupMaintenanceRequestTable();
        setupEventHandlers();
        setupTableSelectionHandlers();
    }

    private void initializeDAOs() {
        try {
            equipmentDAO = new equipmentrequestdao();
            maintenanceDAO = new maintenancereportdao();
            staffDAO = new staffdao();

            // Initialize MongoDB collections for direct updates
            Dotenv dotenv = Dotenv.load();
            String dbname = dotenv.get("DB_NAME");
            String requestsCollectionName = dotenv.get("REQUESTS_COLLECTION_NAME");
            String reportsCollectionName = dotenv.get("REPORTS_COLLECTION_NAME");

            requestsCollection = DBConnection.createConnection(dbname, requestsCollectionName);
            reportsCollection = DBConnection.createConnection(dbname, reportsCollectionName);
        } catch (Exception e) {
            System.err.println("Error initializing DAOs: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to database");
        }
    }

    private void loadDataFromDatabase() {
        deviceRequests = FXCollections.observableArrayList();
        maintenanceRequests = FXCollections.observableArrayList();

        try {
            // Load equipment requests
            List<Document> equipmentDocs = equipmentDAO.getAllEquipmentRequests();
            for (Document doc : equipmentDocs) {
                String deviceName = doc.getString("item_name");
                ObjectId staffId = doc.getObjectId("staff_name");
                String staffName = staffDAO.getFullNameByUsername(getStaffUsernameById(staffId));
                if (staffName == null) staffName = "Unknown Staff";

                Date createdAt = doc.getDate("created_at");
                String dateStr = formatDate(createdAt);
                String timeStr = formatTime(createdAt);

                String priority = doc.getString("priority");
                if (priority == null) priority = "LOW";

                String status = doc.getString("status");
                if ("completed".equalsIgnoreCase(status)) {
                    priority = "Done";
                }

                ObjectId requestId = doc.getObjectId("_id");

                DeviceRequest request = new DeviceRequest(
                        deviceName,
                        staffName,
                        dateStr,
                        timeStr,
                        priority.toUpperCase(),
                        requestId.toString()
                );
                deviceRequests.add(request);
            }

            // Load maintenance reports
            List<Document> maintenanceDocs = getAllMaintenanceReports();
            for (Document doc : maintenanceDocs) {
                String issue = doc.getString("issue");
                ObjectId staffId = doc.getObjectId("staff_name");
                String staffName = staffDAO.getFullNameByUsername(getStaffUsernameById(staffId));
                if (staffName == null) staffName = "Unknown Staff";

                Date createdAt = doc.getDate("created_at");
                String dateStr = formatDate(createdAt);
                String timeStr = formatTime(createdAt);

                String priority = doc.getString("priority");
                if (priority == null) priority = "LOW";

                String status = doc.getString("status");
                if (status == null) status = "pending";

                String action = mapStatusToAction(status);
                ObjectId reportId = doc.getObjectId("_id");

                MaintenanceRequest request = new MaintenanceRequest(
                        issue,
                        staffName,
                        dateStr,
                        timeStr,
                        priority.toUpperCase(),
                        action,
                        reportId.toString()
                );
                maintenanceRequests.add(request);
            }

        } catch (Exception e) {
            System.err.println("Error loading data from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getStaffUsernameById(ObjectId staffId) {
        try {
            Document query = new Document("_id", staffId);
            Document staff = DBConnection.createConnection(
                    Dotenv.load().get("DB_NAME"),
                    Dotenv.load().get("STAFF_COLLECTION_NAME")
            ).find(query).first();

            if (staff != null) {
                return staff.getString("Username");
            }
        } catch (Exception e) {
            System.err.println("Error getting staff username: " + e.getMessage());
        }
        return null;
    }

    private List<Document> getAllMaintenanceReports() {
        List<Document> reports = new ArrayList<>();
        try {
            reports = reportsCollection.find().into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("Error retrieving maintenance reports: " + e.getMessage());
        }
        return reports;
    }

    private String formatDate(Date date) {
        if (date == null) return "Unknown";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = sdf.format(date);

        SimpleDateFormat todayFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = todayFormat.format(new Date());

        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);
        String yesterdayStr = todayFormat.format(yesterday.getTime());

        if (dateStr.equals(today)) {
            return "Today";
        } else if (dateStr.equals(yesterdayStr)) {
            return "Yesterday";
        } else {
            return new SimpleDateFormat("MMM dd").format(date);
        }
    }

    private String formatTime(Date date) {
        if (date == null) return "Unknown";

        long diff = System.currentTimeMillis() - date.getTime();
        long minutes = diff / (60 * 1000);
        long hours = diff / (60 * 60 * 1000);
        long days = diff / (24 * 60 * 60 * 1000);

        if (minutes < 60) {
            return minutes + "m ago";
        } else if (hours < 24) {
            return hours + "h ago";
        } else if (days < 7) {
            return days + "d ago";
        } else {
            return new SimpleDateFormat("HH:mm").format(date);
        }
    }

    private String mapStatusToAction(String status) {
        if (status == null) return "IDLE";

        switch (status.toLowerCase()) {
            case "completed":
            case "done":
                return "Done";
            case "in_progress":
            case "processing":
                return "Pending";
            case "pending":
            case "idle":
            default:
                return "IDLE";
        }
    }

    private void setupTableSelectionHandlers() {
        // Device Request Table selection handler
        deviceRequestTable.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 1) {
                DeviceRequest selected = deviceRequestTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showUpdateDialog(selected, true);
                }
            }
        });

        // Maintenance Request Table selection handler
        maintenanceRequestTable.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 1) {
                MaintenanceRequest selected = maintenanceRequestTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showUpdateDialog(selected, false);
                }
            }
        });
    }

    private void showUpdateDialog(Object request, boolean isDeviceRequest) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update Request");
        dialog.setHeaderText("Update Priority and Status");

        // Create the dialog content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("HIGH", "MEDIUM", "LOW");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("pending", "in_progress", "completed");

        if (isDeviceRequest) {
            DeviceRequest deviceReq = (DeviceRequest) request;
            Label infoLabel = new Label("Device: " + deviceReq.getDevice() + "\nRequested by: " + deviceReq.getRequester());

            // Set current values
            String currentPriority = deviceReq.getPriority();
            if (!"Done".equals(currentPriority)) {
                priorityCombo.setValue(currentPriority);
            } else {
                priorityCombo.setValue("LOW");
                statusCombo.setValue("completed");
            }

            content.getChildren().addAll(
                    infoLabel,
                    new Label("Priority:"),
                    priorityCombo,
                    new Label("Status:"),
                    statusCombo
            );
        } else {
            MaintenanceRequest mainReq = (MaintenanceRequest) request;
            Label infoLabel = new Label("Issue: " + mainReq.getDevice() + "\nReported by: " + mainReq.getRequester());

            priorityCombo.setValue(mainReq.getPriority());

            // Map action back to status
            String currentAction = mainReq.getAction();
            if ("Done".equals(currentAction)) {
                statusCombo.setValue("completed");
            } else if ("Pending".equals(currentAction)) {
                statusCombo.setValue("in_progress");
            } else {
                statusCombo.setValue("pending");
            }

            content.getChildren().addAll(
                    infoLabel,
                    new Label("Priority:"),
                    priorityCombo,
                    new Label("Status:"),
                    statusCombo
            );
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String newPriority = priorityCombo.getValue();
            String newStatus = statusCombo.getValue();

            if (isDeviceRequest) {
                DeviceRequest deviceReq = (DeviceRequest) request;
                updateDeviceRequestInDatabase(deviceReq.getRequestId(), newPriority, newStatus);
            } else {
                MaintenanceRequest mainReq = (MaintenanceRequest) request;
                updateMaintenanceRequestInDatabase(mainReq.getReportId(), newPriority, newStatus);
            }

            // Reload data
            loadDataFromDatabase();
            deviceRequestTable.refresh();
            maintenanceRequestTable.refresh();
        }
    }

    private void updateDeviceRequestInDatabase(String requestId, String priority, String status) {
        try {
            ObjectId id = new ObjectId(requestId);
            requestsCollection.updateOne(
                    Filters.eq("_id", id),
                    Updates.combine(
                            Updates.set("priority", priority.toLowerCase()),
                            Updates.set("status", status)
                    )
            );
            showAlert(Alert.AlertType.INFORMATION, "Success", "Device request updated successfully");
        } catch (Exception e) {
            System.err.println("Error updating device request: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update device request");
        }
    }

    private void updateMaintenanceRequestInDatabase(String reportId, String priority, String status) {
        try {
            ObjectId id = new ObjectId(reportId);
            reportsCollection.updateOne(
                    Filters.eq("_id", id),
                    Updates.combine(
                            Updates.set("priority", priority.toLowerCase()),
                            Updates.set("status", status)
                    )
            );
            showAlert(Alert.AlertType.INFORMATION, "Success", "Maintenance request updated successfully");
        } catch (Exception e) {
            System.err.println("Error updating maintenance request: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update maintenance request");
        }
    }

    private void setupDeviceRequestTable() {
        deviceCol.setCellValueFactory(new PropertyValueFactory<>("device"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));

        // Custom cell factory for device column to show device name and requester
        deviceCol.setCellFactory(new Callback<TableColumn<DeviceRequest, String>, TableCell<DeviceRequest, String>>() {
            @Override
            public TableCell<DeviceRequest, String> call(TableColumn<DeviceRequest, String> param) {
                return new TableCell<DeviceRequest, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || getTableRow().getItem() == null) {
                            setGraphic(null);
                        } else {
                            DeviceRequest request = (DeviceRequest) getTableRow().getItem();
                            VBox vbox = new VBox(2);
                            Label deviceLabel = new Label(request.getDevice());
                            deviceLabel.getStyleClass().add("device-name");
                            Label requesterLabel = new Label(request.getRequester());
                            requesterLabel.getStyleClass().add("requester-name");
                            vbox.getChildren().addAll(deviceLabel, requesterLabel);
                            setGraphic(vbox);
                        }
                    }
                };
            }
        });

        // Custom cell factory for date column
        dateCol.setCellFactory(new Callback<TableColumn<DeviceRequest, String>, TableCell<DeviceRequest, String>>() {
            @Override
            public TableCell<DeviceRequest, String> call(TableColumn<DeviceRequest, String> param) {
                return new TableCell<DeviceRequest, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || getTableRow().getItem() == null) {
                            setGraphic(null);
                        } else {
                            DeviceRequest request = (DeviceRequest) getTableRow().getItem();
                            VBox vbox = new VBox(2);
                            Label dateLabel = new Label(request.getDate());
                            dateLabel.getStyleClass().add("date-text");
                            Label timeLabel = new Label(request.getTime());
                            timeLabel.getStyleClass().add("time-text");
                            vbox.getChildren().addAll(dateLabel, timeLabel);
                            setGraphic(vbox);
                        }
                    }
                };
            }
        });

        // Custom cell factory for priority column
        priorityCol.setCellFactory(new Callback<TableColumn<DeviceRequest, String>, TableCell<DeviceRequest, String>>() {
            @Override
            public TableCell<DeviceRequest, String> call(TableColumn<DeviceRequest, String> param) {
                return new TableCell<DeviceRequest, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            getStyleClass().removeAll("priority-high", "priority-medium", "priority-low", "status-done");
                            switch (item.toUpperCase()) {
                                case "HIGH":
                                    getStyleClass().add("priority-high");
                                    break;
                                case "MEDIUM":
                                    getStyleClass().add("priority-medium");
                                    break;
                                case "LOW":
                                    getStyleClass().add("priority-low");
                                    break;
                                case "DONE":
                                    getStyleClass().add("status-done");
                                    break;
                            }
                        }
                    }
                };
            }
        });

        deviceRequestTable.setItems(deviceRequests);
    }

    private void setupMaintenanceRequestTable() {
        mainDeviceCol.setCellValueFactory(new PropertyValueFactory<>("device"));
        mainDateCol.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        mainPriorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        actionCol.setCellValueFactory(new PropertyValueFactory<>("action"));

        // Custom cell factory for device column
        mainDeviceCol.setCellFactory(new Callback<TableColumn<MaintenanceRequest, String>, TableCell<MaintenanceRequest, String>>() {
            @Override
            public TableCell<MaintenanceRequest, String> call(TableColumn<MaintenanceRequest, String> param) {
                return new TableCell<MaintenanceRequest, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || getTableRow().getItem() == null) {
                            setGraphic(null);
                        } else {
                            MaintenanceRequest request = (MaintenanceRequest) getTableRow().getItem();
                            VBox vbox = new VBox(2);
                            Label deviceLabel = new Label(request.getDevice());
                            deviceLabel.getStyleClass().add("device-name");
                            Label requesterLabel = new Label(request.getRequester());
                            requesterLabel.getStyleClass().add("requester-name");
                            vbox.getChildren().addAll(deviceLabel, requesterLabel);
                            setGraphic(vbox);
                        }
                    }
                };
            }
        });

        // Custom cell factory for date column
        mainDateCol.setCellFactory(new Callback<TableColumn<MaintenanceRequest, String>, TableCell<MaintenanceRequest, String>>() {
            @Override
            public TableCell<MaintenanceRequest, String> call(TableColumn<MaintenanceRequest, String> param) {
                return new TableCell<MaintenanceRequest, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || getTableRow().getItem() == null) {
                            setGraphic(null);
                        } else {
                            MaintenanceRequest request = (MaintenanceRequest) getTableRow().getItem();
                            VBox vbox = new VBox(2);
                            Label dateLabel = new Label(request.getDate());
                            dateLabel.getStyleClass().add("date-text");
                            Label timeLabel = new Label(request.getTime());
                            timeLabel.getStyleClass().add("time-text");
                            vbox.getChildren().addAll(dateLabel, timeLabel);
                            setGraphic(vbox);
                        }
                    }
                };
            }
        });

        // Custom cell factory for priority column
        mainPriorityCol.setCellFactory(new Callback<TableColumn<MaintenanceRequest, String>, TableCell<MaintenanceRequest, String>>() {
            @Override
            public TableCell<MaintenanceRequest, String> call(TableColumn<MaintenanceRequest, String> param) {
                return new TableCell<MaintenanceRequest, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            getStyleClass().removeAll("priority-high", "priority-medium", "priority-low");
                            switch (item.toUpperCase()) {
                                case "HIGH":
                                    getStyleClass().add("priority-high");
                                    break;
                                case "MEDIUM":
                                    getStyleClass().add("priority-medium");
                                    break;
                                case "LOW":
                                    getStyleClass().add("priority-low");
                                    break;
                            }
                        }
                    }
                };
            }
        });

        // Custom cell factory for action column
        actionCol.setCellFactory(new Callback<TableColumn<MaintenanceRequest, String>, TableCell<MaintenanceRequest, String>>() {
            @Override
            public TableCell<MaintenanceRequest, String> call(TableColumn<MaintenanceRequest, String> param) {
                return new TableCell<MaintenanceRequest, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            getStyleClass().removeAll("status-done", "status-idle", "status-pending");
                            switch (item.toUpperCase()) {
                                case "DONE":
                                    getStyleClass().add("status-done");
                                    break;
                                case "IDLE":
                                    getStyleClass().add("status-idle");
                                    break;
                                case "PENDING":
                                    getStyleClass().add("status-pending");
                                    break;
                            }
                        }
                    }
                };
            }
        });

        maintenanceRequestTable.setItems(maintenanceRequests);
    }

    private void setupEventHandlers() {
        // Navigation buttons
        dashboardBtn.setOnAction(e -> handleDashboardClick());
        statisticsBtn.setOnAction(e -> handleStatisticsClick());
        notificationsBtn.setOnAction(e -> handleNotificationsClick());
        logoutBtn.setOnAction(e -> handleLogoutClick());

        // Action buttons
        printBtn1.setOnAction(e -> exportDeviceRequestsToCSV());
        shareBtn1.setOnAction(e -> handleShareDeviceRequests());
        printBtn2.setOnAction(e -> exportMaintenanceRequestsToCSV());
        shareBtn2.setOnAction(e -> handleShareMaintenanceRequests());
        historyBtn.setOnAction(e -> handleHistoryClick());
        addUpdateBtn.setOnAction(e -> handleAddUpdateClick());
        showAllLink.setOnAction(e -> handleShowAllRequestsClick());
    }

    private void exportDeviceRequestsToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Device Requests as CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("device_requests_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

        File file = fileChooser.showSaveDialog(deviceRequestTable.getScene().getWindow());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write header
                writer.append("Device,Requester,Date,Time,Priority\n");

                // Write data
                for (DeviceRequest request : deviceRequests) {
                    writer.append(escapeCSV(request.getDevice())).append(",");
                    writer.append(escapeCSV(request.getRequester())).append(",");
                    writer.append(escapeCSV(request.getDate())).append(",");
                    writer.append(escapeCSV(request.getTime())).append(",");
                    writer.append(escapeCSV(request.getPriority())).append("\n");
                }

                showAlert(Alert.AlertType.INFORMATION, "Export Successful",
                        "Device requests exported to: " + file.getAbsolutePath());

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Export Failed",
                        "Failed to export device requests: " + e.getMessage());
            }
        }
    }

    private void exportMaintenanceRequestsToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Maintenance Requests as CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("maintenance_requests_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

        File file = fileChooser.showSaveDialog(maintenanceRequestTable.getScene().getWindow());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write header
                writer.append("Issue,Reporter,Date,Time,Priority,Status\n");

                // Write data
                for (MaintenanceRequest request : maintenanceRequests) {
                    writer.append(escapeCSV(request.getDevice())).append(",");
                    writer.append(escapeCSV(request.getRequester())).append(",");
                    writer.append(escapeCSV(request.getDate())).append(",");
                    writer.append(escapeCSV(request.getTime())).append(",");
                    writer.append(escapeCSV(request.getPriority())).append(",");
                    writer.append(escapeCSV(request.getAction())).append("\n");
                }

                showAlert(Alert.AlertType.INFORMATION, "Export Successful",
                        "Maintenance requests exported to: " + file.getAbsolutePath());

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Export Failed",
                        "Failed to export maintenance requests: " + e.getMessage());
            }
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // Event handlers
    private void handleDashboardClick() {
        System.out.println("Dashboard clicked");
        updateActiveNavButton(dashboardBtn);
        // Refresh data when dashboard is clicked
        loadDataFromDatabase();
    }

    private void handleStatisticsClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Statistics.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) historyBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Maintenix - Statistics");
        } catch (IOException e) {
            System.err.println("Error loading history page: " + e.getMessage());
        }
        updateActiveNavButton(statisticsBtn);
        // Navigate to statistics view - implement navigation logic here
    }

    private void handleNotificationsClick() {
        System.out.println("Notifications clicked");
        updateActiveNavButton(notificationsBtn);
        // Navigate to notifications view - implement navigation logic here
    }

    private void handleLogoutClick() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to logout?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Implement logout logic here
            System.out.println("User logged out");
            // Navigate to login screen
        }
    }

    private void handleShareDeviceRequests() {
        showAlert(Alert.AlertType.INFORMATION, "Share",
                "Share functionality for device requests - implement sharing logic here");
    }

    private void handleShareMaintenanceRequests() {
        showAlert(Alert.AlertType.INFORMATION, "Share",
                "Share functionality for maintenance requests - implement sharing logic here");
    }

    private void handleHistoryClick() {
        System.out.println("History clicked");
        // Implement history view - could show archived requests
        showAlert(Alert.AlertType.INFORMATION, "History",
                "History view - showing all completed/archived requests");
    }

    private void handleAddUpdateClick() {
        System.out.println("Add Update clicked");
        // Refresh data from database
        loadDataFromDatabase();
        deviceRequestTable.refresh();
        maintenanceRequestTable.refresh();
        showAlert(Alert.AlertType.INFORMATION, "Data Refreshed",
                "All tables have been updated with latest data from database");
    }

    private void handleShowAllRequestsClick() {
        System.out.println("Show all requests clicked");
        // Could implement pagination or show expanded view
        showAlert(Alert.AlertType.INFORMATION, "Show All",
                "Showing all maintenance requests - " + maintenanceRequests.size() + " total");
    }

    private void updateActiveNavButton(Button activeButton) {
        // Remove active class from all nav buttons
        dashboardBtn.getStyleClass().remove("nav-active");
        statisticsBtn.getStyleClass().remove("nav-active");
        notificationsBtn.getStyleClass().remove("nav-active");

        // Add active class to clicked button
        activeButton.getStyleClass().add("nav-active");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Data classes
    public static class DeviceRequest {
        private final SimpleStringProperty device;
        private final SimpleStringProperty requester;
        private final SimpleStringProperty date;
        private final SimpleStringProperty time;
        private final SimpleStringProperty priority;
        private final SimpleStringProperty dateTime;
        private final String requestId;

        public DeviceRequest(String device, String requester, String date, String time, String priority, String requestId) {
            this.device = new SimpleStringProperty(device);
            this.requester = new SimpleStringProperty(requester);
            this.date = new SimpleStringProperty(date);
            this.time = new SimpleStringProperty(time);
            this.priority = new SimpleStringProperty(priority);
            this.dateTime = new SimpleStringProperty(date + " " + time);
            this.requestId = requestId;
        }

        public String getDevice() { return device.get(); }
        public void setDevice(String device) { this.device.set(device); }
        public SimpleStringProperty deviceProperty() { return device; }

        public String getRequester() { return requester.get(); }
        public void setRequester(String requester) { this.requester.set(requester); }
        public SimpleStringProperty requesterProperty() { return requester; }

        public String getDate() { return date.get(); }
        public void setDate(String date) { this.date.set(date); }
        public SimpleStringProperty dateProperty() { return date; }

        public String getTime() { return time.get(); }
        public void setTime(String time) { this.time.set(time); }
        public SimpleStringProperty timeProperty() { return time; }

        public String getPriority() { return priority.get(); }
        public void setPriority(String priority) { this.priority.set(priority); }
        public SimpleStringProperty priorityProperty() { return priority; }

        public String getDateTime() { return dateTime.get(); }
        public SimpleStringProperty dateTimeProperty() { return dateTime; }

        public String getRequestId() { return requestId; }
    }

    public static class MaintenanceRequest {
        private final SimpleStringProperty device;
        private final SimpleStringProperty requester;
        private final SimpleStringProperty date;
        private final SimpleStringProperty time;
        private final SimpleStringProperty priority;
        private final SimpleStringProperty action;
        private final SimpleStringProperty dateTime;
        private final String reportId;

        public MaintenanceRequest(String device, String requester, String date, String time, String priority, String action, String reportId) {
            this.device = new SimpleStringProperty(device);
            this.requester = new SimpleStringProperty(requester);
            this.date = new SimpleStringProperty(date);
            this.time = new SimpleStringProperty(time);
            this.priority = new SimpleStringProperty(priority);
            this.action = new SimpleStringProperty(action);
            this.dateTime = new SimpleStringProperty(date + " " + time);
            this.reportId = reportId;
        }

        public String getDevice() { return device.get(); }
        public void setDevice(String device) { this.device.set(device); }
        public SimpleStringProperty deviceProperty() { return device; }

        public String getRequester() { return requester.get(); }
        public void setRequester(String requester) { this.requester.set(requester); }
        public SimpleStringProperty requesterProperty() { return requester; }

        public String getDate() { return date.get(); }
        public void setDate(String date) { this.date.set(date); }
        public SimpleStringProperty dateProperty() { return date; }

        public String getTime() { return time.get(); }
        public void setTime(String time) { this.time.set(time); }
        public SimpleStringProperty timeProperty() { return time; }

        public String getPriority() { return priority.get(); }
        public void setPriority(String priority) { this.priority.set(priority); }
        public SimpleStringProperty priorityProperty() { return priority; }

        public String getAction() { return action.get(); }
        public void setAction(String action) { this.action.set(action); }
        public SimpleStringProperty actionProperty() { return action; }

        public String getDateTime() { return dateTime.get(); }
        public SimpleStringProperty dateTimeProperty() { return dateTime; }

        public String getReportId() { return reportId; }
    }
}