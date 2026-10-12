package dev.secp192k1.patches.twitch

import app.reseam.patch.CodeScope
import app.reseam.patch.after
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.whenOn

private const val CONTENT_CLASSIFICATION = "tv.twitch.android.models.contentclassification.ContentClassification"

// Age, region and guardian gates keep their screen, and so do the viewer's own limits while Skip my content limits is off
private fun CodeScope.whenOnlyWarned(block: CodeScope.() -> Unit) = whenOn(TwitchSettings.skipContentWarnings) {
    whenFalse(thisObject.callVirtual(CONTENT_CLASSIFICATION, "isContentAgeGated", "()Z")) {
        whenFalse(thisObject.callVirtual(CONTENT_CLASSIFICATION, "isContentGuardianLimited", "()Z")) {
            whenFalse(thisObject.callVirtual(CONTENT_CLASSIFICATION, "isContentUserLimited", "()Z"), block)
        }
    }
}

@Suppress("unused")
val skipContentWarnings = patch("Skip content warnings") {
    description("Removes the Just one second screen and the audience notice.")
    compatibleWith(TWITCH)
    settings(twitchSettings, section(playbackPage, "Content warnings", TwitchSettings.skipContentWarnings, TwitchSettings.hideAudienceNotice, TwitchSettings.skipContentLimits))

    execute {
        val contentClassification = klass(CONTENT_CLASSIFICATION)

        contentClassification.method("isContentSignPosted").after { whenOnlyWarned { returnFalse() } }
        // The labels a Start Watching tap acknowledges; with none, playback waits for no tap
        contentClassification.method("getContentLabelIdsToSignPost").after {
            whenOnlyWarned { returnValue(callStatic("java.util.Collections", "emptyList", "()Ljava/util/List;")) }
        }
        // Decides the Intended for certain audiences notice under the player and on clips
        contentClassification.method("isContentMature").after { whenOn(TwitchSettings.hideAudienceNotice) { returnFalse() } }
        // The limits the viewer set in Twitch's content preferences, which ask for Watch Anyway
        contentClassification.method("isContentUserLimited").after { whenOn(TwitchSettings.skipContentLimits) { returnFalse() } }
    }
}
