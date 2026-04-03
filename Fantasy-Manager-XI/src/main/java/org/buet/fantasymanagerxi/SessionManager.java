package org.buet.fantasymanagerxi;

import org.buet.fantasymanagerxi.model.Player;

import java.util.List;

public class SessionManager {

    private static NetworkThread networkThread;
    private static String        loggedInClub;
    private static List<Player>  squad;

    // ══════════════════════════════════════════════════════════════════════════
    //  NAVIGATION HISTORY TRACKING
    // ══════════════════════════════════════════════════════════════════════════
    private static String currentScene = null;
    private static String previousScene = null;

    // ══════════════════════════════════════════════════════════════════════════
    //  EXISTING GETTERS/SETTERS
    // ══════════════════════════════════════════════════════════════════════════
    public static NetworkThread getNetworkThread() { return networkThread; }
    public static void setNetworkThread(NetworkThread t) { networkThread = t; }

    public static String getLoggedInClub() { return loggedInClub; }
    public static void setLoggedInClub(String club) { loggedInClub = club; }

    public static List<Player> getSquad() { return squad; }
    public static void setSquad(List<Player> s) { squad = s; }

    // ══════════════════════════════════════════════════════════════════════════
    //  NAVIGATION HISTORY METHODS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Updates the navigation history stack when switching scenes.
     * Call this before navigating to a new scene.
     *
     * @param newScene The FXML filename being navigated to (e.g., "transfer-market.fxml")
     */
    public static void updateSceneHistory(String newScene) {
        if (newScene == null || newScene.isBlank()) {
            return;
        }

        // Only update if we're actually changing scenes
        if (currentScene != null && !currentScene.equals(newScene)) {
            previousScene = currentScene;
        }
        currentScene = newScene;
    }

    /**
     * Retrieves the previous scene in the navigation history.
     *
     * @param fallback Default scene to return if no previous scene exists
     * @return The previous scene filename, or fallback if history is empty
     */
    public static String getPreviousScene(String fallback) {
        return (previousScene != null && !previousScene.isBlank())
                ? previousScene
                : fallback;
    }

    /**
     * Gets the current scene without modifying history.
     * Useful for debugging or conditional logic.
     *
     * @return Current scene filename, or null if none set
     */
    public static String getCurrentScene() {
        return currentScene;
    }

    /**
     * Clears the navigation history.
     * Call this when logging out or resetting the session.
     */
    public static void clearNavigationHistory() {
        currentScene = null;
        previousScene = null;
    }

    /**
     * Clears all session data including navigation history.
     * Call this on logout.
     */
    public static void clearSession() {
        networkThread = null;
        loggedInClub = null;
        squad = null;
        clearNavigationHistory();
    }
}