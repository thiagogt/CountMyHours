package com.countmyh.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkHourSellingTest {

    @Test
    void shouldCreateWithAllFields() {
        var selling = new WorkHourSelling(2024, 70, 0, "29h Mar + 41h Abr");
        assertEquals(2024, selling.year());
        assertEquals(70, selling.hoursSold());
        assertEquals(0, selling.vacationDaysSold());
        assertEquals("29h Mar + 41h Abr", selling.note());
    }

    @Test
    void vacationHoursShouldBeCalculatedFromDays() {
        var selling = new WorkHourSelling(2020, 0, 30, "Vendeu ferias");
        assertEquals(240.0, selling.vacationHoursSold());
    }

    @Test
    void equalSellingsShouldBeEqual() {
        var a = new WorkHourSelling(2024, 70, 0, "note");
        var b = new WorkHourSelling(2024, 70, 0, "note");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differentYearsShouldNotBeEqual() {
        var a = new WorkHourSelling(2023, 12, 0, "note");
        var b = new WorkHourSelling(2024, 12, 0, "note");
        assertNotEquals(a, b);
    }

    @Test
    void toStringShouldBeReadable() {
        var selling = new WorkHourSelling(2024, 70, 0, "29h Mar + 41h Abr");
        assertTrue(selling.toString().contains("2024"));
        assertTrue(selling.toString().contains("70"));
    }

    @Test
    void toStringWithVacationShouldShowDays() {
        var selling = new WorkHourSelling(2020, 0, 30, "Vendeu ferias");
        assertTrue(selling.toString().contains("30"));
        assertTrue(selling.toString().contains("vacation"));
    }
}
