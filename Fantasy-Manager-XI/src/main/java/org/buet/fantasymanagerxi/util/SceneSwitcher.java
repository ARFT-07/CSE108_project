package org.buet.fantasymanagerxi.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.buet.fantasymanagerxi.SessionManager;
import java.io.IOException;

public class SceneSwitcher {

    public static void switchScene(String fxml, ActionEvent event, double width, double height) {
        try {
            SessionManager.updateSceneHistory(fxml);
            // 1. Load the FXML
            FXMLLoader loader = new FXMLLoader(
                    SceneSwitcher.class.getResource("/org/buet/fantasymanagerxi/fxml/" + fxml)
            );
            Parent root = loader.load();

            // 2. Get the Stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Fantasy League XI");

            // 3. Create the Scene with dynamic dimensions
            Scene scene = new Scene(root, width, height);

            // 4. Add the Stylesheet (if it exists)
            try {
                scene.getStylesheets().add(
                        SceneSwitcher.class.getResource("/org/buet/fantasymanagerxi/css/homepage-style.css").toExternalForm()
                );
            } catch (Exception e) {
                System.out.println("CSS not found, skipping...");
            }

            // 5. Add the Logo
            try {
                Image img = new Image(
                        SceneSwitcher.class.getResourceAsStream("/org/buet/fantasymanagerxi/images/logo.png")
                );
                stage.getIcons().add(img);
            } catch (Exception e) {
                System.out.println("Logo not found, skipping...");
            }

            // 6. Show the Stage
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void switchScene(String fxml, Node node, double width, double height) {
        try {
            SessionManager.updateSceneHistory(fxml);
            // 1. Load the FXML
            FXMLLoader loader = new FXMLLoader(
                    SceneSwitcher.class.getResource("/org/buet/fantasymanagerxi/fxml/" + fxml)
            );
            Parent root = loader.load();

            // 2. Get the Stage via the Node passed in
            Stage stage = (Stage) node.getScene().getWindow();
            stage.setTitle("Fantasy League XI");

            // 3. Setup Scene
            Scene scene = new Scene(root, width, height);


            // 4. Add the Stylesheet (if it exists)
            try {
                scene.getStylesheets().add(
                        SceneSwitcher.class.getResource("/org/buet/fantasymanagerxi/css/homepage-style.css").toExternalForm()
                );
            } catch (Exception e) {
                System.out.println("CSS not found, skipping...");
            }

            // 5. Add the Logo
            try {
                Image img = new Image(
                        SceneSwitcher.class.getResourceAsStream("/org/buet/fantasymanagerxi/images/logo.png")
                );
                stage.getIcons().add(img);
            } catch (Exception e) {
                System.out.println("Logo not found, skipping...");
            }


            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}