package com.countmyh.service.calendar;

import com.countmyh.util.I18n;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalendarConfigTest {

    private Locale originalLocale;

    @BeforeEach
    void setUp() {
        originalLocale = I18n.getLocale();
    }

    @AfterEach
    void tearDown() {
        I18n.setLocale(originalLocale);
    }

    @Test
    void ptBR_shouldUseDayMonthYear() {
        I18n.setLocale(Locale.of("pt", "BR"));
        assertEquals("dd/MM/yyyy", CalendarConfig.getDateFormat());
    }

    @Test
    void enUS_shouldUseMonthDayYear() {
        I18n.setLocale(Locale.of("en", "US"));
        assertEquals("MM/dd/yyyy", CalendarConfig.getDateFormat());
    }

    @Test
    void enGB_shouldUseDayMonthYear() {
        I18n.setLocale(Locale.of("en", "GB"));
        assertEquals("dd/MM/yyyy", CalendarConfig.getDateFormat());
    }

    @Test
    void enCA_shouldUseIsoFormat() {
        I18n.setLocale(Locale.of("en", "CA"));
        assertEquals("yyyy-MM-dd", CalendarConfig.getDateFormat());
    }

    @Test
    void zhCN_shouldUseYearMonthDay() {
        I18n.setLocale(Locale.of("zh", "CN"));
        assertEquals("yyyy/MM/dd", CalendarConfig.getDateFormat());
    }

    @Test
    void hiIN_shouldUseDayMonthYear() {
        I18n.setLocale(Locale.of("hi", "IN"));
        assertEquals("dd/MM/yyyy", CalendarConfig.getDateFormat());
    }

    @Test
    void jaJP_shouldUseYearMonthDay() {
        I18n.setLocale(Locale.of("ja", "JP"));
        assertEquals("yyyy/MM/dd", CalendarConfig.getDateFormat());
    }

    @Test
    void itIT_shouldUseDayMonthYear() {
        I18n.setLocale(Locale.of("it", "IT"));
        assertEquals("dd/MM/yyyy", CalendarConfig.getDateFormat());
    }

    @Test
    void esES_shouldUseDayMonthYear() {
        I18n.setLocale(Locale.of("es", "ES"));
        assertEquals("dd/MM/yyyy", CalendarConfig.getDateFormat());
    }
}
