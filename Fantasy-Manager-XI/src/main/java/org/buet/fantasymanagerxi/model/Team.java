package org.buet.fantasymanagerxi.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Team {

    private String name;
    private ObservableList<String> players;

    public Team(String name) {
        this.name = name;
        this.players = FXCollections.observableArrayList();
        generateDefaultPlayers();
    }

    private void generateDefaultPlayers() {
        for (int i = 1; i <= 15; i++) {
            players.add("Player " + i);
        }
    }

    public String getName() {
        return name;
    }

    public ObservableList<String> getPlayers() {
        return players;
    }
}