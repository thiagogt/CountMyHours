package com.countmyh.util;

public final class MonthNames {

    private static final String[] ABBREVIATIONS = {
            "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
            "Jul", "Ago", "Set", "Out", "Nov", "Dez"
    };

    private MonthNames() {
    }

    public static String abbreviation(int month) {
        return ABBREVIATIONS[month - 1];
    }

    public static String label(int year, int month) {
        return abbreviation(month) + "/" + year;
    }
}
