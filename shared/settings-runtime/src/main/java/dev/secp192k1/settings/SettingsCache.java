// SPDX-FileCopyrightText: 2026 secp192k1
// SPDX-License-Identifier: AGPL-3.0-or-later

package dev.secp192k1.settings;

import android.content.SharedPreferences;

import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public final class SettingsCache {
    // Patched code reads each setting once per process; the settings screen offers a restart after a change.
    private static final ConcurrentHashMap<String, Object> values = new ConcurrentHashMap<>();

    private SettingsCache() {
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        Object cached = values.get(key);
        if (cached != null) return (Boolean) cached;
        SharedPreferences preferences = ReseamSettings.prefs();
        // Before ReseamSettings.init the stored value is unknown, so the default is not kept.
        if (preferences == null) return defaultValue;
        boolean value = preferences.getBoolean(key, defaultValue);
        values.put(key, value);
        return value;
    }

    public static String getString(String key, String defaultValue) {
        Object cached = values.get(key);
        if (cached != null) return (String) cached;
        SharedPreferences preferences = ReseamSettings.prefs();
        if (preferences == null) return defaultValue;
        String value = preferences.getString(key, defaultValue);
        if (value == null) return defaultValue;
        values.put(key, value);
        return value;
    }
}
