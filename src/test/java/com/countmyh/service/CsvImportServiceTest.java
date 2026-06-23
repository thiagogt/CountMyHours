package com.countmyh.service;

import com.countmyh.model.WorkHourItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvImportServiceTest {

    private CsvImportService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new CsvImportService();
    }

    @Test
    void shouldImportValidCsv() throws IOException {
        File csv = createCsvFile("""
                Data;Cliente;Projeto;Item;Hs
                01/06/2026;Opus;Medscript;MED-238, PR;8
                02/06/2026;Opus;Medscript;MED-251, PRs;8
                03/06/2026;Opus;OOF;OOF-412, review;4
                """);

        List<WorkHourItem> items = service.importFile(csv);

        assertEquals(3, items.size());
        assertEquals("medscript", items.getFirst().project());
        assertEquals(LocalDate.of(2026, 6, 1), items.getFirst().date());
        assertEquals(8.0, items.getFirst().hours());
        assertEquals("Opus", items.getFirst().client());
        assertEquals("MED-238, PR", items.getFirst().item());
    }

    @Test
    void shouldSkipHeaderLine() throws IOException {
        File csv = createCsvFile("""
                Data;Cliente;Projeto;Item;Hs
                01/06/2026;Opus;Medscript;task;8
                """);

        List<WorkHourItem> items = service.importFile(csv);
        assertEquals(1, items.size());
    }

    @Test
    void shouldHandleCsvWithoutHeader() throws IOException {
        File csv = createCsvFile("01/06/2026;Opus;Medscript;task;8\n");

        List<WorkHourItem> items = service.importFile(csv);
        assertEquals(1, items.size());
    }

    @Test
    void shouldSkipMalformedLines() throws IOException {
        File csv = createCsvFile("""
                Data;Cliente;Projeto;Item;Hs
                01/06/2026;Opus;Medscript;task;8
                bad;line;here;missing;date
                02/06/2026;Opus;Medscript;task2;4
                """);

        List<WorkHourItem> items = service.importFile(csv);
        assertEquals(2, items.size());
    }

    @Test
    void shouldSkipLinesWithInvalidDate() throws IOException {
        File csv = createCsvFile("""
                Data;Cliente;Projeto;Item;Hs
                99/99/9999;Opus;Medscript;task;8
                01/06/2026;Opus;Medscript;task;8
                """);

        List<WorkHourItem> items = service.importFile(csv);
        assertEquals(1, items.size());
    }

    @Test
    void shouldSkipLinesWithZeroHours() throws IOException {
        File csv = createCsvFile("""
                Data;Cliente;Projeto;Item;Hs
                01/06/2026;Opus;Medscript;task;0
                02/06/2026;Opus;Medscript;task;8
                """);

        List<WorkHourItem> items = service.importFile(csv);
        assertEquals(1, items.size());
    }

    @Test
    void shouldSkipLinesWithEmptyClient() throws IOException {
        File csv = createCsvFile("""
                Data;Cliente;Projeto;Item;Hs
                01/06/2026;;Medscript;task;8
                02/06/2026;Opus;Medscript;task;8
                """);

        List<WorkHourItem> items = service.importFile(csv);
        assertEquals(1, items.size());
    }

    @Test
    void shouldHandleCommaDecimalSeparator() throws IOException {
        File csv = createCsvFile("""
                Data;Cliente;Projeto;Item;Hs
                01/06/2026;Opus;Medscript;task;4,5
                """);

        List<WorkHourItem> items = service.importFile(csv);
        assertEquals(1, items.size());
        assertEquals(4.5, items.getFirst().hours());
    }

    @Test
    void shouldHandleEmptyFile() throws IOException {
        File csv = createCsvFile("");
        List<WorkHourItem> items = service.importFile(csv);
        assertTrue(items.isEmpty());
    }

    @Test
    void shouldRejectUnsupportedFileFormat() {
        File txt = tempDir.resolve("data.txt").toFile();
        assertThrows(IOException.class, () -> service.importFile(txt));
    }

    @Test
    void shouldSkipEmptyLines() throws IOException {
        File csv = createCsvFile("""
                Data;Cliente;Projeto;Item;Hs
                01/06/2026;Opus;Medscript;task;8

                02/06/2026;Opus;Medscript;task2;8
                """);

        List<WorkHourItem> items = service.importFile(csv);
        assertEquals(2, items.size());
    }

    @Test
    void parseCsvLineShouldReturnNullForTooFewColumns() {
        assertNull(service.parseCsvLine("a;b;c", 1));
    }

    @Test
    void shouldHandleTrailingSemicolons() throws IOException {
        File csv = createCsvFile("""
                Data;Cliente;Projeto;Item;Hs;;;;;
                01/02/2017;Opus;admin;task;4;;;;;
                """);

        List<WorkHourItem> items = service.importFile(csv);

        assertEquals(1, items.size());
        assertEquals(4.0, items.getFirst().hours());
    }

    @Test
    void shouldRejectCommaDelimitedCsv() {
        File csv = createCsvFileUnchecked("""
                Data,Cliente,Projeto,Item,Hs
                01/02/2017,Opus,admin,task,4
                """);

        var ex = assertThrows(IOException.class, () -> service.importFile(csv));
        assertTrue(ex.getMessage().contains("comma"));
        assertTrue(ex.getMessage().contains("semicolon"));
    }

    @Test
    void shouldRejectCsvWithNoDelimiter() {
        File csv = createCsvFileUnchecked("Data Cliente Projeto Item Hs\n");

        var ex = assertThrows(IOException.class, () -> service.importFile(csv));
        assertTrue(ex.getMessage().contains("delimiter"));
    }

    @Test
    void shouldRejectCsvWithTooFewColumns() {
        File csv = createCsvFileUnchecked("Data;Cliente;Projeto\n01/06/2026;Opus;Med\n");

        var ex = assertThrows(IOException.class, () -> service.importFile(csv));
        assertTrue(ex.getMessage().contains("5 columns"));
    }

    @Test
    void shouldRejectCsvWithWrongHeaderNames() {
        File csv = createCsvFileUnchecked("Data;Name;Type;Desc;Value\n01/06/2026;A;B;C;8\n");

        var ex = assertThrows(IOException.class, () -> service.importFile(csv));
        assertTrue(ex.getMessage().contains("header"));
    }

    @Test
    void shouldAcceptDataWithoutHeaderRow() throws IOException {
        File csv = createCsvFile("01/06/2026;Opus;Medscript;task;8\n02/06/2026;Opus;Medscript;task2;4\n");

        List<WorkHourItem> items = service.importFile(csv);
        assertEquals(2, items.size());
    }

    private File createCsvFile(String content) throws IOException {
        Path csvPath = tempDir.resolve("test.csv");
        Files.writeString(csvPath, content);
        return csvPath.toFile();
    }

    private File createCsvFileUnchecked(String content) {
        try {
            return createCsvFile(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
