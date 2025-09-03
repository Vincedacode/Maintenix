package org.example.Maintenix.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.bson.Document;
import org.example.Maintenix.DAO.equipmentrequestdao;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.example.Maintenix.Utils.AdminSession;

public class StatisticsController implements Initializable {

    @FXML
    private PieChart priorityChart;

    @FXML
    private BarChart<String, Number> inactiveDevicesChart;

    @FXML
    private BarChart<String, Number> activeDevicesChart;

    @FXML
    private CategoryAxis inactiveXAxis;

    @FXML
    private NumberAxis inactiveYAxis;

    @FXML
    private CategoryAxis activeXAxis;

    @FXML
    private NumberAxis activeYAxis;

    // Statistics cards
    @FXML
    private Label totalDevicesValue;

    @FXML
    private Label inactivePercentageValue;

    @FXML
    private Label inactiveDevicesValue;

    // Period selection buttons
    @FXML
    private Button fromDateBtn;

    @FXML
    private Button toDateBtn;

    // Navigation buttons
    @FXML
    private Button dashboardBtn;

    @FXML
    private Button notificationsBtn;

    @FXML
    private Button logoutBtn;

    private equipmentrequestdao equipmentDAO;
    private LocalDate fromDate;
    private LocalDate toDate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        equipmentDAO = new equipmentrequestdao();

        // Set default date range (current year)
        fromDate = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        toDate = LocalDate.of(LocalDate.now().getYear(), 12, 31);

        updateDateButtons();
        loadDataAndUpdateCharts();
        setupEventHandlers();
    }

    private void handleDashboardClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/AdminDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) dashboardBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Maintenix - Dashboard");
        } catch (IOException e) {
            System.err.println("Error loading history page: " + e.getMessage());
        }

    }



    private void setupEventHandlers() {
        // Period selection handlers
        fromDateBtn.setOnAction(e -> selectFromDate());
        toDateBtn.setOnAction(e -> selectToDate());

        // Navigation handlers (you can implement these based on your navigation logic)
        dashboardBtn.setOnAction(e -> handleDashboardClick());
        notificationsBtn.setOnAction(e -> navigateToNotifications());
        logoutBtn.setOnAction(e -> logout());
    }

    private void selectFromDate() {
        // Simple date selection - you can implement a DatePicker dialog here
        // For now, let's cycle through some preset options
        if (fromDate.getYear() == LocalDate.now().getYear()) {
            fromDate = LocalDate.of(LocalDate.now().getYear() - 1, 1, 1);
        } else {
            fromDate = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        }
        updateDateButtons();
        loadDataAndUpdateCharts();
    }

    private void selectToDate() {
        // Simple date selection - you can implement a DatePicker dialog here
        if (toDate.equals(LocalDate.of(LocalDate.now().getYear(), 12, 31))) {
            toDate = LocalDate.now();
        } else {
            toDate = LocalDate.of(LocalDate.now().getYear(), 12, 31);
        }
        updateDateButtons();
        loadDataAndUpdateCharts();
    }

    private void updateDateButtons() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        fromDateBtn.setText("from " + fromDate.format(formatter));
        toDateBtn.setText("to " + toDate.format(formatter));
    }

    private void loadDataAndUpdateCharts() {
        try {
            // Get all equipment requests
            List<Document> allRequests = equipmentDAO.getAllEquipmentRequests();

            // Filter requests by date range
            List<Document> filteredRequests = filterRequestsByDateRange(allRequests);

            // Update statistics cards
            updateStatisticsCards(filteredRequests);

            // Update charts
            updatePriorityChart(filteredRequests);
            updateActiveDevicesChart(filteredRequests);
            updateInactiveDevicesChart(filteredRequests);

        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Document> filterRequestsByDateRange(List<Document> requests) {
        Date fromDateAsDate = Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date toDateAsDate = Date.from(toDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        return requests.stream()
                .filter(request -> {
                    Date createdAt = request.getDate("created_at");
                    return createdAt != null &&
                            !createdAt.before(fromDateAsDate) &&
                            !createdAt.after(toDateAsDate);
                })
                .collect(Collectors.toList());
    }

    private void updateStatisticsCards(List<Document> requests) {
        // Total devices (all equipment requests)
        int totalDevices = requests.size();

        // Inactive devices (requests that are not completed)
        long inactiveDevices = requests.stream()
                .filter(request -> !"completed".equalsIgnoreCase(request.getString("status")))
                .count();

        // Calculate percentage
        double inactivePercentage = totalDevices > 0 ? (double) inactiveDevices / totalDevices * 100 : 0;

        // Update UI labels (assuming you have these in your FXML)
        updateLabel("totalDevicesValue", String.valueOf(totalDevices));
        updateLabel("inactivePercentageValue", String.format("%.1f%%", inactivePercentage));
        updateLabel("inactiveDevicesValue", String.valueOf(inactiveDevices));
    }

    private void updateLabel(String labelId, String value) {
        // Helper method to safely update labels
        try {
            switch (labelId) {
                case "totalDevicesValue":
                    if (totalDevicesValue != null) totalDevicesValue.setText(value);
                    break;
                case "inactivePercentageValue":
                    if (inactivePercentageValue != null) inactivePercentageValue.setText(value);
                    break;
                case "inactiveDevicesValue":
                    if (inactiveDevicesValue != null) inactiveDevicesValue.setText(value);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error updating label " + labelId + ": " + e.getMessage());
        }
    }

    private void updatePriorityChart(List<Document> requests) {
        // Count requests by priority
        Map<String, Long> priorityCounts = requests.stream()
                .collect(Collectors.groupingBy(
                        request -> request.getString("priority") != null ?
                                request.getString("priority").toLowerCase() : "low",
                        Collectors.counting()
                ));

        // Create pie chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        long highCount = priorityCounts.getOrDefault("high", 0L);
        long mediumCount = priorityCounts.getOrDefault("medium", 0L);
        long lowCount = priorityCounts.getOrDefault("low", 0L);

        if (highCount > 0) pieChartData.add(new PieChart.Data("High", highCount));
        if (mediumCount > 0) pieChartData.add(new PieChart.Data("Medium", mediumCount));
        if (lowCount > 0) pieChartData.add(new PieChart.Data("Low", lowCount));

        priorityChart.setData(pieChartData);
        priorityChart.setTitle("");
        priorityChart.setLegendVisible(false); // Hide default legend since we have custom one
        priorityChart.setLabelsVisible(false);
        priorityChart.setStartAngle(90);

        // Apply styles after data is set
        applyCSSStylesToPieChart();
    }

    private void applyCSSStylesToPieChart() {
        priorityChart.applyCss();
        priorityChart.layout();

        // Apply custom colors to pie chart slices
        for (int i = 0; i < priorityChart.getData().size(); i++) {
            PieChart.Data data = priorityChart.getData().get(i);
            String dataName = data.getName().toLowerCase();

            switch (dataName) {
                case "high":
                    data.getNode().setStyle("-fx-pie-color: #1976D2;"); // Blue
                    break;
                case "medium":
                    data.getNode().setStyle("-fx-pie-color: #333333;"); // Black
                    break;
                case "low":
                    data.getNode().setStyle("-fx-pie-color: #42A5F5;"); // Light Blue
                    break;
            }
        }
    }

    private void updateActiveDevicesChart(List<Document> requests) {
        // Filter completed requests (active devices)
        List<Document> activeRequests = requests.stream()
                .filter(request -> "completed".equalsIgnoreCase(request.getString("status")))
                .collect(Collectors.toList());

        updateBarChart(activeDevicesChart, activeRequests, activeXAxis, activeYAxis, "Active");
    }

    private void updateInactiveDevicesChart(List<Document> requests) {
        // Filter non-completed requests (inactive devices)
        List<Document> inactiveRequests = requests.stream()
                .filter(request -> !"completed".equalsIgnoreCase(request.getString("status")))
                .collect(Collectors.toList());

        updateBarChart(inactiveDevicesChart, inactiveRequests, inactiveXAxis, inactiveYAxis, "Inactive");
    }

    private void updateBarChart(BarChart<String, Number> chart, List<Document> requests,
                                CategoryAxis xAxis, NumberAxis yAxis, String chartType) {
        // Group requests by month
        Map<String, Long> monthlyData = requests.stream()
                .filter(request -> request.getDate("created_at") != null)
                .collect(Collectors.groupingBy(
                        request -> {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(request.getDate("created_at"));
                            return getMonthName(cal.get(Calendar.MONTH));
                        },
                        Collectors.counting()
                ));

        // Set up axes
        xAxis.setLabel("");
        yAxis.setLabel("");
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);

        // Calculate upper bound based on data
        long maxValue = monthlyData.values().stream().mapToLong(Long::longValue).max().orElse(10);
        yAxis.setUpperBound(Math.max(maxValue + 5, 20)); // At least 20, or max + 5
        yAxis.setTickUnit(Math.max(maxValue / 4, 5));

        // Create data series
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        for (String month : months) {
            long count = monthlyData.getOrDefault(month, 0L);
            series.getData().add(new XYChart.Data<>(month, count));
        }

        chart.getData().clear();
        chart.getData().add(series);
        chart.setLegendVisible(false);
        chart.setTitle("");
        chart.setAnimated(false);
    }

    private String getMonthName(int month) {
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return monthNames[month];
    }

    // Navigation methods (implement based on your application structure)
    private void navigateToDashboard() {
        // Implement navigation to dashboard
        System.out.println("Navigate to Dashboard");
    }

    private void navigateToNotifications() {
        // Implement navigation to notifications
        System.out.println("Navigate to Notifications");
    }

    private void logout() {
        AdminSession adminSession = new AdminSession();
        // Clear admin session
        if (adminSession != null) {
            adminSession.clearSession();
            System.out.println("Admin session cleared on logout");
        }

        // Close database connection
        if (equipmentDAO != null) {
            equipmentDAO.closeConnection();
        }

        // Close current window or navigate to login screen
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/View.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Error loading home page: " + e.getMessage());
        }

        // You can add navigation to login screen here if needed
        // Example: loadLoginScreen();
    }

    // Cleanup method
    public void cleanup() {
        if (equipmentDAO != null) {
            equipmentDAO.closeConnection();
        }
    }
}