package dev.secp192k1.patches.duolingo.settings

import app.reseam.patch.Type
import app.reseam.patch.before
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
private const val SETTINGS_BUTTON_TAG = "reseamSettingsButton"

// The Compose function that draws the buttons at the bottom of Settings, found by the Sign out test tag
val bottomButtonsSection = method("bottomButtonsSection") {
    strings("signOutButton")
}

// Sign out shows for every account, so the entry is drawn right before it with the same button
val signOutButton = bottomButtonsSection.point("signOutButton") {
    string("signOutButton")
}.next { invokeStatic { hasParam(FUNCTION0) } }

val bottomButton = signOutButton.callee("bottomButton")

val duolingoSettings = settingsHost("duolingo") {
    compatibleWith(DUOLINGO)
    brand(reseamBrand)
    about(publisherLinks)

    install {
        SettingsEntry.openSettings.implement {
            openSettings()
            returnVoid()
        }

        // The button takes its label before its test tag, and its click handler is its only Function0 parameter
        val parameters = bottomButton.parameterTypes
        val label = parameters.indexOf(Type.String)
        val testTag = parameters.lastIndexOf(Type.String)
        val onClick = parameters.indexOf(FUNCTION0)
        require(label >= 0 && label != testTag) { "The bottom button no longer takes a label and a test tag" }
        require(onClick == parameters.lastIndexOf(FUNCTION0)) { "The bottom button no longer takes a single click handler" }

        val signOutCall = parameters.foldIndexed(signOutButton) { index, capturedCall, type ->
            capturedCall.captureArgumentAs("argument$index", index, type)
        }

        // The entry reuses the composer and layout arguments of Sign out
        signOutCall.before {
            val arguments = parameters.indices.map { index ->
                when (index) {
                    label -> string(SETTINGS_TITLE)
                    testTag -> string(SETTINGS_BUTTON_TAG)
                    onClick -> SettingsEntry.openSettingsOnClick()
                    else -> capture("argument$index")
                }
            }
            call(bottomButton, *arguments.toTypedArray())
        }
    }
}
