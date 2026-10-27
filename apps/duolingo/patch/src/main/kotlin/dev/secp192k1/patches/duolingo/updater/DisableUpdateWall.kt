package dev.secp192k1.patches.duolingo.updater

import app.reseam.patch.fieldOfType
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import dev.secp192k1.duolingo.settings.DuolingoStrings
import dev.secp192k1.duolingo.settings.PromptSettings
import dev.secp192k1.patches.duolingo.DUOLINGO_PINNED
import dev.secp192k1.patches.duolingo.settings.duolingoSettings

// This server config is a data class, so its generated toString keeps the field names
val updateWall = klass("updateWall") {
    strings("UpdateWall(updateMessage=")
}

val updateWallCreate = updateWall.method("<init>")

// The launch screen blocks the app while this version code is newer than the installed one
val minVersionCode = updateWall.fieldOfType("Ljava/lang/Integer;")

// The prompt that offers a newer version is the field toString prints first
val updateMessage = updateWall.method("toString").point("updateMessage") {
    string("UpdateWall(updateMessage=")
}.next { field { } }.field("updateMessage")

@Suppress("unused")
val disableUpdateWall = patch("Disable update wall") {
    description("Removes the screens that ask or force you to update the app.")
    // Pinned because a wrong match would clear two fields without any error
    compatibleWith(DUOLINGO_PINNED)
    settings(duolingoSettings, section(DuolingoStrings.sectionPrompts, PromptSettings.disableUpdateWall))

    execute {
        gate(PromptSettings.disableUpdateWall) {
            updateWallCreate.after {
                thisObject.set(minVersionCode, nullObject)
                thisObject.set(updateMessage, nullObject)
            }
        }
    }
}
