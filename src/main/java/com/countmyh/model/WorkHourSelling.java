package com.countmyh.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public class WorkHourSelling {

    private int year;
    private double hoursSold;
    private double vacationDaysSold;
    private String note;

    public WorkHourSelling() {
    }

    public WorkHourSelling(int year, double hoursSold, double vacationDaysSold, String note) {
        this.year = year;
        this.hoursSold = hoursSold;
        this.vacationDaysSold = vacationDaysSold;
        this.note = note;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getHoursSold() {
        return hoursSold;
    }

    public void setHoursSold(double hoursSold) {
        this.hoursSold = hoursSold;
    }

    public double getVacationDaysSold() {
        return vacationDaysSold;
    }

    public void setVacationDaysSold(double vacationDaysSold) {
        this.vacationDaysSold = vacationDaysSold;
    }

    @JsonIgnore
    public double getVacationHoursSold() {
        return vacationDaysSold * 8;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkHourSelling that = (WorkHourSelling) o;
        return year == that.year
                && Double.compare(that.hoursSold, hoursSold) == 0
                && Double.compare(that.vacationDaysSold, vacationDaysSold) == 0
                && Objects.equals(note, that.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, hoursSold, vacationDaysSold, note);
    }

    @Override
    public String toString() {
        return year + ": " + hoursSold + "h sold" +
                (vacationDaysSold > 0 ? ", " + vacationDaysSold + " vacation days" : "") +
                (note != null ? " (" + note + ")" : "");
    }
}
