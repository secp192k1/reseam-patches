package dev.secp192k1.twitch.settings

import app.reseam.patch.settings.SettingsGroup
import app.reseam.patch.settings.toggle
import dev.secp192k1.patches.brand.lucide

object PrivacySettings : SettingsGroup(TwitchStrings) {
    val blockTrackers by toggle(default = true, key = "twitch.block_trackers", icon = lucide("shield"))
}

object DeveloperSettings : SettingsGroup(TwitchStrings) {
    // The debug menu can override experiments, so it stays hidden until the user turns this on
    val debugMode by toggle(default = false, key = "twitch.debug_mode", icon = lucide("bug"))
}
