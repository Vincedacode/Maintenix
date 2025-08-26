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
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.ResourceBundle;
import org.example.Maintenix.DAO.maintenancedao;
import org.example.Maintenix.DAO.staffdao;

public class MaintenanceRequestController implements Initializable {

    @FXML
    private ComboBox<String> staffNameCombo;

    @FXML
    private TextField locationField;

    @FXML
    private TextArea issueArea;

    @FXML
    private ComboBox<String> priorityCombo;

    @FXML
    private ComboBox<String> statusCombo;

    @FXML
    private Button selectImageBtn;

    @FXML
    private Label imageNameLabel;

    @FXML
    private ImageView imagePreview;

    @FXML
    private Button submitRequestBtn;

    @FXML
    private Button viewRequestsBtn;

    // Image handling
    private File selectedImageFile;
    private String imageBase64;
    private String imageContentType;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupFormValidation();
        loadStaffNames();
    }

    private void setupComboBoxes() {
        // Priority options
        ObservableList<String> priorities = FXCollections.observableArrayList(
                "Low",
                "Medium",
                "High",
                "Critical"
        );
        priorityCombo.setItems(priorities);
        priorityCombo.setValue("Medium"); // Default value

        // Status options
        ObservableList<String> statuses = FXCollections.observableArrayList(
                "Pending",
                "In Progress",
                "Resolved",
                "Cancelled"
        );
        statusCombo.setItems(statuses);
        statusCombo.setValue("Pending"); // Default value

        // Set prompt texts
        staffNameCombo.setPromptText("Select Staff Member");
        priorityCombo.setPromptText("Select Priority");
        statusCombo.setPromptText("Select Status");
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

    private void setupFormValidation() {
        submitRequestBtn.setOnAction(event -> submitRequest());
        viewRequestsBtn.setOnAction(event -> viewRequests());
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
    private void submitRequest() {
        if (validateForm()) {
            try {
                // Get form data
                String staffName = staffNameCombo.getValue();
                String location = locationField.getText().trim();
                String issue = issueArea.getText().trim();
                String priority = priorityCombo.getValue();
                String status = statusCombo.getValue();

                // Create maintenance request
                maintenancedao dbdao = new maintenancedao();

                // Submit request with or without image
                boolean success;
                if (selectedImageFile != null) {
                    success = dbdao.createMaintenanceRequest(
                            staffName, issue, location, status, priority,
                            selectedImageFile.getName(), imageContentType, imageBase64
                    );
                } else {
                    success = dbdao.createMaintenanceRequest(
                            staffName, issue, location, status, priority,
                            null, null, null
                    );
                }

                if (success) {
                    showAlert("Success", "Maintenance request submitted successfully!");
                    clearForm();
                } else {
                    showAlert("Error", "Failed to submit maintenance request. Please try again.");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/MaintenanceRequestsView.fxml"));
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

        if (priorityCombo.getValue() == null) {
            showAlert("Validation Error", "Please select a priority level.");
            priorityCombo.requestFocus();
            return false;
        }

        if (statusCombo.getValue() == null) {
            showAlert("Validation Error", "Please select a status.");
            statusCombo.requestFocus();
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
        locationField.clear();
        issueArea.clear();
        priorityCombo.setValue("Medium");
        statusCombo.setValue("Pending");
        selectedImageFile = null;
        imageBase64 = null;
        imageContentType = null;
        imageNameLabel.setText("No file selected");
        imagePreview.setVisible(false);
        imagePreview.setImage(null);
    }

    // Getters for accessing form data
    public String getStaffName() {
        return staffNameCombo.getValue();
    }

    public String getLocation() {
        return locationField.getText().trim();
    }

    public String getIssue() {
        return issueArea.getText().trim();
    }

    public String getPriority() {
        return priorityCombo.getValue();
    }

    public String getStatus() {
        return statusCombo.getValue();
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
}