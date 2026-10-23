package dev.secp192k1.patches.trainstatus.notices

import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import dev.secp192k1.patches.trainstatus.TRAINSTATUS
import dev.secp192k1.patches.trainstatus.settings.trainStatusSettings
import dev.secp192k1.trainstatus.settings.AlertSettings
import dev.secp192k1.trainstatus.settings.TrainStatusStrings

// MainActivity starts with the banner hidden, and only this listener shows it once the notice arrives from Firebase
val noticeListener = method("noticeListener") {
    strings("mensagem", "open")
    name("onDataChange")
}

@Suppress("unused")
val hideNotices = patch("Hide notices") {
    description("Removes the notice banner the developer posts on the main screen.")
    compatibleWith(TRAINSTATUS)
    settings(trainStatusSettings, section(TrainStatusStrings.sectionAlerts, AlertSettings.hideNotices))

    execute {
        gate(AlertSettings.hideNotices) {
            noticeListener.alwaysReturn()
        }
    }
}
