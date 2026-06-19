package com.countmyh.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.Objects;

public record WorkHourItem(LocalDate date, String client, String project, String item, double hours, String sourceFile) {

    public WorkHourItem(LocalDate date, String client, String project, String item, double hours) {
        this(date, client, project, item, hours, null);
    }

    public WorkHourItem withSourceFile(String sourceFile) {
        return new WorkHourItem(date, client, project, item, hours, sourceFile);
    }

    @JsonIgnore
    public int year() {
        return date.getYear();
    }

    @JsonIgnore
    public int month() {
        return date.getMonthValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkHourItem that = (WorkHourItem) o;
        return Double.compare(that.hours, hours) == 0
                && Objects.equals(date, that.date)
                && Objects.equals(client, that.client)
                && Objects.equals(project, that.project)
                && Objects.equals(item, that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, client, project, item, hours);
    }

    @Override
    public String toString() {
        return date + ";" + client + ";" + project + ";" + item + ";" + hours;
    }
}
