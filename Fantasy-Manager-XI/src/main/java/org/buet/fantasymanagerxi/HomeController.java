package org.buet.fantasymanagerxi;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.buet.fantasymanagerxi.model.LeagueTableEntry;
import org.buet.fantasymanagerxi.model.Team;
import org.buet.fantasymanagerxi.service.DataService;
import org.buet.fantasymanagerxi.util.SceneSwitcher;

import java.net.URL;

public class HomeController {

    @FXML private ListView<Team> teamListView;
    @FXML private TableView<LeagueTableEntry> leagueTableView;
    @FXML private TableColumn<LeagueTableEntry, Integer> positionCol;
    @FXML private TableColumn<LeagueTableEntry, String> teamCol;
    @FXML private TableColumn<LeagueTableEntry, Integer> pointsCol;
    @FXML private TableColumn<LeagueTableEntry, Integer> gdCol;
    @FXML private Label highlightLabel;
    @FXML private StackPane videoPane;
    @FXML private MediaView mediaView;

    private MediaPlayer mediaPlayer;
    private final DataService dataService = new DataService();

    @FXML
    public void initialize() {
        setupTeamList();
        setupLeagueTable();
        setupMediaPlayer();
        attachMediaLifecycle();
    }

    private void setupMediaPlayer() {
        URL mediaUrl = getClass().getResource("/org/buet/fantasymanagerxi/data/fcb.mp4");
        if (mediaUrl == null) {
            System.out.println("Media file not found.");
            return;
        }

        Media media = new Media(mediaUrl.toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(false);
        mediaPlayer.setCycleCount(1);
        mediaView.setMediaPlayer(mediaPlayer);
    }

    private void attachMediaLifecycle() {
        videoPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null && newScene == null) {
                cleanupMediaPlayer();
                return;
            }

            if (newScene != null) {
                newScene.windowProperty().addListener((windowObs, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        newWindow.setOnHidden(event -> cleanupMediaPlayer());
                    }
                });
            }
        });
    }

    @FXML
    private void handlePlay() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    @FXML
    private void handlePause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    private void cleanupMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        mediaView.setMediaPlayer(null);
    }

    private void setupTeamList() {
        teamListView.setItems(dataService.getTeams());
        teamListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Team item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
    }

    private void setupLeagueTable() {
        positionCol.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getPosition()).asObject());

        teamCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTeamName()));

        pointsCol.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getPoints()).asObject());

        gdCol.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getGoalDifference()).asObject());

        leagueTableView.setItems(dataService.getLeagueTable());
    }

    @FXML
    public void backToPreHome(ActionEvent actionEvent) {
        cleanupMediaPlayer();
        SceneSwitcher.switchScene("prehome-view.fxml", actionEvent, 1100, 720);
    }
}
