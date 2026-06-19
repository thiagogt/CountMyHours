package com.countmyh.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WorkPeriodTracker {

    private List<WorkHourItem> entries;
    private List<WorkHourSelling> hourSellings;
    private List<ImportRecord> importHistory;
    private LocalDateTime lastImportDate;
    private String lastSourceFile;

    public WorkPeriodTracker() {
        this.entries = new ArrayList<>();
        this.hourSellings = new ArrayList<>();
        this.importHistory = new ArrayList<>();
    }

    public List<WorkHourItem> getEntries() {
        return entries;
    }

    public void setEntries(List<WorkHourItem> entries) {
        this.entries = entries != null ? entries : new ArrayList<>();
    }

    public List<WorkHourSelling> getHourSellings() {
        return hourSellings;
    }

    public void setHourSellings(List<WorkHourSelling> hourSellings) {
        this.hourSellings = hourSellings != null ? hourSellings : new ArrayList<>();
    }

    public LocalDateTime getLastImportDate() {
        return lastImportDate;
    }

    public void setLastImportDate(LocalDateTime lastImportDate) {
        this.lastImportDate = lastImportDate;
    }

    public String getLastSourceFile() {
        return lastSourceFile;
    }

    public void setLastSourceFile(String lastSourceFile) {
        this.lastSourceFile = lastSourceFile;
    }

    public List<ImportRecord> getImportHistory() {
        return importHistory;
    }

    public void setImportHistory(List<ImportRecord> importHistory) {
        this.importHistory = importHistory != null ? importHistory : new ArrayList<>();
    }

    public void addEntry(WorkHourItem entry) {
        entries.add(entry);
    }

    public void addHourSelling(WorkHourSelling selling) {
        hourSellings.add(selling);
    }

    public void addImportRecord(ImportRecord record) {
        importHistory.add(record);
    }

    public int addEntriesWithDedup(List<WorkHourItem> newEntries) {
        int added = 0;
        for (WorkHourItem entry : newEntries) {
            if (!entries.contains(entry)) {
                entries.add(entry);
                added++;
            }
        }
        return added;
    }

    public int removeEntriesBySource(String sourceFilePath) {
        int before = entries.size();
        entries.removeIf(e -> sourceFilePath.equals(e.sourceFile()));
        return before - entries.size();
    }

    public void removeImportRecord(ImportRecord record) {
        importHistory.remove(record);
    }
}
