package org.buet.fantasymanagerxi.server;

import org.buet.fantasymanagerxi.model.MarketMessage;
import org.buet.fantasymanagerxi.model.Player;
import org.buet.fantasymanagerxi.model.TransferMarket;
import org.buet.fantasymanagerxi.model.TransferOffer;
import org.buet.fantasymanagerxi.util.ClubRegistry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private final Map<String, List<Player>> clubSquads;
    private final Map<String, String> loginCredentials;
    private final Map<String, Double> clubBudgets;
    private String loggedInClub = null;

    private final TransferMarket transferMarket;
    private final OfferStore offerStore;
    private final List<ClientHandler> allHandlers;

    public ClientHandler(
            Socket socket,
            Map<String, List<Player>> clubSquads,
            Map<String, String> loginCredentials,
            Map<String, Double> clubBudgets,
            TransferMarket transferMarket,
            OfferStore offerStore,
            List<ClientHandler> allHandlers
    ) {
        this.socket = socket;
        this.clubSquads = clubSquads;
        this.loginCredentials = loginCredentials;
        this.clubBudgets = clubBudgets;
        this.transferMarket = transferMarket;
        this.offerStore = offerStore;
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
            System.out.println("Client disconnected: " + (loggedInClub != null ? loggedInClub : "unknown"));
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
            case GET_SCOUT_PLAYERS -> handleGetScoutPlayers();
            case GET_OFFERS -> handleGetOffers();
            case SELL_PLAYER -> handleSellPlayer(msg);
            case BUY_PLAYER -> handleBuyPlayer(msg);
            case MAKE_OFFER -> handleMakeOffer(msg);
            case ACCEPT_OFFER -> handleAcceptOffer(msg);
            case REJECT_OFFER -> handleRejectOffer(msg);
            default -> sendError("Unsupported request.");
        }
    }

    private void handleLogin(MarketMessage msg) {
        String club = ClubRegistry.toCode(msg.getClubName());
        String password = msg.getPassword();
        String expected = loginCredentials.get(club);

        if (expected != null && expected.equals(password)) {
            loggedInClub = club;
            MarketMessage response = new MarketMessage(MarketMessage.Type.LOGIN_OK);
            response.setClubName(club);
            response.setPayload(new ArrayList<>(clubSquads.get(club)));
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

    private void handleGetScoutPlayers() {
        if (loggedInClub == null) {
            return;
        }

        List<Player> scoutable = clubSquads.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .sorted(Comparator.comparing(Player::getClub).thenComparing(Player::getName))
                .collect(Collectors.toCollection(ArrayList::new));

        MarketMessage response = new MarketMessage(MarketMessage.Type.SCOUT_PLAYERS_UPDATE);
        response.setPayload(scoutable);
        sendMessage(response);
    }

    private void handleGetOffers() {
        if (loggedInClub == null) {
            return;
        }

        MarketMessage response = new MarketMessage(MarketMessage.Type.OFFERS_UPDATE);
        response.setPayload(offerStore.getOffersForClub(loggedInClub));
        sendMessage(response);
    }

    private void handleSellPlayer(MarketMessage msg) {
        if (loggedInClub == null) {
            return;
        }

        String playerName = msg.getPlayerId();
        double price = msg.getPrice();

        List<Player> squad = clubSquads.get(loggedInClub);
        Player player = squad.stream()
                .filter(p -> p.getName().equals(playerName))
                .findFirst()
                .orElse(null);

        if (player == null) {
            sendError("Player not found in your squad.");
            return;
        }

        squad.remove(player);
        player.setAskingPrice(price);
        player.setOnMarket(true);
        transferMarket.addListing(player);

        MarketMessage response = new MarketMessage(MarketMessage.Type.SELL_OK);
        response.setPayload(player);
        sendMessage(response);

        notifySquadUpdate(loggedInClub);
        broadcastMarketUpdate();
    }

    private void handleBuyPlayer(MarketMessage msg) {
        if (loggedInClub == null) {
            return;
        }

        String playerName = msg.getPlayerId();
        String sellingClub;

        synchronized (transferMarket) {
            Player player = transferMarket.findById(playerName);

            if (player == null) {
                sendError("Player already sold or not available.");
                return;
            }

            if (ClubRegistry.sameClub(player.getClub(), loggedInClub)) {
                sendError("You cannot buy back your own listed player.");
                return;
            }

            double budget = clubBudgets.getOrDefault(loggedInClub, 0.0);
            if (budget < player.getAskingPrice()) {
                sendError("Insufficient transfer budget to complete this purchase.");
                return;
            }

            sellingClub = ClubRegistry.toCode(player.getClub());

            transferMarket.removeListing(playerName);
            player.setClub(ClubRegistry.toDisplay(loggedInClub));
            player.setOnMarket(false);
            clubBudgets.put(loggedInClub, budget - player.getAskingPrice());
            clubBudgets.put(sellingClub, clubBudgets.getOrDefault(sellingClub, 0.0) + player.getAskingPrice());
            player.setAskingPrice(0);
            clubSquads.get(loggedInClub).add(player);

            MarketMessage response = new MarketMessage(MarketMessage.Type.BUY_OK);
            response.setPayload(player);
            sendMessage(response);
        }

        notifySquadUpdate(loggedInClub);
        notifySquadUpdate(sellingClub);
        broadcastMarketUpdate();
    }

    private void handleMakeOffer(MarketMessage msg) {
        if (loggedInClub == null) {
            return;
        }

        if (!(msg.getPayload() instanceof TransferOffer offer)) {
            sendError("Invalid offer payload.");
            return;
        }

        if (offer.getPrice() <= 0) {
            sendError("Offer price must be greater than 0.");
            return;
        }

        if (ClubRegistry.sameClub(offer.getTargetClubId(), loggedInClub)
                || ClubRegistry.sameClub(offer.getPlayerClubId(), loggedInClub)) {
            sendError("You cannot make an offer for your own player.");
            return;
        }

        Player targetPlayer = findPlayerInSquad(offer.getTargetClubId(), offer.getPlayerId());
        if (targetPlayer == null) {
            sendError("Player is no longer available for private offers.");
            return;
        }

        double budget = clubBudgets.getOrDefault(loggedInClub, 0.0);
        if (budget < offer.getPrice()) {
            sendError("Insufficient transfer budget for this offer.");
            return;
        }

        offer.setOfferingClubId(loggedInClub);
        offer.setOfferingClubName(ClubRegistry.toDisplay(loggedInClub));
        offer.setTargetClubId(ClubRegistry.toCode(offer.getTargetClubId()));
        offer.setTargetClubName(ClubRegistry.toDisplay(offer.getTargetClubId()));
        offer.setPlayerClubId(ClubRegistry.toCode(offer.getPlayerClubId()));
        offer.setPlayerClubName(ClubRegistry.toDisplay(offer.getPlayerClubId()));
        offer.setCreatedAtEpochMillis(System.currentTimeMillis());
        offer.setExpiresAtEpochMillis(System.currentTimeMillis() + 86_400_000L);

        try {
            offerStore.addOffer(offer);
        } catch (IllegalStateException e) {
            sendError(e.getMessage());
            return;
        }

        MarketMessage ok = new MarketMessage(MarketMessage.Type.OFFER_OK);
        ok.setPayload(offer);
        sendMessage(ok);

        notifyOffersUpdate(offer.getTargetClubId());
    }

    private void handleAcceptOffer(MarketMessage msg) {
        if (loggedInClub == null) {
            return;
        }

        TransferOffer offer = offerStore.findOfferById(msg.getOfferId());
        if (offer == null) {
            sendError("Offer not found.");
            return;
        }

        if (!ClubRegistry.sameClub(offer.getTargetClubId(), loggedInClub)) {
            sendError("You can only act on offers sent to your club.");
            return;
        }

        if (offer.isExpired()) {
            offerStore.expireOffer(offer.getOfferId(), "Offer expired before review.");
            notifyOffersUpdate(offer.getTargetClubId());
            notifyOfferDecision(offerStore.findOfferById(offer.getOfferId()));
            sendError("Offer expired before it could be accepted.");
            return;
        }

        Player player = findPlayerInSquad(offer.getTargetClubId(), offer.getPlayerId());
        if (player == null) {
            offerStore.expireOffer(offer.getOfferId(), "Player is no longer owned by the selling club.");
            notifyOffersUpdate(offer.getTargetClubId());
            notifyOfferDecision(offerStore.findOfferById(offer.getOfferId()));
            sendError("Player is no longer available.");
            return;
        }

        double buyerBudget = clubBudgets.getOrDefault(offer.getOfferingClubId(), 0.0);
        if (buyerBudget < offer.getPrice()) {
            offerStore.rejectOffer(offer.getOfferId(), "Rejected because the buyer no longer has enough budget.");
            TransferOffer updatedOffer = offerStore.findOfferById(offer.getOfferId());
            notifyOffersUpdate(offer.getTargetClubId());
            notifyOfferDecision(updatedOffer);
            sendError("Buyer no longer has enough transfer budget.");
            return;
        }

        List<Player> sellerSquad = clubSquads.get(offer.getTargetClubId());
        List<Player> buyerSquad = clubSquads.get(offer.getOfferingClubId());
        sellerSquad.removeIf(existing -> existing.getName().equals(player.getName()));
        player.setClub(ClubRegistry.toDisplay(offer.getOfferingClubId()));
        buyerSquad.add(player);

        clubBudgets.put(offer.getOfferingClubId(), buyerBudget - offer.getPrice());
        clubBudgets.put(offer.getTargetClubId(),
                clubBudgets.getOrDefault(offer.getTargetClubId(), 0.0) + offer.getPrice());

        TransferOffer accepted = offerStore.acceptOffer(offer.getOfferId());

        notifySquadUpdate(offer.getTargetClubId());
        notifySquadUpdate(offer.getOfferingClubId());
        notifyOffersUpdate(offer.getTargetClubId());
        notifyOfferDecision(accepted);
    }

    private void handleRejectOffer(MarketMessage msg) {
        if (loggedInClub == null) {
            return;
        }

        TransferOffer offer = offerStore.findOfferById(msg.getOfferId());
        if (offer == null) {
            sendError("Offer not found.");
            return;
        }

        if (!ClubRegistry.sameClub(offer.getTargetClubId(), loggedInClub)) {
            sendError("You can only act on offers sent to your club.");
            return;
        }

        TransferOffer rejected;
        try {
            rejected = offerStore.rejectOffer(offer.getOfferId(), "Offer rejected by the club.");
        } catch (IllegalStateException e) {
            sendError(e.getMessage());
            return;
        }

        notifyOffersUpdate(offer.getTargetClubId());
        notifyOfferDecision(rejected);
    }

    private Player findPlayerInSquad(String clubId, String playerId) {
        List<Player> squad = clubSquads.get(ClubRegistry.toCode(clubId));
        if (squad == null) {
            return null;
        }
        return squad.stream()
                .filter(player -> player.getName().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    private void broadcastMarketUpdate() {
        MarketMessage update = new MarketMessage(MarketMessage.Type.MARKET_UPDATE);
        update.setPayload(transferMarket.getListings());
        for (ClientHandler handler : allHandlers) {
            handler.sendMessage(update);
        }
    }

    private void notifySquadUpdate(String clubName) {
        String clubId = ClubRegistry.toCode(clubName);
        MarketMessage update = new MarketMessage(MarketMessage.Type.SQUAD_UPDATE);
        update.setPayload(new ArrayList<>(clubSquads.get(clubId)));
        update.setClubName(clubId);

        for (ClientHandler handler : allHandlers) {
            if (ClubRegistry.sameClub(clubId, handler.loggedInClub)) {
                handler.sendMessage(update);
            }
        }
    }

    private void notifyOffersUpdate(String clubId) {
        String normalizedClubId = ClubRegistry.toCode(clubId);
        MarketMessage update = new MarketMessage(MarketMessage.Type.OFFERS_UPDATE);
        update.setPayload(offerStore.getOffersForClub(normalizedClubId));
        update.setClubName(normalizedClubId);

        for (ClientHandler handler : allHandlers) {
            if (ClubRegistry.sameClub(normalizedClubId, handler.loggedInClub)) {
                handler.sendMessage(update);
            }
        }
    }

    private void notifyOfferDecision(TransferOffer offer) {
        if (offer == null) {
            return;
        }

        MarketMessage update = new MarketMessage(MarketMessage.Type.OFFER_STATUS_UPDATE);
        update.setPayload(offer);
        update.setOfferId(offer.getOfferId());

        for (ClientHandler handler : allHandlers) {
            if (ClubRegistry.sameClub(offer.getOfferingClubId(), handler.loggedInClub)
                    || ClubRegistry.sameClub(offer.getTargetClubId(), handler.loggedInClub)) {
                handler.sendMessage(update);
            }
        }
    }

    private void sendError(String message) {
        MarketMessage err = new MarketMessage(MarketMessage.Type.ERROR);
        err.setPayload(message);
        sendMessage(err);
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
