package org.buet.fantasymanagerxi.server;

import org.buet.fantasymanagerxi.model.Player;
import org.buet.fantasymanagerxi.model.TransferMarket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransferMarketServer {

    private static final int PORT = 5000;
    private static final double DEFAULT_TRANSFER_BUDGET = 500.0;

    private static final Map<String, List<Player>> clubSquads = new HashMap<>();
    private static final Map<String, String> loginCredentials = new HashMap<>();
    private static final Map<String, Double> clubBudgets = new HashMap<>();
    private static final TransferMarket transferMarket = new TransferMarket();
    private static final OfferStore offerStore = new OfferStore();
    private static final List<ClientHandler> allHandlers =
            Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) throws IOException {
        loadAllData();

        ServerSocket serverSocket = new ServerSocket(PORT);
        ExecutorService pool = Executors.newCachedThreadPool();

        System.out.println("Transfer Market Server started on port " + PORT);
        System.out.println("Clubs loaded: " + clubSquads.keySet());

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New connection from: " + clientSocket.getInetAddress());

            ClientHandler handler = new ClientHandler(
                    clientSocket,
                    clubSquads,
                    loginCredentials,
                    clubBudgets,
                    transferMarket,
                    offerStore,
                    allHandlers
            );
            allHandlers.add(handler);
            pool.execute(handler);
        }
    }

    private static void loadAllData() {
        Map<String, String> clubMap = new LinkedHashMap<>();
        clubMap.put("CHELSEA", "Chelsea");
        clubMap.put("LIVERPOOL", "Liverpool");
        clubMap.put("ARSENAL", "Arsenal");
        clubMap.put("MANUTD", "ManUtd");
        clubMap.put("MANCITY", "ManCity");
        clubMap.put("SPURS", "Tottenham");

        for (Map.Entry<String, String> entry : clubMap.entrySet()) {
            String credentialName = entry.getKey();
            String fileName = entry.getValue();
            String path = "/org/buet/fantasymanagerxi/data/" + fileName + ".txt";
            InputStream is = TransferMarketServer.class.getResourceAsStream(path);
            if (is == null) {
                System.out.println("WARNING: Could not find " + path);
                clubSquads.put(credentialName, new ArrayList<>());
                clubBudgets.put(credentialName, DEFAULT_TRANSFER_BUDGET);
                continue;
            }
            List<Player> players = parsePlayers(is, fileName);
            clubSquads.put(credentialName, players);
            clubBudgets.put(credentialName, DEFAULT_TRANSFER_BUDGET);
            System.out.println("Loaded " + players.size() + " players for " + credentialName);
        }

        loadCredentials();
    }

    private static List<Player> parsePlayers(InputStream is, String club) {
        List<Player> list = new ArrayList<>();
        try {
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String[] playerBlocks = content.split("---");

            for (String block : playerBlocks) {
                if (block.isBlank()) {
                    continue;
                }
                Player p = parsePlayer(block.trim(), club);
                if (p != null) {
                    list.add(p);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static Player parsePlayer(String block, String club) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (String line : block.split("\n")) {
            line = line.trim();
            if (line.contains(":")) {
                String key = line.substring(0, line.indexOf(':')).trim();
                String val = line.substring(line.indexOf(':') + 1).trim();
                fields.put(key, val);
            }
        }
        if (!fields.containsKey("NAME")) {
            return null;
        }

        Player player = new Player();
        player.setName(fields.get("NAME"));
        player.setClub(club);
        player.setPosition(fields.getOrDefault("POSITION", ""));
        player.setNationality(fields.getOrDefault("NATIONALITY", ""));
        player.setDob(fields.getOrDefault("DOB", ""));
        player.setFoot(fields.getOrDefault("FOOT", ""));
        player.setContractEnd(fields.getOrDefault("CONTRACT_END", ""));
        player.setShirtNo(parseInt(fields.get("SHIRT_NO")));
        player.setHeightCm(parseInt(fields.get("HEIGHT_CM")));
        player.setWeightKg(parseInt(fields.get("WEIGHT_KG")));
        player.setGoals(parseInt(fields.get("GOALS")));
        player.setAssists(parseInt(fields.get("ASSISTS")));
        player.setAppearances(parseInt(fields.get("APPEARANCES")));
        player.setWagePw(parseDouble(fields.get("WAGE_PER_WEEK")));
        player.setMarketValueM(parseDouble(fields.get("MARKET_VALUE_M")));
        player.setRating(parseDouble(fields.get("RATING")));
        player.setTransferHistory(fields.getOrDefault("TRANSFER_HISTORY", ""));

        String imageName = player.getName()
                .toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9_]", "")
                + ".png";
        player.setImagePath("org/buet/fantasymanagerxi/images/players/" + imageName);
        return player;
    }

    private static void loadCredentials() {
        String path = "/org/buet/fantasymanagerxi/data/ValidLoginInfo.txt";
        InputStream is = TransferMarketServer.class.getResourceAsStream(path);
        if (is == null) {
            System.out.println("WARNING: ValidLoginInfo.txt not found. Using defaults.");
            return;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(" ");
                if (parts.length == 2) {
                    loginCredentials.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Credentials loaded: " + loginCredentials.keySet());
    }

    private static int parseInt(String s) {
        try {
            return s == null ? 0 : Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double parseDouble(String s) {
        try {
            return s == null ? 0 : Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
