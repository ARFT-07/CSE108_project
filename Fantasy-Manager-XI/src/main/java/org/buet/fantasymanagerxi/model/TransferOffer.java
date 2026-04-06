package org.buet.fantasymanagerxi.model;

import java.io.Serializable;
import java.util.UUID;

public class TransferOffer implements Serializable {

    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED,
        EXPIRED
    }

    private String offerId = UUID.randomUUID().toString();
    private String playerId;
    private String playerName;
    private String playerClubId;
    private String playerClubName;
    private String offeringClubId;
    private String offeringClubName;
    private String targetClubId;
    private String targetClubName;
    private double price;
    private Status status = Status.PENDING;
    private long createdAtEpochMillis = System.currentTimeMillis();
    private long expiresAtEpochMillis = System.currentTimeMillis() + 86_400_000L;
    private String decisionNote;

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerClubId() {
        return playerClubId;
    }

    public void setPlayerClubId(String playerClubId) {
        this.playerClubId = playerClubId;
    }

    public String getPlayerClubName() {
        return playerClubName;
    }

    public void setPlayerClubName(String playerClubName) {
        this.playerClubName = playerClubName;
    }

    public String getOfferingClubId() {
        return offeringClubId;
    }

    public void setOfferingClubId(String offeringClubId) {
        this.offeringClubId = offeringClubId;
    }

    public String getOfferingClubName() {
        return offeringClubName;
    }

    public void setOfferingClubName(String offeringClubName) {
        this.offeringClubName = offeringClubName;
    }

    public String getTargetClubId() {
        return targetClubId;
    }

    public void setTargetClubId(String targetClubId) {
        this.targetClubId = targetClubId;
    }

    public String getTargetClubName() {
        return targetClubName;
    }

    public void setTargetClubName(String targetClubName) {
        this.targetClubName = targetClubName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getCreatedAtEpochMillis() {
        return createdAtEpochMillis;
    }

    public void setCreatedAtEpochMillis(long createdAtEpochMillis) {
        this.createdAtEpochMillis = createdAtEpochMillis;
    }

    public long getExpiresAtEpochMillis() {
        return expiresAtEpochMillis;
    }

    public void setExpiresAtEpochMillis(long expiresAtEpochMillis) {
        this.expiresAtEpochMillis = expiresAtEpochMillis;
    }

    public String getDecisionNote() {
        return decisionNote;
    }

    public void setDecisionNote(String decisionNote) {
        this.decisionNote = decisionNote;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAtEpochMillis;
    }
}
