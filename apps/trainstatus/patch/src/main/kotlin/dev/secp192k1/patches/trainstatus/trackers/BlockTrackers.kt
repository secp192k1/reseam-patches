package dev.secp192k1.patches.trainstatus.trackers

import app.reseam.patch.Type
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import dev.secp192k1.patches.trainstatus.TRAINSTATUS_PINNED
import dev.secp192k1.patches.trainstatus.settings.trainStatusSettings
import dev.secp192k1.trainstatus.settings.PrivacySettings
import dev.secp192k1.trainstatus.settings.TrainStatusStrings

// Firebase's value for ANALYTICS_DEACTIVATED
private const val ANALYTICS_DEACTIVATED = 1

val analyticsCollectionState = method("analyticsCollectionState") {
    strings("firebase_analytics_collection_deactivated", "firebase_analytics_collection_enabled")
    returns(Type.Int)
}

// The log line is built from this piece, and other Crashlytics releases format it whole
val crashlyticsCollectionLog = method("crashlyticsCollectionLog") {
    stringsStartingWith("Crashlytics automatic data collection ")
}

// Crashlytics and Firebase Sessions keep their reports on the device while the data collection switch reads off
val crashlyticsCollectionEnabled = method("crashlyticsCollectionEnabled") {
    calls(crashlyticsCollectionLog)
    returns(Type.Boolean)
    paramCount(0)
}

@Suppress("unused")
val blockTrackers = patch("Block trackers") {
    description("Disables Firebase Analytics and Crashlytics.")
    // Pinned because a wrong match would rewrite a return value without any error
    compatibleWith(TRAINSTATUS_PINNED)
    settings(trainStatusSettings, section(TrainStatusStrings.sectionPrivacy, PrivacySettings.blockTrackers))

    execute {
        gate(PrivacySettings.blockTrackers) {
            analyticsCollectionState.alwaysReturn(ANALYTICS_DEACTIVATED)
            crashlyticsCollectionEnabled.alwaysReturn(false)
        }
    }
}
