package dev.secp192k1.patches.settings

import app.reseam.patch.CodeScope
import app.reseam.patch.ExtClass
import app.reseam.patch.MethodTarget
import app.reseam.patch.Otherwise
import app.reseam.patch.PointTarget
import app.reseam.patch.Type
import app.reseam.patch.ValueRef
import app.reseam.patch.after
import app.reseam.patch.appEntry
import app.reseam.patch.settings.ChoiceSetting
import app.reseam.patch.settings.TextSetting
import app.reseam.patch.settings.ToggleSetting

const val SETTINGS_TITLE = "Reseam Settings"
private const val SETTINGS_RUNTIME = "dev.secp192k1.settings"

private object SettingsCache : ExtClass("$SETTINGS_RUNTIME.SettingsCache") {
    val getBoolean by static(Type.String, Type.Boolean, returns = Type.Boolean)
    val getString by static(Type.String, Type.String, returns = Type.String)
}

private object SettingsRuntime : ExtClass("$SETTINGS_RUNTIME.ReseamSettings") {
    val init by static(Type.Context)
}

private object SettingsScreen : ExtClass("$SETTINGS_RUNTIME.ReseamSettingsScreen") {
    val open by static()
}

private object SettingsEntry : ExtClass("$SETTINGS_RUNTIME.SettingsEntry") {
    val addSettingsRow by static(Type.View, returns = Type.View)
    val openOnClick by static(Type.View)
}

// Loads the settings on app start, before patched code reads them
fun installSettingsRuntime() {
    appEntry { call(SettingsRuntime.init, application) }
}

// Loads the settings on app start and puts a Reseam Settings row above the view the settings screen method returns
fun installSettingsEntry(settingsScreen: MethodTarget) {
    installSettingsRuntime()
    settingsScreen.after { returnValue(call(SettingsEntry.addSettingsRow, capture("result"))) }
}

fun CodeScope.openSettingsScreen() {
    call(SettingsScreen.open)
}

// Makes a Reseam Settings row the patch added to the app's own layout open the settings screen
fun CodeScope.openSettingsOnClick(row: ValueRef) {
    call(SettingsEntry.openOnClick, row)
}

// Runs the block only while the toggle is on
fun CodeScope.whenOn(setting: ToggleSetting, block: CodeScope.() -> Unit): Otherwise =
    whenTrue(call(SettingsCache.getBoolean, string(setting.key), bool(setting.default)), block)

// Makes the boolean returned by the call at this point read as true while the setting is on
fun PointTarget.forceTrue(setting: ToggleSetting) = next { resultOf(Type.Boolean) }
    .captureAs("result")
    .after { whenOn(setting) { capture("result").assign(bool(true)) } }

// Runs the block only while the choice holds this value
fun CodeScope.whenChosen(setting: ChoiceSetting, value: String, block: CodeScope.() -> Unit): Otherwise =
    whenTrue(string(value).callVirtual(Type.String, "equals", "(Ljava/lang/Object;)Z", valueOf(setting)), block)

fun CodeScope.valueOf(setting: ToggleSetting): ValueRef = call(SettingsCache.getBoolean, string(setting.key), bool(setting.default))
fun CodeScope.valueOf(setting: ChoiceSetting): ValueRef = call(SettingsCache.getString, string(setting.key), string(setting.default))
fun CodeScope.valueOf(setting: TextSetting): ValueRef = call(SettingsCache.getString, string(setting.key), string(setting.default))
