package dev.secp192k1.patches.twitch

import app.reseam.patch.Type
import app.reseam.patch.before
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.whenOn

@Suppress("unused")
val hideHypeTrain = patch("Hide Hype Train") {
    description("Removes the Hype Train banner above chat.")
    compatibleWith(TWITCH)
    settings(twitchSettings, section(chatPage, "Hype Train", TwitchSettings.hideHypeTrain))

    execute {
        // Adds, expands and removes the Hype Train banner above chat, in the classic chat and One Chat alike
        method("hypeTrainBanner") {
            strings("hypetrain")
            returns(Type.Void)
            paramCount(1)
        }.before { whenOn(TwitchSettings.hideHypeTrain) { returnVoid() } }
    }
}
