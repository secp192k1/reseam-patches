package dev.secp192k1.patches.duolingo.rating

import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import dev.secp192k1.duolingo.settings.DuolingoStrings
import dev.secp192k1.duolingo.settings.PromptSettings
import dev.secp192k1.patches.duolingo.DUOLINGO
import dev.secp192k1.patches.duolingo.DUOLINGO_PINNED
import dev.secp192k1.patches.duolingo.settings.duolingoSettings

// Offers the rating screen after a lesson only when the Play Store can open the app's listing
val ratingPrompt = method("ratingPrompt") {
    strings("com.android.vending", DUOLINGO)
}

@Suppress("unused")
val hideRatePrompt = patch("Hide rate prompt") {
    description("Never asks you to rate the app after a lesson.")
    // Pinned because a wrong match would rewrite a return value without any error
    compatibleWith(DUOLINGO_PINNED)
    settings(duolingoSettings, section(DuolingoStrings.sectionPrompts, PromptSettings.hideRatePrompt))

    execute {
        gate(PromptSettings.hideRatePrompt) {
            ratingPrompt.alwaysReturnNull()
        }
    }
}
