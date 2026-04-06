package org.buet.fantasymanagerxi;

import org.buet.fantasymanagerxi.model.MarketMessage;
import javafx.application.Platform;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

public class NetworkThread extends Thread {

    private static final String HOST = "localhost";
    private static final int    PORT = 5000;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream  in;

    private MessageListener listener;

    //public void setListener(PlayerDBController playerDBController) {
      //  this.listener=listener;
    //}

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }
    // Any controller that wants to receive server messages implements this
    public interface MessageListener {
        void onMessageReceived(MarketMessage msg);
        void onConnectionFailed(String reason);
    }

    public NetworkThread(MessageListener listener) {
        this.listener = listener;
        setDaemon(true); // thread dies automatically when JavaFX window closes
    }

    @Override
    public void run() {
        try {
            socket = new Socket(HOST, PORT);

            // IMPORTANT: always create ObjectOutputStream first, flush, then ObjectInputStream
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in  = new ObjectInputStream(socket.getInputStream());

            System.out.println("Connected to server.");

            // Keep listening for messages from server indefinitely
            while (!socket.isClosed()) {
                MarketMessage msg = (MarketMessage) in.readObject();
                // Always update JavaFX UI from the JavaFX thread
                Platform.runLater(() -> {
                    applySessionStateUpdate(msg);
                    if (listener != null) {
                        listener.onMessageReceived(msg);
                    }
                });
            }

        } catch (ConnectException e) {
            Platform.runLater(() ->
                    listener.onConnectionFailed("Could not connect to server. Is it running?"));
        } catch (EOFException | SocketException e) {
            Platform.runLater(() ->
                    listener.onConnectionFailed("Disconnected from server."));
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() ->
                    listener.onConnectionFailed("Network error: " + e.getMessage()));
        }
    }

    public void sendMessage(MarketMessage msg) {
        // Run on a separate thread so we never block the JavaFX UI thread
        new Thread(() -> {
            try {
                out.writeObject(msg);
                out.flush();
                out.reset();
            } catch (IOException e) {
                Platform.runLater(() ->
                        listener.onConnectionFailed("Failed to send message."));
            }
        }).start();
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

    @SuppressWarnings("unchecked")
    private void applySessionStateUpdate(MarketMessage msg) {
        if (msg.getType() == MarketMessage.Type.SQUAD_UPDATE && msg.getPayload() instanceof java.util.List<?> players) {
            SessionManager.setSquad((java.util.List<org.buet.fantasymanagerxi.model.Player>) players);
        }
    }
}
