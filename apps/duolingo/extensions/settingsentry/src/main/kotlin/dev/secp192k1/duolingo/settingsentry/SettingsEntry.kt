@file:JvmName("SettingsEntry")

package dev.secp192k1.duolingo.settingsentry

import app.reseam.runtime.Export

// One handler serves every redraw of the settings screen, so Compose can skip the button
private val openSettingsClick = OpenSettingsOnClick()

// The patch replaces this body with a call that opens the bundle's settings
@Export
fun openSettings() {}

@Suppress("unused")
@Export
fun openSettingsOnClick(): () -> Any? = openSettingsClick
