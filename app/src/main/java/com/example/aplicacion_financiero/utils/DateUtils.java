package com.example.aplicacion_financiero.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {

    public static final long DAY_MILLIS = 24L * 60L * 60L * 1000L;

    private static final Locale SPANISH_LOCALE = Locale.forLanguageTag("es-ES");

    private DateUtils() {
    }

    private static SimpleDateFormat dateFormat() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    private static SimpleDateFormat monthYearFormat() {
        return new SimpleDateFormat("MMMM yyyy", SPANISH_LOCALE);
    }

    private static SimpleDateFormat dayMonthFormat() {
        return new SimpleDateFormat("dd MMM", SPANISH_LOCALE);
    }

    private static SimpleDateFormat shortDateFormat() {
        return new SimpleDateFormat("dd MMM yyyy", SPANISH_LOCALE);
    }

    private static SimpleDateFormat monthOnlyFormat() {
        return new SimpleDateFormat("MMM", SPANISH_LOCALE);
    }

    public static String formatDate(long timestamp) {
        return dateFormat().format(new Date(timestamp));
    }

    public static String formatShortDate(long timestamp) {
        return shortDateFormat().format(new Date(timestamp));
    }

    public static String formatMonthYear(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        String value = monthYearFormat().format(calendar.getTime());
        return capitalize(value);
    }

    public static String formatDayMonth(long timestamp) {
        return dayMonthFormat().format(new Date(timestamp));
    }

    public static String formatMonthName(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month - 1);
        return capitalize(monthOnlyFormat().format(calendar.getTime()));
    }

    public static String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    public static long getStartOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getEndOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1, 23, 59, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return calendar.getTimeInMillis();
    }

    public static long getPeriodStart(int year, int month, int firstDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, Math.min(firstDay, 28), 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getPeriodEnd(int year, int month, int firstDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, Math.min(firstDay, 28), 0, 0, 0);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.SECOND, -1);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    public static long getPeriodStartForDate(long timestamp, int firstDay) {
        int day = Math.max(1, Math.min(firstDay, 28));
        Calendar reference = Calendar.getInstance();
        reference.setTimeInMillis(timestamp);

        Calendar start = Calendar.getInstance();
        start.set(reference.get(Calendar.YEAR), reference.get(Calendar.MONTH), day, 0, 0, 0);
        start.set(Calendar.MILLISECOND, 0);
        if (reference.getTimeInMillis() < start.getTimeInMillis()) {
            start.add(Calendar.MONTH, -1);
        }
        return start.getTimeInMillis();
    }

    public static long getPeriodEndForDate(long timestamp, int firstDay) {
        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(getPeriodStartForDate(timestamp, firstDay));
        end.add(Calendar.MONTH, 1);
        end.add(Calendar.SECOND, -1);
        end.set(Calendar.MILLISECOND, 999);
        return end.getTimeInMillis();
    }

    public static long getStartOfWeek(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getEndOfWeek(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getStartOfWeek(timestamp));
        calendar.add(Calendar.DAY_OF_MONTH, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    public static long getStartOfYear(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getEndOfYear(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, Calendar.DECEMBER, 31, 23, 59, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    public static int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static int getCurrentMonth() {
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }

    public static int getYear(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar.get(Calendar.YEAR);
    }

    public static int getMonth(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar.get(Calendar.MONTH) + 1;
    }

    public static long addMonths(long timestamp, int months) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTimeInMillis();
    }

    public static long addDays(long timestamp, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTimeInMillis();
    }

    public static String formatRange(long start, long end) {
        return formatShortDate(start) + " - " + formatShortDate(end);
    }
}
