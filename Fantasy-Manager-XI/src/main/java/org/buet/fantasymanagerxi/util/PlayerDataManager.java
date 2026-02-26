package org.buet.fantasymanagerxi.util;

import org.buet.fantasymanagerxi.model.Player;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerDataManager {

    private static final List<Player> ALL_PLAYERS = new ArrayList<>();
    private static boolean loaded = false;

    public static void loadFromStream(InputStream is) {
        ALL_PLAYERS.clear();
        try {
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String[] clubBlocks = content.split("===CLUB:");

            for (String block : clubBlocks) {
                if (block.isBlank()) continue;
                int nl = block.indexOf('\n');
                if (nl == -1) continue;
                String club = block.substring(0, nl).replace("===", "").trim();
                String[] playerBlocks = block.substring(nl).split("---");

                for (String pb : playerBlocks) {
                    if (pb.isBlank()) continue;
                    Player p = parsePlayer(pb.trim(), club);
                    if (p != null) ALL_PLAYERS.add(p);
                }
            }
            loaded = true;
            System.out.println("Loaded " + ALL_PLAYERS.size() + " players.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Player parsePlayer(String block, String club) {
        Map<String, String> f = new LinkedHashMap<>();
        for (String line : block.split("\n")) {
            line = line.trim();
            if (line.contains(":")) {
                String key = line.substring(0, line.indexOf(':')).trim();
                String val = line.substring(line.indexOf(':') + 1).trim();
                f.put(key, val);
            }
        }
        if (!f.containsKey("NAME")) return null;

        Player p = new Player();
        p.setName(f.get("NAME"));
        p.setClub(club);
        p.setPosition(f.getOrDefault("POSITION", ""));
        p.setNationality(f.getOrDefault("NATIONALITY", ""));
        p.setDob(f.getOrDefault("DOB", ""));
        p.setFoot(f.getOrDefault("FOOT", ""));
        p.setContractEnd(f.getOrDefault("CONTRACT_END", ""));
        p.setShirtNo(parseInt(f.get("SHIRT_NO")));
        p.setHeightCm(parseInt(f.get("HEIGHT_CM")));
        p.setWeightKg(parseInt(f.get("WEIGHT_KG")));
        p.setGoals(parseInt(f.get("GOALS")));
        p.setAssists(parseInt(f.get("ASSISTS")));
        p.setAppearances(parseInt(f.get("APPEARANCES")));
        p.setWagePw(parseDouble(f.get("WAGE_PER_WEEK")));
        p.setMarketValueM(parseDouble(f.get("MARKET_VALUE_M")));
        p.setRating(parseDouble(f.get("RATING")));
        p.setTransferHistory(f.getOrDefault("TRANSFER_HISTORY", ""));

        String imageName = p.getName()
                .toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9_]", "")
                + ".png";
        p.setImagePath("org/buet/fantasymanagerxi/images/players/" + imageName);

        return p;
    }

    public static List<String> getClubs() {
        return ALL_PLAYERS.stream()
                .map(Player::getClub)
                .distinct()
                .collect(Collectors.toList());
    }

    public static List<Player> filter(String club, String position, String search) {
        return ALL_PLAYERS.stream()
                .filter(p -> club.equals("All Clubs") || p.getClub().equals(club))
                .filter(p -> position.equals("ALL")   || p.getPosition().equals(position))
                .filter(p -> search.isBlank()          ||
                        p.getName().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());
    }

    public static boolean isLoaded() { return loaded; }

    private static int parseInt(String s) {
        try { return s == null ? 0 : Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private static double parseDouble(String s) {
        try { return s == null ? 0 : Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }
}