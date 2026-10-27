package dev.secp192k1.patches.twitch.settings

import app.reseam.patch.after
import app.reseam.patch.method
import app.reseam.patch.settings.openSettings
import app.reseam.patch.settings.settingsHost
import dev.secp192k1.patches.brand.SETTINGS_TITLE
import dev.secp192k1.patches.brand.publisherLinks
import dev.secp192k1.patches.brand.reseamBrand
import dev.secp192k1.patches.twitch.TWITCH
import dev.secp192k1.patches.twitch.shared.mainSettingsFragment
import dev.secp192k1.twitch.settingsentry.bindings.SettingsEntry

val mainSettingsCreateView = mainSettingsFragment.method("onCreateView")

val twitchSettings = settingsHost("twitch") {
    compatibleWith(TWITCH)
    brand(reseamBrand)
    about(publisherLinks)

    install {
        SettingsEntry.openSettings.implement {
            openSettings()
            returnVoid()
        }

        // Twitch builds its main settings screen in Compose from a fixed set of rows, so the entry goes above the whole screen
        mainSettingsCreateView.after {
            returnValue(SettingsEntry.addSettingsRow(result, string(SETTINGS_TITLE)))
        }
    }
}
