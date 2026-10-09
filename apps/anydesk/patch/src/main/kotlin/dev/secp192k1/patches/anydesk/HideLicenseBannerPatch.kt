package dev.secp192k1.patches.anydesk

import app.reseam.patch.patch
import app.reseam.patch.settings.section
import dev.secp192k1.patches.views.keepGoneById

@Suppress("unused")
val hideLicenseBanner = patch("Hide license banner") {
    description("Removes the licence banner from the main screen and PiP.")
    compatibleWith(PACKAGE)
    settings(anyDeskSettings, section("Declutter", AnyDeskSettings.hideLicenseBanner))

    execute {
        keepGoneById("license_banner", AnyDeskSettings.hideLicenseBanner)
    }
}
