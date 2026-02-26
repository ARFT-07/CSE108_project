package org.buet.fantasymanagerxi;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {

    @FXML
    private Label welcomeText;

    @FXML
    private Button pdb_btn;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void onPdbButtonClick(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/buet/fantasymanagerxi/fxml/player-db.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) pdb_btn.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 720));
            stage.setTitle("Player Database");
        } catch (IOException e) {
            e.printStackTrace();
            welcomeText.setText("Error: " + e.getMessage());
        }
    }
}