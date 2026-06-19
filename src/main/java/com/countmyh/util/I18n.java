package com.countmyh.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public final class I18n {

    private static ResourceBundle bundle = ResourceBundle.getBundle("com.countmyh.i18n.messages", Locale.getDefault());

    private I18n() {}

    public static void setLocale(Locale locale) {
        bundle = ResourceBundle.getBundle("com.countmyh.i18n.messages", locale);
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
}
