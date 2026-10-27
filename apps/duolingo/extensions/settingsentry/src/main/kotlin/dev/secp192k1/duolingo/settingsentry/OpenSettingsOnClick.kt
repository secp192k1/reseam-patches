@file:Suppress("unused")

package dev.secp192k1.duolingo.settingsentry

import app.reseam.runtime.Export

// Duolingo's Compose buttons take the app's Function0 as their click handler, and they never read its result
@Export
class OpenSettingsOnClick : () -> Any? {
    override fun invoke(): Any? {
        openSettings()
        return null
    }
}
