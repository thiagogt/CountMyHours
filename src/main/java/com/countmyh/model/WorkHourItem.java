package com.countmyh.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.Objects;

public class WorkHourItem {

    private LocalDate date;
    private String client;
    private String project;
    private String item;
    private double hours;

    public WorkHourItem() {
    }

    public WorkHourItem(LocalDate date, String client, String project, String item, double hours) {
        this.date = date;
        this.client = client;
        this.project = project;
        this.item = item;
        this.hours = hours;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public double getHours() {
        return hours;
    }

    public void setHours(double hours) {
        this.hours = hours;
    }

    @JsonIgnore
    public int getYear() {
        return date.getYear();
    }

    @JsonIgnore
    public int getMonth() {
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
