package org.buet.fantasymanagerxi.model;

public class LeagueTableEntry {

    private int position;
    private String teamName;
    private int points;
    private int goalDifference;

    public LeagueTableEntry(int position, String teamName, int points, int goalDifference) {
        this.position = position;
        this.teamName = teamName;
        this.points = points;
        this.goalDifference = goalDifference;
    }

    public int getPosition() {
        return position;
    }

    public String getTeamName() {
        return teamName;
    }

    public int getPoints() {
        return points;
    }

    public int getGoalDifference() {
        return goalDifference;
    }
}