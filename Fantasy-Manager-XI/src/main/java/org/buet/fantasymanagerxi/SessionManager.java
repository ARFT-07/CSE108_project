package org.buet.fantasymanagerxi;

import org.buet.fantasymanagerxi.model.Player;

import java.util.List;

public class SessionManager {

    private static NetworkThread networkThread;
    private static String        loggedInClub;
    private static List<Player>  squad;

    public static NetworkThread getNetworkThread() { return networkThread; }
    public static void setNetworkThread(NetworkThread t) { networkThread = t; }

    public static String getLoggedInClub() { return loggedInClub; }
    public static void setLoggedInClub(String club) { loggedInClub = club; }

    public static List<Player> getSquad() { return squad; }
    public static void setSquad(List<Player> s) { squad = s; }
}