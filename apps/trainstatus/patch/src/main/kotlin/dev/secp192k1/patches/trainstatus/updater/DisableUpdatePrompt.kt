package dev.secp192k1.patches.trainstatus.updater

import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import dev.secp192k1.patches.trainstatus.TRAINSTATUS
import dev.secp192k1.patches.trainstatus.settings.trainStatusSettings
import dev.secp192k1.trainstatus.settings.AlertSettings
import dev.secp192k1.trainstatus.settings.TrainStatusStrings

val updateCheckListener = klass("updateCheckListener") {
    strings("https://play.google.com/store/apps/details?id=com.jsepol.trainstatuspt")
}

// Opens the dialog whenever the version in Firebase differs from this one, which for a patched app is every start once an update is out
val updateCheck = method("updateCheck") {
    inClass(updateCheckListener)
    name("onDataChange")
}

@Suppress("unused")
val disableUpdatePrompt = patch("Disable update prompt") {
    description("Stops the dialog that offers a newer version from Google Play.")
    compatibleWith(TRAINSTATUS)
    settings(trainStatusSettings, section(TrainStatusStrings.sectionAlerts, AlertSettings.disableUpdatePrompt))

    execute {
        gate(AlertSettings.disableUpdatePrompt) {
            updateCheck.alwaysReturn()
        }
    }
}
