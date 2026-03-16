package org.buet.fantasymanagerxi.model;

import java.io.Serializable;

public class MarketMessage implements Serializable {

    public enum Type {
        // Client → Server
        LOGIN,
        GET_MARKET,
        SELL_PLAYER,
        BUY_PLAYER,

        // Server → Client
        LOGIN_OK,
        LOGIN_FAIL,
        SELL_OK,
        BUY_OK,
        MARKET_UPDATE,
        ERROR
    }

    private Type type;
    private String clubName;
    private String password;
    private String playerId;
    private double price;
    private Object payload;

    public MarketMessage(Type type) {
        this.type = type;
    }

    // Getters and setters
    public Type getType() { return type; }
    public String getClubName() { return clubName; }
    public void setClubName(String clubName) { this.clubName = clubName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }
}