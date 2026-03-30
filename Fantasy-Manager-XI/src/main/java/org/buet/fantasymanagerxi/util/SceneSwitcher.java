package org.buet.fantasymanagerxi.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import throw java.io.IOException;
import javafx.stage.Stage;

public class SceneSwitcher {
    public static void switchScene(String fxml, ActionEvent event, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneSwitcher.class.getResource("/org/buet/fantasymanagerxi/fxml/" + fxml)
            );
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // This version allows each screen to define its own size!
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}