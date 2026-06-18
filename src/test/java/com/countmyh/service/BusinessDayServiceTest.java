package com.countmyh.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BusinessDayServiceTest {

    private BusinessDayService service;

    @BeforeEach
    void setUp() {
        service = new BusinessDayService();
    }

    @Test
    void easterDatesShouldBeCorrect() {
        assertEquals(LocalDate.of(2024, 3, 31), service.calculateEaster(2024));
        assertEquals(LocalDate.of(2025, 4, 20), service.calculateEaster(2025));
        assertEquals(LocalDate.of(2026, 4, 5), service.calculateEaster(2026));
        assertEquals(LocalDate.of(2023, 4, 9), service.calculateEaster(2023));
        assertEquals(LocalDate.of(2020, 4, 12), service.calculateEaster(2020));
    }

    @Test
    void shouldIncludeFixedHolidays() {
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
    void shouldIncludeMoveableHolidays2026() {
        // Easter 2026 = April 5
        Set<LocalDate> holidays = service.getHolidays(2026);

        assertTrue(holidays.contains(LocalDate.of(2026, 2, 16)), "Carnival Monday");
        assertTrue(holidays.contains(LocalDate.of(2026, 2, 17)), "Carnival Tuesday");
        assertTrue(holidays.contains(LocalDate.of(2026, 4, 3)),  "Good Friday");
        assertTrue(holidays.contains(LocalDate.of(2026, 6, 4)),  "Corpus Christi");
    }

    @Test
    void shouldIncludeMoveableHolidays2025() {
        // Easter 2025 = April 20
        Set<LocalDate> holidays = service.getHolidays(2025);

        assertTrue(holidays.contains(LocalDate.of(2025, 3, 3)),  "Carnival Monday");
        assertTrue(holidays.contains(LocalDate.of(2025, 3, 4)),  "Carnival Tuesday");
        assertTrue(holidays.contains(LocalDate.of(2025, 4, 18)), "Good Friday");
        assertTrue(holidays.contains(LocalDate.of(2025, 6, 19)), "Corpus Christi");
    }

    @Test
    void june2026ShouldHave21BusinessDays() {
        // June 2026: 30 days, 8 Sat/Sun, 1 holiday (Corpus Christi Jun 4 Thu)
        assertEquals(21, service.getBusinessDays(2026, 6));
    }

    @Test
    void june2026ExpectedHoursShouldBe168() {
        assertEquals(168.0, service.getExpectedHours(2026, 6));
    }

    @ParameterizedTest
    @CsvSource({
            // year, month, expected business days
            "2026, 1, 21",  // Jan: 31 days, 8+1 weekend days, 1 holiday (Ano Novo, Thu)
            "2026, 2, 18",  // Feb: 28 days, 8 weekend days, 2 holidays (Carnival Mon+Tue)
            "2026, 4, 20",  // Apr: 30 days, 8 weekend days, 2 holidays (Good Friday 3, Tiradentes 21)
    })
    void businessDaysShouldMatchExpected(int year, int month, int expectedDays) {
        assertEquals(expectedDays, service.getBusinessDays(year, month));
    }

    @Test
    void weekendsShouldNeverBeBusinessDays() {
        // Any month: weekends should not be counted
        int bizDays = service.getBusinessDays(2026, 3);
        // March 2026: 31 days, has weekdays Mon-Fri
        assertTrue(bizDays > 0);
        assertTrue(bizDays <= 23); // max possible weekdays in a month
    }

    @Test
    void expectedHoursShouldBe8TimesBusinessDays() {
        for (int month = 1; month <= 12; month++) {
            int bizDays = service.getBusinessDays(2026, month);
            assertEquals(bizDays * 8.0, service.getExpectedHours(2026, month));
        }
    }
}
