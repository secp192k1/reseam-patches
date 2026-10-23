package dev.secp192k1.patches.anydesk.license

import app.reseam.patch.patch
import app.reseam.patch.settings.section
import dev.secp192k1.anydesk.settings.AnyDeskStrings
import dev.secp192k1.anydesk.settings.DeclutterSettings
import dev.secp192k1.patches.anydesk.ANYDESK
import dev.secp192k1.patches.anydesk.settings.anyDeskSettings
import dev.secp192k1.patches.anydesk.shared.keepGoneById

@Suppress("unused")
val hideLicenseBanner = patch("Hide license banner") {
    description("Removes the license banner from the main screen and PiP.")
    compatibleWith(ANYDESK)
    settings(anyDeskSettings, section(AnyDeskStrings.sectionDeclutter, DeclutterSettings.hideLicenseBanner))

    execute {
        keepGoneById("license_banner", DeclutterSettings.hideLicenseBanner)
    }
}
