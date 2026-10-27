package dev.secp192k1.patches.twitch.shared

import app.reseam.patch.Type
import app.reseam.patch.klass
import app.reseam.patch.method

// Twitch's R8 rules keep the names of its fragments, unlike most of its classes
const val MAIN_SETTINGS_FRAGMENT = "tv.twitch.android.settings.main.MainSettingsFragmentV2"
val mainSettingsFragment = klass(MAIN_SETTINGS_FRAGMENT)

// The account check that hides Twitch's internal tools from everyone but staff
val isStaff = method("isStaff") {
    strings("user_type", "staff")
    returns(Type.Boolean)
    paramCount(0)
}
