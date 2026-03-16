package org.buet.fantasymanagerxi;

import javafx.event.ActionEvent;
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

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private Button        loginBtn;

    private NetworkThread networkThread;

    @FXML
    public void initialize() {
        errorLabel.setText("");

        // Start the network thread and connect to server
        networkThread = new NetworkThread(this);
        networkThread.start();
    }

    @FXML
    private void handleLogin() {
        String club     = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (club.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter club name and password.");
            return;
        }

        // Disable button so club can't click twice while waiting
        loginBtn.setDisable(true);
        errorLabel.setText("Connecting...");

        // Build and send the LOGIN message
        MarketMessage msg = new MarketMessage(MarketMessage.Type.LOGIN);
        msg.setClubName(club.toUpperCase());
        msg.setPassword(password);
        networkThread.sendMessage(msg);
    }

    @Override
    public void onMessageReceived(MarketMessage msg) {
        switch (msg.getType()) {

            case LOGIN_OK -> {
                // Server sends back the club's squad as the payload
                @SuppressWarnings("unchecked")
                List<Player> squad = (List<Player>) msg.getPayload();

                // Store the network thread and squad so the next screen can use them
                SessionManager.setNetworkThread(networkThread);
                SessionManager.setLoggedInClub(usernameField.getText().trim());
                SessionManager.setSquad(squad);

                // Switch to the player database screen
                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource(
                                    "/org/buet/fantasymanagerxi/fxml/player-db.fxml")
                    );
                    Parent root = loader.load();
                    Stage stage = (Stage) loginBtn.getScene().getWindow();
                    stage.setScene(new Scene(root, 1100, 720));
                    stage.setTitle("Player Database — " +
                            SessionManager.getLoggedInClub());
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

            default -> {} // ignore other message types on the login screen
        }
    }

    @Override
    public void onConnectionFailed(String reason) {
        errorLabel.setText(reason);
        loginBtn.setDisable(false);
    }

    public void checkValidLogin(ActionEvent actionEvent) {

    }
}