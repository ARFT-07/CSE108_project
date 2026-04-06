package org.buet.fantasymanagerxi;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.buet.fantasymanagerxi.model.MarketMessage;
import org.buet.fantasymanagerxi.model.Player;
import org.buet.fantasymanagerxi.model.TransferOffer;
import org.buet.fantasymanagerxi.util.ClubRegistry;
import org.buet.fantasymanagerxi.util.SceneSwitcher;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ScoutPlayersController implements NetworkThread.MessageListener {

    @FXML private Label clubLabel;
    @FXML private Label countLabel;
    @FXML private Label statusLabel;
    @FXML private TextField searchField;
    @FXML private FlowPane playerGrid;
    @FXML private ToggleButton btnAll;
    @FXML private ToggleButton btnGK;
    @FXML private ToggleButton btnDEF;
    @FXML private ToggleButton btnMID;
    @FXML private ToggleButton btnFWD;

    private final ToggleGroup positionGroup = new ToggleGroup();
    private List<Player> scoutPlayers = new ArrayList<>();
    private String currentPosition = "ALL";

    @FXML
    public void initialize() {
        clubLabel.setText("Scouting for " + SessionManager.getLoggedInClubName());

        btnAll.setToggleGroup(positionGroup);
        btnGK.setToggleGroup(positionGroup);
        btnDEF.setToggleGroup(positionGroup);
        btnMID.setToggleGroup(positionGroup);
        btnFWD.setToggleGroup(positionGroup);
        btnAll.setSelected(true);

        searchField.textProperty().addListener((obs, oldValue, newValue) -> refreshGrid());

        SessionManager.getNetworkThread().setListener(this);
        SessionManager.getNetworkThread().sendMessage(new MarketMessage(MarketMessage.Type.GET_SCOUT_PLAYERS));
        statusLabel.setText("Loading scout report...");
    }

    @FXML
    private void filterPosition(ActionEvent event) {
        ToggleButton button = (ToggleButton) event.getSource();
        currentPosition = (String) button.getUserData();
        refreshGrid();
    }

    @FXML
    private void goHome(ActionEvent event) {
        SceneSwitcher.switchScene("prehome-view.fxml", event, 1100, 720);
    }

    @FXML
    private void goToMarket(ActionEvent event) {
        SceneSwitcher.switchScene("transfer-market.fxml", event, 1100, 720);
    }

    private void refreshGrid() {
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        List<Player> filtered = scoutPlayers.stream()
                .filter(player -> currentPosition.equals("ALL") || currentPosition.equals(player.getPosition()))
                .filter(player -> search.isBlank()
                        || player.getName().toLowerCase().contains(search)
                        || player.getClub().toLowerCase().contains(search))
                .collect(Collectors.toList());

        playerGrid.getChildren().clear();
        for (Player player : filtered) {
            playerGrid.getChildren().add(buildCard(player));
        }

        countLabel.setText(filtered.size() + (filtered.size() == 1 ? " player available" : " players available"));
    }

    private VBox buildCard(Player player) {
        VBox card = new VBox(8);
        card.setPrefWidth(210);
        card.setStyle("""
                -fx-background-color: #16213e;
                -fx-background-radius: 14;
                -fx-padding: 14;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 10, 0, 0, 3);
                """);

        ImageView photo = new ImageView();
        photo.setFitWidth(182);
        photo.setFitHeight(150);
        photo.setPreserveRatio(true);
        Image img = loadPlayerImage(player);
        if (img != null) {
            photo.setImage(img);
        }
        Rectangle clip = new Rectangle(182, 150);
        clip.setArcWidth(14);
        clip.setArcHeight(14);
        photo.setClip(clip);

        Label name = new Label(player.getName());
        name.setWrapText(true);
        name.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");

        Label club = new Label(player.getClub() + " | " + player.getPosition());
        club.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12;");

        Label rating = new Label("Rating: " + player.getRating());
        rating.setStyle("-fx-text-fill: #facc15; -fx-font-size: 12; -fx-font-weight: bold;");

        Label marketStatus = new Label(player.isOnMarket()
                ? "Currently listed on transfer market"
                : "Available for private offer");
        marketStatus.setStyle("-fx-text-fill: " + (player.isOnMarket() ? "#38bdf8" : "#94a3b8") + "; -fx-font-size: 11;");

        Button offerBtn = new Button("Make Offer");
        offerBtn.setMaxWidth(Double.MAX_VALUE);
        offerBtn.setStyle("""
                -fx-background-color: #22c55e;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-background-radius: 8;
                -fx-padding: 8 14;
                -fx-cursor: hand;
                """);
        if (player.isOnMarket()) {
            offerBtn.setDisable(true);
            offerBtn.setText("Use Transfer Market");
            offerBtn.setStyle("""
                    -fx-background-color: #475569;
                    -fx-text-fill: white;
                    -fx-font-weight: bold;
                    -fx-background-radius: 8;
                    -fx-padding: 8 14;
                    """);
        } else {
            offerBtn.setOnAction(event -> openOfferDialog(player));
        }

        card.getChildren().addAll(photo, name, club, rating, marketStatus, offerBtn);
        return card;
    }

    private void openOfferDialog(Player player) {
        if (ClubRegistry.sameClub(player.getClub(), SessionManager.getLoggedInClubId())) {
            statusLabel.setText("You cannot make an offer for your own player.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(Math.max(1, player.getMarketValueM())));
        dialog.setTitle("Make Offer");
        dialog.setHeaderText("Submit an offer for " + player.getName());
        dialog.setContentText("Offer price (GBP M):");

        dialog.showAndWait().ifPresent(input -> {
            try {
                double price = Double.parseDouble(input.trim());
                if (price <= 0) {
                    showError("Offer price must be greater than 0.");
                    return;
                }

                TransferOffer offer = new TransferOffer();
                offer.setPlayerId(player.getName());
                offer.setPlayerName(player.getName());
                offer.setPlayerClubId(ClubRegistry.toCode(player.getClub()));
                offer.setPlayerClubName(ClubRegistry.toDisplay(player.getClub()));
                offer.setTargetClubId(ClubRegistry.toCode(player.getClub()));
                offer.setTargetClubName(ClubRegistry.toDisplay(player.getClub()));
                offer.setPrice(price);

                MarketMessage message = new MarketMessage(MarketMessage.Type.MAKE_OFFER);
                message.setPayload(offer);
                SessionManager.getNetworkThread().sendMessage(message);
                statusLabel.setText("Submitting offer for " + player.getName() + "...");
            } catch (NumberFormatException ex) {
                showError("Please enter a valid number.");
            }
        });
    }

    @Override
    public void onMessageReceived(MarketMessage msg) {
        switch (msg.getType()) {
            case SCOUT_PLAYERS_UPDATE -> {
                @SuppressWarnings("unchecked")
                List<Player> players = (List<Player>) msg.getPayload();
                scoutPlayers = players;
                refreshGrid();
                statusLabel.setText("Scout report updated.");
            }
            case OFFER_OK -> {
                TransferOffer offer = (TransferOffer) msg.getPayload();
                statusLabel.setText("Offer sent to " + offer.getTargetClubName() + " for " + offer.getPlayerName() + ".");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Offer Submitted");
                alert.setHeaderText(null);
                alert.setContentText("Offer sent for " + offer.getPlayerName() + " at GBP "
                        + String.format("%,.0f", offer.getPrice()) + "M.");
                alert.showAndWait();
            }
            case OFFER_STATUS_UPDATE -> {
                TransferOffer offer = (TransferOffer) msg.getPayload();
                statusLabel.setText("Offer update: " + offer.getPlayerName() + " is now "
                        + offer.getStatus().name().toLowerCase() + ".");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Offer Update");
                alert.setHeaderText(null);
                alert.setContentText(offer.getPlayerName() + ": " + offer.getDecisionNote());
                alert.showAndWait();
            }
            case ERROR -> showError(String.valueOf(msg.getPayload()));
            default -> {
            }
        }
    }

    @Override
    public void onConnectionFailed(String reason) {
        statusLabel.setText("Connection lost: " + reason);
    }

    private void showError(String message) {
        statusLabel.setText(message);
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Action Failed");
        alert.showAndWait();
    }

    private Image loadPlayerImage(Player player) {
        if (player.getImagePath() == null || player.getImagePath().isBlank()) {
            String imageName = player.getName().toLowerCase()
                    .replaceAll("\\s+", "_")
                    .replaceAll("[^a-z0-9_]", "")
                    + ".png";
            player.setImagePath("org/buet/fantasymanagerxi/images/players/" + imageName);
        }
        try {
            InputStream is = getClass().getResourceAsStream("/" + player.getImagePath());
            if (is != null) {
                return new Image(is);
            }
        } catch (Exception ignored) {
        }

        try {
            File file = new File(player.getImagePath());
            if (file.exists()) {
                return new Image(file.toURI().toString());
            }
        } catch (Exception ignored) {
        }

        return null;
    }
}
