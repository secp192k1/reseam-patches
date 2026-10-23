package dev.secp192k1.trainstatus.settings

import app.reseam.patch.settings.Choice
import app.reseam.patch.settings.SettingsGroup
import app.reseam.patch.settings.choice
import app.reseam.patch.settings.toggle
import dev.secp192k1.patches.brand.lucide

const val START_TAB_SEARCH = "search"
const val START_TAB_DEPARTURES = "departures"

const val TRACKING_INTERVAL_TWO = "2"
const val TRACKING_INTERVAL_FIVE = "5"
const val TRACKING_INTERVAL_TEN = "10"

object AdsSettings : SettingsGroup(TrainStatusStrings) {
    val hideAds by toggle(default = true, key = "trainstatus.hide_ads", icon = lucide("megaphone-off"))
}

object PrivacySettings : SettingsGroup(TrainStatusStrings) {
    val blockTrackers by toggle(default = true, key = "trainstatus.block_trackers", icon = lucide("shield"))
}

object AlertSettings : SettingsGroup(TrainStatusStrings) {
    val disableUpdatePrompt by toggle(default = true, key = "trainstatus.disable_update_prompt", icon = lucide("refresh-cw-off"))
    val hideNotices by toggle(default = false, key = "trainstatus.hide_notices", icon = lucide("bell-off"))
}

object StartSettings : SettingsGroup(TrainStatusStrings) {
    val startTab by choice(
        default = START_TAB_SEARCH,
        choices = listOf(
            Choice(START_TAB_SEARCH, TrainStatusStrings.startTabSearch),
            Choice(START_TAB_DEPARTURES, TrainStatusStrings.startTabDepartures),
        ),
        key = "trainstatus.start_tab",
        icon = lucide("layout-list"),
    )
}

object TrackingSettings : SettingsGroup(TrainStatusStrings) {
    val trackingInterval by choice(
        default = TRACKING_INTERVAL_TWO,
        choices = listOf(
            Choice(TRACKING_INTERVAL_TWO, TrainStatusStrings.trackingIntervalTwo),
            Choice(TRACKING_INTERVAL_FIVE, TrainStatusStrings.trackingIntervalFive),
            Choice(TRACKING_INTERVAL_TEN, TrainStatusStrings.trackingIntervalTen),
        ),
        key = "trainstatus.tracking_interval",
        icon = lucide("clock"),
    )
}

object AppearanceSettings : SettingsGroup(TrainStatusStrings) {
    // The theme extension compares the same values on the device
    val appTheme by choice(
        default = "system",
        choices = listOf(
            Choice("system", TrainStatusStrings.appThemeSystem),
            Choice("light", TrainStatusStrings.appThemeLight),
            Choice("dark", TrainStatusStrings.appThemeDark),
        ),
        key = "trainstatus.app_theme",
        icon = lucide("palette"),
    )
}
