package dev.secp192k1.patches.twitch.debug

import app.reseam.patch.MethodTarget
import app.reseam.patch.PatchRuntime
import app.reseam.patch.Type
import app.reseam.patch.dex.fieldRef
import app.reseam.patch.method
import app.reseam.patch.methods
import app.reseam.patch.patch
import app.reseam.patch.points
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import dev.secp192k1.patches.twitch.TWITCH
import dev.secp192k1.patches.twitch.settings.twitchSettings
import dev.secp192k1.patches.twitch.shared.mainSettingsFragment
import dev.secp192k1.twitch.settings.DeveloperSettings
import dev.secp192k1.twitch.settings.TwitchStrings

// The account check that hides Twitch's internal tools from everyone but staff
val isStaff = method("isStaff") {
    strings("user_type", "staff")
    returns(Type.Boolean)
    paramCount(0)
}

// The settings screen reads the account from its fragment before it lays out the debug row
val settingsScreenDebugRows = methods("settingsScreenDebugRows") {
    calls(isStaff)
    custom { instructions.any { instruction -> instruction.fieldRef?.definingClass == mainSettingsFragment.descriptor } }
}

// The id literal exists only once resources.id resolves it, so these targets are built at patch time
private fun PatchRuntime.staffChecksUsing(type: String, name: String): List<MethodTarget> {
    val id = resources.id(type, name)?.toLong() ?: error("The app has no $type/$name")
    val checks = methods("$type/$name staff checks") {
        calls(isStaff)
        literals(id)
    }.all
    require(checks.isNotEmpty()) { "The app never checks the staff flag where it uses $type/$name" }
    return checks
}

@Suppress("unused")
val debugMode = patch("Debug mode") {
    description("Shows the staff debug menu in settings, with its debug settings, experiments and logs.")
    compatibleWith(TWITCH)
    settings(twitchSettings, section(TwitchStrings.sectionDeveloper, DeveloperSettings.debugMode))

    execute {
        val settingsRowChecks = settingsScreenDebugRows.all
        require(settingsRowChecks.isNotEmpty()) { "The settings screen never checks the staff flag" }

        // Every place that shows or opens the debug menu checks the staff flag on its own
        val staffChecks = listOf(
            staffChecksUsing("string", "settings_debug_menu"),
            staffChecksUsing("menu", "debug_app_actions"),
            staffChecksUsing("id", "debug_settings_menu_item"),
            settingsRowChecks,
        ).flatten().distinctBy { check -> check.descriptor }

        gate(DeveloperSettings.debugMode) {
            val staffCalls = staffChecks.flatMap { check -> check.points("staffCheck") { calls(isStaff) }.all }
            staffCalls.forEach { staffCall ->
                staffCall.next { resultOf(Type.Boolean) }.captureAs("staff").after {
                    capture("staff").assign(bool(true))
                }
            }
            log.info("Opened the debug menu at ${staffCalls.size} staff checks in ${staffChecks.size} methods")
        }
    }
}
