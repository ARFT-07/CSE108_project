package org.buet.fantasymanagerxi;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.buet.fantasymanagerxi.util.SceneSwitcher;

public class PreHomeController {
    public void movetoplayerDb(ActionEvent actionEvent) {
    }

    public void movetoMyPlayers(ActionEvent actionEvent) {
    }

    @FXML
    private void movetoHomePage(ActionEvent event) {
        SceneSwitcher.switchScene("homepage-view.fxml", event,1200,800);
    }
    public void movetoTransferPage(ActionEvent actionEvent) {
    }
}
