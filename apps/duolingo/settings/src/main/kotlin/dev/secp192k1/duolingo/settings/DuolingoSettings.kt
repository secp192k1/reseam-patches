package dev.secp192k1.duolingo.settings

import app.reseam.patch.settings.SettingsGroup
import app.reseam.patch.settings.text
import app.reseam.patch.settings.toggle
import dev.secp192k1.patches.brand.lucide

object AdsSettings : SettingsGroup(DuolingoStrings) {
    val hideAds by toggle(default = true, key = "duolingo.hide_ads", icon = lucide("megaphone-off"))
}

object PrivacySettings : SettingsGroup(DuolingoStrings) {
    val blockTrackers by toggle(default = true, key = "duolingo.block_trackers", icon = lucide("shield"))
}

object PromptSettings : SettingsGroup(DuolingoStrings) {
    val hideRatePrompt by toggle(default = true, key = "duolingo.hide_rate_prompt", icon = lucide("eye-off"))
    val disableUpdateWall by toggle(default = true, key = "duolingo.disable_update_wall", icon = lucide("refresh-cw-off"))
}

object AppearanceSettings : SettingsGroup(DuolingoStrings) {
    // The patch itself starts off, so the toggle only shows for users who chose to keep the icon
    val keepAppIcon by toggle(default = true, key = "duolingo.keep_app_icon", icon = lucide("palette"))
}

object DeveloperSettings : SettingsGroup(DuolingoStrings) {
    val enableDebugMenu by toggle(default = true, key = "duolingo.enable_debug_menu", icon = lucide("bug"))
    val unlimitedEnergy by toggle(default = true, key = "duolingo.unlimited_energy", icon = lucide("zap"))
}
