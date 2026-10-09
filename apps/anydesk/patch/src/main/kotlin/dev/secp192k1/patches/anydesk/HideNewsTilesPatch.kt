package dev.secp192k1.patches.anydesk

import app.reseam.patch.before
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.whenOn

private val newsTileCallbacks = listOf("onMotdText", "onMotdImage", "onMotdCombined")

@Suppress("unused")
val hideNewsTiles = patch("Hide news tiles") {
    description("Removes the news tiles from the main screen.")
    compatibleWith(PACKAGE)
    settings(anyDeskSettings, section("Declutter", AnyDeskSettings.hideNewsTiles))

    execute {
        newsTileCallbacks.forEach { name ->
            nativeBridge.method(name).before {
                whenOn(AnyDeskSettings.hideNewsTiles) { returnVoid() }
            }
        }
    }
}
