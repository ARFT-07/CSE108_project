package org.buet.fantasymanagerxi.model;

import java.io.Serializable;

public class MarketMessage implements Serializable {
    public enum Type {
        LOGIN,
        GET_MARKET,
        GET_SCOUT_PLAYERS,
        GET_OFFERS,
        SELL_PLAYER,
        BUY_PLAYER,
        MAKE_OFFER,
        ACCEPT_OFFER,
        REJECT_OFFER,
        LOGIN_OK,
        LOGIN_FAIL,
        SELL_OK,
        BUY_OK,
        MARKET_UPDATE,
        SCOUT_PLAYERS_UPDATE,
        OFFERS_UPDATE,
        OFFER_OK,
        OFFER_STATUS_UPDATE,
        SQUAD_UPDATE,
        ERROR
    }

    private Type type;
    private String clubName;
    private String password;
    private String playerId;
    private String offerId;
    private double price;
    private Object payload;

    public MarketMessage(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
