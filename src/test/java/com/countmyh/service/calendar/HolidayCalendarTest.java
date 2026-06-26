package com.countmyh.service.calendar;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HolidayCalendarTest {

    private Map<LocalDate, String> holidays(String localeTag, int year) {
        return HolidayCalendarFactory.forLocale(localeTag).getNamedHolidays(year);
    }

    // ---- Brazil ----
    @Test void brazil_fixedHolidays2026() {
        var h = holidays("pt-BR", 2026);
        assertTrue(h.containsKey(LocalDate.of(2026, 1, 1)),  "Ano Novo");
        assertTrue(h.containsKey(LocalDate.of(2026, 4, 21)), "Tiradentes");
        assertTrue(h.containsKey(LocalDate.of(2026, 12, 25)),"Natal");
    }
    @Test void brazil_moveableHolidays2026() {
        var h = holidays("pt-BR", 2026);
        assertTrue(h.containsKey(LocalDate.of(2026, 2, 16)), "Carnaval Mon");
        assertTrue(h.containsKey(LocalDate.of(2026, 4, 3)),  "Sexta-feira Santa");
        assertTrue(h.containsKey(LocalDate.of(2026, 6, 4)),  "Corpus Christi");
    }

    // ---- USA ----
    @Test void usa_independenceDay() {
        assertTrue(holidays("en-US", 2026).containsKey(LocalDate.of(2026, 7, 4)));
    }
    @Test void usa_thanksgiving2026() {
        // 4th Thursday November 2026 = Nov 26
        assertTrue(holidays("en-US", 2026).containsKey(LocalDate.of(2026, 11, 26)));
    }
    @Test void usa_mlkDay2026() {
        // 3rd Monday January 2026 = Jan 19
        assertTrue(holidays("en-US", 2026).containsKey(LocalDate.of(2026, 1, 19)));
    }
    @Test void usa_enUsTag() {
        assertTrue(holidays("en-US", 2026).containsKey(LocalDate.of(2026, 7, 4)));
    }

    // ---- UK ----
    @Test void uk_boxingDay() {
        assertTrue(holidays("en-GB", 2026).containsKey(LocalDate.of(2026, 12, 26)));
    }
    @Test void uk_goodFriday2026() {
        // Easter 2026 = Apr 5, Good Friday = Apr 3
        assertTrue(holidays("en-GB", 2026).containsKey(LocalDate.of(2026, 4, 3)));
    }

    // ---- Canada ----
    @Test void canada_canadaDay() {
        assertTrue(holidays("en-CA", 2026).containsKey(LocalDate.of(2026, 7, 1)));
    }
    @Test void canada_thanksgivingOctober() {
        // 2nd Monday October 2026 = Oct 12
        assertTrue(holidays("en-CA", 2026).containsKey(LocalDate.of(2026, 10, 12)));
    }

    // ---- India ----
    @Test void india_republicDay() {
        assertTrue(holidays("hi-IN", 2026).containsKey(LocalDate.of(2026, 1, 26)));
    }
    @Test void india_independenceDay() {
        assertTrue(holidays("hi-IN", 2026).containsKey(LocalDate.of(2026, 8, 15)));
    }

    // ---- Italy ----
    @Test void italy_liberationDay() {
        assertTrue(holidays("it-IT", 2026).containsKey(LocalDate.of(2026, 4, 25)));
    }
    @Test void italy_easterMonday2026() {
        // Easter 2026 = Apr 5, Easter Monday = Apr 6
        assertTrue(holidays("it-IT", 2026).containsKey(LocalDate.of(2026, 4, 6)));
    }

    // ---- Japan ----
    @Test void japan_cultureDay() {
        assertTrue(holidays("ja-JP", 2026).containsKey(LocalDate.of(2026, 11, 3)));
    }
    @Test void japan_constitutionDay() {
        assertTrue(holidays("ja-JP", 2026).containsKey(LocalDate.of(2026, 5, 3)));
    }

    // ---- China ----
    @Test void china_nationalDay() {
        assertTrue(holidays("zh-CN", 2026).containsKey(LocalDate.of(2026, 10, 1)));
    }
    @Test void china_springFestival2026() {
        // CNY 2026 = Feb 17, eve = Feb 16
        assertTrue(holidays("zh-CN", 2026).containsKey(LocalDate.of(2026, 2, 17)));
    }

    // ---- Spain ----
    @Test void spain_nationalDay() {
        assertTrue(holidays("es-ES", 2026).containsKey(LocalDate.of(2026, 10, 12)));
    }
    @Test void spain_goodFriday2026() {
        // Easter 2026 = Apr 5, Good Friday = Apr 3
        assertTrue(holidays("es-ES", 2026).containsKey(LocalDate.of(2026, 4, 3)));
    }
    @Test void spain_epiphany() {
        assertTrue(holidays("es-ES", 2026).containsKey(LocalDate.of(2026, 1, 6)));
    }

    // ---- File is empty for unknown year returns empty map ----
    @Test void unknownYearReturnsEmpty() {
        assertTrue(holidays("en-US", 2099).isEmpty());
    }
}
