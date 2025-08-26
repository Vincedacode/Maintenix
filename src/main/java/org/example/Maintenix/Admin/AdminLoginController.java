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

public class AdminLoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginBtn;
    @FXML
    private ImageView illustration,logo;
    @FXML private Button backLink;



    @FXML
    public void initialize() {
        illustration.setImage(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResource("/images/admin login.png")).toExternalForm()));
        logo.setImage(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResource("/images/logo.png")).toExternalForm()));
        emailField.setMaxWidth(300);
        passwordField.setMaxWidth(300);
        loginBtn.setOnAction(e->{
            String email=  emailField.getText();
            String password = passwordField.getText();
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

            if(email.trim().isEmpty() ){
                showAlert("Validation Error", "Please enter email.");
                emailField.requestFocus();
                return;
            }

            if(!email.matches(emailRegex)){
                showAlert("Invalid Email", "Please enter a valid email!");
                return;
            }

            if(password.trim().isEmpty() ){
                showAlert("Validation Error", "Please enter password.");
                passwordField.requestFocus();
                return;
            }

            if(password.trim().length() < 8 || password.trim().length() > 16){
                showAlert("Invalid Password", "Password length must be 8-16 characters.");
                return;
            }

            try {
                admindao dbdao = new admindao();
               Document adminDoc = dbdao.loginAdmin(email,password);
               if(adminDoc != null){
                   showAlert("Success", "Login Successful!");
                   emailField.clear();
                   passwordField.clear();
                   return;
               }else {
                   showAlert("Invalid Login Details", "Account not found!");
                   return;
               }

            }catch (Exception ex){
                System.out.println(ex.getMessage());
            }



        });
    }

    @FXML
    private void handleBackClick() {
        try {
            showAlert("Info", "Redirecting to home page...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/View.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true); // ✅ Ensure full screen on back
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }





    private void showAlert(String title,String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
