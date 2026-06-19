package com.countmyh.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ImportRecordTest {

    @Test
    void shouldCreateWithAllFields() {
        var now = LocalDateTime.of(2026, 6, 18, 10, 0);
        var record = new ImportRecord("hours.csv", "/path/hours.csv", now, 15);

        assertEquals("hours.csv", record.fileName());
        assertEquals("/path/hours.csv", record.filePath());
        assertEquals(now, record.importDate());
        assertEquals(15, record.entriesImported());
    }

    @Test
    void equalRecordsShouldBeEqual() {
        var now = LocalDateTime.of(2026, 6, 18, 10, 0);
        var a = new ImportRecord("f.csv", "/p/f.csv", now, 10);
        var b = new ImportRecord("f.csv", "/p/f.csv", now, 10);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differentPathsShouldNotBeEqual() {
        var now = LocalDateTime.of(2026, 6, 18, 10, 0);
        var a = new ImportRecord("f.csv", "/a/f.csv", now, 10);
        var b = new ImportRecord("f.csv", "/b/f.csv", now, 10);
        assertNotEquals(a, b);
    }

    @Test
    void toStringShouldBeReadable() {
        var record = new ImportRecord("hours.csv", "/p/hours.csv",
                LocalDateTime.of(2026, 6, 18, 10, 0), 42);
        assertTrue(record.toString().contains("hours.csv"));
        assertTrue(record.toString().contains("42"));
    }
}
