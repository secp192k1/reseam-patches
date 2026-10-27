package dev.secp192k1.twitch.settings

import app.reseam.patch.settings.SettingsGroup
import app.reseam.patch.settings.text
import app.reseam.patch.settings.toggle
import dev.secp192k1.patches.brand.lucide

object AdsSettings : SettingsGroup(TwitchStrings) {
    val blockVideoAds by toggle(default = true, key = "twitch.block_video_ads", icon = lucide("megaphone-off"))
    val blockEmbeddedAds by toggle(default = true, key = "twitch.block_embedded_ads", icon = lucide("monitor-off"))
    val embeddedAdsProxy by text(default = "https://eu.luminous.dev", key = "twitch.embedded_ads_proxy", icon = lucide("server"))
    val hideSponsoredChannelSkins by toggle(default = true, key = "twitch.hide_sponsored_channel_skins", icon = lucide("eye-off"))
    val ignoreAdEvents by toggle(default = true, key = "twitch.ignore_ad_events", icon = lucide("bell-off"))
}

object PrivacySettings : SettingsGroup(TwitchStrings) {
    val blockTrackers by toggle(default = true, key = "twitch.block_trackers", icon = lucide("shield"))
}

object UpdateSettings : SettingsGroup(TwitchStrings) {
    val disableUpdatePrompt by toggle(default = true, key = "twitch.disable_update_prompt", icon = lucide("refresh-cw-off"))
}

object DeveloperSettings : SettingsGroup(TwitchStrings) {
    val debugMode by toggle(default = true, key = "twitch.debug_mode", icon = lucide("bug"))
}
