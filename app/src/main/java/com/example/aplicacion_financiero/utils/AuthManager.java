package com.example.aplicacion_financiero.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public final class AuthManager {

    private static final String PREFS_NAME = "finanzas_pro_auth";
    private static final String KEY_ACCOUNTS = "accounts_json";
    private static final String KEY_ACTIVE_EMAIL = "active_email";
    private static final String KEY_LOGGED_IN = "session_logged_in";
    private static final String KEY_SCHEMA = "accounts_schema_version";

    private static final String LEGACY_KEY_EMAIL = "account_email";
    private static final String LEGACY_KEY_DNI_HASH = "account_dni_hash";
    private static final String LEGACY_KEY_SALT = "account_salt";

    public static final String LEGACY_DATABASE = "finance_database";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern DNI_PATTERN = Pattern.compile("^\\d{8}$");
    private static final String RECOVERY_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int RECOVERY_GROUPS = 3;
    private static final int RECOVERY_GROUP_LENGTH = 4;

    private AuthManager() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static final class Account {
        String email;
        final String salt;
        String hash;
        final String database;
        String recoveryHash;

        Account(String email, String salt, String hash, String database, String recoveryHash) {
            this.email = email;
            this.salt = salt;
            this.hash = hash;
            this.database = database;
            this.recoveryHash = recoveryHash;
        }

        JSONObject toJson() {
            JSONObject object = new JSONObject();
            try {
                object.put("email", email);
                object.put("salt", salt);
                object.put("hash", hash);
                object.put("database", database);
                if (recoveryHash != null && !recoveryHash.isEmpty()) {
                    object.put("recovery_hash", recoveryHash);
                }
            } catch (Exception ignored) {
            }
            return object;
        }

        static Account fromJson(JSONObject object) {
            String email = object.optString("email", "");
            String salt = object.optString("salt", "");
            String hash = object.optString("hash", "");
            String database = object.optString("database", LEGACY_DATABASE);
            String recoveryHash = object.optString("recovery_hash", "");
            if (email.isEmpty() || salt.isEmpty() || hash.isEmpty()) {
                return null;
            }
            if (database.isEmpty()) {
                database = LEGACY_DATABASE;
            }
            if (recoveryHash.isEmpty()) {
                recoveryHash = null;
            }
            return new Account(email, salt, hash, database, recoveryHash);
        }
    }

    private static List<Account> loadAccounts(Context context) {
        SharedPreferences preferences = prefs(context);
        String raw = preferences.getString(KEY_ACCOUNTS, null);
        if (raw == null) {
            List<Account> migrated = new ArrayList<>();
            if (preferences.getInt(KEY_SCHEMA, 0) < 1) {
                migrateLegacyAccount(preferences, migrated);
                preferences.edit().putInt(KEY_SCHEMA, 1).apply();
            }
            saveAccounts(preferences, migrated);
            return migrated;
        }

        List<Account> accounts = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                if (object == null) {
                    continue;
                }
                Account account = Account.fromJson(object);
                if (account != null) {
                    accounts.add(account);
                }
            }
        } catch (Exception ignored) {
        }
        return accounts;
    }

    private static void migrateLegacyAccount(SharedPreferences preferences,
                                             List<Account> accounts) {
        String email = preferences.getString(LEGACY_KEY_EMAIL, null);
        String hash = preferences.getString(LEGACY_KEY_DNI_HASH, null);
        String salt = preferences.getString(LEGACY_KEY_SALT, null);

        if (email != null && !email.isEmpty() && hash != null && !hash.isEmpty()
                && salt != null && !salt.isEmpty()) {
            String normalizedEmail = email.trim().toLowerCase(Locale.getDefault());
            accounts.add(new Account(normalizedEmail, salt, hash, LEGACY_DATABASE, null));
            preferences.edit().putString(KEY_ACTIVE_EMAIL, normalizedEmail).apply();
        }

        preferences.edit()
                .remove(LEGACY_KEY_EMAIL)
                .remove(LEGACY_KEY_DNI_HASH)
                .remove(LEGACY_KEY_SALT)
                .apply();
    }

    private static void saveAccounts(SharedPreferences preferences, List<Account> accounts) {
        JSONArray array = new JSONArray();
        for (Account account : accounts) {
            array.put(account.toJson());
        }
        preferences.edit().putString(KEY_ACCOUNTS, array.toString()).apply();
    }

    public static boolean hasAccount(Context context) {
        return !loadAccounts(context).isEmpty();
    }

    public static boolean isRegistered(Context context, String email) {
        if (email == null) {
            return false;
        }
        return findAccount(loadAccounts(context), email) != null;
    }

    public static String getEmail(Context context) {
        return prefs(context).getString(KEY_ACTIVE_EMAIL, "");
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidDni(String dni) {
        return dni != null && DNI_PATTERN.matcher(dni.trim()).matches();
    }

    public static String register(Context context, String email, String dni) {
        if (!isValidEmail(email) || !isValidDni(dni)) {
            return null;
        }
        List<Account> accounts = loadAccounts(context);
        String normalizedEmail = normalizeEmail(email);
        if (findAccount(accounts, normalizedEmail) != null) {
            return null;
        }

        String salt = newSalt();
        String hash = hashDni(dni.trim(), salt);
        String recoveryCode = newRecoveryCode();
        String recoveryHash = hashRecovery(recoveryCode, salt);
        String database = accounts.isEmpty()
                ? LEGACY_DATABASE
                : "finance_database_" + UUID.randomUUID().toString()
                        .replace("-", "").substring(0, 16);

        accounts.add(new Account(normalizedEmail, salt, hash, database, recoveryHash));
        saveAccounts(prefs(context), accounts);
        return recoveryCode;
    }

    public static boolean login(Context context, String email, String dni) {
        if (!isValidEmail(email) || dni == null) {
            return false;
        }
        SharedPreferences preferences = prefs(context);
        List<Account> accounts = loadAccounts(context);
        Account account = findAccount(accounts, email);
        if (account == null) {
            return false;
        }
        String candidate = hashDni(dni.trim(), account.salt);
        if (!constantTimeEquals(account.hash, candidate)) {
            return false;
        }
        preferences.edit()
                .putString(KEY_ACTIVE_EMAIL, account.email)
                .putBoolean(KEY_LOGGED_IN, true)
                .apply();
        return true;
    }

    public static void logout(Context context) {
        prefs(context).edit().putBoolean(KEY_LOGGED_IN, false).apply();
    }

    public static boolean isLoggedIn(Context context) {
        return hasAccount(context) && prefs(context).getBoolean(KEY_LOGGED_IN, false)
                && findAccount(loadAccounts(context), getEmail(context)) != null;
    }

    public static String getUserDatabaseName(Context context) {
        List<Account> accounts = loadAccounts(context);
        Account account = findAccount(accounts, getEmail(context));
        if (account != null && account.database != null && !account.database.isEmpty()) {
            return account.database;
        }
        return LEGACY_DATABASE;
    }

    public static boolean changeDni(Context context, String currentDni, String newDni) {
        if (!isValidDni(newDni) || currentDni == null || !isValidDni(currentDni)) {
            return false;
        }
        List<Account> accounts = loadAccounts(context);
        Account account = findAccount(accounts, getEmail(context));
        if (account == null) {
            return false;
        }
        if (!constantTimeEquals(account.hash, hashDni(currentDni.trim(), account.salt))) {
            return false;
        }
        account.hash = hashDni(newDni.trim(), account.salt);
        saveAccounts(prefs(context), accounts);
        return true;
    }

    public static boolean changeEmail(Context context, String dni, String newEmail) {
        if (!isValidEmail(newEmail) || dni == null || !isValidDni(dni)) {
            return false;
        }
        List<Account> accounts = loadAccounts(context);
        Account account = findAccount(accounts, getEmail(context));
        if (account == null) {
            return false;
        }
        if (!constantTimeEquals(account.hash, hashDni(dni.trim(), account.salt))) {
            return false;
        }
        String normalizedEmail = normalizeEmail(newEmail);
        Account existing = findAccount(accounts, normalizedEmail);
        if (existing != null && existing != account) {
            return false;
        }

        account.email = normalizedEmail;
        saveAccounts(prefs(context), accounts);
        prefs(context).edit().putString(KEY_ACTIVE_EMAIL, normalizedEmail).apply();
        return true;
    }

    public static boolean hasRecoveryCode(Context context, String email) {
        Account account = findAccount(loadAccounts(context), email);
        return account != null && account.recoveryHash != null
                && !account.recoveryHash.isEmpty();
    }

    public static boolean hasRecoveryCode(Context context) {
        return hasRecoveryCode(context, getEmail(context));
    }

    public static boolean isRecoveryCodeValid(Context context, String email, String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        Account account = findAccount(loadAccounts(context), email);
        if (account == null || account.recoveryHash == null || account.recoveryHash.isEmpty()) {
            return false;
        }
        return constantTimeEquals(account.recoveryHash, hashRecovery(code, account.salt));
    }

    public static String generateRecoveryCode(Context context) {
        List<Account> accounts = loadAccounts(context);
        Account account = findAccount(accounts, getEmail(context));
        if (account == null) {
            return null;
        }
        String code = newRecoveryCode();
        account.recoveryHash = hashRecovery(code, account.salt);
        saveAccounts(prefs(context), accounts);
        return code;
    }

    public static boolean resetDni(Context context, String email, String recoveryCode,
                                   String newDni) {
        if (!isValidEmail(email) || !isValidDni(newDni) || recoveryCode == null
                || recoveryCode.trim().isEmpty()) {
            return false;
        }
        List<Account> accounts = loadAccounts(context);
        Account account = findAccount(accounts, email);
        if (account == null || account.recoveryHash == null || account.recoveryHash.isEmpty()) {
            return false;
        }
        if (!constantTimeEquals(account.recoveryHash, hashRecovery(recoveryCode, account.salt))) {
            return false;
        }
        account.hash = hashDni(newDni.trim(), account.salt);
        saveAccounts(prefs(context), accounts);
        prefs(context).edit().putBoolean(KEY_LOGGED_IN, false).apply();
        return true;
    }

    private static Account findAccount(List<Account> accounts, String email) {
        if (email == null) {
            return null;
        }
        String normalizedEmail = normalizeEmail(email);
        for (Account account : accounts) {
            if (account.email.equals(normalizedEmail)) {
                return account;
            }
        }
        return null;
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.getDefault());
    }

    private static String newSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    private static String newRecoveryCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder();
        for (int group = 0; group < RECOVERY_GROUPS; group++) {
            if (group > 0) {
                builder.append('-');
            }
            for (int i = 0; i < RECOVERY_GROUP_LENGTH; i++) {
                builder.append(RECOVERY_ALPHABET.charAt(
                        random.nextInt(RECOVERY_ALPHABET.length())));
            }
        }
        return builder.toString();
    }

    private static String normalizeRecoveryCode(String code) {
        if (code == null) {
            return "";
        }
        return code.replace("-", "").replace(" ", "").trim().toUpperCase(Locale.ROOT);
    }

    private static String hashDni(String dni, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] result = digest.digest((salt + ":" + dni).getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(result, Base64.NO_WRAP);
        } catch (Exception e) {
            return "";
        }
    }

    private static String hashRecovery(String code, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] result = digest.digest(("recovery:" + salt + ":"
                    + normalizeRecoveryCode(code)).getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(result, Base64.NO_WRAP);
        } catch (Exception e) {
            return "";
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
