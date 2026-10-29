package dev.secp192k1.patches.duolingo.trackers

import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import dev.secp192k1.duolingo.settings.DuolingoStrings
import dev.secp192k1.duolingo.settings.PrivacySettings
import dev.secp192k1.patches.duolingo.DUOLINGO
import dev.secp192k1.patches.duolingo.settings.duolingoSettings

// R8 keeps the names of these public SDK classes, and of the methods SentryAndroidOptions inherits from its renamed superclass
val adjust = klass("com.adjust.sdk.AdjustInstance")
val sentryAndroidOptions = klass("io.sentry.android.core.SentryAndroidOptions")

val adjustStart = adjust.method("initSdk")

// Sentry skips its whole setup, crash reports and session replays included, when its options report it disabled
val sentryEnabled = sentryAndroidOptions.method("isEnabled", inherited = true) { paramCount(0) }

@Suppress("unused")
val blockTrackers = patch("Block trackers") {
    description("Turns off Adjust, and Sentry with its crash reports and session replays.")
    compatibleWith(DUOLINGO)
    settings(duolingoSettings, section(DuolingoStrings.sectionPrivacy, PrivacySettings.blockTrackers))

    execute {
        gate(PrivacySettings.blockTrackers) {
            adjustStart.alwaysReturn()
            sentryEnabled.alwaysReturn(false)
        }
    }
}
