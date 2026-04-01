package org.buet.fantasymanagerxi;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.buet.fantasymanagerxi.model.Player;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MyTeamController {

    // ══════════════════════════════════════════════════════════════════════════
    //  FORMATIONS
    // ══════════════════════════════════════════════════════════════════════════
    private static final Map<String, List<List<String>>> FORMATIONS = new LinkedHashMap<>();

    static {
        FORMATIONS.put("4-3-3", List.of(
                List.of("LW", "ST", "RW"),
                List.of("LM", "CM", "RM"),
                List.of("LB", "CB", "CB", "RB"),
                List.of("GK")
        ));
        FORMATIONS.put("4-4-2", List.of(
                List.of("ST", "ST"),
                List.of("LM", "CM", "CM", "RM"),
                List.of("LB", "CB", "CB", "RB"),
                List.of("GK")
        ));
        FORMATIONS.put("4-2-3-1", List.of(
                List.of("ST"),
                List.of("LAM", "CAM", "RAM"),
                List.of("CDM", "CDM"),
                List.of("LB", "CB", "CB", "RB"),
                List.of("GK")
        ));
        FORMATIONS.put("3-5-2", List.of(
                List.of("ST", "ST"),
                List.of("LM", "CM", "CM", "CM", "RM"),
                List.of("CB", "CB", "CB"),
                List.of("GK")
        ));
        FORMATIONS.put("5-3-2", List.of(
                List.of("ST", "ST"),
                List.of("LM", "CM", "RM"),
                List.of("LB", "CB", "CB", "CB", "RB"),
                List.of("GK")
        ));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FXML NODES
    // ══════════════════════════════════════════════════════════════════════════
    @FXML private Pane   pitchPane;
    @FXML private VBox   subsPanel;
    @FXML private HBox   formationBar;
    @FXML private Label  clubLabel;
    @FXML private Label  statusLabel;
    @FXML private Label  budgetValueLabel;

    // ══════════════════════════════════════════════════════════════════════════
    //  STATE
    // ══════════════════════════════════════════════════════════════════════════
    private String currentFormation  = "4-3-3";
    private final List<Player>     startingXI  = new ArrayList<>();
    private final List<Player>     substitutes = new ArrayList<>();
    private Player  selectedPlayer   = null;
    private boolean selectedFromPitch = false;

    private final List<StackPane> pitchSlots  = new ArrayList<>();
    private final List<HBox>      benchCards  = new ArrayList<>();

    /** Cached card background image (bg_card.png). */
    private Image cardBgImage = null;
    /** Cached football field image. */
    private Image fieldImage  = null;

    // ══════════════════════════════════════════════════════════════════════════
    //  INITIALISE
    // ══════════════════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        // Pre-load card background
        cardBgImage = loadResource("/org/buet/fantasymanagerxi/images/bg_card.png");
        fieldImage  = loadResource("/org/buet/fantasymanagerxi/images/football_field.png");

        // Set pitch background programmatically so we can fall back gracefully
        if (fieldImage != null) {
            BackgroundImage bg = new BackgroundImage(
                    fieldImage,
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
            );
            pitchPane.setBackground(new Background(bg));
        }

        clubLabel.setText(SessionManager.getLoggedInClub() != null
                ? SessionManager.getLoggedInClub() : "My Team");

        buildFormationButtons();
        distributeSquad();
        renderPitch();
        renderBench();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SQUAD DISTRIBUTION
    // ══════════════════════════════════════════════════════════════════════════
    private void distributeSquad() {
        startingXI.clear();
        substitutes.clear();

        List<Player> squad = SessionManager.getSquad();
        if (squad == null || squad.isEmpty()) return;

        List<Player> sorted = new ArrayList<>(squad);
        sorted.sort(Comparator
                .comparingInt(MyTeamController::positionOrder)
                .thenComparingDouble(p -> -p.getRating()));

        List<List<String>> rows = FORMATIONS.get(currentFormation);
        int totalSlots = rows.stream().mapToInt(List::size).sum();

        for (int i = 0; i < sorted.size(); i++) {
            if (i < totalSlots) startingXI.add(sorted.get(i));
            else                substitutes.add(sorted.get(i));
        }
    }

    private static int positionOrder(Player p) {
        return switch (p.getPosition()) {
            case "GK"  -> 0;
            case "DEF" -> 1;
            case "MID" -> 2;
            case "FWD" -> 3;
            default    -> 4;
        };
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FORMATION BUTTONS
    // ══════════════════════════════════════════════════════════════════════════
    private void buildFormationButtons() {
        formationBar.getChildren().clear();

        Label lbl = new Label("FORMATION:");
        lbl.setStyle("-fx-text-fill:#c9a84c; -fx-font-size:11; -fx-font-weight:bold;");
        formationBar.getChildren().add(lbl);

        for (String name : FORMATIONS.keySet()) {
            Button btn = new Button(name);
            styleFormationBtn(btn, name.equals(currentFormation));
            btn.setOnAction(e -> switchFormation(name));
            formationBar.getChildren().add(btn);
        }
    }

    private void styleFormationBtn(Button btn, boolean active) {
        if (active) {
            btn.setStyle("""
                -fx-background-color: linear-gradient(to bottom, #c9a84c, #a0722a);
                -fx-text-fill: #0d1b2a;
                -fx-background-radius: 20;
                -fx-padding: 5 16;
                -fx-font-size: 11;
                -fx-font-weight: bold;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(201,168,76,0.6), 8, 0, 0, 2);
            """);
        } else {
            btn.setStyle("""
                -fx-background-color: rgba(201,168,76,0.08);
                -fx-text-fill: #c9a84c;
                -fx-border-color: rgba(201,168,76,0.4);
                -fx-border-radius: 20;
                -fx-background-radius: 20;
                -fx-padding: 5 16;
                -fx-font-size: 11;
                -fx-cursor: hand;
            """);
        }
    }

    private void switchFormation(String name) {
        currentFormation = name;
        selectedPlayer   = null;
        buildFormationButtons();
        distributeSquad();
        renderPitch();
        renderBench();
        setStatus("Formation changed to " + name);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PITCH RENDERING
    // ══════════════════════════════════════════════════════════════════════════
    private void renderPitch() {
        pitchPane.getChildren().clear();
        pitchSlots.clear();

        List<List<String>> rows = FORMATIONS.get(currentFormation);
        double pW = pitchPane.getPrefWidth();
        double pH = pitchPane.getPrefHeight();

        int numRows = rows.size();
        double rowH = pH / (numRows + 1);

        int slotIndex = 0;
        for (int r = 0; r < numRows; r++) {
            List<String> cols = rows.get(r);
            int numCols = cols.size();
            double colW = pW / (numCols + 1);
            double y = rowH * (r + 1);

            for (int c = 0; c < numCols; c++) {
                double x = colW * (c + 1);
                String posLabel = cols.get(c);

                Player p = slotIndex < startingXI.size() ? startingXI.get(slotIndex) : null;
                final int   finalSlot   = slotIndex;
                final Player finalPlayer = p;

                // Card is 80×106 — centre it on the slot coordinate
                StackPane card = buildFifaCard(p, posLabel, false);
                card.setLayoutX(x - 40);
                card.setLayoutY(y - 53);
                card.setOnMouseClicked(e -> handlePitchClick(finalSlot, finalPlayer, card));

                pitchPane.getChildren().add(card);
                pitchSlots.add(card);
                slotIndex++;
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FIFA-STYLE CARD BUILDER  (uses bg_card.png as background)
    // ══════════════════════════════════════════════════════════════════════════
    /**
     * Builds an 80×106 card that mimics the FIFA Ultimate Team gold card style.
     *
     * @param p          Player data (may be null for empty slots).
     * @param posLabel   Position label shown on the card (e.g. "ST", "CB").
     * @param selected   Whether to render the selected / highlighted state.
     */
    private StackPane buildFifaCard(Player p, String posLabel, boolean selected) {
        // ── Root container ─────────────────────────────────────────────────
        StackPane root = new StackPane();
        root.setPrefSize(80, 106);
        root.setMaxSize(80, 106);

        // ── Card background: bg_card.png, or fallback gold gradient ────────
        StackPane cardBg = new StackPane();
        cardBg.setPrefSize(80, 106);

        if (cardBgImage != null) {
            ImageView bgIv = new ImageView(cardBgImage);
            bgIv.setFitWidth(80);
            bgIv.setFitHeight(106);
            bgIv.setPreserveRatio(false);
            // Rectangle clip so the card never overflows its bounds
            Rectangle bgClip = new Rectangle(80, 106);
            bgIv.setClip(bgClip);
            cardBg.getChildren().add(bgIv);
        } else {
            // CSS fallback
            cardBg.setStyle("""
                -fx-background-color: linear-gradient(to bottom, #d4a820, #8b6914);
                -fx-background-radius: 8;
            """);
        }

        // ── Selection glow overlay ─────────────────────────────────────────
        if (selected) {
            cardBg.setStyle("-fx-effect: dropshadow(gaussian, #00FFA3, 16, 0.8, 0, 0);");
        }

        // ── Content VBox (sits on top of the card image) ───────────────────
        VBox content = new VBox(2);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(6, 4, 4, 4));
        content.setPrefSize(80, 106);
        content.setPickOnBounds(false); // let clicks pass to root

        // ── Rating (top-left corner) ───────────────────────────────────────
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.TOP_LEFT);
        VBox topLeft = new VBox(0);
        topLeft.setAlignment(Pos.TOP_LEFT);

        Label ratingLbl = new Label(p != null ? String.valueOf((int)p.getRating()) : "—");
        ratingLbl.setStyle("-fx-text-fill:#1a1200; -fx-font-size:13; -fx-font-weight:bold;");
        Label posTopLbl = new Label(p != null ? p.getPosition() : posLabel);
        posTopLbl.setStyle("-fx-text-fill:#2a1a00; -fx-font-size:7; -fx-font-weight:bold;");
        topLeft.getChildren().addAll(ratingLbl, posTopLbl);

        topRow.getChildren().add(topLeft);
        HBox.setHgrow(topLeft, Priority.NEVER);
        content.getChildren().add(topRow);

        // ── Player photo / initials ───────────────────────────────────────
        StackPane photoWrap = new StackPane();
        photoWrap.setPrefSize(46, 46);
        photoWrap.setMinSize(46, 46);
        photoWrap.setMaxSize(46, 46);

        Image playerImg = p != null ? loadPlayerImage(p) : null;
        if (playerImg != null) {
            ImageView pIv = new ImageView(playerImg);
            pIv.setFitWidth(46);
            pIv.setFitHeight(46);
            Circle pClip = new Circle(23, 23, 23);
            pIv.setClip(pClip);
            photoWrap.getChildren().add(pIv);
        } else {
            // Initials circle in dark gold
            StackPane circle = new StackPane();
            circle.setPrefSize(42, 42);
            circle.setMaxSize(42, 42);
            circle.setStyle("""
                -fx-background-color: rgba(0,0,0,0.30);
                -fx-background-radius: 21;
                -fx-border-color: rgba(201,168,76,0.6);
                -fx-border-radius: 21;
                -fx-border-width: 1;
            """);
            Text initText = new Text(p != null ? initials(p.getName()) : "?");
            initText.setFill(Color.web("#c9a84c"));
            initText.setStyle("-fx-font-size:13; -fx-font-weight:bold;");
            circle.getChildren().add(initText);
            photoWrap.getChildren().add(circle);
        }
        content.getChildren().add(photoWrap);

        // ── Divider line ──────────────────────────────────────────────────
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setMaxWidth(60);
        divider.setStyle("-fx-background-color: rgba(0,0,0,0.25);");
        content.getChildren().add(divider);

        // ── Player name ───────────────────────────────────────────────────
        Label nameLbl = new Label(p != null ? shortName(p.getName()) : "—");
        nameLbl.setStyle("-fx-text-fill:#1a1200; -fx-font-size:8; -fx-font-weight:bold;");
        nameLbl.setMaxWidth(72);
        nameLbl.setAlignment(Pos.CENTER);
        content.getChildren().add(nameLbl);

        // ── Stats row (for non-GK) ────────────────────────────────────────
        if (p != null) {
            HBox statsRow = buildMiniStats(p);
            content.getChildren().add(statsRow);
        }

        // ── Assemble ─────────────────────────────────────────────────────
        root.getChildren().addAll(cardBg, content);

        // Drop shadow always present
        root.setStyle(selected
                ? "-fx-effect: dropshadow(gaussian, #00FFA3, 18, 0.7, 0, 0); -fx-cursor:hand;"
                : "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 8, 0, 0, 3); -fx-cursor:hand;");

        return root;
    }

    /** Returns a tiny 2-column stats HBox (PAC/SHO or DEF/PAS). */
    private HBox buildMiniStats(Player p) {
        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER);

        String[][] attrs;
        switch (p.getPosition()) {
            case "GK"  -> attrs = new String[][]{{"DIV", "85"}, {"HND", "82"}};
            case "DEF" -> attrs = new String[][]{{"DEF", fmt(p.getRating())}, {"PHY", fmt(p.getRating() - 2)}};
            case "MID" -> attrs = new String[][]{{"PAS", fmt(p.getRating())}, {"DRI", fmt(p.getRating() - 1)}};
            default    -> attrs = new String[][]{{"PAC", fmt(p.getRating())}, {"SHO", fmt(p.getRating() - 2)}};
        }

        for (String[] attr : attrs) {
            VBox cell = new VBox(0);
            cell.setAlignment(Pos.CENTER);
            Label val = new Label(attr[1]);
            val.setStyle("-fx-text-fill:#1a1200; -fx-font-size:8; -fx-font-weight:bold;");
            Label key = new Label(attr[0]);
            key.setStyle("-fx-text-fill:#4a3000; -fx-font-size:6;");
            cell.getChildren().addAll(val, key);
            row.getChildren().add(cell);
        }
        return row;
    }

    private String fmt(double v) { return String.valueOf((int) Math.min(99, Math.max(1, v))); }

    // ══════════════════════════════════════════════════════════════════════════
    //  HIGHLIGHT / SELECTION
    // ══════════════════════════════════════════════════════════════════════════
    private void highlightCard(StackPane card, boolean highlight) {
        card.setStyle(highlight
                ? "-fx-effect: dropshadow(gaussian, #00FFA3, 18, 0.8, 0, 0); -fx-cursor:hand;"
                : "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 8, 0, 0, 3); -fx-cursor:hand;");
    }

    private void highlightBench(HBox card, boolean highlight) {
        if (highlight) {
            card.setStyle("""
                -fx-background-color: rgba(0,255,163,0.12);
                -fx-background-radius: 10;
                -fx-border-color: #00FFA3;
                -fx-border-width: 2;
                -fx-border-radius: 10;
                -fx-padding: 8;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, #00FFA3, 10, 0.4, 0, 0);
            """);
        } else {
            card.setStyle("""
                -fx-background-color: rgba(201,168,76,0.06);
                -fx-background-radius: 10;
                -fx-border-color: rgba(201,168,76,0.25);
                -fx-border-width: 1;
                -fx-border-radius: 10;
                -fx-padding: 8;
                -fx-cursor: hand;
            """);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  BENCH RENDERING
    // ══════════════════════════════════════════════════════════════════════════
    private void renderBench() {
        subsPanel.getChildren().clear();
        benchCards.clear();

        for (int i = 0; i < substitutes.size(); i++) {
            Player p = substitutes.get(i);
            final int idx = i;
            HBox card = buildBenchCard(p);
            card.setOnMouseClicked(e -> handleBenchClick(idx, p, card));
            subsPanel.getChildren().add(card);
            benchCards.add(card);
        }

        if (substitutes.isEmpty()) {
            Label empty = new Label("No substitutes\navailable");
            empty.setStyle("-fx-text-fill:#555; -fx-font-size:11; -fx-text-alignment:center;");
            empty.setWrapText(true);
            subsPanel.getChildren().add(empty);
        }

        Label hint = new Label("⇅  Tap to select\nthen tap to swap");
        hint.setStyle("""
            -fx-text-fill: rgba(201,168,76,0.5);
            -fx-font-size: 10;
            -fx-padding: 16 0 0 0;
            -fx-text-alignment: center;
        """);
        hint.setWrapText(true);
        hint.setMaxWidth(Double.MAX_VALUE);
        hint.setAlignment(Pos.CENTER);
        subsPanel.getChildren().add(hint);
    }

    private HBox buildBenchCard(Player p) {
        HBox card = new HBox(8);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(8));
        highlightBench(card, false); // apply default style
        card.setMaxWidth(Double.MAX_VALUE);

        // Mini FIFA card thumbnail (40×53)
        StackPane miniCard = buildFifaCard(p, p.getPosition(), false);
        miniCard.setPrefSize(40, 53);
        miniCard.setMaxSize(40, 53);
        miniCard.setScaleX(0.5);
        miniCard.setScaleY(0.5);
        // Wrap in a fixed box because scale doesn't shrink layout bounds
        StackPane miniWrap = new StackPane(miniCard);
        miniWrap.setPrefSize(40, 53);
        miniWrap.setMinSize(40, 53);
        miniWrap.setMaxSize(40, 53);
        miniWrap.setClip(new Rectangle(40, 53));

        // Info
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(shortName(p.getName()));
        name.setStyle("-fx-text-fill: white; -fx-font-size:11; -fx-font-weight:bold;");
        Label pos = new Label(p.getPosition() + "  ·  ★ " + (int)p.getRating());
        pos.setStyle("-fx-text-fill:" + posColor(p.getPosition()) + "; -fx-font-size:9;");
        info.getChildren().addAll(name, pos);

        card.getChildren().addAll(miniWrap, info);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SWAP LOGIC
    // ══════════════════════════════════════════════════════════════════════════
    private void handlePitchClick(int slotIndex, Player player, StackPane card) {
        if (player == null) return;

        if (selectedPlayer == null) {
            selectedPlayer    = player;
            selectedFromPitch = true;
            clearAllHighlights();
            highlightCard(card, true);
            setStatus("Selected " + shortName(player.getName()) + " — tap another player to swap");
        } else if (selectedFromPitch && selectedPlayer == player) {
            deselect();
        } else {
            if (selectedFromPitch) {
                int otherSlot = startingXI.indexOf(selectedPlayer);
                if (otherSlot >= 0 && slotIndex != otherSlot) {
                    Collections.swap(startingXI, otherSlot, slotIndex);
                    setStatus("Swapped " + shortName(selectedPlayer.getName())
                            + " ↔ " + shortName(player.getName()));
                }
            } else {
                int benchIdx = substitutes.indexOf(selectedPlayer);
                substitutes.set(benchIdx, player);
                startingXI.set(slotIndex, selectedPlayer);
                setStatus(shortName(selectedPlayer.getName()) + " moved to starting XI");
            }
            deselect();
            renderPitch();
            renderBench();
        }
    }

    private void handleBenchClick(int benchIndex, Player player, HBox card) {
        if (selectedPlayer == null) {
            selectedPlayer    = player;
            selectedFromPitch = false;
            clearAllHighlights();
            highlightBench(card, true);
            setStatus("Selected " + shortName(player.getName()) + " — tap a pitch player to swap");
        } else if (!selectedFromPitch && selectedPlayer == player) {
            deselect();
        } else if (!selectedFromPitch) {
            int otherIdx = substitutes.indexOf(selectedPlayer);
            if (otherIdx >= 0 && otherIdx != benchIndex) {
                Collections.swap(substitutes, otherIdx, benchIndex);
                setStatus("Swapped bench positions");
            }
            deselect();
            renderBench();
        } else {
            // Pitch → Bench
            int pitchIdx = startingXI.indexOf(selectedPlayer);
            startingXI.set(pitchIdx, player);
            substitutes.set(benchIndex, selectedPlayer);
            setStatus(shortName(selectedPlayer.getName()) + " moved to bench");
            deselect();
            renderPitch();
            renderBench();
        }
    }

    private void deselect() {
        selectedPlayer = null;
        clearAllHighlights();
    }

    private void clearAllHighlights() {
        for (StackPane s : pitchSlots) highlightCard(s, false);
        for (HBox h     : benchCards)  highlightBench(h, false);
    }

    private void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════════════════════════════
    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/buet/fantasymanagerxi/fxml/prehome-view.fxml"));
            Parent root = loader.load();
            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root, 1000, 600));
            stage.setTitle("Fantasy League XI");
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void goToMarket() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/buet/fantasymanagerxi/fxml/transfer-market.fxml"));
            Parent root = loader.load();
            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root, 1200, 760));
            stage.setTitle("Transfer Market — " + SessionManager.getLoggedInClub());
        } catch (IOException e) { e.printStackTrace(); }
    }

    private Stage getCurrentStage() {
        return (Stage) pitchPane.getScene().getWindow();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════════
    private String shortName(String fullName) {
        if (fullName == null) return "";
        String[] parts = fullName.split(" ");
        return parts.length == 1 ? fullName : parts[parts.length - 1];
    }

    private String initials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1)
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private String posColor(String pos) {
        if (pos == null) return "#aaaaaa";
        return switch (pos) {
            case "GK"  -> "#F39C12";
            case "DEF" -> "#3498DB";
            case "MID" -> "#2ECC71";
            case "FWD" -> "#E74C3C";
            default    -> "#888888";
        };
    }

    private Image loadPlayerImage(Player p) {
        if (p == null || p.getImagePath() == null) return null;
        return loadResource("/" + p.getImagePath());
    }

    private Image loadResource(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is != null) return new Image(is);
        } catch (Exception ignored) {}
        return null;
    }
}
