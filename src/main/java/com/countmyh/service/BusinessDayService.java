package com.countmyh.service;

import com.countmyh.service.calendar.HolidayCalendarFactory;
import com.countmyh.util.I18n;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BusinessDayService {

    private static final double HOURS_PER_DAY = 8.0;

    public int getBusinessDays(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        Set<LocalDate> holidays = getHolidays(year);

        int count = 0;
        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            LocalDate date = LocalDate.of(year, month, day);
            DayOfWeek dow = date.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY && !holidays.contains(date)) {
                count++;
            }
        }
        return count;
    }

    public double getExpectedHours(int year, int month) {
        return getBusinessDays(year, month) * HOURS_PER_DAY;
    }

    public Set<LocalDate> getHolidays(int year) {
        return getNamedHolidays(year).keySet();
    }

    public Map<LocalDate, String> getNamedHolidays(int year) {
        return HolidayCalendarFactory
                .forLocale(I18n.getLocale().toLanguageTag())
                .getNamedHolidays(year);
    }

    public List<Map.Entry<LocalDate, String>> getHolidaysInMonth(int year, int month) {
        return getNamedHolidays(year).entrySet().stream()
                .filter(e -> e.getKey().getMonthValue() == month)
                .filter(e -> {
                    DayOfWeek dow = e.getKey().getDayOfWeek();
                    return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
                })
                .sorted(Map.Entry.comparingByKey())
                .toList();
    }
}
