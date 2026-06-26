package com.countmyh.service;

import com.countmyh.util.I18n;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BusinessDayServiceTest {

    private BusinessDayService service;
    private Locale originalLocale;

    @BeforeEach
    void setUp() {
        originalLocale = I18n.getLocale();
        I18n.setLocale(Locale.of("pt", "BR"));
        service = new BusinessDayService();
    }

    @AfterEach
    void tearDown() {
        I18n.setLocale(originalLocale);
    }

    @Test
    void shouldIncludeFixedBrazilianHolidays() {
        Set<LocalDate> holidays = service.getHolidays(2026);

        assertTrue(holidays.contains(LocalDate.of(2026, 1, 1)),   "Ano Novo");
        assertTrue(holidays.contains(LocalDate.of(2026, 4, 21)),  "Tiradentes");
        assertTrue(holidays.contains(LocalDate.of(2026, 5, 1)),   "Dia do Trabalho");
        assertTrue(holidays.contains(LocalDate.of(2026, 7, 9)),   "Revolucao Constitucionalista SP");
        assertTrue(holidays.contains(LocalDate.of(2026, 9, 7)),   "Independencia");
        assertTrue(holidays.contains(LocalDate.of(2026, 10, 12)), "N.S. Aparecida");
        assertTrue(holidays.contains(LocalDate.of(2026, 11, 2)),  "Finados");
        assertTrue(holidays.contains(LocalDate.of(2026, 11, 15)), "Republica");
        assertTrue(holidays.contains(LocalDate.of(2026, 11, 20)), "Consciencia Negra");
        assertTrue(holidays.contains(LocalDate.of(2026, 12, 25)), "Natal");
    }

    @Test
    void shouldIncludeMoveableBrazilianHolidays2026() {
        Set<LocalDate> holidays = service.getHolidays(2026);

        assertTrue(holidays.contains(LocalDate.of(2026, 2, 16)), "Carnival Monday");
        assertTrue(holidays.contains(LocalDate.of(2026, 2, 17)), "Carnival Tuesday");
        assertTrue(holidays.contains(LocalDate.of(2026, 4, 3)),  "Good Friday");
        assertTrue(holidays.contains(LocalDate.of(2026, 6, 4)),  "Corpus Christi");
    }

    @Test
    void shouldIncludeMoveableBrazilianHolidays2025() {
        Set<LocalDate> holidays = service.getHolidays(2025);

        assertTrue(holidays.contains(LocalDate.of(2025, 3, 3)),  "Carnival Monday");
        assertTrue(holidays.contains(LocalDate.of(2025, 3, 4)),  "Carnival Tuesday");
        assertTrue(holidays.contains(LocalDate.of(2025, 4, 18)), "Good Friday");
        assertTrue(holidays.contains(LocalDate.of(2025, 6, 19)), "Corpus Christi");
    }

    @Test
    void june2026BrazilShouldHave21BusinessDays() {
        // June 2026: 30 days, 8 Sat/Sun, 1 holiday (Corpus Christi Jun 4 Thu)
        assertEquals(21, service.getBusinessDays(2026, 6));
    }

    @Test
    void june2026ExpectedHoursShouldBe168() {
        assertEquals(168.0, service.getExpectedHours(2026, 6));
    }

    @ParameterizedTest
    @CsvSource({
            "2026, 1, 21",  // Jan: 31 days, 8+1 weekend days, 1 holiday (Ano Novo Thu)
            "2026, 2, 18",  // Feb: 28 days, 8 weekend days, 2 holidays (Carnival Mon+Tue)
            "2026, 4, 20",  // Apr: 30 days, 8 weekend days, 2 holidays (Good Friday 3, Tiradentes 21)
    })
    void businessDaysShouldMatchExpected(int year, int month, int expectedDays) {
        assertEquals(expectedDays, service.getBusinessDays(year, month));
    }

    @Test
    void weekendsShouldNeverBeBusinessDays() {
        int bizDays = service.getBusinessDays(2026, 3);
        assertTrue(bizDays > 0);
        assertTrue(bizDays <= 23);
    }

    @Test
    void expectedHoursShouldBe8TimesBusinessDays() {
        for (int month = 1; month <= 12; month++) {
            int bizDays = service.getBusinessDays(2026, month);
            assertEquals(bizDays * 8.0, service.getExpectedHours(2026, month));
        }
    }
}
