package com.countmyh.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WorkPeriodTracker {

    private List<WorkHourItem> entries;
    private List<WorkHourSelling> hourSellings;
    private List<ImportRecord> importHistory;
    private List<VacationEntry> vacationDays;
    private List<MonthNote> monthNotes;
    private List<String> hiddenProjects;
    private LocalDateTime lastImportDate;
    private String lastSourceFile;

    public WorkPeriodTracker() {
        this.entries = new ArrayList<>();
        this.hourSellings = new ArrayList<>();
        this.importHistory = new ArrayList<>();
        this.vacationDays = new ArrayList<>();
        this.monthNotes = new ArrayList<>();
        this.hiddenProjects = new ArrayList<>();
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

    public List<VacationEntry> getVacationDays() {
        return vacationDays;
    }

    public void setVacationDays(List<VacationEntry> vacationDays) {
        this.vacationDays = vacationDays != null ? vacationDays : new ArrayList<>();
    }

    public void setVacation(int year, int month, int days) {
        vacationDays.removeIf(v -> v.year() == year && v.month() == month);
        if (days > 0) {
            vacationDays.add(new VacationEntry(year, month, days));
        }
    }

    public int getVacation(int year, int month) {
        return vacationDays.stream()
                .filter(v -> v.year() == year && v.month() == month)
                .mapToInt(VacationEntry::days)
                .findFirst().orElse(0);
    }

    public List<MonthNote> getMonthNotes() {
        return monthNotes;
    }

    public void setMonthNotes(List<MonthNote> monthNotes) {
        this.monthNotes = monthNotes != null ? monthNotes : new ArrayList<>();
    }

    public void setMonthNote(int year, int month, int holidays, String observation) {
        monthNotes.removeIf(n -> n.year() == year && n.month() == month);
        monthNotes.add(new MonthNote(year, month, holidays, observation));
    }

    public MonthNote getMonthNote(int year, int month) {
        return monthNotes.stream()
                .filter(n -> n.year() == year && n.month() == month)
                .findFirst().orElse(null);
    }

    public List<String> getHiddenProjects() {
        return hiddenProjects;
    }

    public void setHiddenProjects(List<String> hiddenProjects) {
        this.hiddenProjects = hiddenProjects != null ? hiddenProjects : new ArrayList<>();
    }

    public boolean isProjectHidden(String project) {
        return hiddenProjects.contains(project);
    }

    public void setProjectHidden(String project, boolean hidden) {
        hiddenProjects.remove(project);
        if (hidden) {
            hiddenProjects.add(project);
        }
    }

    @JsonIgnore
    public List<WorkHourItem> getVisibleEntries() {
        return entries.stream()
                .filter(e -> !hiddenProjects.contains(e.project()))
                .toList();
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

    public void clearAll() {
        entries.clear();
        hourSellings.clear();
        importHistory.clear();
        vacationDays.clear();
        monthNotes.clear();
        hiddenProjects.clear();
        lastImportDate = null;
        lastSourceFile = null;
    }
}
