package org.hugoandrade.calendarviewlib.helpers;

import java.util.Calendar;
import java.util.Date;

public class YMDCalendar {

    public final int day;
    public final int month;
    public final int year;

    public YMDCalendar(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public YMDCalendar(Calendar calendar) {
        this(calendar == null? - 1: calendar.get(Calendar.DAY_OF_MONTH),
                calendar == null? - 1: calendar.get(Calendar.MONTH),
                calendar == null? - 1: calendar.get(Calendar.YEAR));
    }

    @Override
    public YMDCalendar clone() {
        try {
            return (YMDCalendar) super.clone();
        } catch (CloneNotSupportedException e) {
            return new YMDCalendar(day, month, year);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof YMDCalendar) {
            YMDCalendar c = (YMDCalendar) o;
            return day == c.day && month == c.month && year == c.year;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return day + month * 100 + year * 10000;
    }

    public static Date toDate(YMDCalendar ymdCalendar) {
        return toCalendar(ymdCalendar).getTime();
    }

    public static Calendar toCalendar(YMDCalendar ymdCalendar) {
        Calendar c = Calendar.getInstance();
        c.set(ymdCalendar.year, ymdCalendar.month, ymdCalendar.day, 12, 0, 0);
        return c;
    }

    public boolean isAfter(YMDCalendar ymdCalendar) {
        return hashCode() > ymdCalendar.hashCode();
    }

    public boolean isBefore(YMDCalendar ymdCalendar) {
        return hashCode() < ymdCalendar.hashCode();
    }
}
