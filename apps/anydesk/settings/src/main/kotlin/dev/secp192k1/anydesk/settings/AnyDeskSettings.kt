package dev.secp192k1.anydesk.settings

import app.reseam.patch.settings.SettingsGroup
import app.reseam.patch.settings.toggle
import dev.secp192k1.patches.brand.lucide

object DeclutterSettings : SettingsGroup(AnyDeskStrings) {
    val hideLicenseBanner by toggle(default = true, key = "anydesk.hide_license_banner", icon = lucide("eye-off"))
    val hideMicrophoneButton by toggle(default = true, key = "anydesk.hide_microphone_button", icon = lucide("eye-off"))
    // AnyDesk may post notices as news tiles too, so they stay visible until the user turns this on
    val hideNewsTiles by toggle(default = false, key = "anydesk.hide_news_tiles", icon = lucide("megaphone-off"))
    val hideTelemetryPrompt by toggle(default = true, key = "anydesk.hide_telemetry_prompt", icon = lucide("chart-no-axes-column"))
    val hideLicenseTile by toggle(default = true, key = "anydesk.hide_license_tile", icon = lucide("ticket"))
}
