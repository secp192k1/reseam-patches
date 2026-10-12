package dev.secp192k1.patches.twitch

import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.invoke
import app.reseam.patch.method
import app.reseam.patch.methods
import app.reseam.patch.points
import app.reseam.patch.settings.ToggleSetting
import dev.secp192k1.patches.settings.whenOn

internal val TWITCH = "tv.twitch.android.app"("31.6.0_BETA")

// The account check that hides Twitch's internal tools from everyone but staff
internal val isStaff = method("isStaff") {
    strings("user_type", "staff")
    returns(Type.Boolean)
    paramCount(0)
}

// Points every URL on the server at an address that refuses connections while the setting is on, and counts the URLs
internal fun cutOffServer(server: String, setting: ToggleSetting): Int {
    val urls = methods(server) { stringsStartingWith(server) }.points(server) { stringContains(server) }.all
    urls.forEach { url ->
        url.captureAs("url").after {
            whenOn(setting) {
                val cutOff = capture("url").callVirtual(
                    Type.String,
                    "replace",
                    "(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;",
                    string(server),
                    // Nothing listens on the discard port, so a request sent here fails at once
                    string("https://127.0.0.1:9/"),
                )
                capture("url").assign(cutOff)
            }
        }
    }
    return urls.size
}
