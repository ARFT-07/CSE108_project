package org.buet.fantasymanagerxi;

import org.buet.fantasymanagerxi.model.Player;
import org.buet.fantasymanagerxi.util.PlayerDataManager;
import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.*;
import java.util.List;

public class PlayerDBController {

    @FXML private TextField        searchField;
    @FXML private ComboBox<String> clubFilter;
    @FXML private FlowPane         playerGrid;
    @FXML private Label            countLabel;
    @FXML private ToggleButton     btnAll, btnGK, btnDEF, btnMID, btnFWD;
    @FXML private Button           backBtn;

    private ToggleGroup positionGroup;
    private String currentPosition = "ALL";

    @FXML
    public void initialize() {

        // ── Load data from bundled txt ────────────────────────────────────────
        if (!PlayerDataManager.isLoaded()) {
            InputStream is = getClass().getResourceAsStream(
                    "/org/buet/fantasymanagerxi/data/players_db.txt"
            );
            if (is != null) {
                PlayerDataManager.loadFromStream(is);
            } else {
                System.out.println("ERROR: players_db.txt not found in resources!");
            }
        }

        // ── Position toggle group ─────────────────────────────────────────────
        positionGroup = new ToggleGroup();
        btnAll.setToggleGroup(positionGroup);
        btnGK.setToggleGroup(positionGroup);
        btnDEF.setToggleGroup(positionGroup);
        btnMID.setToggleGroup(positionGroup);
        btnFWD.setToggleGroup(positionGroup);
        btnAll.setSelected(true);

        // ── Club dropdown ─────────────────────────────────────────────────────
        clubFilter.getItems().add("All Clubs");
        clubFilter.getItems().addAll(PlayerDataManager.getClubs());
        clubFilter.setValue("All Clubs");
        clubFilter.setOnAction(e -> refreshGrid());

        // ── Live search ───────────────────────────────────────────────────────
        searchField.textProperty().addListener((obs, o, n) -> refreshGrid());

        refreshGrid();
    }

    @FXML
    private void filterPosition(javafx.event.ActionEvent e) {
        ToggleButton btn = (ToggleButton) e.getSource();
        currentPosition = (String) btn.getUserData();
        refreshGrid();
    }

    private void refreshGrid() {
        String club   = clubFilter.getValue() == null ? "All Clubs" : clubFilter.getValue();
        String search = searchField.getText() == null ? "" : searchField.getText();
        List<Player> players = PlayerDataManager.filter(club, currentPosition, search);

        playerGrid.getChildren().clear();
        for (Player p : players) {
            playerGrid.getChildren().add(buildCard(p));
        }
        countLabel.setText(players.size() + " player" + (players.size() != 1 ? "s" : ""));
    }

    private VBox buildCard(Player p) {
        VBox card = new VBox(8);
        card.setPrefWidth(170);
        card.setStyle("""
            -fx-background-color: #16213e;
            -fx-background-radius: 12;
            -fx-padding: 12;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);
            """);

        // ── Photo ─────────────────────────────────────────────────────────────
        ImageView photo = new ImageView();
        photo.setFitWidth(146);
        photo.setFitHeight(146);
        photo.setPreserveRatio(true);
        Image img = loadPlayerImage(p);
        if (img != null) photo.setImage(img);
        Rectangle clip = new Rectangle(146, 146);
        clip.setArcWidth(12);
        clip.setArcHeight(12);
        photo.setClip(clip);

        // ── Position badge ────────────────────────────────────────────────────
        Label posBadge = new Label(p.getPosition());
        posBadge.setStyle(
                "-fx-background-color: " + p.getPositionColor() + ";" +
                        "-fx-text-fill: white; -fx-font-size: 11; -fx-font-weight: bold;" +
                        "-fx-background-radius: 4; -fx-padding: 2 6 2 6;"
        );

        // ── Name ──────────────────────────────────────────────────────────────
        Label name = new Label(p.getName());
        name.setWrapText(true);
        name.setStyle("-fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold;");

        // ── Club ──────────────────────────────────────────────────────────────
        Label club = new Label(p.getClub());
        club.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11;");

        // ── Rating ────────────────────────────────────────────────────────────
        Label rating = new Label("★ " + p.getRating());
        rating.setStyle("-fx-text-fill: #F1C40F; -fx-font-size: 12; -fx-font-weight: bold;");

        card.getChildren().addAll(photo, posBadge, name, club, rating);

        // ── Hover ─────────────────────────────────────────────────────────────
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

        // ── Click ─────────────────────────────────────────────────────────────
        card.setOnMouseClicked(e -> openDetail(p));

        return card;
    }

    private Image loadPlayerImage(Player p) {
        // 1. Try bundled resource
        try {
            InputStream is = getClass().getResourceAsStream("/" + p.getImagePath());
            if (is != null) return new Image(is);
        } catch (Exception ignored) {}

        // 2. Try file system
        try {
            File f = new File(p.getImagePath());
            if (f.exists()) return new Image(f.toURI().toString());
        } catch (Exception ignored) {}

        // 3. Placeholder
        try {
            InputStream is = getClass().getResourceAsStream(
                    "/org/buet/fantasymanagerxi/images/players/placeholder.png"
            );
            if (is != null) return new Image(is);
        } catch (Exception ignored) {}

        return null;
    }

    private void openDetail(Player p) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/buet/fantasymanagerxi/fxml/player-detail.fxml")
            );
            Parent root = loader.load();
            PlayerDetailController ctrl = loader.getController();
            ctrl.setPlayer(p);
            Stage stage = (Stage) playerGrid.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 720));
            stage.setTitle(p.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/org/buet/fantasymanagerxi/fxml/hello-view.fxml")
            );
            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 600));
            stage.setTitle("Fantasy League XI");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}