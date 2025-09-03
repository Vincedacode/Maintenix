package org.example.Maintenix;
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

public class MainController {
    @FXML
    private ImageView illustration, logo;

    @FXML
    private Button adminBtn, staffBtn;

    @FXML
    public void initialize() {
        // Load image
        illustration.setImage(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResource("/images/home image.png")).toExternalForm()));
        logo.setImage(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResource("/images/logo.png")).toExternalForm()));
        // Button Actions
        adminBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/AdminLogin.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setMaximized(true); // ✅ Full screen on forward navigation
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        staffBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/StaffLogin.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setMaximized(true); // ✅ Full screen on forward navigation
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

    }

}
