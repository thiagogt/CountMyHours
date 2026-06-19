package com.countmyh.model;

import java.time.LocalDateTime;
import java.util.Objects;

public record ImportRecord(String fileName, String filePath, LocalDateTime importDate, int entriesImported) {

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
