package com.countmyh.service.calendar;

import java.time.LocalDate;
import java.util.Map;

public interface HolidayCalendar {
    Map<LocalDate, String> getNamedHolidays(int year);
}
