package org.buet.fantasymanagerxi;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.buet.fantasymanagerxi.model.Formation;
import org.buet.fantasymanagerxi.model.Manager;
import org.buet.fantasymanagerxi.model.Player;
import org.buet.fantasymanagerxi.model.UserTeam;
import org.buet.fantasymanagerxi.util.ManagerDataManager;
import org.buet.fantasymanagerxi.util.PlayerDataManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class MyTeamController {

    @FXML private ImageView        managerPhoto;
    @FXML private Label            managerNameLabel;
    @FXML private Label            managerClubLabel;
    @FXML private Label            managerNatLabel;
    @FXML private Label            managerAgeLabel;
    @FXML private Label            managerSinceLabel;
    @FXML private Label            budgetLabel;
    @FXML private Label            trophiesLabel;

    @FXML private StackPane        pitchStack;
    @FXML private ImageView        pitchImageView;
    @FXML private VBox             pitchRows;
    @FXML private HBox             attackRow;
    @FXML private HBox             midfieldRow;
    @FXML private HBox             defenceRow;
    @FXML private HBox             gkRow;

    @FXML private ListView<Player> subsListView;
    @FXML private ComboBox<String> formationCombo;
    @FXML private ComboBox<String> clubCombo;

    private final UserTeam userTeam     = UserTeam.getInstance();
    private       int      selectedSlot = -1;

    private Image cardBgImage;
    private Image placeholderImage;

    private static final int[] SLOTS_433  = {3, 3, 4, 1};
    private static final int[] SLOTS_4231 = {1, 5, 4, 1};
    private static final int[] SLOTS_352  = {2, 5, 3, 1};

    private static final int CARD_W     = 85;
    private static final int CARD_H     = 110;
    private static final int PHOTO_SIZE = 52;

    private static final List<String> CLUBS = List.of(
            "Manchester City", "Arsenal", "Liverpool",
            "Chelsea", "Manchester United", "Tottenham Hotspur"
    );

    @FXML
    public void initialize() {
        loadSharedImages();
        loadData();
        loadCss();
        setupPitchImage();
        setupClubCombo();
        setupFormationCombo();

        String existing = userTeam.getManagerClub();
        String startClub = (existing != null && CLUBS.contains(existing))
                ? existing : CLUBS.get(0);

        clubCombo.setValue(startClub);
        loadSquadForClub(startClub);
        loadManagerCard(startClub);
        renderPitch();
        setupSubsList();
    }

    // ── Load CSS manually so it definitely applies ────────────────────────────

    private void loadCss() {
        try {
            String css = getClass().getResource(
                    "/org/buet/fantasymanagerxi/css/myteam-style.css"
            ).toExternalForm();
            // applied after scene is set — do it via runLater
            javafx.application.Platform.runLater(() -> {
                if (pitchStack.getScene() != null) {
                    pitchStack.getScene().getStylesheets().add(css);
                }
            });
        } catch (Exception e) {
            System.out.println("CSS load failed: " + e.getMessage());
        }
    }

    // ── Load shared images once ───────────────────────────────────────────────

    private void loadSharedImages() {
        cardBgImage     = loadImage("/org/buet/fantasymanagerxi/images/bg_card.png");
        placeholderImage = loadImage("/org/buet/fantasymanagerxi/images/players/placeholder.png");
        if (cardBgImage     == null) System.out.println("WARN: bg_card.png not found");
        if (placeholderImage == null) System.out.println("WARN: placeholder.png not found");
    }

    private void loadData() {
        if (!PlayerDataManager.isLoaded()) {
            InputStream is = getClass().getResourceAsStream(
                    "/org/buet/fantasymanagerxi/data/players_db.txt");
            if (is != null) PlayerDataManager.loadFromStream(is);
        }
        if (!ManagerDataManager.isLoaded()) {
            InputStream is = getClass().getResourceAsStream(
                    "/org/buet/fantasymanagerxi/data/manager_data.txt");
            if (is != null) ManagerDataManager.loadFromStream(is);
        }
    }

    private void setupPitchImage() {
        Image pitch = loadImage("/org/buet/fantasymanagerxi/images/pitch.png");
        if (pitch != null) {
            pitchImageView.setImage(pitch);
        } else {
            System.out.println("WARN: pitch.png not found");
        }
    }

    // ── Club combo ────────────────────────────────────────────────────────────

    private void setupClubCombo() {
        clubCombo.getItems().addAll(CLUBS);
    }

    @FXML
    private void onClubChanged() {
        String club = clubCombo.getValue();
        if (club == null) return;
        userTeam.setManagerClub(club);
        loadSquadForClub(club);
        loadManagerCard(club);
        selectedSlot = -1;
        renderPitch();
        setupSubsList();
    }

    // ── Load squad from players_db ────────────────────────────────────────────

    private void loadSquadForClub(String club) {
        for (int i = 0; i < 11; i++) userTeam.setPlayerInSlot(i, null);
        userTeam.getSubs().clear();

        int[] counts  = getSlotCounts(userTeam.getFormation());
        List<Player> all  = PlayerDataManager.filter(club, "ALL", "");
        List<Player> gks  = byPos(all, "GK");
        List<Player> defs = byPos(all, "DEF");
        List<Player> mids = byPos(all, "MID");
        List<Player> fwds = byPos(all, "FWD");

        int slot = 0;
        for (int i = 0; i < counts[0] && i < fwds.size(); i++) userTeam.setPlayerInSlot(slot++, fwds.get(i));
        for (int i = 0; i < counts[1] && i < mids.size(); i++) userTeam.setPlayerInSlot(slot++, mids.get(i));
        for (int i = 0; i < counts[2] && i < defs.size(); i++) userTeam.setPlayerInSlot(slot++, defs.get(i));
        if (!gks.isEmpty()) userTeam.setPlayerInSlot(slot, gks.get(0));

        int added = 0;
        for (Player p : all) {
            if (!userTeam.isInSXI(p) && added < 7) {
                userTeam.addSubstitute(p);
                added++;
            }
        }
    }

    private List<Player> byPos(List<Player> players, String pos) {
        return players.stream()
                .filter(p -> p.getPosition().equalsIgnoreCase(pos))
                .sorted((a, b) -> Double.compare(b.getRating(), a.getRating()))
                .collect(Collectors.toList());
    }

    // ── Manager card ──────────────────────────────────────────────────────────

    private void loadManagerCard(String club) {
        Manager m = ManagerDataManager.getManagerForClub(club);
        if (m == null) {
            managerNameLabel.setText("Unknown");
            managerClubLabel.setText(club);
            managerNatLabel.setText("—");
            managerAgeLabel.setText("");
            managerSinceLabel.setText("");
            budgetLabel.setText(userTeam.getBudgetFormatted());
            trophiesLabel.setText("—");
            return;
        }
        managerNameLabel.setText(m.getName());
        managerClubLabel.setText(m.getClub());
        managerNatLabel.setText(m.getNationality());
        managerAgeLabel.setText("Age " + m.getAge());
        managerSinceLabel.setText("Since " + m.getSince());
        budgetLabel.setText(userTeam.getBudgetFormatted());
        trophiesLabel.setText(m.getTrophies());

        Image img = loadImage("/" + m.getImagePath());
        if (img != null) {
            managerPhoto.setImage(img);
            Rectangle clip = new Rectangle(110, 110);
            clip.setArcWidth(110); clip.setArcHeight(110);
            managerPhoto.setClip(clip);
        }
    }

    // ── Formation ─────────────────────────────────────────────────────────────

    private void setupFormationCombo() {
        formationCombo.getItems().addAll(Formation.getAvailableFormations());
        formationCombo.setValue(userTeam.getFormation());
    }

    @FXML
    private void onFormationChanged() {
        String f = formationCombo.getValue();
        if (f == null) return;
        userTeam.setFormation(f);
        String club = clubCombo.getValue();
        if (club != null) loadSquadForClub(club);
        selectedSlot = -1;
        renderPitch();
        setupSubsList();
    }

    // ── Pitch rendering ───────────────────────────────────────────────────────

    private void renderPitch() {
        attackRow.getChildren().clear();
        midfieldRow.getChildren().clear();
        defenceRow.getChildren().clear();
        gkRow.getChildren().clear();

        List<String> labels = Formation.getLabels(userTeam.getFormation());
        List<Player> xi     = userTeam.getStartingXI();
        int[]        counts = getSlotCounts(userTeam.getFormation());

        int idx = 0;
        idx = fillRow(attackRow,   labels, xi, idx, counts[0]);
        idx = fillRow(midfieldRow, labels, xi, idx, counts[1]);
        idx = fillRow(defenceRow,  labels, xi, idx, counts[2]);
              fillRow(gkRow,       labels, xi, idx, counts[3]);
    }

    private int fillRow(HBox row, List<String> labels, List<Player> xi,
                        int start, int count) {
        Region left = new Region();
        HBox.setHgrow(left, Priority.ALWAYS);
        row.getChildren().add(left);

        for (int i = 0; i < count; i++) {
            row.getChildren().add(buildCard(start + i, labels.get(start + i), xi.get(start + i)));
            if (i < count - 1) {
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                row.getChildren().add(spacer);
            }
        }

        Region right = new Region();
        HBox.setHgrow(right, Priority.ALWAYS);
        row.getChildren().add(right);

        return start + count;
    }

    // ── Build card as StackPane ───────────────────────────────────────────────

    private StackPane buildCard(int slotIndex, String posLabel, Player player) {
        StackPane card = new StackPane();
        card.getStyleClass().add("player-card-pane");
        card.setPrefWidth(CARD_W);
        card.setMaxWidth(CARD_W);
        card.setPrefHeight(CARD_H);
        card.setMaxHeight(CARD_H);

        if (slotIndex == selectedSlot) card.getStyleClass().add("selected");

        // Layer 1 — card background image
        if (cardBgImage != null) {
            ImageView bg = new ImageView(cardBgImage);
            bg.setFitWidth(CARD_W);
            bg.setFitHeight(CARD_H);
            bg.setPreserveRatio(false);
            card.getChildren().add(bg);
        } else {
            // fallback solid gold background if image missing
            card.setStyle("-fx-background-color: linear-gradient(to bottom, #c8a84b, #a07830); -fx-background-radius: 8;");
        }

        // Layer 2 — content
        VBox content = new VBox(3);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(CARD_W);
        content.setMaxHeight(CARD_H);
        content.setStyle("-fx-background-color: transparent; -fx-padding: 5 4 5 4;");

        if (player == null) {
            Label posLbl = new Label(posLabel);
            posLbl.setStyle("-fx-text-fill: rgba(200,168,75,0.7); -fx-font-size: 10px;");
            content.getChildren().add(posLbl);
        } else {
            // rating + position top-left
            VBox topInfo = new VBox(1);
            topInfo.setAlignment(Pos.TOP_LEFT);
            Label ratingLbl = new Label(String.valueOf((int) player.getRating()));
            ratingLbl.setStyle("-fx-text-fill: #1a0030; -fx-font-size: 15px; -fx-font-weight: bold;");
            Label posLbl = new Label(posLabel);
            posLbl.setStyle("-fx-text-fill: #1a0030; -fx-font-size: 10px; -fx-font-weight: bold;");
            topInfo.getChildren().addAll(ratingLbl, posLbl);

            // photo
            ImageView photo = new ImageView();
            photo.setFitWidth(PHOTO_SIZE);
            photo.setFitHeight(PHOTO_SIZE);
            photo.setPreserveRatio(true);
            Rectangle clip = new Rectangle(PHOTO_SIZE, PHOTO_SIZE);
            clip.setArcWidth(PHOTO_SIZE); clip.setArcHeight(PHOTO_SIZE);
            photo.setClip(clip);
            Image img = loadImage("/" + player.getImagePath());
            photo.setImage(img != null ? img : placeholderImage);

            // name — white with shadow
            Label nameLbl = new Label(shortName(player.getName()));
            nameLbl.setStyle(
                "-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;" +
                "-fx-alignment: center; -fx-wrap-text: true; -fx-max-width: 82;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.95), 3, 0, 0, 1);"
            );

            content.getChildren().addAll(topInfo, photo, nameLbl);
        }

        card.getChildren().add(content);
        card.setOnMouseClicked(e -> onCardClicked(slotIndex));
        return card;
    }

    private void onCardClicked(int slotIndex) {
        if (selectedSlot == -1) {
            selectedSlot = slotIndex;
        } else if (selectedSlot == slotIndex) {
            selectedSlot = -1;
        } else {
            Player a = userTeam.getPlayerInSlot(selectedSlot);
            Player b = userTeam.getPlayerInSlot(slotIndex);
            userTeam.setPlayerInSlot(selectedSlot, b);
            userTeam.setPlayerInSlot(slotIndex, a);
            selectedSlot = -1;
        }
        renderPitch();
    }

    // ── Subs list ─────────────────────────────────────────────────────────────

    private void setupSubsList() {
        subsListView.setItems(userTeam.getSubs());
        subsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Player p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null
                        : (int) p.getRating() + "  " + p.getPosition() + "  " + p.getName());
            }
        });
        subsListView.setOnMouseClicked(e -> {
            Player sub = subsListView.getSelectionModel().getSelectedItem();
            if (sub != null && selectedSlot != -1) {
                userTeam.benchPlayer(selectedSlot, sub);
                selectedSlot = -1;
                renderPitch();
            }
        });
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML
    private void goToAllPlayers() {
        navigateTo("/org/buet/fantasymanagerxi/fxml/player-db.fxml", 1100, 720);
    }

    @FXML
    private void goToTransferMarket() {
        navigateTo("/org/buet/fantasymanagerxi/fxml/transfer-market.fxml", 1100, 720);
    }

    private void navigateTo(String path, int w, int h) {
        try {
            Parent root  = FXMLLoader.load(getClass().getResource(path));
            Stage  stage = (Stage) pitchStack.getScene().getWindow();
            stage.setScene(new Scene(root, w, h));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int[] getSlotCounts(String formation) {
        return switch (formation) {
            case Formation.F_4_2_3_1 -> SLOTS_4231;
            case Formation.F_3_5_2   -> SLOTS_352;
            default                  -> SLOTS_433;
        };
    }

    private Image loadImage(String path) {
        if (path == null || path.isBlank()) return null;
        try {
            String p = path.startsWith("/") ? path : "/" + path;
            InputStream is = getClass().getResourceAsStream(p);
            if (is != null) return new Image(is);
        } catch (Exception ignored) {}
        return null;
    }

    private String shortName(String full) {
        if (full == null || full.isBlank()) return "";
        String[] parts = full.trim().split(" ");
        return parts[parts.length - 1];
    }
}
