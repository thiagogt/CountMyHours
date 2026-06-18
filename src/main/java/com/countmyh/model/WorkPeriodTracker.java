package com.countmyh.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WorkPeriodTracker {

    private List<WorkHourItem> entries;
    private List<WorkHourSelling> hourSellings;
    private LocalDateTime lastImportDate;
    private String lastSourceFile;

    public WorkPeriodTracker() {
        this.entries = new ArrayList<>();
        this.hourSellings = new ArrayList<>();
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

    public void addEntry(WorkHourItem entry) {
        entries.add(entry);
    }

    public void addHourSelling(WorkHourSelling selling) {
        hourSellings.add(selling);
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
}
