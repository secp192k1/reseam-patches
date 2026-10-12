package dev.secp192k1.patches.twitch

import app.reseam.patch.dex.fieldRef
import app.reseam.patch.methods
import app.reseam.patch.patch
import app.reseam.patch.points
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.forceTrue

@Suppress("unused")
val debugMode = patch("Debug mode") {
    description("Shows staff debug menu in settings, with its debug settings, experiments and logs.")
    compatibleWith(TWITCH)
    settings(twitchSettings, section(developerPage, "Debug", TwitchSettings.debugMode))

    execute {
        val debugMenuTitle = resources.id("string", "settings_debug_menu")?.toLong() ?: error("The app has no settings_debug_menu string")
        val debugActionsMenu = resources.id("menu", "debug_app_actions")?.toLong() ?: error("The app has no debug_app_actions menu")
        val debugSettingsItem = resources.id("id", "debug_settings_menu_item")?.toLong() ?: error("The app has no debug_settings_menu_item item")

        // Every place that shows or opens the debug menu checks the staff flag on its own
        val staffGates = listOf(
            methods("debugMenuEntries") {
                calls(isStaff)
                literals(debugMenuTitle)
            },
            methods("debugActionsMenu") {
                calls(isStaff)
                literals(debugActionsMenu)
            },
            methods("debugSettingsItem") {
                calls(isStaff)
                literals(debugSettingsItem)
            },
            methods("settingsScreenDebugRow") {
                calls(isStaff)
                // The settings screen reads the account from its fragment before it lays out the debug row
                custom { instructions.any { instruction ->
                    instruction.fieldRef?.definingClass == "Ltv/twitch/android/settings/main/MainSettingsFragmentV2;"
                } }
            },
        ).flatMap { gates -> gates.all }.distinctBy { gate -> gate.descriptor }

        require(staffGates.isNotEmpty()) { "Found none of the staff checks around the debug menu" }

        staffGates.forEach { gate -> gate.points("staff check") { calls(isStaff) }.forEach {
            forceTrue(TwitchSettings.debugMode) }
        }

        log.info("Opened the debug menu at ${staffGates.size} staff checks")
    }
}
