@file:JvmName("AppTheme")

package dev.secp192k1.trainstatus.theme

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import app.reseam.runtime.Export

private var checkFirstScreen = false
private var startNight = 0

// The patch replaces this body with a read of the App theme setting
@Export
fun theme(): String = "system"

// Android 12 and later keep a night mode for each app; earlier versions only follow the system
@Suppress("unused")
@Export
fun applyTheme(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

    val uiModeManager = context.getSystemService(UiModeManager::class.java) ?: return
    val nightMode = when (theme()) {
        "dark" -> UiModeManager.MODE_NIGHT_YES
        "light" -> UiModeManager.MODE_NIGHT_NO
        else -> UiModeManager.MODE_NIGHT_AUTO
    }
    startNight = night(context)
    checkFirstScreen = true
    uiModeManager.setApplicationNightMode(nightMode)
}

// The first screen after a change gets its window before the new mode arrives, so it is drawn again once
@Suppress("unused")
@Export
fun refresh(activity: Activity) {
    if (!checkFirstScreen) return

    checkFirstScreen = false
    if (night(activity.applicationContext) != startNight) activity.recreate()
}

private fun night(context: Context): Int = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
