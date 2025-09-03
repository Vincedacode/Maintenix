package org.example.Maintenix.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import java.net.URL;
import java.util.ResourceBundle;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializePieChart();
        initializeInactiveDevicesChart();
        initializeActiveDevicesChart();
    }

    private void initializePieChart() {
        // Create pie chart data for priority breakdown
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Medium", 50.9),
                new PieChart.Data("High", 20.1),
                new PieChart.Data("Low", 29.0)
        );

        priorityChart.setData(pieChartData);
        priorityChart.setTitle("");
        priorityChart.setLegendVisible(true);
        priorityChart.setLabelsVisible(false);
        priorityChart.setStartAngle(90);

        // Style the pie chart slices
        priorityChart.getData().get(0).getNode().setStyle("-fx-pie-color: #333333;"); // Medium - Black
        priorityChart.getData().get(1).getNode().setStyle("-fx-pie-color: #1976D2;"); // High - Blue
        priorityChart.getData().get(2).getNode().setStyle("-fx-pie-color: #42A5F5;"); // Low - Light Blue
    }

    private void initializeInactiveDevicesChart() {
        // Set up axes
        inactiveXAxis.setLabel("");
        inactiveYAxis.setLabel("");
        inactiveYAxis.setAutoRanging(false);
        inactiveYAxis.setLowerBound(0);
        inactiveYAxis.setUpperBound(30);
        inactiveYAxis.setTickUnit(5);

        // Create data series for inactive devices per month
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Jan", 7));
        series.getData().add(new XYChart.Data<>("Feb", 12));
        series.getData().add(new XYChart.Data<>("Mar", 5));
        series.getData().add(new XYChart.Data<>("Apr", 8));
        series.getData().add(new XYChart.Data<>("May", 11));
        series.getData().add(new XYChart.Data<>("Jun", 10));
        series.getData().add(new XYChart.Data<>("Jul", 6));
        series.getData().add(new XYChart.Data<>("Aug", 7));
        series.getData().add(new XYChart.Data<>("Sep", 13));
        series.getData().add(new XYChart.Data<>("Oct", 18));
        series.getData().add(new XYChart.Data<>("Nov", 25));
        series.getData().add(new XYChart.Data<>("Dec", 9));

        inactiveDevicesChart.getData().add(series);
        inactiveDevicesChart.setLegendVisible(false);
        inactiveDevicesChart.setTitle("");
        inactiveDevicesChart.setAnimated(false);
    }

    private void initializeActiveDevicesChart() {
        // Set up axes
        activeXAxis.setLabel("");
        activeYAxis.setLabel("");
        activeYAxis.setAutoRanging(false);
        activeYAxis.setLowerBound(0);
        activeYAxis.setUpperBound(40);
        activeYAxis.setTickUnit(10);

        // Create data series for active devices per month
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Jan", 8));
        series.getData().add(new XYChart.Data<>("Feb", 23));
        series.getData().add(new XYChart.Data<>("Mar", 12));
        series.getData().add(new XYChart.Data<>("Apr", 25));
        series.getData().add(new XYChart.Data<>("May", 37));
        series.getData().add(new XYChart.Data<>("Jun", 35));
        series.getData().add(new XYChart.Data<>("Jul", 30));
        series.getData().add(new XYChart.Data<>("Aug", 29));
        series.getData().add(new XYChart.Data<>("Sep", 37));
        series.getData().add(new XYChart.Data<>("Oct", 36));
        series.getData().add(new XYChart.Data<>("Nov", 34));
        series.getData().add(new XYChart.Data<>("Dec", 32));

        activeDevicesChart.getData().add(series);
        activeDevicesChart.setLegendVisible(false);
        activeDevicesChart.setTitle("");
        activeDevicesChart.setAnimated(false);
    }
}