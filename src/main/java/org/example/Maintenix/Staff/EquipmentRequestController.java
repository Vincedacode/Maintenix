package org.example.Maintenix.Staff;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import org.example.Maintenix.DAO.equipmentrequestdao;
import org.example.Maintenix.DAO.staffdao;

public class EquipmentRequestController implements Initializable {

    @FXML
    private ComboBox<String> staffNameCombo;

    @FXML
    private ComboBox<String> typeCombo;

    @FXML
    private TextField itemNameField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Button submitRequestBtn;

    @FXML
    private Button viewRequestsBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFormValidation();
        loadStaffNames();
        setupTypeCombo();
    }

    private void loadStaffNames() {
        try {
            staffdao dbdao = new staffdao();
            ObservableList<String> staffNames = dbdao.getAllStaffNames();
            staffNameCombo.setItems(staffNames);
        } catch (Exception e) {
            System.err.println("Error loading staff names: " + e.getMessage());
            showAlert("Error", "Could not load staff names. Please try again.");
        }
    }

    private void setupTypeCombo() {
        ObservableList<String> types = FXCollections.observableArrayList();
        types.addAll("hardware", "software");
        typeCombo.setItems(types);
    }

    private void setupFormValidation() {
        submitRequestBtn.setOnAction(event -> submitRequest());
        viewRequestsBtn.setOnAction(event -> viewRequests());
    }

    @FXML
    private void submitRequest() {
        if (validateForm()) {
            try {
                // Get form data
                String staffName = staffNameCombo.getValue();
                String type = typeCombo.getValue();
                String itemName = itemNameField.getText().trim();
                String description = descriptionArea.getText().trim();

                // Create equipment request
                equipmentrequestdao dbdao = new equipmentrequestdao();

                boolean success = dbdao.createEquipmentRequest(staffName, type, itemName, description);

                if (success) {
                    showAlert("Success", "Equipment request submitted successfully!");
                    clearForm();
                    viewRequests();

                } else {
                    showAlert("Error", "Failed to submit equipment request. Please try again.");
                }

            } catch (Exception e) {
                System.err.println("Error submitting request: " + e.getMessage());
                showAlert("Error", "An error occurred while submitting the request: " + e.getMessage());
            }
        }
    }

    @FXML
    private void viewRequests() {
        try {
            // Navigate to maintenance requests view page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/HistoryPage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) viewRequestsBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert("Error", "Could not open requests view page.");
        }
    }

    private boolean validateForm() {
        if (staffNameCombo.getValue() == null || staffNameCombo.getValue().trim().isEmpty()) {
            showAlert("Validation Error", "Please select a staff member.");
            staffNameCombo.requestFocus();
            return false;
        }

        if (typeCombo.getValue() == null || typeCombo.getValue().trim().isEmpty()) {
            showAlert("Validation Error", "Please select equipment type.");
            typeCombo.requestFocus();
            return false;
        }

        if (itemNameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter the item name.");
            itemNameField.requestFocus();
            return false;
        }

        if (descriptionArea.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please provide a description.");
            descriptionArea.requestFocus();
            return false;
        }

        if (descriptionArea.getText().trim().length() < 10) {
            showAlert("Validation Error", "Please provide a more detailed description (at least 10 characters).");
            descriptionArea.requestFocus();
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearForm() {
        staffNameCombo.setValue(null);
        typeCombo.setValue(null);
        itemNameField.clear();
        descriptionArea.clear();
    }

    // Getters for accessing form data
    public String getStaffName() {
        return staffNameCombo.getValue();
    }

    public String getType() {
        return typeCombo.getValue();
    }

    public String getItemName() {
        return itemNameField.getText().trim();
    }

    public String getDescription() {
        return descriptionArea.getText().trim();
    }
}