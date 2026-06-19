package com.countmyh.service;

import com.countmyh.model.WorkHourItem;
import com.countmyh.model.WorkHourSelling;
import com.countmyh.model.WorkPeriodTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JsonPersistenceServiceTest {

    private JsonPersistenceService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Path dataFile = tempDir.resolve("data.json");
        service = new JsonPersistenceService(dataFile);
    }

    @Test
    void shouldSaveAndLoadRoundTrip() throws IOException {
        var tracker = new WorkPeriodTracker();
        tracker.addEntry(new WorkHourItem(
                LocalDate.of(2026, 6, 1), "Opus", "Medscript", "MED-238", 8.0
        ));
        tracker.addEntry(new WorkHourItem(
                LocalDate.of(2026, 6, 2), "Opus", "OOF", "OOF-412", 4.0
        ));
        tracker.addHourSelling(new WorkHourSelling(2024, 70, 0, "29h Mar + 41h Abr"));
        tracker.setLastImportDate(LocalDateTime.of(2026, 6, 18, 10, 0));
        tracker.setLastSourceFile("/path/to/file.csv");

        service.save(tracker);
        var loaded = service.load();

        assertEquals(2, loaded.getEntries().size());
        assertEquals("Opus", loaded.getEntries().getFirst().client());
        assertEquals("Medscript", loaded.getEntries().getFirst().project());
        assertEquals(LocalDate.of(2026, 6, 1), loaded.getEntries().getFirst().date());
        assertEquals(8.0, loaded.getEntries().getFirst().hours());

        assertEquals(1, loaded.getHourSellings().size());
        assertEquals(70, loaded.getHourSellings().getFirst().hoursSold());

        assertEquals(LocalDateTime.of(2026, 6, 18, 10, 0), loaded.getLastImportDate());
        assertEquals("/path/to/file.csv", loaded.getLastSourceFile());
    }

    @Test
    void shouldReturnEmptyTrackerWhenFileDoesNotExist() throws IOException {
        var loaded = service.load();
        assertNotNull(loaded);
        assertTrue(loaded.getEntries().isEmpty());
        assertTrue(loaded.getHourSellings().isEmpty());
    }

    @Test
    void dataFileExistsShouldReturnFalseInitially() {
        assertFalse(service.dataFileExists());
    }

    @Test
    void dataFileExistsShouldReturnTrueAfterSave() throws IOException {
        service.save(new WorkPeriodTracker());
        assertTrue(service.dataFileExists());
    }

    @Test
    void shouldPreserveDateFormatsInJson() throws IOException {
        var tracker = new WorkPeriodTracker();
        tracker.addEntry(new WorkHourItem(
                LocalDate.of(2026, 1, 15), "Client", "Project", "item", 6.0
        ));

        service.save(tracker);

        String json = java.nio.file.Files.readString(service.getDataFile().toPath());
        assertTrue(json.contains("2026-01-15"), "Date should be ISO format in JSON");
    }

    @Test
    void shouldOverwriteExistingFile() throws IOException {
        var tracker1 = new WorkPeriodTracker();
        tracker1.addEntry(new WorkHourItem(LocalDate.of(2026, 1, 1), "A", "P1", "t", 8.0));
        service.save(tracker1);

        var tracker2 = new WorkPeriodTracker();
        tracker2.addEntry(new WorkHourItem(LocalDate.of(2026, 2, 1), "B", "P2", "t", 4.0));
        service.save(tracker2);

        var loaded = service.load();
        assertEquals(1, loaded.getEntries().size());
        assertEquals("B", loaded.getEntries().getFirst().client());
    }
}
