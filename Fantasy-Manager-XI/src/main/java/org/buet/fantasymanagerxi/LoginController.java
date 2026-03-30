package org.buet.fantasymanagerxi;

import org.buet.fantasymanagerxi.model.MarketMessage;
import org.buet.fantasymanagerxi.model.Player;
import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class LoginController implements NetworkThread.MessageListener {

    @FXML private ComboBox<String> clubDropdown;
    @FXML private PasswordField    passwordField;
    @FXML private Label            errorLabel;
    @FXML private Button           loginBtn;

    private NetworkThread networkThread;

    @FXML
    public void initialize() {
        errorLabel.setText("");

        // Populate dropdown with all six clubs
        clubDropdown.getItems().addAll(
                "CHELSEA",
                "LIVERPOOL",
                "ARSENAL",
                "MANUTD",
                "MANCITY",
                "SPURS"
        );

        // Start network thread and connect to server
        networkThread = new NetworkThread(this);
        networkThread.start();
    }

    @FXML
    private void handleLogin() {
        String club     = clubDropdown.getValue();
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

                SessionManager.setNetworkThread(networkThread);
                SessionManager.setLoggedInClub(clubDropdown.getValue());
                SessionManager.setSquad(squad);

                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource(
                                    "/org/buet/fantasymanagerxi/fxml/player-db.fxml")
                    );
                    Parent root = loader.load();
                    Stage stage = (Stage) loginBtn.getScene().getWindow();
                    stage.setScene(new Scene(root, 1100, 720));
                    stage.setTitle("Player Database — " +
                            clubDropdown.getValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            case LOGIN_FAIL -> {
                errorLabel.setText((String) msg.getPayload());
                loginBtn.setDisable(false);
            }

            case ERROR -> {
                errorLabel.setText("Server error. Please try again.");
                loginBtn.setDisable(false);
            }

            default -> {}
        }
    }

    @Override
    public void onConnectionFailed(String reason) {
        errorLabel.setText(reason);
        loginBtn.setDisable(false);
    }
}