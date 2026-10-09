package dev.secp192k1.patches.anydesk

import app.reseam.patch.patch
import app.reseam.patch.settings.section
import dev.secp192k1.patches.views.keepGoneById

@Suppress("unused")
val hideMicrophoneButton = patch("Hide microphone button") {
    description("Removes the microphone button from the address fields.")
    compatibleWith(PACKAGE)
    settings(anyDeskSettings, section("Declutter", AnyDeskSettings.hideMicrophoneButton))

    execute {
        keepGoneById("ad_edit_text_speak_button", AnyDeskSettings.hideMicrophoneButton)
    }
}
