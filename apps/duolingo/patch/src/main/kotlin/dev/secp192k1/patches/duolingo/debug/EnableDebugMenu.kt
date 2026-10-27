package dev.secp192k1.patches.duolingo.debug

import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import dev.secp192k1.duolingo.settings.DeveloperSettings
import dev.secp192k1.duolingo.settings.DuolingoStrings
import dev.secp192k1.patches.duolingo.DUOLINGO_PINNED
import dev.secp192k1.patches.duolingo.settings.duolingoSettings

// This state of the buttons at the bottom of Settings is a data class, so its generated toString keeps the field names
val bottomButtonsState = klass("bottomButtonsState") {
    strings("BottomButtonsSection(showDebugMenu=")
}

val bottomButtonsStateCreate = bottomButtonsState.method("<init>")

// Duolingo shows its Debug options button, which opens the debug menu, only to staff
val showDebugMenu = bottomButtonsState.method("toString").point("showDebugMenu") {
    string("BottomButtonsSection(showDebugMenu=")
}.next { field { } }.field("showDebugMenu")

val debugCategory = klass("debugCategory") {
    enumConstants("POP_QUIZ_SESSION", "JWT_LOGIN")
}

// Duolingo's R8 rules keep this getter's name, and outside debug builds the menu lists only the categories it allows
val allowOnReleaseBuilds = debugCategory.method("getAllowOnReleaseBuilds")

@Suppress("unused")
val enableDebugMenu = patch("Enable debug menu") {
    description("Adds the Debug options button to the bottom of Settings and lists every category in Duolingo's staff debug menu.")
    // Pinned because a wrong match would set a field without any error
    compatibleWith(DUOLINGO_PINNED)
    settings(duolingoSettings, section(DuolingoStrings.sectionDeveloper, DeveloperSettings.enableDebugMenu))

    execute {
        gate(DeveloperSettings.enableDebugMenu) {
            bottomButtonsStateCreate.after {
                thisObject.set(showDebugMenu, bool(true))
            }
            allowOnReleaseBuilds.alwaysReturn(true)
        }
    }
}
