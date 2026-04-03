package org.buet.fantasymanagerxi;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import org.buet.fantasymanagerxi.util.SceneSwitcher;

public class PreHomeController {
    public void movetoplayerDb(ActionEvent actionEvent) {
        SceneSwitcher.switchScene("player-db.fxml", actionEvent, 1100, 720);
    }

    @FXML
    private void movetoMyPlayers(ActionEvent event) {
        SceneSwitcher.switchScene("my-team.fxml", event, 1100, 720);
    }

    @FXML
    private void movetoHomePage(ActionEvent event) {
        SceneSwitcher.switchScene("homepage-view.fxml", event,1200,800);
    }
    public void movetoTransferPage(ActionEvent event) {
        SceneSwitcher.switchScene("transfer-market.fxml", event,1100,720);
    }
}
