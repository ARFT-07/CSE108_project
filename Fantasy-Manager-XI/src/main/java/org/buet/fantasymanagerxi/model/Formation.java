package org.buet.fantasymanagerxi.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Formation {

    public static final String F_4_3_3   = "4-3-3";
    public static final String F_4_2_3_1 = "4-2-3-1";
    public static final String F_3_5_2   = "3-5-2";

    private static final Map<String, List<String>> FORMATIONS = new HashMap<>();

    static {
        FORMATIONS.put(F_4_3_3, List.of(
                "LW",  "ST",  "RW",   // row 1 — attack  (slots 0,1,2)
                "LM",  "CM",  "RM",   // row 2 — midfield (slots 3,4,5)
                "LB",  "CB",  "CB",  "RB",  // row 3 — defence  (slots 6,7,8,9)
                "GK"                   // row 4 — goalkeeper (slot 10)
        ));

        FORMATIONS.put(F_4_2_3_1, List.of(
                "ST",                  // row 1 — attack   (slot 0)
                "LM",  "CAM", "RM",   // row 2 — att mid  (slots 1,2,3)
                "CDM", "CDM",          // row 3 — def mid  (slots 4,5)
                "LB",  "CB",  "CB",  "RB",  // row 4 — defence  (slots 6,7,8,9)
                "GK"                   // row 5 — goalkeeper (slot 10)
        ));

        FORMATIONS.put(F_3_5_2, List.of(
                "ST",  "ST",           // row 1 — attack   (slots 0,1)
                "LWB", "CM",  "CM",  "CM", "RWB",  // row 2 — midfield (slots 2,3,4,5,6)
                "CB",  "CB",  "CB",   // row 3 — defence  (slots 7,8,9)
                "GK"                   // row 4 — goalkeeper (slot 10)
        ));
    }

    public static List<String> getLabels(String formation) {
        return FORMATIONS.getOrDefault(formation, FORMATIONS.get(F_4_3_3));
    }

    public static List<String> getAvailableFormations() {
        return List.of(F_4_3_3, F_4_2_3_1, F_3_5_2);
    }
}