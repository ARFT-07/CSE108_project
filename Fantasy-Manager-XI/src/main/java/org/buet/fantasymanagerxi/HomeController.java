package org.buet.fantasymanagerxi;

//package com.fantasymanagerxi.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.*;
import org.buet.fantasymanagerxi.model.*;
import org.buet.fantasymanagerxi.service.DataService;
public class HomeController {

    @FXML private ListView<Team> teamListView;
    @FXML private TableView<LeagueTableEntry> leagueTableView;

    @FXML private TableColumn<LeagueTableEntry, Integer> positionCol;
    @FXML private TableColumn<LeagueTableEntry, String> teamCol;
    @FXML private TableColumn<LeagueTableEntry, Integer> pointsCol;
    @FXML private TableColumn<LeagueTableEntry, Integer> gdCol;

    @FXML private Label highlightLabel;

    private DataService dataService = new DataService();

    @FXML
    public void initialize() {

        setupTeamList();
        setupLeagueTable();
        setupHighlights();
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

    private void setupHighlights() {

        highlightLabel.setText(
                "Matchday 1: Arsenal 3 - 1 Chelsea | City 2 - 0 Liverpool"
        );
    }
}