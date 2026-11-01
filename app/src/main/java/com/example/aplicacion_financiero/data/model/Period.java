package com.example.aplicacion_financiero.data.model;

public class Period {

    private final int year;
    private final int month;

    public Period(int year, int month) {
        this.year = year;
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public Period previous() {
        if (month == 1) {
            return new Period(year - 1, 12);
        }
        return new Period(year, month - 1);
    }

    public Period next() {
        if (month == 12) {
            return new Period(year + 1, 1);
        }
        return new Period(year, month + 1);
    }
}
