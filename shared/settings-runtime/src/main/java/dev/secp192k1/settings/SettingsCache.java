// SPDX-FileCopyrightText: 2026 secp192k1
// SPDX-License-Identifier: AGPL-3.0-or-later

package dev.secp192k1.settings;

import app.reseam.runtime.Reseam;
import app.reseam.runtime.Settings;

import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public final class SettingsCache {
    // The name in manifest.toml, under which the engine's app runtime keeps this bundle's settings.
    static final String BUNDLE = "koi-patches";

    // Patched code reads each setting once per process; the settings screen offers a restart after a change.
    private static final ConcurrentHashMap<String, Object> values = new ConcurrentHashMap<>();

    private SettingsCache() {
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        Object cached = values.get(key);
        if (cached != null) return (Boolean) cached;
        // Before Reseam.init the stored value is unknown, so the default is not kept.
        if (!Reseam.isStarted()) return defaultValue;
        boolean value = settings().getBoolean(key, defaultValue);
        values.put(key, value);
        return value;
    }

    public static String getString(String key, String defaultValue) {
        Object cached = values.get(key);
        if (cached != null) return (String) cached;
        if (!Reseam.isStarted()) return defaultValue;
        String value = settings().getString(key, defaultValue);
        if (value == null) return defaultValue;
        values.put(key, value);
        return value;
    }

    // A stored value the choice no longer lists reads as its default.
    public static String getChoice(String key, String defaultValue) {
        Object cached = values.get(key);
        if (cached != null) return (String) cached;
        if (!Reseam.isStarted()) return defaultValue;
        String value = settings().getChoice(key);
        values.put(key, value);
        return value;
    }

    private static Settings settings() {
        return Reseam.bundle(BUNDLE).settings();
    }
}
