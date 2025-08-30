package org.example.Maintenix.Admin;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeData();
        setupDeviceRequestTable();
        setupMaintenanceRequestTable();
        setupEventHandlers();
    }

    private void initializeData() {
        // Sample data for Device Requests
        deviceRequests = FXCollections.observableArrayList(
                new DeviceRequest("Mouse", "Daniel Levy", "Today", "2m ago", "HIGH"),
                new DeviceRequest("Printer", "Leo Messi", "Today", "5m ago", "HIGH"),
                new DeviceRequest("Printer Ink", "Babatunde Rema", "Today", "1h ago", "MEDIUM"),
                new DeviceRequest("Keyboard", "Kunle Remi", "Today", "2h ago", "Done")
        );

        // Sample data for Maintenance Requests
        maintenanceRequests = FXCollections.observableArrayList(
                new MaintenanceRequest("Faulty Printer", "Daniel Levy", "Today", "2m ago", "HIGH", "IDLE"),
                new MaintenanceRequest("Bad Monitor", "Leo Messi", "Today", "5m ago", "HIGH", "IDLE"),
                new MaintenanceRequest("Lamps are out", "Babatunde Rema", "Today", "1h ago", "LOW", "Done"),
                new MaintenanceRequest("Faulty Mouse", "Tolu David", "Today", "2h ago", "MEDIUM", "Pending"),
                new MaintenanceRequest("Faulty Scanner", "Ibrahim Kabiru", "Yesterday", "09:00 AM", "LOW", "Done"),
                new MaintenanceRequest("Printer is Stuck", "Grace Emma", "Yesterday", "08:00 AM", "MEDIUM", "Done")
        );
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
        printBtn1.setOnAction(e -> handlePrintDeviceRequests());
        shareBtn1.setOnAction(e -> handleShareDeviceRequests());
        printBtn2.setOnAction(e -> handlePrintMaintenanceRequests());
        shareBtn2.setOnAction(e -> handleShareMaintenanceRequests());
        historyBtn.setOnAction(e -> handleHistoryClick());
        addUpdateBtn.setOnAction(e -> handleAddUpdateClick());
        showAllLink.setOnAction(e -> handleShowAllRequestsClick());
    }

    // Event handlers
    private void handleDashboardClick() {
        System.out.println("Dashboard clicked");
        // Navigate to dashboard view
    }

    private void handleStatisticsClick() {
        System.out.println("Statistics clicked");
        // Navigate to statistics view
        updateActiveNavButton(statisticsBtn);
    }

    private void handleNotificationsClick() {
        System.out.println("Notifications clicked");
        // Navigate to notifications view
        updateActiveNavButton(notificationsBtn);
    }

    private void handleLogoutClick() {
        System.out.println("Logout clicked");
        // Handle logout logic
    }

    private void handlePrintDeviceRequests() {
        System.out.println("Printing device requests...");
        // Implement print functionality for device requests
    }

    private void handleShareDeviceRequests() {
        System.out.println("Sharing device requests...");
        // Implement share functionality for device requests
    }

    private void handlePrintMaintenanceRequests() {
        System.out.println("Printing maintenance requests...");
        // Implement print functionality for maintenance requests
    }

    private void handleShareMaintenanceRequests() {
        System.out.println("Sharing maintenance requests...");
        // Implement share functionality for maintenance requests
    }

    private void handleHistoryClick() {
        System.out.println("History clicked");
        // Navigate to history view
    }

    private void handleAddUpdateClick() {
        System.out.println("Add Update clicked");
        // Open add update dialog
    }

    private void handleShowAllRequestsClick() {
        System.out.println("Show all requests clicked");
        // Show expanded view of all maintenance requests
    }

    private void updateActiveNavButton(Button activeButton) {
        // Remove active class from all nav buttons
        dashboardBtn.getStyleClass().remove("nav-active");
        statisticsBtn.getStyleClass().remove("nav-active");
        notificationsBtn.getStyleClass().remove("nav-active");

        // Add active class to clicked button
        activeButton.getStyleClass().add("nav-active");
    }

    // Data classes
    public static class DeviceRequest {
        private final SimpleStringProperty device;
        private final SimpleStringProperty requester;
        private final SimpleStringProperty date;
        private final SimpleStringProperty time;
        private final SimpleStringProperty priority;
        private final SimpleStringProperty dateTime;

        public DeviceRequest(String device, String requester, String date, String time, String priority) {
            this.device = new SimpleStringProperty(device);
            this.requester = new SimpleStringProperty(requester);
            this.date = new SimpleStringProperty(date);
            this.time = new SimpleStringProperty(time);
            this.priority = new SimpleStringProperty(priority);
            this.dateTime = new SimpleStringProperty(date + " " + time);
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
    }

    public static class MaintenanceRequest {
        private final SimpleStringProperty device;
        private final SimpleStringProperty requester;
        private final SimpleStringProperty date;
        private final SimpleStringProperty time;
        private final SimpleStringProperty priority;
        private final SimpleStringProperty action;
        private final SimpleStringProperty dateTime;

        public MaintenanceRequest(String device, String requester, String date, String time, String priority, String action) {
            this.device = new SimpleStringProperty(device);
            this.requester = new SimpleStringProperty(requester);
            this.date = new SimpleStringProperty(date);
            this.time = new SimpleStringProperty(time);
            this.priority = new SimpleStringProperty(priority);
            this.action = new SimpleStringProperty(action);
            this.dateTime = new SimpleStringProperty(date + " " + time);
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
    }
}