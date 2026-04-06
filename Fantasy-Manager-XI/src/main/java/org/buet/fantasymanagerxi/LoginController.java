package org.buet.fantasymanagerxi;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import org.buet.fantasymanagerxi.model.MarketMessage;
import org.buet.fantasymanagerxi.model.Player;
import org.buet.fantasymanagerxi.util.ClubRegistry;
import org.buet.fantasymanagerxi.util.SceneSwitcher;

import java.util.List;

public class LoginController implements NetworkThread.MessageListener {

    @FXML private ComboBox<String> clubDropdown;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginBtn;

    private NetworkThread networkThread;

    @FXML
    public void initialize() {
        errorLabel.setText("");

        clubDropdown.getItems().addAll(
                "CHELSEA",
                "LIVERPOOL",
                "ARSENAL",
                "MANUTD",
                "MANCITY",
                "SPURS"
        );

        networkThread = new NetworkThread(this);
        networkThread.start();
    }

    @FXML
    private void handleLogin() {
        String club = clubDropdown.getValue();
        String password = passwordField.getText().trim();

        if (club == null) {
            errorLabel.setText("Please select a club.");
            return;
        }
        if (password.isEmpty()) {
            errorLabel.setText("Please enter your password.");
            return;
        }

        loginBtn.setDisable(true);
        errorLabel.setText("Connecting...");

        MarketMessage msg = new MarketMessage(MarketMessage.Type.LOGIN);
        msg.setClubName(club);
        msg.setPassword(password);
        networkThread.sendMessage(msg);
    }

    @Override
    public void onMessageReceived(MarketMessage msg) {
        switch (msg.getType()) {
            case LOGIN_OK -> {
                @SuppressWarnings("unchecked")
                List<Player> squad = (List<Player>) msg.getPayload();

                String clubId = ClubRegistry.toCode(msg.getClubName() != null ? msg.getClubName() : clubDropdown.getValue());
                SessionManager.startSession(networkThread, clubId, ClubRegistry.toDisplay(clubId), squad);
                SceneSwitcher.switchScene("prehome-view.fxml", loginBtn, 1100, 720);
            }
            case LOGIN_FAIL -> {
                errorLabel.setText((String) msg.getPayload());
                loginBtn.setDisable(false);
            }
            case ERROR -> {
                errorLabel.setText("Server error. Please try again.");
                loginBtn.setDisable(false);
            }
            default -> {
            }
        }
    }

    @Override
    public void onConnectionFailed(String reason) {
        errorLabel.setText(reason);
        loginBtn.setDisable(false);
    }
}
