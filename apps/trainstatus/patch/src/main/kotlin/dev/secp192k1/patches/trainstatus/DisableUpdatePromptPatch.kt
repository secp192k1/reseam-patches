package dev.secp192k1.patches.trainstatus

import app.reseam.patch.before
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.whenOn

@Suppress("unused")
val disableUpdatePrompt = patch("Disable update prompt") {
    description("Stops the dialog that offers a newer version from Google Play.")
    compatibleWith(PACKAGE)
    settings(trainStatusSettings, section("Alerts", TrainStatusSettings.disableUpdatePrompt))

    execute {
        // Opens the dialog whenever the version in Firebase differs from this one, which for a patched app is every start once an update is out
        method("updateCheck") {
            inClass(klass("updateCheckListener") { strings("https://play.google.com/store/apps/details?id=$PACKAGE") })
            name("onDataChange")
        }.before { whenOn(TrainStatusSettings.disableUpdatePrompt) { returnVoid() } }
    }
}
