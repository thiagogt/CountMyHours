package com.countmyh.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public final class I18n {

    private static final Path PREFS_DIR = AppDirs.DATA_DIR;
    private static final Path LOCALE_FILE = PREFS_DIR.resolve("locale");

    private static ResourceBundle bundle;
    private static Locale currentLocale;

    static {
        Locale saved = loadSavedLocale();
        currentLocale = saved != null ? saved : Locale.getDefault();
        bundle = ResourceBundle.getBundle("com.countmyh.i18n.messages", currentLocale);
    }

    private I18n() {}

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        bundle = ResourceBundle.getBundle("com.countmyh.i18n.messages", locale);
        saveLocale(locale);
    }

    public static Locale getLocale() {
        return currentLocale;
    }

    public static boolean hasSavedLocale() {
        return Files.exists(LOCALE_FILE);
    }

    public static String get(String key) {
        try {
            return bundle.getString(key);
        } catch (java.util.MissingResourceException e) {
            return key;
        }
    }

    public static String get(String key, Object... args) {
        return MessageFormat.format(get(key), args);
    }

    private static Locale loadSavedLocale() {
        try {
            if (Files.exists(LOCALE_FILE)) {
                String tag = Files.readString(LOCALE_FILE).trim();
                return Locale.forLanguageTag(tag);
            }
        } catch (IOException ignored) {}
        return null;
    }

    private static void saveLocale(Locale locale) {
        try {
            Files.createDirectories(PREFS_DIR);
            Files.writeString(LOCALE_FILE, locale.toLanguageTag());
        } catch (IOException ignored) {}
    }
}
