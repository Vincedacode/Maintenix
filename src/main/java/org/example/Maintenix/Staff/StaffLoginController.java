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


import org.example.Maintenix.Utils.UserSession;

import org.bson.Document;
import org.example.Maintenix.DAO.staffdao;

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
        illustration.setImage(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResource("/images/Staff login image.png")).toExternalForm()));
        logo.setImage(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResource("/images/logo.png")).toExternalForm()));
        emailField.setMaxWidth(300);
        passwordField.setMaxWidth(300);

        signup_link.setOnAction(e->{
            try {
                showAlert("Info", "Redirecting to register page...");
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

            if(email.trim().isEmpty()){
                showAlert("Validation Error", "Please enter email.");
                emailField.requestFocus();
                return;
            }
            if(!email.matches(emailRegex)){
                showAlert("Invalid Email", "Please enter a valid email!");
                emailField.requestFocus();
                return;
            }
            if(password.trim().isEmpty()){
                showAlert("Validation Error", "Please enter password.");
                passwordField.requestFocus();
                return;
            }

            if(password.length() < 8 || password.length() > 16){
                showAlert("Invalid Password", "Password length must be 8-16 characters.");
                passwordField.requestFocus();
                return;
            }

            try{
                staffdao dbdao = new staffdao();
                Document staffDoc = dbdao.loginStaff(email, password);
                if(staffDoc != null){
                    String username = staffDoc.getString("Username");
                    String fullName = staffDoc.getString("Fullname");

                    // Set user session
                    UserSession.getInstance().setCurrentUser(username, fullName);

                    showAlert("Success", "Login Successful!");
                    emailField.clear();
                    passwordField.clear();

                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/StaffDashboard.fxml"));
                        Parent root = loader.load();
                        // No need to pass username manually anymore - it's in the session
                        Stage stage = (Stage) loginBtn.getScene().getWindow();
                        stage.setScene(new Scene(root));
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    return;
                } else {
                    showAlert("Invalid Login Details", "Account not found!");
                    return;
                }
            } catch (Exception ex){
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
