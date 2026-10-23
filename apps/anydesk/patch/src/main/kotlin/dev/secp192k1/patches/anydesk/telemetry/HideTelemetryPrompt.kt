package dev.secp192k1.patches.anydesk.telemetry

import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.points
import app.reseam.patch.settings.section
import app.reseam.patch.settings.whenEnabled
import dev.secp192k1.anydesk.settings.AnyDeskStrings
import dev.secp192k1.anydesk.settings.DeclutterSettings
import dev.secp192k1.patches.anydesk.ANYDESK
import dev.secp192k1.patches.anydesk.settings.anyDeskSettings
import dev.secp192k1.patches.anydesk.shared.mainFragment
import dev.secp192k1.patches.anydesk.shared.nativeBridge

val telemetryConsent = method("telemetryConsent") {
    inClass(nativeBridge)
    calls { name("jniGetTelemetryConsent") }
    returns(Type.Boolean)
}

val mainScreenTiles = method("mainScreenTiles") {
    inClass(mainFragment)
    calls(telemetryConsent)
}

val telemetryConsentReads = mainScreenTiles.points("telemetryConsentReads") {
    calls(telemetryConsent)
}

@Suppress("unused")
val hideTelemetryPrompt = patch("Hide telemetry prompt") {
    description("Removes the main screen tile that asks to share usage data.")
    // Pinned because a wrong match would force a consent value to true without any error
    compatibleWith(ANYDESK)
    settings(anyDeskSettings, section(AnyDeskStrings.sectionDeclutter, DeclutterSettings.hideTelemetryPrompt))

    execute {
        // Only the main screen reads the consent as given, so the telemetry setting keeps its real value
        telemetryConsentReads.all.forEach { read ->
            read.next { resultOf(Type.Boolean) }.captureAs("consent").after {
                whenEnabled(DeclutterSettings.hideTelemetryPrompt) { capture("consent").assign(bool(true)) }
            }
        }
    }
}
