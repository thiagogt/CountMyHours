package com.countmyh.service;

import com.countmyh.model.WorkHourItem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
        } else if (name.endsWith(".xlsx")) {
            return importXlsx(file);
        }
        throw new IOException("Unsupported file format: " + name + ". Expected .csv or .xlsx");
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

    List<WorkHourItem> importXlsx(File file) throws IOException {
        List<WorkHourItem> items = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean headerSkipped = false;

            for (Row row : sheet) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    Cell firstCell = row.getCell(0);
                    if (firstCell != null && firstCell.getCellType() == CellType.STRING
                            && isHeaderLine(firstCell.getStringCellValue())) {
                        continue;
                    }
                }

                WorkHourItem item = parseXlsxRow(row);
                if (item != null) {
                    items.add(item);
                }
            }
        }
        return items;
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
            String project = parts[2].trim();
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

    private WorkHourItem parseXlsxRow(Row row) {
        try {
            Cell dateCell = row.getCell(0);
            Cell clientCell = row.getCell(1);
            Cell projectCell = row.getCell(2);
            Cell itemCell = row.getCell(3);
            Cell hoursCell = row.getCell(4);

            if (dateCell == null || clientCell == null || projectCell == null || hoursCell == null) {
                return null;
            }

            LocalDate date;
            if (dateCell.getCellType() == CellType.NUMERIC) {
                date = dateCell.getLocalDateTimeCellValue().toLocalDate();
            } else {
                date = LocalDate.parse(dateCell.getStringCellValue().trim(), DATE_FORMAT);
            }

            String client = getCellString(clientCell);
            String project = getCellString(projectCell);
            String item = itemCell != null ? getCellString(itemCell) : "";
            double hours;
            if (hoursCell.getCellType() == CellType.NUMERIC) {
                hours = hoursCell.getNumericCellValue();
            } else {
                hours = parseHours(hoursCell.getStringCellValue().trim());
            }

            if (client.isEmpty() || project.isEmpty() || hours <= 0) {
                return null;
            }

            return new WorkHourItem(date, client, project, item, hours);
        } catch (Exception e) {
            return null;
        }
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((int) cell.getNumericCellValue());
        return "";
    }

    private double parseHours(String value) {
        return Double.parseDouble(value.replace(",", "."));
    }
}
