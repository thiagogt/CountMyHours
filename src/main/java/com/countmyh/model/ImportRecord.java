package com.countmyh.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class ImportRecord {

    private String fileName;
    private String filePath;
    private LocalDateTime importDate;
    private int entriesImported;

    public ImportRecord() {
    }

    public ImportRecord(String fileName, String filePath, LocalDateTime importDate, int entriesImported) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.importDate = importDate;
        this.entriesImported = entriesImported;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getImportDate() {
        return importDate;
    }

    public void setImportDate(LocalDateTime importDate) {
        this.importDate = importDate;
    }

    public int getEntriesImported() {
        return entriesImported;
    }

    public void setEntriesImported(int entriesImported) {
        this.entriesImported = entriesImported;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImportRecord that = (ImportRecord) o;
        return Objects.equals(filePath, that.filePath) && Objects.equals(importDate, that.importDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath, importDate);
    }

    @Override
    public String toString() {
        return fileName + " (" + entriesImported + " entries, " + importDate + ")";
    }
}
