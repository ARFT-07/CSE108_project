package org.buet.fantasymanagerxi;

//package com.fantasymanagerxi.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.beans.property.*;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.buet.fantasymanagerxi.model.*;
import org.buet.fantasymanagerxi.service.DataService;

import java.io.File;
import java.net.URL;

public class HomeController {

    @FXML
    private ListView<Team> teamListView;
    @FXML
    private TableView<LeagueTableEntry> leagueTableView;

    @FXML
    private TableColumn<LeagueTableEntry, Integer> positionCol;
    @FXML
    private TableColumn<LeagueTableEntry, String> teamCol;
    @FXML
    private TableColumn<LeagueTableEntry, Integer> pointsCol;
    @FXML
    private TableColumn<LeagueTableEntry, Integer> gdCol;

    @FXML
    private Label highlightLabel;
    @FXML
    private StackPane videoPane;
    @FXML
    private MediaView mediaView;
    private MediaPlayer mediaPlayer;


    private DataService dataService = new DataService();

    @FXML
    public void initialize() {

        setupTeamList();
        setupLeagueTable();
        //setupHighlights();
        setupMediaPlayer();

    }

    private void setupMediaPlayer() {
        URL mediaUrl = getClass().getResource("/org/buet/fantasymanagerxi/data/fcb.mp4");
        // String path = new File("src/main/resources/org/buet/fantasymanagerxi/data/fcb.mp4").toURI().toString();
        if (mediaUrl == null) {
            System.out.println("File not found");
            return;
        }

        Media media = new Media(mediaUrl.toString());
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.setAutoPlay(false);
//        mediaView.fitWidthProperty().bind(videoPane.widthProperty());
//        mediaView.fitHeightProperty().bind(videoPane.heightProperty());
//        mediaView.setPreserveRatio(true);
    }

    @FXML
    private void handlePlay() {
        mediaPlayer.play();
    }

    @FXML
    private void handlePause() {
        mediaPlayer.pause();
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
    private void movetoplayerDb(ActionEvent actionEvent) {
        return;
    }

}