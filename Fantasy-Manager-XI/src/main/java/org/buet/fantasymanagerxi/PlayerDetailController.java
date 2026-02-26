package org.buet.fantasymanagerxi;

import org.buet.fantasymanagerxi.model.Player;
import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.*;

public class PlayerDetailController {

    @FXML private ImageView playerPhoto;
    @FXML private Label     playerName, playerClub, playerPosition, playerRating;
    @FXML private VBox      detailPanel;
    @FXML private Button    backBtn;

    public void setPlayer(Player p) {

        // ── Photo ─────────────────────────────────────────────────────────────
        loadPhoto(p);

        // ── Header ────────────────────────────────────────────────────────────
        playerName.setText(p.getName());
        playerClub.setText(p.getClub());
        playerRating.setText("★ " + p.getRating());
        playerPosition.setText("  " + p.getPosition() + "  ");
        playerPosition.setStyle(
                "-fx-background-color: " + p.getPositionColor() + ";" +
                        "-fx-background-radius: 6; -fx-padding: 4 12;" +
                        "-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: white;"
        );

        // Clip photo to circle
        Rectangle clip = new Rectangle(220, 220);
        clip.setArcWidth(220);
        clip.setArcHeight(220);
        playerPhoto.setClip(clip);

        // ── Detail rows ───────────────────────────────────────────────────────
        detailPanel.getChildren().clear();
        detailPanel.getChildren().addAll(
                sectionHeader("Personal"),
                statRow("Date of Birth",  p.getDob()),
                statRow("Nationality",    p.getNationality()),
                statRow("Height",         p.getHeightCm() + " cm"),
                statRow("Weight",         p.getWeightKg() + " kg"),
                statRow("Preferred Foot", p.getFoot()),

                sectionHeader("Contract"),
                statRow("Club",           p.getClub()),
                statRow("Shirt Number",   "#" + p.getShirtNo()),
                statRow("Contract Until", p.getContractEnd()),
                statRow("Weekly Wage",    "£" + String.format("%,d", (int) p.getWagePw())),
                statRow("Market Value",   "£" + p.getMarketValueM() + "M"),

                sectionHeader("Career Stats"),
                statRow("Appearances",    String.valueOf(p.getAppearances())),
                statRow("Goals",          String.valueOf(p.getGoals())),
                statRow("Assists",        String.valueOf(p.getAssists())),
                statRow("Rating",         String.valueOf(p.getRating())),

                sectionHeader("Transfer History"),
                transferBlock(p.getTransferHistory())
        );
    }

    private Label sectionHeader(String title) {
        Label lbl = new Label(title.toUpperCase());
        lbl.setStyle("""
            -fx-text-fill: #e94560;
            -fx-font-size: 11;
            -fx-font-weight: bold;
            -fx-padding: 20 0 6 0;
            """);
        return lbl;
    }

    private HBox statRow(String label, String value) {
        Label key = new Label(label);
        key.setPrefWidth(160);
        key.setStyle("-fx-text-fill: #888888; -fx-font-size: 13;");

        Label val = new Label(value);
        val.setStyle("-fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold;");

        HBox row = new HBox(key, val);
        row.setStyle("""
            -fx-padding: 7 0;
            -fx-border-color: transparent transparent #2a2a4a transparent;
            -fx-border-width: 1;
            """);
        return row;
    }

    private VBox transferBlock(String history) {
        VBox box = new VBox(6);
        if (history == null || history.isBlank()) {
            Label none = new Label("No transfer history");
            none.setStyle("-fx-text-fill: #666;");
            box.getChildren().add(none);
            return box;
        }
        String[] entries = history.split(",");
        for (String entry : entries) {
            String[] parts = entry.trim().split("\\|");
            if (parts.length < 2) continue;
            String club = parts[0].trim();
            String year = parts[1].trim();
            boolean loan = club.contains("(L)");
            club = club.replace("(L)", "").trim();

            HBox row = new HBox(8);
            Label arrow = new Label(loan ? "↗" : "→");
            arrow.setStyle("-fx-text-fill: " + (loan ? "#F39C12" : "#2ECC71") + "; -fx-font-size: 14;");
            Label info = new Label(year + "  —  " + club + (loan ? "  (Loan)" : ""));
            info.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13;");
            row.getChildren().addAll(arrow, info);
            box.getChildren().add(row);
        }
        return box;
    }

    private void loadPhoto(Player p) {
        Image img = null;

        try {
            InputStream is = getClass().getResourceAsStream("/" + p.getImagePath());
            if (is != null) img = new Image(is);
        } catch (Exception ignored) {}

        if (img == null) {
            try {
                File f = new File(p.getImagePath());
                if (f.exists()) img = new Image(f.toURI().toString());
            } catch (Exception ignored) {}
        }

        if (img == null) {
            try {
                InputStream is = getClass().getResourceAsStream(
                        "/org/buet/fantasymanagerxi/images/players/placeholder.png"
                );
                if (is != null) img = new Image(is);
            } catch (Exception ignored) {}
        }

        if (img != null) playerPhoto.setImage(img);
    }

    @FXML
    private void goBack() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/org/buet/fantasymanagerxi/fxml/player-db.fxml")
            );
            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 720));
            stage.setTitle("Player Database");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
