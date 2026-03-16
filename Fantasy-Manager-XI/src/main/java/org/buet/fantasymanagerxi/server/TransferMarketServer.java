package org.buet.fantasymanagerxi.server;

import org.buet.fantasymanagerxi.model.Player;
import org.buet.fantasymanagerxi.model.TransferMarket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransferMarketServer {

    private static final int PORT = 5000;

    // Club name → list of players
    private static final Map<String, List<Player>> clubSquads = new HashMap<>();

    // Club name → password
    private static final Map<String, String> loginCredentials = new HashMap<>();

    // The shared transfer market
    private static final TransferMarket transferMarket = new TransferMarket();

    // All currently connected handlers (for broadcasting)
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
                    transferMarket,
                    allHandlers
            );
            allHandlers.add(handler);
            pool.execute(handler);
        }
    }

    private static void loadAllData() {
        String[] clubs = {
                "Arsenal", "Chelsea", "Liverpool",
                "ManCity", "ManUtd", "Tottenham"
        };

        for (String club : clubs) {
            String path = "/org/buet/fantasymanagerxi/data/" + club + ".txt";
            InputStream is = TransferMarketServer.class.getResourceAsStream(path);
            if (is == null) {
                System.out.println("WARNING: Could not find " + path);
                clubSquads.put(club, new ArrayList<>());
                continue;
            }
            List<Player> players = parsePlayers(is, club);
            clubSquads.put(club, players);
            System.out.println("Loaded " + players.size() + " players for " + club);
        }

        loadCredentials();
    }

    private static List<Player> parsePlayers(InputStream is, String club) {
        List<Player> list = new ArrayList<>();
        try {
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String[] playerBlocks = content.split("---");

            for (String block : playerBlocks) {
                if (block.isBlank()) continue;
                Player p = parsePlayer(block.trim(), club);
                if (p != null) list.add(p);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
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
        return p;
    }

    private static void loadCredentials() {
        String path = "/org/buet/fantasymanagerxi/data/ValidLoginInfo.txt";
        InputStream is = TransferMarketServer.class.getResourceAsStream(path);
        if (is == null) {
            System.out.println("WARNING: ValidLoginInfo.txt not found. Using defaults.");
            loginCredentials.put("Arsenal",   "arsenal");
            loginCredentials.put("Chelsea",   "chelsea");
            loginCredentials.put("Liverpool", "liverpool");
            loginCredentials.put("ManCity",   "mancity");
            loginCredentials.put("ManUtd",    "manutd");
            loginCredentials.put("Tottenham", "tottenham");
            return;
        }
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isBlank()) continue;
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    loginCredentials.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Credentials loaded for: " + loginCredentials.keySet());
    }

    private static int parseInt(String s) {
        try { return s == null ? 0 : Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private static double parseDouble(String s) {
        try { return s == null ? 0 : Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }
}