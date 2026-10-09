package dev.secp192k1.patches.anydesk

import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.points
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.forceTrue

private val telemetryConsent = method("telemetryConsent") {
    inClass(nativeBridge)
    calls { name("jniGetTelemetryConsent") }
}

@Suppress("unused")
val hideTelemetryPrompt = patch("Hide telemetry prompt") {
    description("Removes the main screen tile that asks to share usage data.")
    compatibleWith(PACKAGE)
    settings(anyDeskSettings, section("Declutter", AnyDeskSettings.hideTelemetryPrompt))

    execute {
        method("mainScreenTiles") {
            inClass(mainFragment)
            calls(telemetryConsent)
        }.points("telemetry consent") { calls(telemetryConsent) }.forEach {
            forceTrue(AnyDeskSettings.hideTelemetryPrompt)
        }
    }
}
