package org.buet.fantasymanagerxi.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.buet.fantasymanagerxi.model.Team;
import org.buet.fantasymanagerxi.model.LeagueTableEntry;

public class DataService {

    private ObservableList<Team> teams;
    private ObservableList<LeagueTableEntry> leagueTable;

    public DataService() {
        loadTeams();
        loadLeagueTable();
    }

    private void loadTeams() {
        teams = FXCollections.observableArrayList(
                new Team("Manchester City"),
                new Team("Arsenal"),
                new Team("Liverpool"),
                new Team("Chelsea"),
                new Team("Manchester United"),
                new Team("Tottenham Hotspur")
        );
    }

    private void loadLeagueTable() {
        leagueTable = FXCollections.observableArrayList(
                new LeagueTableEntry(1, "Manchester City", 18, 12),
                new LeagueTableEntry(2, "Arsenal", 15, 8),
                new LeagueTableEntry(3, "Liverpool", 12, 5),
                new LeagueTableEntry(4, "Chelsea", 9, 2),
                new LeagueTableEntry(5, "Manchester United", 6, -1),
                new LeagueTableEntry(6, "Tottenham Hotspur", 3, -6)
        );
    }

    public ObservableList<Team> getTeams() {
        return teams;
    }

    public ObservableList<LeagueTableEntry> getLeagueTable() {
        return leagueTable;
    }
}