package com.countmyh.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ColorPalette {

    private static final Map<String, String> PROJECT_COLORS = new LinkedHashMap<>();
    private static final String[] FALLBACK_COLORS = {
            "#6366f1", "#a855f7", "#22d3ee", "#f59e0b", "#10b981",
            "#ef4444", "#ec4899", "#14b8a6", "#8b5cf6", "#f97316",
            "#06b6d4", "#84cc16", "#e11d48", "#7c3aed", "#0ea5e9"
    };

    static {
        PROJECT_COLORS.put("cbss", "#6366f1");
        PROJECT_COLORS.put("OOF", "#a855f7");
        PROJECT_COLORS.put("FieldForce Goodyear", "#22d3ee");
        PROJECT_COLORS.put("admin", "#64748b");
        PROJECT_COLORS.put("pagbem", "#f59e0b");
        PROJECT_COLORS.put("Kiosk Mc Operation", "#10b981");
        PROJECT_COLORS.put("Medscript", "#ef4444");
        PROJECT_COLORS.put("iFood", "#ec4899");
        PROJECT_COLORS.put("Yconnect Yazigi", "#14b8a6");
        PROJECT_COLORS.put("Tablet Commerce", "#8b5cf6");
        PROJECT_COLORS.put("Carrefour", "#f97316");
    }

    private static int fallbackIndex = 0;

    private ColorPalette() {
    }

    public static String getColor(String project) {
        String color = PROJECT_COLORS.get(project);
        if (color != null) return color;

        for (var entry : PROJECT_COLORS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(project)) {
                return entry.getValue();
            }
        }

        color = FALLBACK_COLORS[fallbackIndex % FALLBACK_COLORS.length];
        PROJECT_COLORS.put(project, color);
        fallbackIndex++;
        return color;
    }
}
