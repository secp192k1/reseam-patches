package dev.secp192k1.patches.trainstatus

import app.reseam.patch.Type
import app.reseam.patch.before
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.whenOn

@Suppress("unused")
val blockTrackers = patch("Block trackers") {
    description("Disable Firebase Analytics and Crashlytics.")
    compatibleWith(PACKAGE)
    settings(trainStatusSettings, section("Privacy", TrainStatusSettings.blockTrackers))

    execute {
        method("analyticsCollectionState") {
            strings("firebase_analytics_collection_deactivated", "firebase_analytics_collection_enabled")
            returns(Type.Int)
        }.before { whenOn(TrainStatusSettings.blockTrackers) {
            // ANALYTICS_DEACTIVATED = 1
            returnValue(int(1)) }
        }

        // Crashlytics and Firebase Sessions keep their reports on the device while the data collection switch reads off
        method("crashlyticsCollectionEnabled") {
            calls(method("crashlyticsCollectionLog") { strings("Crashlytics automatic data collection ") })
            returns(Type.Boolean)
            paramCount(0)
        }.before { whenOn(TrainStatusSettings.blockTrackers) { returnFalse() } }
    }
}
