package org.buet.fantasymanagerxi;

import org.buet.fantasymanagerxi.model.Player;

import java.util.List;

public class SessionManager {

    private static NetworkThread networkThread;
    private static String loggedInClubId;
    private static String loggedInClubName;
    private static List<Player> squad;

    private static String currentScene = null;
    private static String previousScene = null;

    public static NetworkThread getNetworkThread() {
        return networkThread;
    }

    public static void setNetworkThread(NetworkThread t) {
        networkThread = t;
    }

    public static String getLoggedInClub() {
        return loggedInClubName;
    }

    public static String getLoggedInClubId() {
        return loggedInClubId;
    }

    public static String getLoggedInClubName() {
        return loggedInClubName;
    }

    public static void startSession(NetworkThread thread, String clubId, String clubName, List<Player> squad) {
        networkThread = thread;
        loggedInClubId = clubId;
        loggedInClubName = clubName;
        SessionManager.squad = squad;
        clearNavigationHistory();
    }

    public static List<Player> getSquad() {
        return squad;
    }

    public static void setSquad(List<Player> s) {
        squad = s;
    }

    public static void updateSceneHistory(String newScene) {
        if (newScene == null || newScene.isBlank()) {
            return;
        }

        if (currentScene != null && !currentScene.equals(newScene)) {
            previousScene = currentScene;
        }
        currentScene = newScene;
    }

    public static String getPreviousScene(String fallback) {
        return (previousScene != null && !previousScene.isBlank()) ? previousScene : fallback;
    }

    public static String getCurrentScene() {
        return currentScene;
    }

    public static void clearNavigationHistory() {
        currentScene = null;
        previousScene = null;
    }

    public static void logout() {
        clearSession();
    }

    public static void clearSession() {
        if (networkThread != null) {
            networkThread.disconnect();
        }
        networkThread = null;
        loggedInClubId = null;
        loggedInClubName = null;
        squad = null;
        clearNavigationHistory();
    }
}
