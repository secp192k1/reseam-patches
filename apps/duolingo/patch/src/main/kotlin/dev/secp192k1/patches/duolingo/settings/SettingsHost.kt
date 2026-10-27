package dev.secp192k1.patches.duolingo.settings

import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.before
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.point
import app.reseam.patch.settings.openSettings
import app.reseam.patch.settings.settingsHost
import dev.secp192k1.duolingo.settingsentry.bindings.SettingsEntry
import dev.secp192k1.patches.brand.SETTINGS_TITLE
import dev.secp192k1.patches.brand.publisherLinks
import dev.secp192k1.patches.brand.reseamBrand
import dev.secp192k1.patches.duolingo.DUOLINGO

private const val FUNCTION0 = "Lkotlin/jvm/functions/Function0;"

// This state of the buttons at the bottom of Settings is a data class, so its generated toString keeps the field names
val bottomButtonsState = klass("bottomButtonsState") {
    strings("BottomButtonsSection(showDebugMenu=")
}

val bottomButtonsStateCreate = bottomButtonsState.method("<init>")

// Duolingo shows its Debug options button above Sign out only to staff
val showDebugMenu = bottomButtonsState.method("toString").point("showDebugMenu") {
    string("BottomButtonsSection(showDebugMenu=")
}.next { field { } }.field("showDebugMenu")

// The Compose function that draws those buttons, found by their test tags
val bottomButtonsSection = method("bottomButtonsSection") {
    strings("debugOptionsButton", "signOutButton")
}

val debugOptionsButton = bottomButtonsSection.point("debugOptionsButton") {
    string("debugOptionsButton")
}.next { invokeStatic { hasParam(FUNCTION0) } }

val settingsButton = debugOptionsButton.callee("settingsButton")

val duolingoSettings = settingsHost("duolingo") {
    compatibleWith(DUOLINGO)
    brand(reseamBrand)
    about(publisherLinks)

    install {
        SettingsEntry.openSettings.implement {
            openSettings()
            returnVoid()
        }

        // The Debug options button becomes the entry, so it shows for everyone and opens Reseam Settings
        bottomButtonsStateCreate.after {
            thisObject.set(showDebugMenu, bool(true))
        }

        // The button takes its label before its test tag, and its click handler is its only function
        val buttonParameters = settingsButton.parameterTypes
        debugOptionsButton
            .captureArgumentAs("label", buttonParameters.indexOf(Type.String), Type.String)
            .captureArgumentAs("onClick", buttonParameters.indexOf(FUNCTION0), FUNCTION0)
            .before {
                capture("label").assign(string(SETTINGS_TITLE))
                capture("onClick").assign(SettingsEntry.openSettingsOnClick())
            }
    }
}
