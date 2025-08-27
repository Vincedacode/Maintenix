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
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import org.example.Maintenix.DAO.staffdao;

public class StaffRegistrationController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private ComboBox<String> departmentCombo;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button showPasswordBtn1;

    @FXML
    private Button showPasswordBtn2;

    @FXML
    private Button createAccountBtn;

    @FXML
    private Button loginBtn;

    // TextField versions for password visibility toggle
    private TextField passwordVisible1;
    private TextField passwordVisible2;
    private boolean isPassword1Visible = false;
    private boolean isPassword2Visible = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDepartmentComboBox();
        setupPasswordVisibilityToggle();
        setupFormValidation();
    }

    private void setupDepartmentComboBox() {
        ObservableList<String> departments = FXCollections.observableArrayList(
                "Engineering",
                "Maintenance",
                "Operations",
                "Quality Assurance",
                "Planning",
                "Materials",
                "Safety",
                "IT Support"
        );

        departmentCombo.setItems(departments);
        departmentCombo.setPromptText("Select Department");
    }

    private void setupPasswordVisibilityToggle() {
        // Create TextField versions of password fields for visibility toggle
        passwordVisible1 = new TextField();
        passwordVisible2 = new TextField();

        // Style them the same as password fields
        passwordVisible1.getStyleClass().add("password-field");
        passwordVisible2.getStyleClass().add("password-field");

        // Bind text properties
        passwordVisible1.textProperty().bindBidirectional(passwordField.textProperty());
        passwordVisible2.textProperty().bindBidirectional(confirmPasswordField.textProperty());
    }

    private void setupFormValidation() {
        // Add listeners for real-time validation if needed
        createAccountBtn.setOnAction(event -> createAccount());
        loginBtn.setOnAction(event -> login());
    }

//    @FXML
//    private void togglePasswordVisibility1() {
//        var passwordContainer = passwordField.getParent();
//
//        if (isPassword1Visible) {
//            // Hide password - switch back to PasswordField
//            passwordContainer.getChildren().set(0, passwordField);
//            updateEyeIcon(showPasswordBtn1, false);
//            isPassword1Visible = false;
//        } else {
//            // Show password - switch to TextField
//            passwordContainer.getChildren().set(0, passwordVisible1);
//            updateEyeIcon(showPasswordBtn1, true);
//            isPassword1Visible = true;
//        }
//    }

//    @FXML
//    private void togglePasswordVisibility2() {
//        var passwordContainer = confirmPasswordField.getParent();
//
//        if (isPassword2Visible) {
//            // Hide password - switch back to PasswordField
//            passwordContainer.getChildren().set(0, confirmPasswordField);
//            updateEyeIcon(showPasswordBtn2, false);
//            isPassword2Visible = false;
//        } else {
//            // Show password - switch to TextField
//            passwordContainer.getChildren().set(0, passwordVisible2);
//            updateEyeIcon(showPasswordBtn2, true);
//            isPassword2Visible = true;
//        }
//    }

    private void updateEyeIcon(Button button, boolean isVisible) {
        if (isVisible) {
            // Use eye-slash when password is visible
            button.setText("🙈");
        } else {
            // Use eye when password is hidden
            button.setText("👁");
        }
    }

    @FXML
    private void createAccount() {
        if (validateForm()) {
            // Get form data
            String username = usernameField.getText().trim();
            String department = departmentCombo.getValue();
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            // Check if passwords match
            if (!password.equals(confirmPassword)) {
                showAlert("Error", "Passwords do not match!");
                return;
            }

            // Create account logic here
            System.out.println("Creating account for: " + username);
            System.out.println("Department: " + department);
            System.out.println("Full Name: " + fullName);
            System.out.println("Email: " + email);
            try {
                staffdao dbdao = new staffdao();
                dbdao.registerStaff(username,department,fullName,email,password);
                showAlert("Success", "Account created successfully!");
                clearForm();
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/StaffLogin.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) createAccountBtn.getScene().getWindow();
                    stage.setScene(new Scene(root));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }catch (Exception e){
                System.out.println(e.getMessage());
            }

        }
    }

    @FXML
    private void login() {
        showAlert("Info", "Redirecting to login page...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/StaffLogin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true); // ✅ Ensure full screen on back
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean validateForm() {
        staffdao dbdao = new staffdao();
        if (usernameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter a username.");
            usernameField.requestFocus();
            return false;
        }

        if (departmentCombo.getValue() == null) {
            showAlert("Validation Error", "Please select a department.");
            departmentCombo.requestFocus();
            return false;
        }

        if (fullNameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter your full name.");
            fullNameField.requestFocus();
            return false;
        }

        if (emailField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter your email address.");
            emailField.requestFocus();
            return false;
        }

        if (!isValidEmail(emailField.getText().trim())) {
            showAlert("Validation Error", "Please enter a valid email address.");
            emailField.requestFocus();
            return false;
        }

        if(dbdao.checkStaffEmail(emailField.getText().trim())){
            showAlert("Duplicate Email", "Email already exists!");
            emailField.requestFocus();
            return false;
        }

        if (passwordField.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter a password.");
            passwordField.requestFocus();
            return false;
        }

        if(passwordField.getText().length() < 8 || passwordField.getText().length() > 16){
            showAlert("Validation Error", "Password length must be 8-16 characters.");
            passwordField.requestFocus();
            return false;
        }


        if (confirmPasswordField.getText().isEmpty()) {
            showAlert("Validation Error", "Please confirm your password.");
            confirmPasswordField.requestFocus();
            return false;
        }

        if(confirmPasswordField.getText().length() < 8 || confirmPasswordField.getText().length() > 16){
            showAlert("Validation Error", "Password length must be 8-16 characters.");
            confirmPasswordField.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearForm() {
        usernameField.clear();
        departmentCombo.setValue(null);
        fullNameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    // Getters for accessing form data from other classes
    public String getUsername() {
        return usernameField.getText().trim();
    }

    public String getDepartment() {
        return departmentCombo.getValue();
    }

    public String getFullName() {
        return fullNameField.getText().trim();
    }

    public String getEmail() {
        return emailField.getText().trim();
    }

    public String getPassword() {
        return passwordField.getText();
    }
}