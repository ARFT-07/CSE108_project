package org.buet.fantasymanagerxi.model;

public class Player {
    private String name, club, position, nationality, dob, foot, contractEnd;
    private int shirtNo, heightCm, weightKg, goals, assists, appearances;
    private double wagePw, marketValueM, rating;
    private String transferHistory;
    private String imagePath; // e.g. "images/players/salah.png"

    // ── Constructors ──────────────────────────────────────────
    public Player() {}

    // ── Getters & Setters ─────────────────────────────────────
    public String getName()            { return name; }
    public void   setName(String v)    { this.name = v; }

    public String getClub()            { return club; }
    public void   setClub(String v)    { this.club = v; }

    public String getPosition()        { return position; }
    public void   setPosition(String v){ this.position = v; }

    public String getNationality()     { return nationality; }
    public void   setNationality(String v){ this.nationality = v; }

    public String getDob()             { return dob; }
    public void   setDob(String v)     { this.dob = v; }

    public String getFoot()            { return foot; }
    public void   setFoot(String v)    { this.foot = v; }

    public String getContractEnd()     { return contractEnd; }
    public void   setContractEnd(String v){ this.contractEnd = v; }

    public int    getShirtNo()         { return shirtNo; }
    public void   setShirtNo(int v)    { this.shirtNo = v; }

    public int    getHeightCm()        { return heightCm; }
    public void   setHeightCm(int v)   { this.heightCm = v; }

    public int    getWeightKg()        { return weightKg; }
    public void   setWeightKg(int v)   { this.weightKg = v; }

    public int    getGoals()           { return goals; }
    public void   setGoals(int v)      { this.goals = v; }

    public int    getAssists()         { return assists; }
    public void   setAssists(int v)    { this.assists = v; }

    public int    getAppearances()     { return appearances; }
    public void   setAppearances(int v){ this.appearances = v; }

    public double getWagePw()          { return wagePw; }
    public void   setWagePw(double v)  { this.wagePw = v; }

    public double getMarketValueM()    { return marketValueM; }
    public void   setMarketValueM(double v){ this.marketValueM = v; }

    public double getRating()          { return rating; }
    public void   setRating(double v)  { this.rating = v; }

    public String getTransferHistory() { return transferHistory; }
    public void   setTransferHistory(String v){ this.transferHistory = v; }

    public String getImagePath()       { return imagePath; }
    public void   setImagePath(String v){ this.imagePath = v; }

    // Display helper for position badge color
    public String getPositionColor() {
        return switch (position) {
            case "GK"  -> "#F39C12";
            case "DEF" -> "#2980B9";
            case "MID" -> "#27AE60";
            case "FWD" -> "#E74C3C";
            default    -> "#7F8C8D";
        };
    }
}