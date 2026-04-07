package org.buet.fantasymanagerxi;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.buet.fantasymanagerxi.model.Player;
import org.buet.fantasymanagerxi.util.SceneSwitcher;

import java.io.InputStream;
import java.util.*;

public class MyTeamController {

    // ══════════════════════════════════════════════════════════════════════════
    //  CARD SCALE — computed dynamically at render time, NOT fixed constants
    // ══════════════════════════════════════════════════════════════════════════
    // These are set in renderPitch() based on actual pane size
    private double CARD_W  = 100;
    private double CARD_H  = 134;
    private double PHOTO_H = 74;

    // ══════════════════════════════════════════════════════════════════════════
    //  FORMATIONS  — rows listed TOP → BOTTOM  (FWD first, GK last)
    // ══════════════════════════════════════════════════════════════════════════
    private static final Map<String, int[]> FORMATION_ROWS = new LinkedHashMap<>();
    // value = [fwd_count, mid_count, def_count]  (gk always 1)
    static {
        FORMATION_ROWS.put("4-3-3", new int[]{3, 3, 4});
        FORMATION_ROWS.put("4-4-2", new int[]{2, 4, 4});
        FORMATION_ROWS.put("4-2-3-1", new int[]{1, 5, 4});   // 3 CAM + 2 CDM = 5 mid slots
        FORMATION_ROWS.put("3-5-2", new int[]{2, 5, 3});
        FORMATION_ROWS.put("5-3-2", new int[]{2, 3, 5});
    }

    // Position labels per formation row (top → bottom)
    private static final Map<String, List<List<String>>> FORMATION_LABELS = new LinkedHashMap<>();
    static {
        FORMATION_LABELS.put("4-3-3",   List.of(
                List.of("LW","ST","RW"), List.of("LM","CM","RM"),
                List.of("LB","CB","CB","RB"), List.of("GK")));
        FORMATION_LABELS.put("4-4-2",   List.of(
                List.of("ST","ST"), List.of("LM","CM","CM","RM"),
                List.of("LB","CB","CB","RB"), List.of("GK")));
        FORMATION_LABELS.put("4-2-3-1", List.of(
                List.of("ST"), List.of("LAM","CAM","RAM"),
                List.of("CDM","CDM"), List.of("LB","CB","CB","RB"), List.of("GK")));
        FORMATION_LABELS.put("3-5-2",   List.of(
                List.of("ST","ST"), List.of("LM","CM","CM","CM","RM"),
                List.of("CB","CB","CB"), List.of("GK")));
        FORMATION_LABELS.put("5-3-2",   List.of(
                List.of("ST","ST"), List.of("LM","CM","RM"),
                List.of("LB","CB","CB","CB","RB"), List.of("GK")));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FXML NODES
    // ══════════════════════════════════════════════════════════════════════════
    @FXML private Pane   pitchPane;
    @FXML private VBox   subsPanel;
    @FXML private HBox   formationBar;
    @FXML private Label  clubLabel;
    @FXML private Label  statusLabel;
    @FXML private Button backbtn;
    @FXML private Button marketBtn;
    @FXML private Label  squadCountLabel;

    // ══════════════════════════════════════════════════════════════════════════
    //  STATE
    // ══════════════════════════════════════════════════════════════════════════
    private String             currentFormation  = "4-3-3";
    private final List<Player> startingXI        = new ArrayList<>();
    private final List<Player> substitutes       = new ArrayList<>();
    private Player             selectedPlayer    = null;
    private boolean            selectedFromPitch = false;

    private final List<StackPane> pitchSlots = new ArrayList<>();
    private final List<HBox>      benchCards = new ArrayList<>();

    private Image grassImage = null;

    // ══════════════════════════════════════════════════════════════════════════
    //  INITIALIZE
    // ══════════════════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        // Load grass image
        grassImage = loadResource("/org/buet/fantasymanagerxi/images/grass_bg.png");

        // Apply background — cover the full pane, tile if needed
        applyPitchBackground();

        // Club / squad info
        String club = SessionManager.getLoggedInClub();
        clubLabel.setText(club != null ? club : "My Team");

        List<Player> squad = SessionManager.getSquad();
        int squadSize = (squad != null) ? squad.size() : 0;
        if (squadCountLabel != null)
            squadCountLabel.setText(squadSize + " Players");

        buildFormationButtons();
        distributeSquad();

        // Re-render on every resize — use runLater so first render fires after layout pass
        pitchPane.widthProperty().addListener((obs, o, n) -> {
            if (n.doubleValue() > 20) renderPitch();
        });
        pitchPane.heightProperty().addListener((obs, o, n) -> {
            if (n.doubleValue() > 20) { applyPitchBackground(); renderPitch(); }
        });

        // Force initial render after scene is laid out
        pitchPane.layoutBoundsProperty().addListener((obs, o, n) -> {
            if (n.getWidth() > 20 && n.getHeight() > 20) renderPitch();
        });

        renderBench();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  BUG 1 FIX — Background: use ImageView child, not BackgroundImage API
    //  BackgroundImage ignores CSS override; an ImageView child is reliable.
    // ══════════════════════════════════════════════════════════════════════════
    private void applyPitchBackground() {
        // Remove any previous bg ImageView (tagged with id "bgIV")
        pitchPane.getChildren().removeIf(n -> "bgIV".equals(n.getId()));

        if (grassImage != null) {
            ImageView bgIV = new ImageView(grassImage);
            bgIV.setId("bgIV");
            bgIV.setPreserveRatio(false);
            bgIV.setSmooth(true);
            // Bind size to pane so it always fills exactly
            bgIV.fitWidthProperty().bind(pitchPane.widthProperty());
            bgIV.fitHeightProperty().bind(pitchPane.heightProperty());
            bgIV.setLayoutX(0);
            bgIV.setLayoutY(0);
            // Insert at index 0 so player cards render on top
            pitchPane.getChildren().add(0, bgIV);
        } else {
            // Fallback: dark navy-green that matches the page theme instead of #2d5a1b
            pitchPane.setStyle("""
                -fx-background-color: linear-gradient(to bottom, #0d2818, #0a1f13);
                -fx-background-radius: 14;
                -fx-border-color: #1a3a10;
                -fx-border-radius: 14;
                -fx-border-width: 2;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 20, 0, 0, 8);
            """);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SQUAD DISTRIBUTION — position-aware slot filling
    // ══════════════════════════════════════════════════════════════════════════
    private void distributeSquad() {
        startingXI.clear();
        substitutes.clear();

        List<Player> squad = SessionManager.getSquad();
        if (squad == null || squad.isEmpty()) return;

        // Assign image paths if missing
        for (Player p : squad) {
            if (p.getImagePath() == null) {
                String imageName = p.getName()
                        .toLowerCase()
                        .replaceAll("\\s+", "_")
                        .replaceAll("[^a-z0-9_]", "")
                        + ".png";
                p.setImagePath("org/buet/fantasymanagerxi/images/players/" + imageName);
            }
        }

        // BUG 3 FIX — fill each formation row with correct position category
        int[] rows = FORMATION_ROWS.get(currentFormation); // [fwd, mid, def]
        int fwdNeed = rows[0], midNeed = rows[1], defNeed = rows[2];

        List<Player> gks   = playersOf(squad, "GK");
        List<Player> defs  = playersOf(squad, "DEF");
        List<Player> mids  = playersOf(squad, "MID");
        List<Player> fwds  = playersOf(squad, "FWD");
        List<Player> rest  = new ArrayList<>(); // wrong-position fillers

        // Sort each group by rating desc
        Comparator<Player> byRating = Comparator.comparingDouble(p -> -p.getRating());
        gks.sort(byRating); defs.sort(byRating); mids.sort(byRating); fwds.sort(byRating);

        // Fill starting XI respecting positional slots
        Player gkSlot = gks.isEmpty() ? null : gks.remove(0);
        if (gkSlot != null) startingXI.add(gkSlot); else rest.add(null);

        fillSlots(startingXI, defs, defNeed, rest);
        fillSlots(startingXI, mids, midNeed, rest);
        fillSlots(startingXI, fwds, fwdNeed, rest);

        // If any slots still empty, fill with remaining players regardless of position
        List<Player> overflow = new ArrayList<>();
        overflow.addAll(gks); overflow.addAll(defs);
        overflow.addAll(mids); overflow.addAll(fwds);
        overflow.sort(byRating);

        while (startingXI.size() < 11 && !overflow.isEmpty())
            startingXI.add(overflow.remove(0));

        // Everyone not in starting XI goes to bench
        Set<Player> xi = new HashSet<>(startingXI);
        for (Player p : squad)
            if (!xi.contains(p)) substitutes.add(p);
    }

    private List<Player> playersOf(List<Player> squad, String pos) {
        List<Player> out = new ArrayList<>();
        for (Player p : squad) if (pos.equals(p.getPosition())) out.add(p);
        return out;
    }

    /** Fill `need` slots from `pool` into `target`; surplus goes to `rest`. */
    private void fillSlots(List<Player> target, List<Player> pool, int need, List<Player> rest) {
        for (int i = 0; i < need; i++) {
            if (!pool.isEmpty()) target.add(pool.remove(0));
            else                 rest.add(null); // placeholder — will be backfilled
        }
        rest.addAll(pool); // remaining pool players become bench candidates
        pool.clear();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FORMATION BUTTONS
    // ══════════════════════════════════════════════════════════════════════════
    private void buildFormationButtons() {
        formationBar.getChildren().clear();
        Label lbl = new Label("FORMATION");
        lbl.setStyle("-fx-text-fill:#64748b; -fx-font-size:10; -fx-font-weight:bold;");
        formationBar.getChildren().add(lbl);

        Region sep = new Region();
        sep.setPrefWidth(1); sep.setPrefHeight(18);
        sep.setStyle("-fx-background-color: #1e293b;");
        formationBar.getChildren().add(sep);

        for (String name : FORMATION_LABELS.keySet()) {
            Button btn = new Button(name);
            styleFormationBtn(btn, name.equals(currentFormation));
            btn.setOnAction(e -> switchFormation(name));
            formationBar.getChildren().add(btn);
        }
    }

    private void styleFormationBtn(Button btn, boolean active) {
        btn.setStyle(active ? """
            -fx-background-color: #3b82f6; -fx-text-fill: white;
            -fx-background-radius: 6; -fx-padding: 5 14;
            -fx-font-size: 11; -fx-font-weight: bold; -fx-cursor: hand;
        """ : """
            -fx-background-color: rgba(255,255,255,0.05); -fx-text-fill: #64748b;
            -fx-border-color: #1e293b; -fx-border-radius: 6; -fx-background-radius: 6;
            -fx-padding: 5 14; -fx-font-size: 11; -fx-cursor: hand;
        """);
    }

    private void switchFormation(String name) {
        currentFormation = name;
        selectedPlayer   = null;
        buildFormationButtons();
        distributeSquad();
        renderPitch();
        renderBench();
        setStatus("Formation: " + name);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PITCH RENDERING — BUG 2 FIX: dynamic card scaling
    // ══════════════════════════════════════════════════════════════════════════
    private void renderPitch() {
        // Remove everything EXCEPT the bg ImageView
        pitchPane.getChildren().removeIf(n -> !"bgIV".equals(n.getId()));
        pitchSlots.clear();

        double pW = pitchPane.getWidth();
        double pH = pitchPane.getHeight();
        if (pW < 20 || pH < 20) return;

        List<List<String>> rows = FORMATION_LABELS.get(currentFormation);
        int numRows = rows.size();

        // ── BUG 2 FIX: compute card size from available space ─────────────
        // Find the row with most columns to determine max horizontal density
        int maxCols = rows.stream().mapToInt(List::size).max().orElse(4);
        // Horizontal: fit maxCols cards with padding, cap at reasonable size
        double maxCardW = Math.min(108, (pW / (maxCols + 1.5)));
        // Vertical: fit numRows cards with padding
        double maxCardH = Math.min(144, (pH / (numRows + 1.0)));
        // Keep aspect ratio ~ 100:134
        double scale = Math.min(maxCardW / 100.0, maxCardH / 134.0);
        CARD_W  = Math.max(60,  scale * 100);
        CARD_H  = Math.max(80,  scale * 134);
        PHOTO_H = Math.max(44,  scale * 74);

        // ── Layout each row ───────────────────────────────────────────────
        double rowH = pH / (numRows + 1.0);
        int slotIndex = 0;

        for (int r = 0; r < numRows; r++) {
            List<String> cols = rows.get(r);
            int numCols = cols.size();
            double colW = pW / (numCols + 1.0);
            double y    = rowH * (r + 1);

            for (int c = 0; c < numCols; c++) {
                double x        = colW * (c + 1);
                String posLabel = cols.get(c);
                Player p        = slotIndex < startingXI.size() ? startingXI.get(slotIndex) : null;
                final int  fSlot = slotIndex;
                final Player fP  = p;

                StackPane card = buildScoutCard(p, posLabel, false);
                card.setLayoutX(x - CARD_W / 2.0);
                card.setLayoutY(y - CARD_H / 2.0);
                card.setOnMouseClicked(e -> handlePitchClick(fSlot, fP, card));

                pitchPane.getChildren().add(card);
                pitchSlots.add(card);
                slotIndex++;
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SCOUT CARD — uses instance CARD_W/CARD_H (dynamic)
    // ══════════════════════════════════════════════════════════════════════════
    private StackPane buildScoutCard(Player p, String posLabel, boolean selected) {
        StackPane root = new StackPane();
        root.setPrefSize(CARD_W, CARD_H);
        root.setMaxSize(CARD_W, CARD_H);
        root.setMinSize(CARD_W, CARD_H);

        VBox card = new VBox(0);
        card.setPrefSize(CARD_W, CARD_H);
        card.setMaxSize(CARD_W, CARD_H);
        card.setMinSize(CARD_W, CARD_H);
        card.setStyle("""
            -fx-background-color: linear-gradient(to bottom, #1a2535, #0d1520);
            -fx-background-radius: 10;
            -fx-border-color: #243447;
            -fx-border-radius: 10;
            -fx-border-width: 1;
        """);

        // ── TOP BAR ───────────────────────────────────────────────────────
        String posText  = (p != null) ? p.getPosition() : posLabel;
        int jerseyNum   = (p != null && p.getShirtNo() > 0) ? p.getShirtNo() : defaultJerseyNo(posText);
        double topBarH  = Math.max(16, CARD_H * 0.15);
        double fontSize = Math.max(7, CARD_W * 0.085);

        HBox topBar = new HBox();
        topBar.setPrefHeight(topBarH); topBar.setMinHeight(topBarH); topBar.setMaxHeight(topBarH);
        topBar.setPadding(new Insets(0, 5, 0, 5));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: " + posColorDark(posText) +
                "; -fx-background-radius: 9 9 0 0;");

        Label posLbl = new Label(posText);
        posLbl.setStyle("-fx-text-fill: white; -fx-font-size: " + fontSize + "; -fx-font-weight: bold;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label jerseyLbl = new Label("#" + jerseyNum);
        jerseyLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: " + fontSize + "; -fx-font-weight: bold;");
        topBar.getChildren().addAll(posLbl, sp, jerseyLbl);
        card.getChildren().add(topBar);

        // ── PHOTO ─────────────────────────────────────────────────────────
        StackPane photoWrap = new StackPane();
        photoWrap.setPrefSize(CARD_W, PHOTO_H);
        photoWrap.setMinSize(CARD_W, PHOTO_H);
        photoWrap.setMaxSize(CARD_W, PHOTO_H);
        photoWrap.setStyle("-fx-background-color: #0f1c2c;");

        Image playerImg = (p != null) ? loadPlayerImage(p) : null;
        if (playerImg != null) {
            ImageView pIv = new ImageView(playerImg);
            pIv.setFitWidth(CARD_W);
            pIv.setFitHeight(PHOTO_H);
            pIv.setPreserveRatio(true);
            pIv.setSmooth(true);
            Rectangle photoClip = new Rectangle(CARD_W, PHOTO_H);
            pIv.setClip(photoClip);
            photoWrap.getChildren().add(pIv);
        } else {
            double d = Math.max(28, CARD_W * 0.38);
            StackPane circle = new StackPane();
            circle.setPrefSize(d, d); circle.setMaxSize(d, d);
            circle.setStyle("-fx-background-color: #1e3a5f; -fx-background-radius: " + (d/2) + ";" +
                    "-fx-border-color: #3b82f6; -fx-border-radius: " + (d/2) + "; -fx-border-width: 2;");
            Text ini = new Text(p != null ? initials(p.getName())
                    : posText.substring(0, Math.min(2, posText.length())));
            ini.setFill(Color.web("#93c5fd"));
            ini.setStyle("-fx-font-size:" + Math.max(9, d * 0.32) + "; -fx-font-weight:bold;");
            circle.getChildren().add(ini);
            photoWrap.getChildren().add(circle);
        }

        if (p != null) {
            Label ratingBadge = new Label(String.valueOf((int) p.getRating()));
            ratingBadge.setStyle("-fx-background-color: #facc15; -fx-text-fill: #0a0f18;" +
                    "-fx-font-size: " + Math.max(7, CARD_W * 0.075) + "; -fx-font-weight: bold;" +
                    "-fx-background-radius: 4; -fx-padding: 1 4;");
            StackPane.setAlignment(ratingBadge, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(ratingBadge, new Insets(0, 3, 3, 0));
            photoWrap.getChildren().add(ratingBadge);
        }
        card.getChildren().add(photoWrap);

        // ── ACCENT LINE ───────────────────────────────────────────────────
        Region accentLine = new Region();
        accentLine.setPrefHeight(2); accentLine.setMinHeight(2);
        accentLine.setStyle("-fx-background-color: " + posColorDark(posText) + ";");
        card.getChildren().add(accentLine);

        // ── NAME + MINI STATS ─────────────────────────────────────────────
        double infoH = CARD_H - topBarH - PHOTO_H - 2;
        VBox infoArea = new VBox(1);
        infoArea.setPadding(new Insets(2, 5, 2, 5));
        infoArea.setAlignment(Pos.CENTER_LEFT);
        infoArea.setPrefHeight(infoH); infoArea.setMinHeight(infoH); infoArea.setMaxHeight(infoH);

        Label nameLbl = new Label(p != null ? shortName(p.getName()) : "—");
        nameLbl.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: " +
                Math.max(8, CARD_W * 0.095) + "; -fx-font-weight: bold;");
        nameLbl.setMaxWidth(CARD_W - 10);
        infoArea.getChildren().add(nameLbl);

        if (p != null && infoH > 22)
            infoArea.getChildren().add(buildMiniStats(p));

        card.getChildren().add(infoArea);
        root.getChildren().add(card);

        root.setStyle(selected
                ? "-fx-effect: dropshadow(gaussian, #3b82f6, 18, 0.8, 0, 0); -fx-cursor:hand;"
                : "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 5); -fx-cursor:hand;");
        return root;
    }

    private HBox buildMiniStats(Player p) {
        HBox row = new HBox(3);
        row.setAlignment(Pos.CENTER_LEFT);
        double statFontSize = Math.max(7, CARD_W * 0.075);

        String[][] attrs = switch (p.getPosition()) {
            case "GK"  -> new String[][]{{"DIV","87"},{"HND","83"}};
            case "DEF" -> new String[][]{{"DEF",fmt(p.getRating())},{"PHY",fmt(p.getRating()-2)}};
            case "MID" -> new String[][]{{"PAS",fmt(p.getRating())},{"DRI",fmt(p.getRating()-1)}};
            default    -> new String[][]{{"PAC",fmt(p.getRating())},{"SHO",fmt(p.getRating()-2)}};
        };

        for (int i = 0; i < attrs.length; i++) {
            String[] attr = attrs[i];
            HBox stat = new HBox(2); stat.setAlignment(Pos.CENTER_LEFT);
            Label val = new Label(attr[1]);
            val.setStyle("-fx-text-fill: #facc15; -fx-font-size:" + statFontSize + "; -fx-font-weight:bold;");
            Label key = new Label(attr[0]);
            key.setStyle("-fx-text-fill: #475569; -fx-font-size:" + (statFontSize - 1) + ";");
            stat.getChildren().addAll(val, key);
            row.getChildren().add(stat);
            if (i < attrs.length - 1) {
                Label dot = new Label("·"); dot.setStyle("-fx-text-fill: #1e293b; -fx-font-size:8;");
                row.getChildren().add(dot);
            }
        }
        return row;
    }

    private String fmt(double v) { return String.valueOf((int) Math.min(99, Math.max(1, v))); }

    // ══════════════════════════════════════════════════════════════════════════
    //  HIGHLIGHT
    // ══════════════════════════════════════════════════════════════════════════
    private void highlightCard(StackPane card, boolean on) {
        card.setStyle(on
                ? "-fx-effect: dropshadow(gaussian, #3b82f6, 18, 0.8, 0, 0); -fx-cursor:hand;"
                : "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 5); -fx-cursor:hand;");
    }

    private void highlightBench(HBox card, boolean on) {
        card.setStyle(on ? """
            -fx-background-color: rgba(59,130,246,0.10); -fx-background-radius: 8;
            -fx-border-color: #3b82f6; -fx-border-width: 1.5; -fx-border-radius: 8;
            -fx-padding: 8; -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(59,130,246,0.35), 12, 0, 0, 0);
        """ : """
            -fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 8;
            -fx-border-color: #1a2535; -fx-border-width: 1; -fx-border-radius: 8;
            -fx-padding: 8; -fx-cursor: hand;
        """);
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
            HBox row = buildBenchRow(p);
            row.setOnMouseClicked(e -> handleBenchClick(idx, p, row));
            subsPanel.getChildren().add(row);
            benchCards.add(row);
        }

        if (substitutes.isEmpty()) {
            Label empty = new Label("No substitutes\navailable");
            empty.setStyle("-fx-text-fill:#334155; -fx-font-size:11; -fx-text-alignment:center;");
            empty.setWrapText(true);
            subsPanel.getChildren().add(empty);
        }

        Label hint = new Label("↕  Select & tap to swap");
        hint.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 10; -fx-padding: 10 0 0 0;");
        hint.setMaxWidth(Double.MAX_VALUE);
        hint.setAlignment(Pos.CENTER);
        subsPanel.getChildren().add(hint);
    }

    private HBox buildBenchRow(Player p) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        highlightBench(row, false);
        row.setMaxWidth(Double.MAX_VALUE);

        StackPane photoWrap = new StackPane();
        double thumbSize = 42;
        photoWrap.setPrefSize(thumbSize, thumbSize);
        photoWrap.setMinSize(thumbSize, thumbSize);
        photoWrap.setMaxSize(thumbSize, thumbSize);

        Image img = loadPlayerImage(p);
        if (img != null) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(thumbSize); iv.setFitHeight(thumbSize);
            iv.setPreserveRatio(true);
            Circle clip = new Circle(thumbSize / 2, thumbSize / 2, thumbSize / 2);
            iv.setClip(clip);
            photoWrap.getChildren().add(iv);
        } else {
            photoWrap.setStyle("-fx-background-color: #1a2535; -fx-background-radius: 21;");
            Text ini = new Text(initials(p.getName()));
            ini.setFill(Color.web("#64748b"));
            ini.setStyle("-fx-font-size:13; -fx-font-weight:bold;");
            photoWrap.getChildren().add(ini);
        }

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);
        info.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(shortName(p.getName()));
        name.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size:12; -fx-font-weight:bold;");

        HBox meta = new HBox(6); meta.setAlignment(Pos.CENTER_LEFT);
        Label posBadge = new Label(p.getPosition());
        posBadge.setStyle("-fx-background-color: " + posColorDark(p.getPosition()) +
                "; -fx-text-fill: white; -fx-font-size: 8; -fx-font-weight: bold;" +
                "-fx-background-radius: 3; -fx-padding: 1 5;");
        Label ratingLbl = new Label("★ " + (int) p.getRating());
        ratingLbl.setStyle("-fx-text-fill: #facc15; -fx-font-size: 10; -fx-font-weight:bold;");
        int jersey = (p.getShirtNo() > 0) ? p.getShirtNo() : defaultJerseyNo(p.getPosition());
        Label jerseyLabel = new Label("#" + jersey);
        jerseyLabel.setStyle("-fx-text-fill: #334155; -fx-font-size: 9;");
        meta.getChildren().addAll(posBadge, ratingLbl, jerseyLabel);
        info.getChildren().addAll(name, meta);
        row.getChildren().addAll(photoWrap, info);
        return row;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SWAP LOGIC
    // ══════════════════════════════════════════════════════════════════════════
    private void handlePitchClick(int slotIndex, Player player, StackPane card) {
        if (player == null) return;
        if (selectedPlayer == null) {
            selectedPlayer = player; selectedFromPitch = true;
            clearAllHighlights(); highlightCard(card, true);
            setStatus("Selected " + shortName(player.getName()) + " — tap another to swap");
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
            deselect(); renderPitch(); renderBench();
        }
    }

    private void handleBenchClick(int benchIndex, Player player, HBox card) {
        if (selectedPlayer == null) {
            selectedPlayer = player; selectedFromPitch = false;
            clearAllHighlights(); highlightBench(card, true);
            setStatus("Selected " + shortName(player.getName()) + " — tap pitch player to swap");
        } else if (!selectedFromPitch && selectedPlayer == player) {
            deselect();
        } else if (!selectedFromPitch) {
            int otherIdx = substitutes.indexOf(selectedPlayer);
            if (otherIdx >= 0 && otherIdx != benchIndex) {
                Collections.swap(substitutes, otherIdx, benchIndex);
                setStatus("Swapped bench positions");
            }
            deselect(); renderBench();
        } else {
            int pitchIdx = startingXI.indexOf(selectedPlayer);
            startingXI.set(pitchIdx, player);
            substitutes.set(benchIndex, selectedPlayer);
            setStatus(shortName(selectedPlayer.getName()) + " moved to bench");
            deselect(); renderPitch(); renderBench();
        }
    }

    private void deselect() { selectedPlayer = null; clearAllHighlights(); }

    private void clearAllHighlights() {
        for (StackPane s : pitchSlots)  highlightCard(s, false);
        for (HBox h       : benchCards) highlightBench(h, false);
    }

    private void setStatus(String msg) { if (statusLabel != null) statusLabel.setText(msg); }

    // ══════════════════════════════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════════════════════════════
    @FXML private void goBack()     { SceneSwitcher.switchScene("prehome-view.fxml",    backbtn,   1100, 720); }
    @FXML private void goToMarket() { SceneSwitcher.switchScene("transfer-market.fxml", marketBtn, 1100, 720); }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════════
    private String shortName(String n) {
        if (n == null) return "";
        String[] p = n.split(" ");
        return p.length == 1 ? n : p[p.length - 1];
    }

    private String initials(String n) {
        if (n == null || n.isBlank()) return "?";
        String[] p = n.trim().split("\\s+");
        if (p.length == 1) return p[0].substring(0, Math.min(2, p[0].length())).toUpperCase();
        return ("" + p[0].charAt(0) + p[p.length-1].charAt(0)).toUpperCase();
    }

    private int defaultJerseyNo(String pos) {
        if (pos == null) return 0;
        return switch (pos) { case "GK" -> 1; case "DEF" -> 5; case "MID" -> 8; case "FWD" -> 9; default -> 7; };
    }

    private String posColorDark(String pos) {
        if (pos == null) return "#1e293b";
        return switch (pos) {
            case "GK" -> "#92400e"; case "DEF" -> "#1d4ed8";
            case "MID" -> "#15803d"; case "FWD" -> "#b91c1c"; default -> "#1e293b";
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