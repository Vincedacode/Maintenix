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
import org.example.Maintenix.Utils.UserSession;

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

    // Optional: Add welcome label if it exists in FXML
    @FXML
    private Label welcomeLabel;

    private staffdao staffDAO;
    private equipmentrequestdao equipmentDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize DAOs
        staffDAO = new staffdao();
        equipmentDAO = new equipmentrequestdao();

        setupFormValidation();
        loadCurrentUserData();
        setupTypeCombo();
    }

    /**
     * Load current user's data and populate the staff name dropdown with only their name
     */
    private void loadCurrentUserData() {
        UserSession session = UserSession.getInstance();

        if (!session.isLoggedIn()) {
            showAlert("Session Error", "No active user session found. Please log in again.");
            redirectToLogin();
            return;
        }

        try {
            String currentUsername = session.getCurrentUsername();
            String currentUserFullName = session.getCurrentUserFullName();

            // Set welcome message if label exists
            if (welcomeLabel != null) {
                welcomeLabel.setText("Welcome, " + currentUsername + "!");
            }

            // Create observable list with only current user's name
            ObservableList<String> currentUserOnly = FXCollections.observableArrayList();

            // If we have the full name from session, use it
            if (currentUserFullName != null && !currentUserFullName.trim().isEmpty()) {
                currentUserOnly.add(currentUserFullName);
            } else {
                // Fallback: get full name from database using username
                String fullName = staffDAO.getFullNameByUsername(currentUsername);
                if (fullName != null && !fullName.trim().isEmpty()) {
                    currentUserOnly.add(fullName);
                    // Update session with full name for future use
                    session.setCurrentUser(currentUsername, fullName);
                } else {
                    // Ultimate fallback: use username
                    currentUserOnly.add(currentUsername);
                }
            }

            // Set the dropdown items and pre-select the user
            staffNameCombo.setItems(currentUserOnly);
            if (!currentUserOnly.isEmpty()) {
                staffNameCombo.setValue(currentUserOnly.get(0));
                // Disable the dropdown since there's only one option
                staffNameCombo.setDisable(true);
            }

            System.out.println("Loaded current user: " + currentUsername);

        } catch (Exception e) {
            System.err.println("Error loading current user data: " + e.getMessage());
            showAlert("Error", "Could not load user information. Please try logging in again.");
            e.printStackTrace();
        }
    }

    private void setupTypeCombo() {
        ObservableList<String> types = FXCollections.observableArrayList();
        types.addAll("Hardware", "Software");
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

                // Create equipment request using the logged-in user's information
                boolean success = equipmentDAO.createEquipmentRequest(staffName, type, itemName, description);

                if (success) {
                    showAlert("Success", "Equipment request submitted successfully!");
                    clearForm();

                    // Ask user if they want to view their requests
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Request Submitted");
                    confirmAlert.setHeaderText("Equipment request submitted successfully!");
                    confirmAlert.setContentText("Would you like to view your request history?");

                    confirmAlert.showAndWait().ifPresent(response -> {
                        if (response.getButtonData().isDefaultButton()) {
                            viewRequests();
                        }
                    });

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
        // Check if user session is still valid
        UserSession session = UserSession.getInstance();
        if (!session.isLoggedIn()) {
            showAlert("Session Error", "Your session has expired. Please log in again.");
            redirectToLogin();
            return false;
        }

        if (staffNameCombo.getValue() == null || staffNameCombo.getValue().trim().isEmpty()) {
            showAlert("Validation Error", "Staff name is required. Please log in again if this field is empty.");
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
        Alert alert;
        if (title.contains("Error")) {
            alert = new Alert(Alert.AlertType.ERROR);
        } else if (title.contains("Success")) {
            alert = new Alert(Alert.AlertType.INFORMATION);
        } else {
            alert = new Alert(Alert.AlertType.INFORMATION);
        }

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearForm() {
        // Don't clear staff name since it's pre-filled with current user
        typeCombo.setValue(null);
        itemNameField.clear();
        descriptionArea.clear();

        // Re-load current user data to ensure staff name is still populated
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn() && (staffNameCombo.getValue() == null || staffNameCombo.getValue().isEmpty())) {
            loadCurrentUserData();
        }
    }

    /**
     * Redirect to login page if session is invalid
     */
    private void redirectToLogin() {
        try {
            // Clear the session
            UserSession.getInstance().clearSession();

            // Redirect to login page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/View.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) submitRequestBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Error redirecting to login: " + e.getMessage());
        }
    }

    /**
     * Method to refresh user data (can be called externally)
     */
    public void refreshUserData() {
        loadCurrentUserData();
    }

    /**
     * Check if current user session is valid
     */
    public boolean isSessionValid() {
        UserSession session = UserSession.getInstance();
        return session.isLoggedIn();
    }

    // Getters for accessing form data (keeping for backward compatibility)
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

    // Method to get current user info
    public String getCurrentUser() {
        UserSession session = UserSession.getInstance();
        return session.isLoggedIn() ? session.getCurrentUsername() : null;
    }
}