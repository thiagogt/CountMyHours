package com.countmyh.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkPeriodTrackerTest {

    @Test
    void shouldStartWithEmptyLists() {
        var tracker = new WorkPeriodTracker();
        assertNotNull(tracker.getEntries());
        assertTrue(tracker.getEntries().isEmpty());
        assertNotNull(tracker.getHourSellings());
        assertTrue(tracker.getHourSellings().isEmpty());
        assertNotNull(tracker.getImportHistory());
        assertTrue(tracker.getImportHistory().isEmpty());
    }

    @Test
    void shouldAddEntry() {
        var tracker = new WorkPeriodTracker();
        var item = new WorkHourItem(LocalDate.of(2026, 6, 1), "Opus", "Medscript", "task", 8.0);
        tracker.addEntry(item);
        assertEquals(1, tracker.getEntries().size());
        assertEquals(item, tracker.getEntries().getFirst());
    }

    @Test
    void shouldAddHourSelling() {
        var tracker = new WorkPeriodTracker();
        var selling = new WorkHourSelling(2024, 70, 0, "note");
        tracker.addHourSelling(selling);
        assertEquals(1, tracker.getHourSellings().size());
    }

    @Test
    void addEntriesWithDedupShouldSkipDuplicates() {
        var tracker = new WorkPeriodTracker();
        var item1 = new WorkHourItem(LocalDate.of(2026, 6, 1), "Opus", "Medscript", "task", 8.0);
        var item2 = new WorkHourItem(LocalDate.of(2026, 6, 2), "Opus", "Medscript", "task2", 8.0);
        var duplicate = new WorkHourItem(LocalDate.of(2026, 6, 1), "Opus", "Medscript", "task", 8.0);

        tracker.addEntry(item1);
        int added = tracker.addEntriesWithDedup(List.of(duplicate, item2));

        assertEquals(1, added);
        assertEquals(2, tracker.getEntries().size());
    }

    @Test
    void addEntriesWithDedupShouldAddAllWhenNoDuplicates() {
        var tracker = new WorkPeriodTracker();
        var item1 = new WorkHourItem(LocalDate.of(2026, 6, 1), "Opus", "Medscript", "task1", 8.0);
        var item2 = new WorkHourItem(LocalDate.of(2026, 6, 2), "Opus", "Medscript", "task2", 8.0);

        int added = tracker.addEntriesWithDedup(List.of(item1, item2));

        assertEquals(2, added);
        assertEquals(2, tracker.getEntries().size());
    }

    @Test
    void setEntriesNullShouldDefaultToEmptyList() {
        var tracker = new WorkPeriodTracker();
        tracker.setEntries(null);
        assertNotNull(tracker.getEntries());
        assertTrue(tracker.getEntries().isEmpty());
    }

    @Test
    void setHourSellingsNullShouldDefaultToEmptyList() {
        var tracker = new WorkPeriodTracker();
        tracker.setHourSellings(null);
        assertNotNull(tracker.getHourSellings());
        assertTrue(tracker.getHourSellings().isEmpty());
    }

    @Test
    void setImportHistoryNullShouldDefaultToEmptyList() {
        var tracker = new WorkPeriodTracker();
        tracker.setImportHistory(null);
        assertNotNull(tracker.getImportHistory());
        assertTrue(tracker.getImportHistory().isEmpty());
    }

    @Test
    void shouldAddImportRecord() {
        var tracker = new WorkPeriodTracker();
        var record = new ImportRecord("f.csv", "/p/f.csv", LocalDateTime.now(), 10);
        tracker.addImportRecord(record);
        assertEquals(1, tracker.getImportHistory().size());
        assertEquals(record, tracker.getImportHistory().getFirst());
    }

    @Test
    void shouldRemoveEntriesBySource() {
        var tracker = new WorkPeriodTracker();
        var item1 = new WorkHourItem(LocalDate.of(2026, 6, 1), "A", "P1", "t", 8.0, "/path/a.csv");
        var item2 = new WorkHourItem(LocalDate.of(2026, 6, 2), "A", "P1", "t", 8.0, "/path/b.csv");
        var item3 = new WorkHourItem(LocalDate.of(2026, 6, 3), "A", "P1", "t", 8.0, "/path/a.csv");

        tracker.addEntry(item1);
        tracker.addEntry(item2);
        tracker.addEntry(item3);

        int removed = tracker.removeEntriesBySource("/path/a.csv");

        assertEquals(2, removed);
        assertEquals(1, tracker.getEntries().size());
        assertEquals("/path/b.csv", tracker.getEntries().getFirst().sourceFile());
    }

    @Test
    void removeEntriesBySourceShouldReturnZeroWhenNoMatch() {
        var tracker = new WorkPeriodTracker();
        var item = new WorkHourItem(LocalDate.of(2026, 6, 1), "A", "P1", "t", 8.0, "/path/a.csv");
        tracker.addEntry(item);

        int removed = tracker.removeEntriesBySource("/path/nonexistent.csv");

        assertEquals(0, removed);
        assertEquals(1, tracker.getEntries().size());
    }

    @Test
    void clearAllShouldResetEverything() {
        var tracker = new WorkPeriodTracker();
        tracker.addEntry(new WorkHourItem(LocalDate.of(2026, 6, 1), "A", "P1", "t", 8.0));
        tracker.addHourSelling(new WorkHourSelling(2026, 10, 0, "note"));
        tracker.addImportRecord(new ImportRecord("f.csv", "/p/f.csv", LocalDateTime.now(), 1));
        tracker.setLastImportDate(LocalDateTime.now());
        tracker.setLastSourceFile("/p/f.csv");

        tracker.clearAll();

        assertTrue(tracker.getEntries().isEmpty());
        assertTrue(tracker.getHourSellings().isEmpty());
        assertTrue(tracker.getImportHistory().isEmpty());
        assertNull(tracker.getLastImportDate());
        assertNull(tracker.getLastSourceFile());
    }

    @Test
    void shouldRemoveImportRecord() {
        var tracker = new WorkPeriodTracker();
        var record = new ImportRecord("f.csv", "/p/f.csv", LocalDateTime.now(), 10);
        tracker.addImportRecord(record);

        tracker.removeImportRecord(record);

        assertTrue(tracker.getImportHistory().isEmpty());
    }
}
