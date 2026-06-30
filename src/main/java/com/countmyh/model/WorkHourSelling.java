package com.countmyh.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record WorkHourSelling(int year, int hoursSold, double vacationDaysSold, String note) {

    @JsonIgnore
    public double vacationHoursSold() {
        return vacationDaysSold * 8;
    }

    @Override
    public String toString() {
        return year + ": " + hoursSold + "h sold"
                + (vacationDaysSold > 0 ? ", " + vacationDaysSold + " vacation days" : "")
                + (note != null ? " (" + note + ")" : "");
    }
}
