package dev.secp192k1.patches.duolingo.ads

import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import dev.secp192k1.duolingo.settings.AdsSettings
import dev.secp192k1.duolingo.settings.DuolingoStrings
import dev.secp192k1.patches.duolingo.DUOLINGO_PINNED
import dev.secp192k1.patches.duolingo.settings.duolingoSettings

// Picks the one ad or Super offer screen shown after a lesson, and its only caller skips a null result
val lessonEndAdScreen = method("lessonEndAdScreen") {
    strings("NETWORK_AD_BACKUP_CLIENT_FALLBACK", "NETWORK_AD_BACKUP_BACKEND_RANDOM_FALLBACK")
}

@Suppress("unused")
val hideAds = patch("Hide ads") {
    description("Removes the ads and Super offers shown after lessons.")
    // Pinned because a wrong match would rewrite a return value without any error
    compatibleWith(DUOLINGO_PINNED)
    settings(duolingoSettings, section(DuolingoStrings.sectionAds, AdsSettings.hideAds))

    execute {
        gate(AdsSettings.hideAds) {
            lessonEndAdScreen.alwaysReturnNull()
        }
    }
}
