package org.buet.fantasymanagerxi.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TransferMarket implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<Player> listings=new ArrayList<>();
    public synchronized void addListing(Player p) {
        listings.add(p);
    }
   public synchronized boolean removeListing(String playerId) {
        return listings.removeIf(p->p.getName().equals(playerId));
   }

    public synchronized Player findById(String playerName) {
        return listings.stream()
                .filter(p -> p.getName().equals(playerName))
                .findFirst()
                .orElse(null);
    }

    public synchronized List<Player> getListings() {
        return new ArrayList<>(listings); // always return a copy, never the live list
    }

    public synchronized boolean isEmpty() {
        return listings.isEmpty();
    }

}