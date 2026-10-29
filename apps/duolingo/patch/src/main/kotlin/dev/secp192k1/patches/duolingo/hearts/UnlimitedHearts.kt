package dev.secp192k1.patches.duolingo.hearts

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

private const val PERSISTED_STATE = "Lcom/duolingo/data/session/state/PersistedState;"

// Duolingo leaves its session state models unobfuscated, so the class is found by name
val persistedState = klass("com.duolingo.data.session.state.PersistedState")

// Legendary levels and Jump here tests count hearts down in this field, and it stays null in lessons without hearts
// toString reads the field right after its label
val mistakesRemaining = persistedState.method("toString").point("mistakesRemaining") {
    string(", mistakesRemaining=")
}.next { field { } }.field("mistakesRemaining")

// Grading builds the state after every answer through this generated copy method, and its fourth parameter is the new count
val persistedStateCopy = method("persistedStateCopy") {
    inClass(persistedState)
    hasParam(PERSISTED_STATE)
    returns(PERSISTED_STATE)
}

@Suppress("unused")
val unlimitedHearts = patch("Unlimited hearts") {
    description("Keeps your hearts full in Legendary levels and Jump here tests, so mistakes never end them.")
    // Pinned because a wrong match would overwrite a value without any error
    compatibleWith(DUOLINGO_PINNED)
    // Applied by default so its switch always shows in Settings, where it can be turned off
    settings(duolingoSettings, section(DuolingoStrings.sectionDeveloper, DeveloperSettings.unlimitedHearts))

    execute {
        gate(DeveloperSettings.unlimitedHearts) {
            persistedStateCopy.before {
                val previousCount = param(0).field(mistakesRemaining)
                val newCount = param(3)
                // The first count a lesson gets still goes through, and every later copy keeps it, so a mistake never lowers it
                whenNotNull(previousCount) {
                    whenNotNull(newCount) {
                        newCount.assign(previousCount)
                    }
                }
            }
        }
    }
}
