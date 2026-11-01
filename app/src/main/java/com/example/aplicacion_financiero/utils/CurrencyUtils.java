package com.example.aplicacion_financiero.utils;

import android.content.Context;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyUtils {

    public static final String[] SUPPORTED_CODES = {"PEN", "USD", "EUR", "MXN", "COP", "ARS", "CLP"};

    private static final Locale LOCALE = Locale.forLanguageTag("es-PE");

    private CurrencyUtils() {
    }

    public static String getCurrencySymbol(Context context) {
        String code = context == null ? PrefsManager.DEFAULT_CURRENCY : PrefsManager.getCurrencyCode(context);
        switch (code) {
            case "USD":
                return "$";
            case "EUR":
                return "\u20AC";
            case "MXN":
                return "MX$";
            case "COP":
                return "COL$";
            case "ARS":
                return "AR$";
            case "CLP":
                return "CL$";
            case "PEN":
            default:
                return "S/";
        }
    }

    public static String getCurrencyName(Context context) {
        String code = context == null ? PrefsManager.DEFAULT_CURRENCY : PrefsManager.getCurrencyCode(context);
        switch (code) {
            case "USD":
                return "D\u00F3lar estadounidense";
            case "EUR":
                return "Euro";
            case "MXN":
                return "Peso mexicano";
            case "COP":
                return "Peso colombiano";
            case "ARS":
                return "Peso argentino";
            case "CLP":
                return "Peso chileno";
            case "PEN":
            default:
                return "Sol peruano";
        }
    }

    public static String formatCurrency(Context context, long amountInCents) {
        String symbol = getCurrencySymbol(context);
        boolean negative = amountInCents < 0;
        String value = formatAmount(Math.abs(amountInCents));
        return (negative ? "-" : "") + symbol + " " + value;
    }

    public static String formatCurrency(long amountInCents) {
        return formatCurrency(null, amountInCents);
    }

    public static String formatAmount(long amountInCents) {
        BigDecimal value = BigDecimal.valueOf(amountInCents, 2);
        NumberFormat formatter = NumberFormat.getNumberInstance(LOCALE);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(value);
    }

    public static String formatSignedCurrency(Context context, long amountInCents, boolean income) {
        String prefix = income ? "+ " : "- ";
        return prefix + formatCurrency(context, Math.abs(amountInCents));
    }

    public static long toCents(String amountString) {
        if (amountString == null) {
            return 0L;
        }
        String clean = amountString.trim().replaceAll("[^0-9,.-]", "");
        if (clean.isEmpty() || "-".equals(clean) || ".".equals(clean) || ",".equals(clean)) {
            return 0L;
        }

        int lastComma = clean.lastIndexOf(',');
        int lastDot = clean.lastIndexOf('.');

        if (lastComma >= 0 && lastDot >= 0) {
            if (lastComma > lastDot) {
                clean = clean.replace(".", "").replace(',', '.');
            } else {
                clean = clean.replace(",", "");
            }
        } else if (lastComma >= 0) {
            int decimals = clean.length() - lastComma - 1;
            clean = decimals <= 2 ? clean.replace(',', '.') : clean.replace(",", "");
        } else if (lastDot >= 0) {
            int decimals = clean.length() - lastDot - 1;
            if (decimals == 3 && clean.lastIndexOf('.') != clean.indexOf('.')) {
                clean = clean.replace(".", "");
            } else if (decimals == 3) {
                clean = clean.replace(".", "");
            }
        }

        try {
            BigDecimal value = new BigDecimal(clean);
            return value.movePointRight(2)
                    .setScale(0, java.math.RoundingMode.HALF_UP)
                    .longValueExact();
        } catch (Exception e) {
            return 0L;
        }
    }
}
