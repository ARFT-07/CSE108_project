package org.buet.fantasymanagerxi;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class SignupApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Login_SignupApplication.class.getResource("/org/buet/fantasymanagerxi/fxml/signup-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/org/buet/fantasymanagerxi/css/signup_style.css").toExternalForm());
        Image img = (Image) new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/buet/fantasymanagerxi/images/logo.png")));
        stage.getIcons().add(img);
        stage.setScene(scene);
        stage.show();


    }

}