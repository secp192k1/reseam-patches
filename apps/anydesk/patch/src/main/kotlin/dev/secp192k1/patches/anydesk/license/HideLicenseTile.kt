package dev.secp192k1.patches.anydesk.license

import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import dev.secp192k1.anydesk.settings.AnyDeskStrings
import dev.secp192k1.anydesk.settings.DeclutterSettings
import dev.secp192k1.patches.anydesk.ANYDESK
import dev.secp192k1.patches.anydesk.settings.anyDeskSettings
import dev.secp192k1.patches.anydesk.shared.mainFragment
import dev.secp192k1.patches.anydesk.shared.nativeBridge

val licenseConfigString = method("licenseConfigString") {
    inClass(nativeBridge)
    calls { name("jniGetLicenseConfigString") }
}

val licenseTile = method("licenseTile") {
    inClass(mainFragment)
    calls(licenseConfigString)
}

// The tile is the only item this method adds, after it clears the list
val addLicenseTile = licenseTile.point("addLicenseTile") {
    invokeVirtual { owner("java.util.ArrayList"); name("add") }
}

@Suppress("unused")
val hideLicenseTile = patch("Hide license tile") {
    description("Removes the colored tile with a link for the license settings.")
    compatibleWith(ANYDESK)
    settings(anyDeskSettings, section(AnyDeskStrings.sectionDeclutter, DeclutterSettings.hideLicenseTile))

    execute {
        gate(DeclutterSettings.hideLicenseTile) {
            addLicenseTile.skip()
        }
    }
}
