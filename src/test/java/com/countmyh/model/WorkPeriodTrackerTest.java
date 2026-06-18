package com.countmyh.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
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
}
