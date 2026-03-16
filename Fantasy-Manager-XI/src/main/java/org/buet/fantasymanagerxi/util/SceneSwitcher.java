package org.buet.fantasymanagerxi.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class SceneSwitcher {

    public static void switchScene(String fxml, ActionEvent event) {
        try {

            Parent root = FXMLLoader.load(
                    SceneSwitcher.class.getResource("/org/buet/fantasymanagerxi/fxml/" + fxml)
            );

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Fantasy League XI");

            Scene scene = new Scene(root, 1000, 600);

            scene.getStylesheets().add(
                    SceneSwitcher.class.getResource("/org/buet/fantasymanagerxi/css/homepage-style.css").toExternalForm()
            );

            Image img = new Image(
                    SceneSwitcher.class.getResourceAsStream("/org/buet/fantasymanagerxi/images/logo.png")
            );

            stage.getIcons().add(img);

            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}