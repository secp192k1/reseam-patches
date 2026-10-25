package dev.secp192k1.patches.twitch.shared

import app.reseam.patch.klass

// Twitch's R8 rules keep the names of its fragments, unlike most of its classes
val mainSettingsFragment = klass("tv.twitch.android.settings.main.MainSettingsFragmentV2")
