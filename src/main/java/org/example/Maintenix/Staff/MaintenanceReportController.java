package org.example.Maintenix.Staff;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
import java.util.ResourceBundle;
import org.example.Maintenix.DAO.maintenancereportdao;
import org.example.Maintenix.DAO.staffdao;
import org.example.Maintenix.Utils.UserSession;

public class MaintenanceReportController implements Initializable {

    @FXML
    private ComboBox<String> staffNameCombo;

    @FXML
    private TextField locationField;

    @FXML
    private TextArea issueArea;

    @FXML
    private Button selectImageBtn;

    @FXML
    private Label imageNameLabel;

    @FXML
    private ImageView imagePreview;

    @FXML
    private Button submitReportBtn;

    @FXML
    private Button viewReportsBtn;

    // Optional: Add welcome label if it exists in FXML
    @FXML
    private Label welcomeLabel;

    // Image handling
    private File selectedImageFile;
    private String imageBase64;
    private String imageContentType;

    // DAOs
    private staffdao staffDAO;
    private maintenancereportdao maintenanceDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize DAOs
        staffDAO = new staffdao();
        maintenanceDAO = new maintenancereportdao();

        setupFormValidation();
        loadCurrentUserData();
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

            System.out.println("Loaded current user for maintenance report: " + currentUsername);

        } catch (Exception e) {
            System.err.println("Error loading current user data: " + e.getMessage());
            showAlert("Error", "Could not load user information. Please try logging in again.");
            e.printStackTrace();
        }
    }

    private void setupFormValidation() {
        submitReportBtn.setOnAction(event -> submitReport());
        viewReportsBtn.setOnAction(event -> viewReports());
        selectImageBtn.setOnAction(event -> selectImage());
    }

    @FXML
    private void selectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");

        // Set extension filters
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp");
        fileChooser.getExtensionFilters().add(imageFilter);

        // Show open file dialog
        Stage stage = (Stage) selectImageBtn.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedImageFile = file;
            imageNameLabel.setText(file.getName());

            try {
                // Convert image to base64
                byte[] fileContent = Files.readAllBytes(file.toPath());
                imageBase64 = Base64.getEncoder().encodeToString(fileContent);

                // Determine content type
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".png")) {
                    imageContentType = "image/png";
                } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    imageContentType = "image/jpeg";
                } else if (fileName.endsWith(".gif")) {
                    imageContentType = "image/gif";
                } else if (fileName.endsWith(".bmp")) {
                    imageContentType = "image/bmp";
                } else {
                    imageContentType = "image/jpeg"; // Default
                }

                // Show image preview
                Image image = new Image(file.toURI().toString());
                imagePreview.setImage(image);
                imagePreview.setVisible(true);

            } catch (IOException e) {
                showAlert("Error", "Could not read the selected image file.");
                selectedImageFile = null;
                imageNameLabel.setText("No file selected");
                imagePreview.setVisible(false);
            }
        }
    }

    @FXML
    private void submitReport() {
        if (validateForm()) {
            try {
                // Get form data
                String staffName = staffNameCombo.getValue();
                String location = locationField.getText().trim();
                String issue = issueArea.getText().trim();

                // Submit request with or without image
                boolean success;
                if (selectedImageFile != null) {
                    success = maintenanceDAO.createMaintenanceRequest(
                            staffName, issue, location,
                            selectedImageFile.getName(), imageContentType, imageBase64
                    );
                } else {
                    success = maintenanceDAO.createMaintenanceRequest(
                            staffName, issue, location,
                            null, null, null
                    );
                }

                if (success) {
                    showAlert("Success", "Maintenance report submitted successfully!");
                    clearForm();

                    // Ask user if they want to view their reports
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Report Submitted");
                    confirmAlert.setHeaderText("Maintenance report submitted successfully!");
                    confirmAlert.setContentText("Would you like to view your report history?");

                    confirmAlert.showAndWait().ifPresent(response -> {
                        if (response.getButtonData().isDefaultButton()) {
                            viewReports();
                        }
                    });

                } else {
                    showAlert("Error", "Failed to submit maintenance report. Please try again.");
                }

            } catch (Exception e) {
                System.err.println("Error submitting report: " + e.getMessage());
                showAlert("Error", "An error occurred while submitting the report: " + e.getMessage());
            }
        }
    }

    @FXML
    private void viewReports() {
        try {
            // Navigate to maintenance reports view page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/HistoryPage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) viewReportsBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert("Error", "Could not open reports view page.");
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

        if (locationField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter the location.");
            locationField.requestFocus();
            return false;
        }

        if (issueArea.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please describe the issue.");
            issueArea.requestFocus();
            return false;
        }

        if (issueArea.getText().trim().length() < 10) {
            showAlert("Validation Error", "Please provide a more detailed description (at least 10 characters).");
            issueArea.requestFocus();
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
        locationField.clear();
        issueArea.clear();

        selectedImageFile = null;
        imageBase64 = null;
        imageContentType = null;
        imageNameLabel.setText("No file selected");
        imagePreview.setVisible(false);
        imagePreview.setImage(null);

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
            Stage stage = (Stage) submitReportBtn.getScene().getWindow();
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

    public String getLocation() {
        return locationField.getText().trim();
    }

    public String getIssue() {
        return issueArea.getText().trim();
    }

    public File getSelectedImageFile() {
        return selectedImageFile;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public String getImageContentType() {
        return imageContentType;
    }

    // Method to get current user info
    public String getCurrentUser() {
        UserSession session = UserSession.getInstance();
        return session.isLoggedIn() ? session.getCurrentUsername() : null;
    }
}