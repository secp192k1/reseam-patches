package dev.secp192k1.patches.anydesk

import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.settings.section
import app.reseam.patch.skipWhen
import dev.secp192k1.patches.settings.valueOf

private val licenseConfigString = method("licenseConfigString") {
    inClass(nativeBridge)
    calls { name("jniGetLicenseConfigString") }
}

@Suppress("unused")
val hideLicenseTile = patch("Hide license tile") {
    description("Removes the colored tile with a link for the license settings.")
    compatibleWith(PACKAGE)
    settings(anyDeskSettings, section("Declutter", AnyDeskSettings.hideLicenseTile))

    execute {
        method("licenseTile") {
            inClass(mainFragment)
            calls(licenseConfigString)
        }.point("add license tile") {
            invokeVirtual { owner("java.util.ArrayList"); name("add") }
        }.skipWhen {
            valueOf(AnyDeskSettings.hideLicenseTile)
        }
    }
}
