package org.buet.fantasymanagerxi.model;

public class Manager {

    private String name;
    private String club;
    private int    age;
    private String nationality;
    private String imagePath;
    private long   budget;
    private String trophies;
    private String since;

    public Manager() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getName()              { return name; }
    public void   setName(String name)   { this.name = name; }

    public String getClub()              { return club; }
    public void   setClub(String club)   { this.club = club; }

    public int    getAge()               { return age; }
    public void   setAge(int age)        { this.age = age; }

    public String getNationality()                   { return nationality; }
    public void   setNationality(String nationality) { this.nationality = nationality; }

    public String getImagePath()                 { return imagePath; }
    public void   setImagePath(String imagePath) { this.imagePath = imagePath; }

    public long   getBudget()            { return budget; }
    public void   setBudget(long budget) { this.budget = budget; }

    public String getTrophies()                  { return trophies; }
    public void   setTrophies(String trophies)   { this.trophies = trophies; }

    public String getSince()             { return since; }
    public void   setSince(String since) { this.since = since; }

    public String getBudgetFormatted() {
        return String.format("£%,d", budget);
    }
}