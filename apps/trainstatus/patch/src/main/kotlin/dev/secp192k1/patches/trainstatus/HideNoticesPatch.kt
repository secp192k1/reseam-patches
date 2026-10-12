package dev.secp192k1.patches.trainstatus

import app.reseam.patch.before
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.whenOn

@Suppress("unused")
val hideNotices = patch("Hide notices") {
    description("Removes the notice banner the developer posts on the main screen.")
    compatibleWith(PACKAGE)
    settings(trainStatusSettings, section("Alerts", TrainStatusSettings.hideNotices))

    execute {
        // MainActivity starts with the banner hidden, and only this listener shows it once the notice arrives from Firebase
        method("noticeListener") { strings("mensagem", "open") }
            .before { whenOn(TrainStatusSettings.hideNotices) { returnVoid() } }
    }
}
