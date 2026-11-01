package com.example.aplicacion_financiero.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class PrefsManager {

    private static final String PREFS_NAME = "finanzas_pro_prefs";
    private static final String KEY_CURRENCY = "currency_code";
    private static final String KEY_THEME = "theme_mode";
    private static final String KEY_FIRST_DAY = "first_day_of_period";
    private static final String KEY_WELCOME = "welcome_shown";

    public static final String DEFAULT_CURRENCY = "PEN";
    public static final int DEFAULT_FIRST_DAY = 1;

    private PrefsManager() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static String getCurrencyCode(Context context) {
        return prefs(context).getString(KEY_CURRENCY, DEFAULT_CURRENCY);
    }

    public static void setCurrencyCode(Context context, String code) {
        prefs(context).edit().putString(KEY_CURRENCY, code).apply();
    }

    public static int getThemeMode(Context context) {
        return prefs(context).getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public static void setThemeMode(Context context, int mode) {
        prefs(context).edit().putInt(KEY_THEME, mode).apply();
    }

    public static int getFirstDayOfPeriod(Context context) {
        return prefs(context).getInt(KEY_FIRST_DAY, DEFAULT_FIRST_DAY);
    }

    public static void setFirstDayOfPeriod(Context context, int day) {
        prefs(context).edit().putInt(KEY_FIRST_DAY, day).apply();
    }

    public static boolean isWelcomeShown(Context context) {
        return prefs(context).getBoolean(KEY_WELCOME, false);
    }

    public static void setWelcomeShown(Context context) {
        prefs(context).edit().putBoolean(KEY_WELCOME, true).apply();
    }
}
