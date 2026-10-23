package dev.secp192k1.patches.trainstatus.theme

import app.reseam.patch.after
import app.reseam.patch.before
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.section
import app.reseam.patch.settings.valueOf
import dev.secp192k1.patches.trainstatus.TRAINSTATUS
import dev.secp192k1.patches.trainstatus.settings.trainStatusSettings
import dev.secp192k1.patches.trainstatus.shared.mainActivityCreate
import dev.secp192k1.trainstatus.settings.AppearanceSettings
import dev.secp192k1.trainstatus.settings.TrainStatusStrings
import dev.secp192k1.trainstatus.theme.bindings.AppTheme

// The Application class and the activities are manifest entry points, so R8 keeps their names
val appCreate = klass("com.jsepol.trainstatuspt.App").method("onCreate")
val trainDetailsCreate = klass("com.jsepol.trainstatuspt.TrainDetailsActivity").method("onCreate")
val departuresCreate = klass("com.jsepol.trainstatuspt.partidasechegadas").method("onCreate")

@Suppress("unused")
val overrideTheme = patch("Override theme") {
    description("Changes the app theme to light or dark on Android 12 and later.")
    compatibleWith(TRAINSTATUS)
    settings(trainStatusSettings, section(TrainStatusStrings.sectionAppearance, AppearanceSettings.appTheme))

    execute {
        AppTheme.theme.implement { returnValue(valueOf(AppearanceSettings.appTheme)) }
        appCreate.before { AppTheme.applyTheme(thisObject) }

        // Links and the widgets open the train and station screens directly, so any of these can be the first screen
        listOf(mainActivityCreate, trainDetailsCreate, departuresCreate).forEach { screen ->
            screen.after { AppTheme.refresh(thisObject) }
        }
    }
}
