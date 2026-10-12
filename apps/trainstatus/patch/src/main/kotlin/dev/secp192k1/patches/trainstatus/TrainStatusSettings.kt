package dev.secp192k1.patches.trainstatus

import app.reseam.patch.before
import app.reseam.patch.method
import app.reseam.patch.settings.Choice
import app.reseam.patch.settings.choice
import app.reseam.patch.settings.openSettings
import app.reseam.patch.settings.settingsHost
import app.reseam.patch.settings.toggle
import dev.secp192k1.patches.settings.SETTINGS_TITLE

internal object TrainStatusSettings {
    val hideAds by toggle(
        "Hide ads",
        summary = "Removes the ad banners from the search, train and station screens.",
        default = true,
        key = "trainstatus.hide_ads",
    )
    val blockTrackers by toggle(
        "Block trackers",
        summary = "Disables Firebase Analytics and Crashlytics.",
        default = true,
        key = "trainstatus.block_trackers",
    )
    val disableUpdatePrompt by toggle(
        "Disable update prompt",
        summary = "Stops the dialog that offers a newer version from Google Play on every start.",
        default = true,
        key = "trainstatus.disable_update_prompt",
    )
    val hideNotices by toggle(
        "Hide notices",
        summary = "Removes the notice banner the developer posts on the main screen.",
        default = false,
        key = "trainstatus.hide_notices",
    )
    val startTab by choice(
        "Start tab",
        summary = "The screen to open when the app starts.",
        default = "search",
        choices = listOf(
            Choice("search", "Search train, like Train Status PT"),
            Choice("departures", "Arrivals and departures"),
        ),
        key = "trainstatus.start_tab",
    )
    val trackingInterval by choice(
        "Tracking interval",
        summary = "How often the notification of a followed train checks for news.",
        default = "2",
        choices = listOf(
            Choice("2", "Every 2 minutes, like Train Status PT"),
            Choice("5", "Every 5 minutes"),
            Choice("10", "Every 10 minutes"),
        ),
        key = "trainstatus.tracking_interval",
    )
    val appTheme by choice(
        "App theme",
        default = "system",
        choices = listOf(
            Choice("system", "Follow system"),
            Choice("light", "Light"),
            Choice("dark", "Dark"),
        ),
        key = "trainstatus.app_theme",
    )
}

val trainStatusSettings = settingsHost("trainstatus") {
    compatibleWith(PACKAGE)

    install {
        val settingsItem = resources.addId("reseam_settings_item") ?: error("Could not add id/reseam_settings_item")
        val appSettingsItem = resources.id("id", "definicoes") ?: error("The app has no id/definicoes")

        // A copy of the overflow menu's last item, Definições, that opens Reseam Settings
        resources.editXml("menu", "example_menu") {
            val appSettings = findByAttribute("android:id", "@0x%08x".format(appSettingsItem.toInt())).single()
            val item = appSettings.clone(true)
            appSettings.parent?.appendChild(item) ?: error("The Definições item has no parent")
            item.setResourceRef("android:id", settingsItem)
            item["android:title"] = SETTINGS_TITLE
        }

        mainActivity.method("onOptionsItemSelected").before {
            whenEqual(param(0).callInterface("android.view.MenuItem", "getItemId", "()I"), int(settingsItem.toInt())) {
                openSettings()
                returnTrue()
            }
        }
    }
}
