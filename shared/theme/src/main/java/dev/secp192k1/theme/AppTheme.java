package dev.secp192k1.theme;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

@SuppressWarnings("unused")
public final class AppTheme {
    private static boolean checkFirstScreen;
    private static int startNight;

    private AppTheme() {}

    public static String theme() {
        return "system";
    }

    // Android 12 and later keep a night mode for each app; earlier versions only follow the system.
    public static void apply(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        UiModeManager uiModeManager = context.getSystemService(UiModeManager.class);
        if (uiModeManager == null) return;
        String theme = theme();
        int nightMode = UiModeManager.MODE_NIGHT_AUTO;
        if ("dark".equals(theme)) {
            nightMode = UiModeManager.MODE_NIGHT_YES;
        } else if ("light".equals(theme)) {
            nightMode = UiModeManager.MODE_NIGHT_NO;
        }
        startNight = night(context);
        checkFirstScreen = true;
        uiModeManager.setApplicationNightMode(nightMode);
    }

    // The first screen after a change gets its window before the new mode arrives, so it is drawn again once.
    public static void refresh(Activity activity) {
        if (!checkFirstScreen) return;
        checkFirstScreen = false;
        if (night(activity.getApplicationContext()) != startNight) activity.recreate();
    }

    private static int night(Context context) {
        return context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    }
}
