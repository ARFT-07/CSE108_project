package org.buet.fantasymanagerxi.server;

import org.buet.fantasymanagerxi.model.TransferOffer;
import org.buet.fantasymanagerxi.util.ClubRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OfferStore {

    private final Map<String, List<TransferOffer>> offersByTargetClub = new LinkedHashMap<>();

    public synchronized void addOffer(TransferOffer offer) {
        purgeExpiredOffers();

        String clubId = ClubRegistry.toCode(offer.getTargetClubId());
        List<TransferOffer> offers = offersByTargetClub.computeIfAbsent(clubId, ignored -> new ArrayList<>());

        for (TransferOffer existing : offers) {
            if (existing.getStatus() == TransferOffer.Status.PENDING
                    && existing.getPlayerId().equals(offer.getPlayerId())
                    && ClubRegistry.sameClub(existing.getOfferingClubId(), offer.getOfferingClubId())) {
                existing.setStatus(TransferOffer.Status.REJECTED);
                existing.setDecisionNote("Superseded by a newer offer from the same club.");
            }
        }

        offers.add(offer);
    }

    public synchronized List<TransferOffer> getOffersForClub(String clubId) {
        purgeExpiredOffers();
        List<TransferOffer> offers = offersByTargetClub.getOrDefault(ClubRegistry.toCode(clubId), List.of());
        return offers.stream()
                .sorted(Comparator.comparing(TransferOffer::getStatus).thenComparingLong(TransferOffer::getCreatedAtEpochMillis))
                .map(this::copyOffer)
                .toList();
    }

    public synchronized TransferOffer acceptOffer(String offerId) {
        purgeExpiredOffers();
        TransferOffer offer = findOffer(offerId);
        if (offer == null || offer.getStatus() != TransferOffer.Status.PENDING) {
            throw new IllegalStateException("Offer is no longer active.");
        }

        offer.setStatus(TransferOffer.Status.ACCEPTED);
        offer.setDecisionNote("Offer accepted.");

        rejectOtherPendingOffersForPlayer(offer.getPlayerId(), offer.getOfferId());
        return copyOffer(offer);
    }

    public synchronized TransferOffer rejectOffer(String offerId, String note) {
        purgeExpiredOffers();
        TransferOffer offer = findOffer(offerId);
        if (offer == null || offer.getStatus() != TransferOffer.Status.PENDING) {
            throw new IllegalStateException("Offer is no longer active.");
        }

        offer.setStatus(TransferOffer.Status.REJECTED);
        offer.setDecisionNote(note);
        return copyOffer(offer);
    }

    public synchronized void expireOffer(String offerId, String note) {
        TransferOffer offer = findOffer(offerId);
        if (offer != null && offer.getStatus() == TransferOffer.Status.PENDING) {
            offer.setStatus(TransferOffer.Status.EXPIRED);
            offer.setDecisionNote(note);
        }
    }

    public synchronized TransferOffer findOfferById(String offerId) {
        purgeExpiredOffers();
        TransferOffer offer = findOffer(offerId);
        return offer == null ? null : copyOffer(offer);
    }

    private void rejectOtherPendingOffersForPlayer(String playerId, String acceptedOfferId) {
        for (List<TransferOffer> offers : offersByTargetClub.values()) {
            for (TransferOffer offer : offers) {
                if (!offer.getOfferId().equals(acceptedOfferId)
                        && offer.getPlayerId().equals(playerId)
                        && offer.getStatus() == TransferOffer.Status.PENDING) {
                    offer.setStatus(TransferOffer.Status.REJECTED);
                    offer.setDecisionNote("Player transferred via another accepted offer.");
                }
            }
        }
    }

    private void purgeExpiredOffers() {
        for (List<TransferOffer> offers : offersByTargetClub.values()) {
            for (TransferOffer offer : offers) {
                if (offer.getStatus() == TransferOffer.Status.PENDING && offer.isExpired()) {
                    offer.setStatus(TransferOffer.Status.EXPIRED);
                    offer.setDecisionNote("Offer expired.");
                }
            }
        }
    }

    private TransferOffer findOffer(String offerId) {
        for (List<TransferOffer> offers : offersByTargetClub.values()) {
            for (TransferOffer offer : offers) {
                if (offer.getOfferId().equals(offerId)) {
                    return offer;
                }
            }
        }
        return null;
    }

    private TransferOffer copyOffer(TransferOffer original) {
        TransferOffer copy = new TransferOffer();
        copy.setOfferId(original.getOfferId());
        copy.setPlayerId(original.getPlayerId());
        copy.setPlayerName(original.getPlayerName());
        copy.setPlayerClubId(original.getPlayerClubId());
        copy.setPlayerClubName(original.getPlayerClubName());
        copy.setOfferingClubId(original.getOfferingClubId());
        copy.setOfferingClubName(original.getOfferingClubName());
        copy.setTargetClubId(original.getTargetClubId());
        copy.setTargetClubName(original.getTargetClubName());
        copy.setPrice(original.getPrice());
        copy.setStatus(original.getStatus());
        copy.setCreatedAtEpochMillis(original.getCreatedAtEpochMillis());
        copy.setExpiresAtEpochMillis(original.getExpiresAtEpochMillis());
        copy.setDecisionNote(original.getDecisionNote());
        return copy;
    }
}
