package dev.secp192k1.patches.anydesk.microphone

import app.reseam.patch.patch
import app.reseam.patch.settings.section
import dev.secp192k1.anydesk.settings.AnyDeskStrings
import dev.secp192k1.anydesk.settings.DeclutterSettings
import dev.secp192k1.patches.anydesk.ANYDESK
import dev.secp192k1.patches.anydesk.settings.anyDeskSettings
import dev.secp192k1.patches.anydesk.shared.keepGoneById

@Suppress("unused")
val hideMicrophoneButton = patch("Hide microphone button") {
    description("Removes the microphone button from the address fields.")
    compatibleWith(ANYDESK)
    settings(anyDeskSettings, section(AnyDeskStrings.sectionDeclutter, DeclutterSettings.hideMicrophoneButton))

    execute {
        keepGoneById("ad_edit_text_speak_button", DeclutterSettings.hideMicrophoneButton)
    }
}
