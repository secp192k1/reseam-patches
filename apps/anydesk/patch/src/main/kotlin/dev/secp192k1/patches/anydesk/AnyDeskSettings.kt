package dev.secp192k1.patches.anydesk

import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.before
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.point
import app.reseam.patch.settings.settingsHost
import app.reseam.patch.settings.toggle
import dev.secp192k1.patches.settings.SETTINGS_TITLE
import dev.secp192k1.patches.settings.installSettingsRuntime
import dev.secp192k1.patches.settings.openSettingsScreen

internal object AnyDeskSettings {
    val hideLicenseBanner by toggle(
        "Hide license banner",
        summary = "Removes the licence banner from the main screen and PiP.",
        default = true,
        key = "anydesk.hide_license_banner",
    )
    val hideMicrophoneButton by toggle(
        "Hide microphone button",
        summary = "Removes the microphone button from the address fields.",
        default = true,
        key = "anydesk.hide_microphone_button",
    )
    val hideNewsTiles by toggle(
        "Hide news tiles",
        summary = "Removes the news tiles from the main screen.",
        default = false,
        key = "anydesk.hide_news_tiles",
    )
    val hideTelemetryPrompt by toggle(
        "Hide telemetry prompt",
        summary = "Removes the main screen tile that asks to share usage data.",
        default = true,
        key = "anydesk.hide_telemetry_prompt",
    )
    val hideLicenseTile by toggle(
        "Hide license tile",
        summary = "Removes the colored tile with a link for the license settings.",
        default = true,
        key = "anydesk.hide_license_tile",
    )
}

private val settingsCategories = klass("$PACKAGE.gui.fragment.SettingsCategoryFragment")

private val categoryList = method("settingsCategoryList") {
    inClass(settingsCategories)
    strings("s_application")
}

val anyDeskSettings = settingsHost("anydesk") {
    compatibleWith(PACKAGE)

    install {
        installSettingsRuntime()
        val wrenchIcon = resources.id("drawable", "ic_nav_settings")?.toInt() ?: error("The app has no ic_nav_settings drawable")
        val categoryAdapter = categoryList.point("category adapter") { invokeVirtual { owner("android.widget.ArrayAdapter"); name("clear") } }.writer(0).field()
        val sectionHeader = categoryList.point("section header") { invokeDirect { name("<init>"); params(Type.String, Type.String) } }.callee()
        val categoryEntry = categoryList.point("category entry") { invokeDirect { name("<init>"); paramCount(5) } }.callee()
        val selectedCategory = method("showSettingsCategory") {
            inClass(settingsCategories)
            params("android.widget.ListView", Type.Int, Type.Boolean)
        }.point("selected category") { field { type(Type.Int) } }.field()

        categoryList.after {
            val adapter = thisObject.field(categoryAdapter)
            whenNotNull(adapter) {
                adapter.callVirtual("android.widget.ArrayAdapter", "add", "(Ljava/lang/Object;)V", newInstance(sectionHeader.owner, sectionHeader.proto, string("s_reseam"), string("Reseam")))
                // No category fragment belongs to the entry, since a tap on it opens the Reseam Settings screen.
                val entry = newInstance(categoryEntry.owner, categoryEntry.proto, int(wrenchIcon), int(0), nullObject, string("s_reseam_settings"), string(SETTINGS_TITLE))
                adapter.callVirtual("android.widget.ArrayAdapter", "add", "(Ljava/lang/Object;)V", entry)
            }
        }

        method("settingsCategoryTap") {
            inClass(settingsCategories)
            params("android.widget.ListView", Type.Int)
            returns(Type.Void)
        }.before {
            val list = param(0)
            whenEqual(param(1), list.callVirtual("android.widget.AdapterView", "getCount", "()I").minus(int(1))) {
                list.callVirtual("android.widget.AbsListView", "setItemChecked", "(IZ)V", thisObject.field(selectedCategory), bool(true))
                openSettingsScreen()
                returnVoid()
            }
        }
    }
}
