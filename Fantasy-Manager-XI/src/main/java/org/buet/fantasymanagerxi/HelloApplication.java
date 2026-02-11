package org.buet.fantasymanagerxi;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {



        FXMLLoader fxmlLoader =
                new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("Fantasy Manager XI");
        stage.setScene(scene);

        Image icon =
                   new Image(HelloApplication.class.getResourceAsStream("img.png"));
        stage.getIcons().add(icon);

        stage.show();
    }
}
