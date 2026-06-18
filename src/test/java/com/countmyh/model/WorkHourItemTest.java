package com.countmyh.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class WorkHourItemTest {

    @Test
    void shouldCreateWithAllFields() {
        var item = new WorkHourItem(
                LocalDate.of(2026, 6, 1), "Opus", "Medscript", "MED-238, PR", 8.0
        );

        assertEquals(LocalDate.of(2026, 6, 1), item.getDate());
        assertEquals("Opus", item.getClient());
        assertEquals("Medscript", item.getProject());
        assertEquals("MED-238, PR", item.getItem());
        assertEquals(8.0, item.getHours());
    }

    @Test
    void shouldDeriveYearAndMonth() {
        var item = new WorkHourItem(LocalDate.of(2025, 3, 15), "Opus", "OOF", "task", 4.0);
        assertEquals(2025, item.getYear());
        assertEquals(3, item.getMonth());
    }

    @Test
    void equalItemsShouldBeEqual() {
        var a = new WorkHourItem(LocalDate.of(2026, 6, 1), "Opus", "Medscript", "task", 8.0);
        var b = new WorkHourItem(LocalDate.of(2026, 6, 1), "Opus", "Medscript", "task", 8.0);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differentItemsShouldNotBeEqual() {
        var a = new WorkHourItem(LocalDate.of(2026, 6, 1), "Opus", "Medscript", "task-a", 8.0);
        var b = new WorkHourItem(LocalDate.of(2026, 6, 1), "Opus", "Medscript", "task-b", 8.0);
        assertNotEquals(a, b);
    }

    @Test
    void differentHoursShouldNotBeEqual() {
        var a = new WorkHourItem(LocalDate.of(2026, 6, 1), "Opus", "Medscript", "task", 8.0);
        var b = new WorkHourItem(LocalDate.of(2026, 6, 1), "Opus", "Medscript", "task", 4.0);
        assertNotEquals(a, b);
    }

    @Test
    void toStringShouldFormatAsCsv() {
        var item = new WorkHourItem(LocalDate.of(2026, 6, 1), "Opus", "Medscript", "MED-238", 8.0);
        assertEquals("2026-06-01;Opus;Medscript;MED-238;8.0", item.toString());
    }

    @Test
    void defaultConstructorShouldCreateEmptyItem() {
        var item = new WorkHourItem();
        assertNull(item.getDate());
        assertNull(item.getClient());
        assertEquals(0.0, item.getHours());
    }
}
