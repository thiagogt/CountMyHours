package com.countmyh.service.calendar;

import com.countmyh.util.I18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads locale-specific calendar settings from calendar_<locale>.properties files.
 * Convention: "pt-BR" → com/countmyh/calendar/calendar_pt_BR.properties
 * Property key: date.format (e.g. "dd/MM/yyyy", "MM/dd/yyyy", "yyyy/MM/dd")
 */
public final class CalendarConfig {

    private static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
    private static final String RESOURCE_PREFIX = "com/countmyh/calendar/calendar_";
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    private CalendarConfig() {}

    public static String getDateFormat() {
        return CACHE.computeIfAbsent(I18n.getLocale().toLanguageTag(), CalendarConfig::load);
    }

    public static DateTimeFormatter getDateFormatter() {
        return DateTimeFormatter.ofPattern(getDateFormat());
    }

    private static String load(String languageTag) {
        String resource = RESOURCE_PREFIX + languageTag.replace("-", "_") + ".properties";
        try (InputStream is = CalendarConfig.class.getClassLoader().getResourceAsStream(resource)) {
            if (is == null) return DEFAULT_DATE_FORMAT;
            Properties props = new Properties();
            props.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            return props.getProperty("date.format", DEFAULT_DATE_FORMAT);
        } catch (IOException e) {
            return DEFAULT_DATE_FORMAT;
        }
    }
}
