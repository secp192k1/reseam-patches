package dev.secp192k1.patches.duolingo.appicon

import app.reseam.patch.Type
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import dev.secp192k1.duolingo.settings.AppearanceSettings
import dev.secp192k1.duolingo.settings.DuolingoStrings
import dev.secp192k1.patches.duolingo.DUOLINGO_PINNED
import dev.secp192k1.patches.duolingo.settings.duolingoSettings

private const val INELIGIBLE_DEVICE_LOG = "Attempted to set app display on ineligible device: appDisplay="

val appIconSwitch = method("appIconSwitch") {
    strings(INELIGIBLE_DEVICE_LOG)
}

// The device check right before this log decides whether the launcher icon may change
val deviceEligible = appIconSwitch.point("deviceEligible") {
    string(INELIGIBLE_DEVICE_LOG)
}.previous { resultOf(Type.Boolean) }

@Suppress("unused")
val keepAppIcon = patch("Keep app icon") {
    description("Keeps the regular launcher icon instead of the streak and seasonal icons Duolingo swaps in.")
    // Pinned because a wrong match would overwrite a check without any error
    compatibleWith(DUOLINGO_PINNED)
    enabledByDefault(false)
    settings(duolingoSettings, section(DuolingoStrings.sectionAppearance, AppearanceSettings.keepAppIcon))

    execute {
        gate(AppearanceSettings.keepAppIcon) {
            deviceEligible.captureAs("eligible").after {
                capture("eligible").assign(bool(false))
            }
        }
    }
}
