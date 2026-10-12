package dev.secp192k1.patches.twitch

import app.reseam.patch.before
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.whenOn

@Suppress("unused")
val hideStories = patch("Hide stories") {
    description("Removes the stories shelf and the stories button from Home.")
    compatibleWith(TWITCH)
    settings(twitchSettings, section(homePage, "Stories", TwitchSettings.hideStories))

    execute {
        // The Home screen state; with stories it shows the stories shelf, or the stories button while the shelf is closed
        klass("homeScreenState") { strings("State(hasStoriesShelf=") }
            .method("<init>").before { whenOn(TwitchSettings.hideStories) { param(0).assign(bool(false)) } }
    }
}
