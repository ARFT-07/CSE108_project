package org.buet.fantasymanagerxi.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ClubRegistry {

    private static final Map<String, String> CODE_TO_NAME = new LinkedHashMap<>();

    static {
        CODE_TO_NAME.put("CHELSEA", "Chelsea");
        CODE_TO_NAME.put("LIVERPOOL", "Liverpool");
        CODE_TO_NAME.put("ARSENAL", "Arsenal");
        CODE_TO_NAME.put("MANUTD", "ManUtd");
        CODE_TO_NAME.put("MANCITY", "ManCity");
        CODE_TO_NAME.put("SPURS", "Tottenham");
    }

    private ClubRegistry() {
    }

    public static String toCode(String rawClub) {
        if (rawClub == null) {
            return null;
        }

        String normalized = rawClub.trim();
        if (normalized.isEmpty()) {
            return normalized;
        }

        String upper = normalized.toUpperCase();
        if (CODE_TO_NAME.containsKey(upper)) {
            return upper;
        }

        for (Map.Entry<String, String> entry : CODE_TO_NAME.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(normalized)) {
                return entry.getKey();
            }
        }

        return upper;
    }

    public static String toDisplay(String rawClub) {
        if (rawClub == null) {
            return null;
        }

        String code = toCode(rawClub);
        return CODE_TO_NAME.getOrDefault(code, rawClub.trim());
    }

    public static boolean sameClub(String left, String right) {
        return Objects.equals(toCode(left), toCode(right));
    }
}
