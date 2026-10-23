package dev.secp192k1.patches.trainstatus.settings

import app.reseam.patch.before
import app.reseam.patch.method
import app.reseam.patch.settings.openSettings
import app.reseam.patch.settings.settingsHost
import dev.secp192k1.patches.brand.SETTINGS_TITLE
import dev.secp192k1.patches.brand.reseamBrand
import dev.secp192k1.patches.trainstatus.TRAINSTATUS
import dev.secp192k1.patches.trainstatus.shared.mainActivity

val mainActivityOptionsItemSelected = mainActivity.method("onOptionsItemSelected")

val trainStatusSettings = settingsHost("trainstatus") {
    compatibleWith(TRAINSTATUS)
    brand(reseamBrand)

    install {
        val settingsItemId = resources.addId("reseam_settings_item") ?: error("Could not add id/reseam_settings_item")
        val appSettingsItemId = resources.id("id", "definicoes") ?: error("The app has no id/definicoes")

        // A copy of the overflow menu's last item, Definições, that opens Reseam Settings
        resources.editXml("menu", "example_menu") {
            val appSettingsItem = findByAttribute("android:id", "@0x%08x".format(appSettingsItemId.toInt())).single()
            val menu = appSettingsItem.parent ?: error("The Definições item has no parent")
            val settingsItem = appSettingsItem.clone(true)
            menu.appendChild(settingsItem)
            settingsItem.setResourceRef("android:id", settingsItemId)
            settingsItem["android:title"] = SETTINGS_TITLE
        }

        mainActivityOptionsItemSelected.before {
            val itemId = param(0).callInterface("android.view.MenuItem", "getItemId", "()I")
            whenEqual(itemId, int(settingsItemId.toInt())) {
                openSettings()
                returnTrue()
            }
        }
    }
}
