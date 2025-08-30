package org.example.Maintenix.Admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

import org.bson.Document;
import org.example.Maintenix.DAO.admindao;
import org.example.Maintenix.Utils.AdminSession;

public class AdminLoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginBtn;
    @FXML private ImageView illustration, logo;
    @FXML private Button backLink;

    @FXML
    public void initialize() {
        illustration.setImage(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResource("/images/admin login.png")).toExternalForm()));
        logo.setImage(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResource("/images/logo.png")).toExternalForm()));
        emailField.setMaxWidth(300);
        passwordField.setMaxWidth(300);

        loginBtn.setOnAction(e -> handleLogin());
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

        // Validation
        if (email.isEmpty()) {
            showAlert("Validation Error", "Please enter email.");
            emailField.requestFocus();
            return;
        }

        if (!email.matches(emailRegex)) {
            showAlert("Invalid Email", "Please enter a valid email!");
            return;
        }

        if (password.isEmpty()) {
            showAlert("Validation Error", "Please enter password.");
            passwordField.requestFocus();
            return;
        }

        if (password.length() < 8 || password.length() > 16) {
            showAlert("Invalid Password", "Password length must be 8-16 characters.");
            return;
        }

        try {
            admindao dbdao = new admindao();
            Document adminDoc = dbdao.loginAdmin(email, password);

            if (adminDoc != null) {
                // Extract admin information
                String adminName = adminDoc.getString("name") != null ? adminDoc.getString("name") : "Administrator";
                String adminId = adminDoc.getObjectId("_id").toString();

                // Set admin session
                AdminSession.getInstance().setAdminSession(email, adminName, adminId);

                showAlert("Success", "Login Successful! Welcome " + adminName);

                // Clear fields
                emailField.clear();
                passwordField.clear();

                // Navigate to dashboard
                navigateToAdminDashboard();

            } else {
                showAlert("Invalid Login Details", "Account not found!");
            }

        } catch (Exception ex) {
            System.err.println("Login error: " + ex.getMessage());
            showAlert("Login Error", "An error occurred during login. Please try again.");
        }
    }

    private void navigateToAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/AdminDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Maintenix - Admin Dashboard");
            stage.setMaximized(true);
        } catch (IOException ex) {
            System.err.println("Error loading admin dashboard: " + ex.getMessage());
            showAlert("Navigation Error", "Could not load admin dashboard.");
        }
    }

    @FXML
    private void handleBackClick() {
        try {
            showAlert("Info", "Redirecting to home page...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/View.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}