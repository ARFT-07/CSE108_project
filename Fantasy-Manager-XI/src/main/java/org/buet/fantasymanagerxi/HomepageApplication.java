package org.buet.fantasymanagerxi;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class HomepageApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HomepageApplication.class.getResource("/org/buet/fantasymanagerxi/fxml/homepage-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
        stage.setTitle("Fantasy League XI");
        //Image img=(Image) new Image(getClass().getResourceAsStream("/images/logo.png"));
        Image img = (Image) new Image(
                getClass().getResourceAsStream("/org/buet/fantasymanagerxi/images/logo.png")
        );

        stage.getIcons().add(img);
        stage.setScene(scene);
        stage.show();
    }
}

