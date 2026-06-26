package com.countmyh.service.calendar;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public class HolidayCalendarLoader {

    private static final Map<String, List<Map.Entry<LocalDate, String>>> CACHE = new HashMap<>();

    public static HolidayCalendar load(String resourceName) {
        List<Map.Entry<LocalDate, String>> entries =
                CACHE.computeIfAbsent(resourceName, HolidayCalendarLoader::parse);
        return year -> {
            Map<LocalDate, String> result = new LinkedHashMap<>();
            for (var entry : entries) {
                if (entry.getKey().getYear() == year) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            return result;
        };
    }

    private static List<Map.Entry<LocalDate, String>> parse(String resourceName) {
        List<Map.Entry<LocalDate, String>> result = new ArrayList<>();
        String path = "/com/countmyh/holidays/" + resourceName;
        try (var is = HolidayCalendarLoader.class.getResourceAsStream(path)) {
            if (is == null) return result;
            var props = new Properties();
            props.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            for (String key : props.stringPropertyNames()) {
                try {
                    LocalDate date = LocalDate.parse(key.trim());
                    result.add(Map.entry(date, props.getProperty(key).trim()));
                } catch (DateTimeParseException ignored) {}
            }
        } catch (IOException ignored) {}
        result.sort(Map.Entry.comparingByKey());
        return result;
    }
}
