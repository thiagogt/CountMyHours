package com.countmyh.service;

import com.countmyh.model.WorkHourItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CsvImportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String CSV_DELIMITER = ";";

    public List<WorkHourItem> importFile(File file) throws IOException {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".csv")) {
            return importCsv(file);
        }
        throw new IOException("Unsupported file format: " + name + ". Expected .csv");
    }

    List<WorkHourItem> importCsv(File file) throws IOException {
        List<WorkHourItem> items = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();
            if (firstLine == null) {
                return items;
            }

            validateCsvFormat(firstLine);

            String line = firstLine;
            if (isHeaderLine(line)) {
                line = reader.readLine();
            }

            int lineNumber = 1;
            while (line != null) {
                lineNumber++;
                line = line.trim();
                if (!line.isEmpty()) {
                    WorkHourItem item = parseCsvLine(line, lineNumber);
                    if (item != null) {
                        items.add(item);
                    }
                }
                line = reader.readLine();
            }
        }
        return items;
    }

    void validateCsvFormat(String firstLine) throws IOException {
        if (!firstLine.contains(";")) {
            if (firstLine.contains(",")) {
                throw new IOException("Wrong CSV format: file uses comma (,) as delimiter. Expected semicolon (;). "
                        + "Required format: Data;Cliente;Projeto;Item;Hs");
            }
            throw new IOException("Wrong CSV format: delimiter ';' not found. "
                    + "Required format: Data;Cliente;Projeto;Item;Hs");
        }

        String[] parts = firstLine.split(";", -1);
        if (parts.length < 5) {
            throw new IOException("Wrong CSV format: expected at least 5 columns (Data;Cliente;Projeto;Item;Hs) "
                    + "but found " + parts.length);
        }

        if (isHeaderLine(firstLine)) {
            String header = firstLine.toLowerCase().trim();
            if (!header.contains("cliente") || !header.contains("projeto")) {
                throw new IOException("Wrong CSV format: header doesn't match expected columns. "
                        + "Required: Data;Cliente;Projeto;Item;Hs");
            }
        }
    }

    private boolean isHeaderLine(String line) {
        String lower = line.toLowerCase().trim();
        return lower.startsWith("data") || lower.startsWith("date");
    }

    WorkHourItem parseCsvLine(String line, int lineNumber) {
        String[] parts = line.split(CSV_DELIMITER, -1);
        if (parts.length < 5) {
            return null;
        }

        try {
            LocalDate date = LocalDate.parse(parts[0].trim(), DATE_FORMAT);
            String client = parts[1].trim();
            String project = parts[2].trim().toLowerCase();
            String item = parts[3].trim();
            double hours = parseHours(parts[4].trim());

            if (client.isEmpty() || project.isEmpty() || hours <= 0) {
                return null;
            }

            return new WorkHourItem(date, client, project, item, hours);
        } catch (DateTimeParseException | NumberFormatException e) {
            return null;
        }
    }

    private double parseHours(String value) {
        return Double.parseDouble(value.replace(",", "."));
    }
}
