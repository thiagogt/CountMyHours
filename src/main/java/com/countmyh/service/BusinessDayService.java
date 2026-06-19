package com.countmyh.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
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
        Map<LocalDate, String> holidays = new LinkedHashMap<>();

        holidays.put(LocalDate.of(year, 1, 1), "Ano Novo");
        holidays.put(LocalDate.of(year, 4, 21), "Tiradentes");
        holidays.put(LocalDate.of(year, 5, 1), "Dia do Trabalho");
        holidays.put(LocalDate.of(year, 7, 9), "Rev. Constitucionalista");
        holidays.put(LocalDate.of(year, 9, 7), "Independência");
        holidays.put(LocalDate.of(year, 10, 12), "N.S. Aparecida");
        holidays.put(LocalDate.of(year, 11, 2), "Finados");
        holidays.put(LocalDate.of(year, 11, 15), "Proclamação da República");
        holidays.put(LocalDate.of(year, 11, 20), "Consciência Negra");
        holidays.put(LocalDate.of(year, 12, 25), "Natal");

        LocalDate easter = calculateEaster(year);
        holidays.put(easter.minusDays(48), "Carnaval");
        holidays.put(easter.minusDays(47), "Carnaval");
        holidays.put(easter.minusDays(2), "Sexta-feira Santa");
        holidays.put(easter.plusDays(60), "Corpus Christi");

        return holidays;
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

    /**
     * Anonymous Gregorian algorithm for Easter date calculation.
     */
    LocalDate calculateEaster(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }
}
