package org.buet.fantasymanagerxi;

import javafx.event.ActionEvent;
import org.buet.fantasymanagerxi.model.MarketMessage;
import org.buet.fantasymanagerxi.model.Player;
import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.buet.fantasymanagerxi.util.SceneSwitcher;

import java.io.*;
import java.util.List;

public class TransferMarketController implements NetworkThread.MessageListener {

    @FXML private FlowPane marketGrid;
    @FXML private Label    countLabel;
    @FXML private Label    clubLabel;
    @FXML private Label    statusLabel;
    @FXML private Button   backBtn;

    private List<Player> listings;

    @FXML
    public void initialize() {
        // Register as the active listener
        SessionManager.getNetworkThread().setListener(this);

        clubLabel.setText("Logged in as: " + SessionManager.getLoggedInClub());
        statusLabel.setText("Ready.");

        // Request fresh market data from server
        MarketMessage msg = new MarketMessage(MarketMessage.Type.GET_MARKET);
        SessionManager.getNetworkThread().sendMessage(msg);
    }

    private void refreshGrid() {
        marketGrid.getChildren().clear();

        if (listings == null || listings.isEmpty()) {
            Label empty = new Label("No players currently listed on the market.");
            empty.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 14;");
            marketGrid.getChildren().add(empty);
            countLabel.setText("0 players listed");
            return;
        }

        for (Player p : listings) {
            marketGrid.getChildren().add(buildMarketCard(p));
        }
        countLabel.setText(listings.size() + " player" +
                (listings.size() != 1 ? "s" : "") + " listed");
    }

    private VBox buildMarketCard(Player p) {
        VBox card = new VBox(8);
        card.setPrefWidth(180);
        card.setStyle("""
            -fx-background-color: #16213e;
            -fx-background-radius: 12;
            -fx-padding: 12;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);
            """);

        // Photo
        ImageView photo = new ImageView();
        photo.setFitWidth(156);
        photo.setFitHeight(156);
        photo.setPreserveRatio(true);
        Image img = loadPlayerImage(p);
        if (img != null) photo.setImage(img);
        Rectangle clip = new Rectangle(156, 156);
        clip.setArcWidth(12);
        clip.setArcHeight(12);
        photo.setClip(clip);

        // Position badge
        Label posBadge = new Label(p.getPosition());
        posBadge.setStyle(
                "-fx-background-color: " + p.getPositionColor() + ";" +
                        "-fx-text-fill: white; -fx-font-size: 11; -fx-font-weight: bold;" +
                        "-fx-background-radius: 4; -fx-padding: 2 6 2 6;"
        );

        // Name
        Label name = new Label(p.getName());
        name.setWrapText(true);
        name.setStyle("-fx-text-fill: white; -fx-font-size: 13; " +
                "-fx-font-weight: bold;");

        // Selling club
        Label fromClub = new Label("From: " + p.getClub());
        fromClub.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11;");

        // Rating
        Label rating = new Label("★ " + p.getRating());
        rating.setStyle("-fx-text-fill: #F1C40F; -fx-font-size: 12; " +
                "-fx-font-weight: bold;");

        // Asking price
        Label price = new Label("£" + String.format("%,.0f", p.getAskingPrice()) + "M");
        price.setStyle("-fx-text-fill: #2ECC71; -fx-font-size: 13; " +
                "-fx-font-weight: bold;");

        // Buy button — disabled if this is your own player
        Button buyBtn = new Button("Buy");
        boolean isOwnPlayer = p.getClub().equals(SessionManager.getLoggedInClub());
        buyBtn.setDisable(isOwnPlayer);
        buyBtn.setStyle("-fx-background-color: " + (isOwnPlayer ? "#555555" : "#e94560") +
                "; -fx-text-fill: white; -fx-background-radius: 6;" +
                " -fx-padding: 6 16; -fx-cursor: hand;");
        buyBtn.setMaxWidth(Double.MAX_VALUE);
        buyBtn.setOnAction(e -> confirmAndBuy(p));

        card.getChildren().addAll(photo, posBadge, name, fromClub, rating, price, buyBtn);

        card.setOnMouseEntered(e -> card.setStyle("""
            -fx-background-color: #0f3460;
            -fx-background-radius: 12;
            -fx-padding: 12;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(233,69,96,0.5), 14, 0, 0, 0);
            """));
        card.setOnMouseExited(e -> card.setStyle("""
            -fx-background-color: #16213e;
            -fx-background-radius: 12;
            -fx-padding: 12;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);
            """));

        return card;
    }

    private void confirmAndBuy(Player p) {
        // Show confirmation dialog before buying
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Purchase");
        alert.setHeaderText("Buy " + p.getName() + "?");
        alert.setContentText("Asking price: £" +
                String.format("%,.0f", p.getAskingPrice()) + "M\n" +
                "This player will be added to your squad.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                statusLabel.setText("Purchasing " + p.getName() + "...");

                MarketMessage msg = new MarketMessage(MarketMessage.Type.BUY_PLAYER);
                msg.setPlayerId(p.getName());
                SessionManager.getNetworkThread().sendMessage(msg);
            }
        });
    }

    @Override
    public void onMessageReceived(MarketMessage msg) {
        switch (msg.getType()) {

            case MARKET_UPDATE -> {
                @SuppressWarnings("unchecked")
                List<Player> updated = (List<Player>) msg.getPayload();
                listings = updated;
                refreshGrid();
                statusLabel.setText("Market updated.");
            }

            case BUY_OK -> {
                Player bought = (Player) msg.getPayload();

                // Add the new player to the local squad
                SessionManager.getSquad().add(bought);

                statusLabel.setText(bought.getName() +
                        " successfully added to your squad!");

                // Request updated market to reflect the purchase
                MarketMessage req = new MarketMessage(MarketMessage.Type.GET_MARKET);
                SessionManager.getNetworkThread().sendMessage(req);
            }

            case ERROR -> {
                String error = (String) msg.getPayload();
                statusLabel.setText("Error: " + error);

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Purchase Failed");
                alert.setHeaderText(null);
                alert.setContentText(error);
                alert.showAndWait();

                // Refresh market in case the player was already sold
                MarketMessage req = new MarketMessage(MarketMessage.Type.GET_MARKET);
                SessionManager.getNetworkThread().sendMessage(req);
            }
            case SQUAD_UPDATE -> {
                @SuppressWarnings("unchecked")
                List<Player> updatedSquad = (List<Player>) msg.getPayload();
                SessionManager.setSquad(updatedSquad);
            }

            default -> {}
        }
    }

    @Override
    public void onConnectionFailed(String reason) {
        statusLabel.setText("Connection lost: " + reason);
    }

    private Image loadPlayerImage(Player p) {
        if (p.getImagePath() == null) {
            String imageName = p.getName()
                    .toLowerCase()
                    .replaceAll("\\s+", "_")
                    .replaceAll("[^a-z0-9_]", "")
                    + ".png";
            p.setImagePath("org/buet/fantasymanagerxi/images/players/" + imageName);
        }
        return new Image(p.getImagePath());
    }

    @FXML

    private void gotoPlayerDB(ActionEvent event) {

        SceneSwitcher.switchScene("player-db.fxml", event, 1100, 720);
    }

    public void gotoHome(ActionEvent event) {
        SceneSwitcher.switchScene("prehome-view.fxml", event, 1100, 720);
    }
}