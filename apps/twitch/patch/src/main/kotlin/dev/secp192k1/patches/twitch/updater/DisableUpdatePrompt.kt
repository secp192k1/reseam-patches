package dev.secp192k1.patches.twitch.updater

import app.reseam.patch.Type
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import dev.secp192k1.patches.twitch.TWITCH
import dev.secp192k1.patches.twitch.settings.twitchSettings
import dev.secp192k1.twitch.settings.TwitchStrings
import dev.secp192k1.twitch.settings.UpdateSettings

// Google Play's UpdateAvailability.UPDATE_NOT_AVAILABLE
private const val UPDATE_NOT_AVAILABLE = 1

// Reads Google Play's answer to whether an update is available, which decides Twitch's update prompt
val appUpdateInfoReceiver = method("appUpdateInfoReceiver") {
    strings("update.availability", "nonblocking.intent")
}

val updateAvailability = appUpdateInfoReceiver.point("updateAvailability") {
    string("update.availability")
}.next { resultOf(Type.Int) }

@Suppress("unused")
val disableUpdatePrompt = patch("Disable update prompt") {
    description("Stops the prompt that offers a newer version from Google Play.")
    compatibleWith(TWITCH)
    settings(twitchSettings, section(TwitchStrings.sectionUpdates, UpdateSettings.disableUpdatePrompt))

    execute {
        gate(UpdateSettings.disableUpdatePrompt) {
            updateAvailability.captureAs("availability").after {
                capture("availability").assign(int(UPDATE_NOT_AVAILABLE))
            }
        }
    }
}
