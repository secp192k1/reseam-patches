package dev.secp192k1.patches.twitch

import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.whenOn

// Reads Google Play's answer to whether an update is available, which decides Twitch's update prompt
private val appUpdateInfoReceiver = method("appUpdateInfoReceiver") {
    strings("update.availability", "nonblocking.intent")
}

@Suppress("unused")
val disableUpdatePrompt = patch("Disable update prompt") {
    description("Stops offering Google Play updates.")
    compatibleWith(TWITCH)
    settings(twitchSettings, section(privacyPage, "Updates", TwitchSettings.disableUpdatePrompt))

    execute {
        appUpdateInfoReceiver.point("update availability") { string("update.availability") }
            .next { resultOf(Type.Int) }
            .captureAs("availability")
            .after { whenOn(TwitchSettings.disableUpdatePrompt) {
                // Google Play's update availability for an app without an update
                // UPDATE_NOT_AVAILABLE = 1
                capture("availability").assign(int(1)) }
            }
    }
}
