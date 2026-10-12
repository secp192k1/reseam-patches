package dev.secp192k1.twitch.exactpoints;

import java.text.NumberFormat;

public final class ExactPoints {
    private ExactPoints() {}

    public static boolean enabled() {
        return false;
    }

    public static String compact(long points, boolean longStyle) {
        return null;
    }

    // The class loader reads the setting on first use, so later updates take no lock for it.
    private static final class Preference {
        static final boolean ENABLED = enabled();
    }

    // Twitch prints the same numbers in full in the rewards list.
    public static String format(long points, boolean longStyle) {
        if (!Preference.ENABLED) return compact(points, longStyle);
        return NumberFormat.getInstance().format(points);
    }
}
