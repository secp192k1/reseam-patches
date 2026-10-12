package dev.secp192k1.patches.twitch

import app.reseam.patch.ExtClass
import app.reseam.patch.Type
import app.reseam.patch.before
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.valueOf

private object VideoQuality : ExtClass("dev.secp192k1.twitch.videoquality.VideoQuality") {
    val quality by static(returns = Type.String)
    val onStateChanged by static("com.amazonaws.ivs.player.Player", Type.Int)
}

@Suppress("unused")
val defaultVideoQuality = patch("Default video quality") {
    description("Starts streams, videos and clips at the chosen quality instead of Auto.")
    compatibleWith(TWITCH)
    settings(twitchSettings, section(playbackPage, "Quality", TwitchSettings.videoQuality))

    execute {
        VideoQuality.quality.implement { returnValue(valueOf(TwitchSettings.videoQuality)) }

        // Hands every player state change to the listeners, for the native and the React Native player alike
        method("playerStateDispatch") {
            inClass(klass("com.amazonaws.ivs.player.CorePlayerImpl"))
            params(Type.Int)
            calls {
                owner($$"com.amazonaws.ivs.player.Player$Listener")
                name("onStateChanged")
            }
        }.before { call(VideoQuality.onStateChanged, thisObject, param(0)) }
    }
}
