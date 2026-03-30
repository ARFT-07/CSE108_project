package org.buet.fantasymanagerxi.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class UserTeam {

    private static UserTeam instance;

    // team details
    private List<Player> startingXI;
    private ObservableList<Player> subs;
    private String formation;

    // manager info
    private String managerName  = "Unknown Manager";
    private String managerClub  = "Unknown Club";
    private String managerImage = "";
    private long   budget       = 100_000_000L;

    private UserTeam() {
        startingXI = new ArrayList<>();
        subs       = FXCollections.observableArrayList();
        formation  = "4-3-3";

        for (int i = 0; i < 11; i++) {
            startingXI.add(null);
        }
    }

    public static UserTeam getInstance() {
        if (instance == null) {
            instance = new UserTeam();
        }
        return instance;
    }

    // ── Budget ────────────────────────────────────────────────────────────────

    public long getBudget() { return budget; }

    public void setBudget(long budget) { this.budget = budget; }

    public boolean deductBudget(long amount) {
        if (budget >= amount) {
            budget -= amount;
            return true;
        }
        return false;
    }

    public void addBudget(long amount) { budget += amount; }

    public String getBudgetFormatted() {
        return String.format("£%,d", budget);
    }

    // ── Starting XI ───────────────────────────────────────────────────────────

    public List<Player> getStartingXI() { return startingXI; }

    public void setPlayerInSlot(int slotIndex, Player player) {
        if (slotIndex >= 0 && slotIndex < 11) {
            startingXI.set(slotIndex, player);
        }
    }

    public Player getPlayerInSlot(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < 11) {
            return startingXI.get(slotIndex);
        }
        return null;
    }

    public boolean isStartingXIComplete() {
        for (Player p : startingXI) {
            if (p == null) return false;
        }
        return true;
    }

    public int getFilledSlotCount() {
        int count = 0;
        for (Player p : startingXI) {
            if (p != null) count++;
        }
        return count;
    }

    // ── Substitutes ───────────────────────────────────────────────────────────

    public ObservableList<Player> getSubs() { return subs; }

    public boolean addSubstitute(Player player) {
        if (subs.size() >= 7) {
            return false;
        }
        subs.add(player);
        return true;
    }

    public void removeSub(Player player) { subs.remove(player); }

    public void benchPlayer(int slot, Player benched) {
        Player starter = startingXI.get(slot);
        startingXI.set(slot, benched);
        int idx = subs.indexOf(benched);
        if (idx >= 0) {
            subs.set(idx, starter);
        }
    }

    // ── Squad checks ──────────────────────────────────────────────────────────

    public boolean isInSXI(Player p) { return startingXI.contains(p); }

    public boolean isSub(Player p) { return subs.contains(p); }

    public boolean isInSquad(Player p) { return isInSXI(p) || isSub(p); }

    // ── Formation ─────────────────────────────────────────────────────────────

    public String getFormation() { return formation; }

    public void setFormation(String f) {
        this.formation = f;
        for (int i = 0; i < 11; i++) {
            startingXI.set(i, null);
        }
    }

    // ── Manager info ──────────────────────────────────────────────────────────

    public String getManagerName()             { return managerName; }
    public void   setManagerName(String name)  { this.managerName = name; }

    public String getManagerClub()             { return managerClub; }
    public void   setManagerClub(String club)  { this.managerClub = club; }

    public String getManagerImage()            { return managerImage; }
    public void   setManagerImage(String path) { this.managerImage = path; }

    // ── Reset ─────────────────────────────────────────────────────────────────

    public static void reset() { instance = null; }
}
