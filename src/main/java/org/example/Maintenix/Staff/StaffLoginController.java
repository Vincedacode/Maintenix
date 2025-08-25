package org.example.Maintenix.Staff;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class StaffLoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMe;
    @FXML private Button loginBtn;
    @FXML
    private ImageView illustration,logo;
    @FXML private Button backLink;
   @FXML private Hyperlink signup_link;


    @FXML
    public void initialize() {
        illustration.setImage(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResource("/images/staff login.png")).toExternalForm()));
        logo.setImage(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResource("/images/logo.png")).toExternalForm()));
        emailField.setMaxWidth(300);
        passwordField.setMaxWidth(300);

        signup_link.setOnAction(e->{
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/StaffRegistration.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setMaximized(true); // ✅ Full screen on forward navigation
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        loginBtn.setOnAction(e->{
            String email=  emailField.getText();
            String password = passwordField.getText();
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

            if(email.isEmpty() || password.isEmpty()){
                showErrorAlert("Input Error", "Please fill all required fields.");
                return;
            }
            if(!email.matches(emailRegex)){
                showErrorAlert("Invalid Email", "Please enter a valid email!");
                return;
            }
            if(password.length() < 8 || password.length() > 16){
                showErrorAlert("Invalid Password", "Password length must be 8-16 characters.");
                return;
            }

            showSuccessAlert("", "Login Successful!");
            System.out.println("Login Successful!");
            emailField.clear();
            passwordField.clear();
        });
    }

    @FXML
    private void handleBackClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/View.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true); // ✅ Ensure full screen on back
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void showErrorAlert(String title,String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarningAlert(String title,String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title,String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
