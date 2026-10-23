package dev.secp192k1.patches.anydesk.news

import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import dev.secp192k1.anydesk.settings.AnyDeskStrings
import dev.secp192k1.anydesk.settings.DeclutterSettings
import dev.secp192k1.patches.anydesk.ANYDESK
import dev.secp192k1.patches.anydesk.settings.anyDeskSettings
import dev.secp192k1.patches.anydesk.shared.nativeBridge

// Native code delivers each news tile through one of these callbacks
val onMotdText = nativeBridge.method("onMotdText")
val onMotdImage = nativeBridge.method("onMotdImage")
val onMotdCombined = nativeBridge.method("onMotdCombined")

@Suppress("unused")
val hideNewsTiles = patch("Hide news tiles") {
    description("Removes the news tiles from the main screen.")
    compatibleWith(ANYDESK)
    settings(anyDeskSettings, section(AnyDeskStrings.sectionDeclutter, DeclutterSettings.hideNewsTiles))

    execute {
        gate(DeclutterSettings.hideNewsTiles) {
            onMotdText.alwaysReturn()
            onMotdImage.alwaysReturn()
            onMotdCombined.alwaysReturn()
        }
    }
}
