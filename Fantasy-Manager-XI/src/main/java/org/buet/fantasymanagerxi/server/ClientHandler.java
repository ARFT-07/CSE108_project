package org.buet.fantasymanagerxi.server;

import org.buet.fantasymanagerxi.model.MarketMessage;
import org.buet.fantasymanagerxi.model.Player;
import org.buet.fantasymanagerxi.model.TransferMarket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private final Map<String, List<Player>> clubSquads;
    private final Map<String, String> loginCredentials;
    private String loggedInClub = null;

    private final TransferMarket transferMarket;
    private final List<ClientHandler> allHandlers;

    public ClientHandler(Socket socket, Map<String, List<Player>> clubSquads, Map<String, String> loginCredentials, TransferMarket transferMarket, List<ClientHandler> allHandlers) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.clubSquads = clubSquads;
        this.loginCredentials = loginCredentials;
        this.transferMarket = transferMarket;
        this.allHandlers = allHandlers;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                MarketMessage msg = (MarketMessage) in.readObject();
                dispatch(msg);
            }

        } catch (Exception e) {
            System.out.println("Client disconnected: " +
                    (loggedInClub != null ? loggedInClub : "unknown"));
        } finally {
            allHandlers.remove(this);
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void dispatch(MarketMessage msg) {
        switch (msg.getType()) {
            case LOGIN -> handleLogin(msg);
            case GET_MARKET -> handleGetMarket();
            case SELL_PLAYER -> handleSellPlayer(msg);
            case BUY_PLAYER -> handleBuyPlayer(msg);
            default -> sendMessage(new MarketMessage(MarketMessage.Type.ERROR));
        }
    }


    private void handleLogin(MarketMessage msg) {
        String club = msg.getClubName();
        String password = msg.getPassword();
        String expected = loginCredentials.get(club);

        if (expected != null && expected.equals(password)) {
            loggedInClub = club;
            MarketMessage response = new MarketMessage(MarketMessage.Type.LOGIN_OK);
            response.setPayload(clubSquads.get(club));
            sendMessage(response);
            System.out.println(club + " logged in.");
        } else {
            MarketMessage response = new MarketMessage(MarketMessage.Type.LOGIN_FAIL);
            response.setPayload("Invalid club name or password.");
            sendMessage(response);
        }
    }

    private void handleGetMarket() {
        MarketMessage response = new MarketMessage(MarketMessage.Type.MARKET_UPDATE);
        response.setPayload(transferMarket.getListings());
        sendMessage(response);
    }

    private void handleSellPlayer(MarketMessage msg) {
        if (loggedInClub == null) return;

        String playerName = msg.getPlayerId();
        double price      = msg.getPrice();

        List<Player> squad = clubSquads.get(loggedInClub);
        Player player = squad.stream()
                .filter(p -> p.getName().equals(playerName))
                .findFirst()
                .orElse(null);

        if (player == null) {
            MarketMessage err = new MarketMessage(MarketMessage.Type.ERROR);
            err.setPayload("Player not found in your squad.");
            sendMessage(err);
            return;
        }

        // Move player to transfer market
        squad.remove(player);
        player.setAskingPrice(price);
        player.setOnMarket(true);
        transferMarket.addListing(player);

        // Confirm to the seller
        MarketMessage response = new MarketMessage(MarketMessage.Type.SELL_OK);
        response.setPayload(player);
        sendMessage(response);

        // Send updated squad back to seller so local list is in sync
        MarketMessage squadUpdate = new MarketMessage(MarketMessage.Type.SQUAD_UPDATE);
        squadUpdate.setPayload(new ArrayList<>(squad));
        squadUpdate.setClubName(loggedInClub);
        sendMessage(squadUpdate);

        broadcastMarketUpdate();
    }
    private void handleBuyPlayer(MarketMessage msg) {
        if (loggedInClub == null) return;

        String playerName = msg.getPlayerId();

        String sellingClub = null;

        synchronized (transferMarket) {
            Player player = transferMarket.findById(playerName);

            if (player == null) {
                MarketMessage err = new MarketMessage(MarketMessage.Type.ERROR);
                err.setPayload("Player already sold or not available.");
                sendMessage(err);
                return;
            }

            // Remember who was selling before we change the club
            sellingClub = player.getClub();

            transferMarket.removeListing(playerName);
            player.setClub(loggedInClub);
            player.setOnMarket(false);
            player.setAskingPrice(0);
            clubSquads.get(loggedInClub).add(player);

            MarketMessage response = new MarketMessage(MarketMessage.Type.BUY_OK);
            response.setPayload(player);
            sendMessage(response);
        }

        // Tell the selling club to remove the player from their squad view
        if (sellingClub != null) {
            notifySquadUpdate(sellingClub);
        }

        broadcastMarketUpdate();
    }

    private void broadcastMarketUpdate() {
        MarketMessage update = new MarketMessage(MarketMessage.Type.MARKET_UPDATE);
        update.setPayload(transferMarket.getListings());
        for (ClientHandler handler : allHandlers) {
            handler.sendMessage(update);
        }
    }
    private void notifySquadUpdate(String clubName) {
        MarketMessage update = new MarketMessage(MarketMessage.Type.SQUAD_UPDATE);
        update.setPayload(clubSquads.get(clubName));
        update.setClubName(clubName);

        for (ClientHandler handler : allHandlers) {
            if (clubName.equals(handler.loggedInClub)) {
                handler.sendMessage(update);
            }
        }
    }
    public synchronized void sendMessage(MarketMessage msg) {
        try {
            out.writeObject(msg);
            out.flush();
            out.reset();
        } catch (IOException e) {
            System.out.println("Failed to send message to " + loggedInClub);
        }
    }

}