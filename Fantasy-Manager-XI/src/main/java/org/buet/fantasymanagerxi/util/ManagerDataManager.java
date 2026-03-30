package org.buet.fantasymanagerxi.util;

import org.buet.fantasymanagerxi.model.Manager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ManagerDataManager {

    private static final List<Manager> ALL_MANAGERS = new ArrayList<>();
    private static boolean loaded = false;

    public static void loadFromStream(InputStream is) {
        ALL_MANAGERS.clear();
        try {
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String[] blocks = content.split("===MANAGER:");

            for (String block : blocks) {
                if (block.isBlank()) continue;
                int nl = block.indexOf('\n');
                if (nl == -1) continue;
                String club = block.substring(0, nl).replace("===", "").trim();
                String[] parts = block.substring(nl).split("---");

                for (String part : parts) {
                    if (part.isBlank()) continue;
                    Manager m = parseManager(part.trim(), club);
                    if (m != null) ALL_MANAGERS.add(m);
                }
            }
            loaded = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Manager parseManager(String block, String club) {
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

        Manager m = new Manager();
        m.setName(f.get("NAME"));
        m.setClub(club);
        m.setAge(parseInt(f.get("AGE")));
        m.setNationality(f.getOrDefault("NATIONALITY", ""));
        m.setImagePath(f.getOrDefault("IMAGE", ""));
        m.setBudget(parseLong(f.get("BUDGET")));
        m.setTrophies(f.getOrDefault("TROPHIES", ""));
        m.setSince(f.getOrDefault("SINCE", ""));
        return m;
    }

    public static Manager getManagerForClub(String club) {
        for (Manager m : ALL_MANAGERS) {
            if (m.getClub().equalsIgnoreCase(club)) return m;
        }
        return null;
    }

    public static List<Manager> getAllManagers() { return ALL_MANAGERS; }

    public static boolean isLoaded() { return loaded; }

    private static int parseInt(String s) {
        try { return s == null ? 0 : Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private static long parseLong(String s) {
        try { return s == null ? 0 : Long.parseLong(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }
}