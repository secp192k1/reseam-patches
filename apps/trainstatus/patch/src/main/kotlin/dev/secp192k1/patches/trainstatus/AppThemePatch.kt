package dev.secp192k1.patches.trainstatus

import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.section
import dev.secp192k1.patches.theme.installAppTheme

@Suppress("unused")
val appTheme = patch("Override theme") {
    description("Change app theme to light or dark.")
    compatibleWith(PACKAGE)
    settings(trainStatusSettings, section("Appearance", TrainStatusSettings.appTheme))

    execute {
        // Links and the widgets open the train and station screens directly
        val firstScreens = listOf("MainActivity", "TrainDetailsActivity", "partidasechegadas").map { klass("$PACKAGE.$it").method("onCreate") }
        installAppTheme(TrainStatusSettings.appTheme, klass("$PACKAGE.App").method("onCreate"), firstScreens)
    }
}
