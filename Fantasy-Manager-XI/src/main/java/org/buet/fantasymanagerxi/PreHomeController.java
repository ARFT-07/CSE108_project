package org.buet.fantasymanagerxi;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.buet.fantasymanagerxi.model.MarketMessage;
import org.buet.fantasymanagerxi.model.TransferOffer;
import org.buet.fantasymanagerxi.util.SceneSwitcher;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class PreHomeController implements NetworkThread.MessageListener {

    @FXML private Label clubNameLabel;
    @FXML private Label offersSummaryLabel;
    @FXML private VBox offersContainer;

    @FXML
    public void initialize() {
        clubNameLabel.setText(SessionManager.getLoggedInClubName() != null
                ? SessionManager.getLoggedInClubName()
                : "Club Administrator");

        if (SessionManager.getNetworkThread() != null) {
            SessionManager.getNetworkThread().setListener(this);
            SessionManager.getNetworkThread().sendMessage(new MarketMessage(MarketMessage.Type.GET_OFFERS));
        }

        renderOffers(List.of());
    }

    public void movetoplayerDb(ActionEvent actionEvent) {
        SceneSwitcher.switchScene("player-db.fxml", actionEvent, 1100, 720);
    }

    @FXML
    private void movetoMyPlayers(ActionEvent event) {
        SceneSwitcher.switchScene("my-team.fxml", event, 1100, 720);
    }

    @FXML
    private void movetoHomePage(ActionEvent event) {
        SceneSwitcher.switchScene("homepage-view.fxml", event, 1200, 800);
    }

    @FXML
    private void movetoTransferPage(ActionEvent event) {
        SceneSwitcher.switchScene("transfer-market.fxml", event, 1100, 720);
    }

    @FXML
    private void movetoScoutPlayers(ActionEvent event) {
        SceneSwitcher.switchScene("scout-players.fxml", event, 1200, 760);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.logout();

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/buet/fantasymanagerxi/fxml/login-view.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) offersContainer.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(
                    getClass().getResource("/org/buet/fantasymanagerxi/css/login_style.css").toExternalForm()
            );
            stage.setTitle("Fantasy League XI");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(MarketMessage msg) {
        switch (msg.getType()) {
            case OFFERS_UPDATE -> {
                @SuppressWarnings("unchecked")
                List<TransferOffer> offers = (List<TransferOffer>) msg.getPayload();
                renderOffers(offers);
            }
            case OFFER_STATUS_UPDATE -> {
                TransferOffer offer = (TransferOffer) msg.getPayload();
                if (offer.getStatus() == TransferOffer.Status.ACCEPTED) {
                    showInfo("Offer accepted for " + offer.getPlayerName() + ".");
                } else if (offer.getStatus() == TransferOffer.Status.REJECTED) {
                    showInfo(offer.getDecisionNote());
                } else if (offer.getStatus() == TransferOffer.Status.EXPIRED) {
                    showInfo("Offer expired for " + offer.getPlayerName() + ".");
                }
                SessionManager.getNetworkThread().sendMessage(new MarketMessage(MarketMessage.Type.GET_OFFERS));
            }
            case ERROR -> offersSummaryLabel.setText(String.valueOf(msg.getPayload()));
            default -> {
            }
        }
    }

    @Override
    public void onConnectionFailed(String reason) {
        offersSummaryLabel.setText("Connection lost: " + reason);
    }

    private void renderOffers(List<TransferOffer> offers) {
        offersContainer.getChildren().clear();

        if (offers == null || offers.isEmpty()) {
            offersSummaryLabel.setText("No incoming offers");
            Label empty = new Label("No clubs have submitted transfer offers yet.");
            empty.setWrapText(true);
            empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13;");
            offersContainer.getChildren().add(empty);
            return;
        }

        offersSummaryLabel.setText(offers.size() + (offers.size() == 1 ? " incoming offer" : " incoming offers"));

        for (TransferOffer offer : offers) {
            VBox card = new VBox(10);
            card.setStyle("""
                    -fx-background-color: #0f172a;
                    -fx-background-radius: 12;
                    -fx-border-color: #334155;
                    -fx-border-radius: 12;
                    -fx-padding: 14;
                    """);

            Label player = new Label(offer.getPlayerName());
            player.setStyle("-fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold;");

            Label club = new Label("From: " + offer.getOfferingClubName());
            club.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12;");

            Label price = new Label("Offer: GBP " + String.format("%,.0f", offer.getPrice()) + "M");
            price.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 13; -fx-font-weight: bold;");

            Label status = new Label(buildStatusText(offer));
            status.setWrapText(true);
            status.setStyle("-fx-text-fill: " + statusColor(offer.getStatus()) + "; -fx-font-size: 12; -fx-font-weight: bold;");

            card.getChildren().addAll(player, club, price, status);

            if (offer.getStatus() == TransferOffer.Status.PENDING) {
                HBox actions = new HBox(10);
                actions.setAlignment(Pos.CENTER_LEFT);

                Button acceptBtn = new Button("Accept Offer");
                acceptBtn.setStyle("""
                        -fx-background-color: #16a34a;
                        -fx-text-fill: white;
                        -fx-font-weight: bold;
                        -fx-background-radius: 8;
                        -fx-padding: 8 14;
                        -fx-cursor: hand;
                        """);
                acceptBtn.setOnAction(event -> handleOfferDecision(offer.getOfferId(), true));

                Button rejectBtn = new Button("Reject Offer");
                rejectBtn.setStyle("""
                        -fx-background-color: #b91c1c;
                        -fx-text-fill: white;
                        -fx-font-weight: bold;
                        -fx-background-radius: 8;
                        -fx-padding: 8 14;
                        -fx-cursor: hand;
                        """);
                rejectBtn.setOnAction(event -> handleOfferDecision(offer.getOfferId(), false));

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                actions.getChildren().addAll(acceptBtn, rejectBtn, spacer);
                card.getChildren().add(actions);
            }

            offersContainer.getChildren().add(card);
        }
    }

    private void handleOfferDecision(String offerId, boolean accept) {
        MarketMessage request = new MarketMessage(
                accept ? MarketMessage.Type.ACCEPT_OFFER : MarketMessage.Type.REJECT_OFFER
        );
        request.setOfferId(offerId);
        SessionManager.getNetworkThread().sendMessage(request);
    }

    private String buildStatusText(TransferOffer offer) {
        return switch (offer.getStatus()) {
            case PENDING -> "Pending review | expires " + formatTimeLeft(offer.getExpiresAtEpochMillis());
            case ACCEPTED -> "Accepted";
            case REJECTED -> offer.getDecisionNote() != null ? offer.getDecisionNote() : "Rejected";
            case EXPIRED -> "Expired";
        };
    }

    private String formatTimeLeft(long expiresAtEpochMillis) {
        long millisLeft = Math.max(0L, expiresAtEpochMillis - System.currentTimeMillis());
        Duration duration = Duration.ofMillis(millisLeft);
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m";
    }

    private String statusColor(TransferOffer.Status status) {
        return switch (status) {
            case PENDING -> "#facc15";
            case ACCEPTED -> "#22c55e";
            case REJECTED -> "#f97316";
            case EXPIRED -> "#94a3b8";
        };
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Offer Update");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
