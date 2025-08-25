package org.example.Maintenix;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/View.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        // Load CSS
        URL cssURL = getClass().getResource("/Styles/style.css");
        if (cssURL != null) {
            scene.getStylesheets().add(cssURL.toExternalForm());
        } else {
            System.out.println("CSS not found.");
        }

        stage.setTitle("Maintenix");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png"))));
        stage.setScene(scene);
        stage.setMaximized(true); // ✅ Start maximized
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
