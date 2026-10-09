// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later
// SPDX-FileCopyrightText: 2026 secp192k1
// Modified by secp192k1 on 8 October 2026: moved to the package dev.secp192k1.settings,
// so this copy never collides with the official Reseam bundle's settings runtime.

package dev.secp192k1.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.util.Map;
import java.util.Objects;

/**
 * Offers a restart after the settings close with changed values, since most patches read their settings once at startup.
 */
final class RestartPrompt {
    private RestartPrompt() {
    }

    static void offer(Activity activity, Map<String, ?> opened) {
        if (activity.isFinishing() || opened.equals(Objects.requireNonNull(ReseamSettings.prefs()).getAll())) return;
        ReseamSettingsScreen.dialog(activity).setTitle("Restart to apply changes?").setMessage("Most settings take effect after the app restarts.").setPositiveButton("Restart", (dialog, which) -> restart(activity)).setNegativeButton("Later", null).show();
    }

    private static void restart(Context ctx) {
        Intent launch = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        assert launch != null;
        ctx.startActivity(Intent.makeRestartActivityTask(launch.getComponent()));
        Runtime.getRuntime().exit(0);
    }
}
