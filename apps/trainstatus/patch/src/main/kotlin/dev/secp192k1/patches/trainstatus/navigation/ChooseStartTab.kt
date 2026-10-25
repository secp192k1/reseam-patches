package dev.secp192k1.patches.trainstatus.navigation

import app.reseam.patch.after
import app.reseam.patch.patch
import app.reseam.patch.settings.section
import dev.secp192k1.patches.trainstatus.TRAINSTATUS
import dev.secp192k1.patches.trainstatus.settings.trainStatusSettings
import dev.secp192k1.patches.trainstatus.shared.mainActivityCreate
import dev.secp192k1.patches.trainstatus.shared.whenChosen
import dev.secp192k1.trainstatus.settings.START_TAB_DEPARTURES
import dev.secp192k1.trainstatus.settings.StartSettings
import dev.secp192k1.trainstatus.settings.TrainStatusStrings

@Suppress("unused")
val chooseStartTab = patch("Choose start tab") {
    description("Picks the screen the app opens on.")
    compatibleWith(TRAINSTATUS)
    settings(trainStatusSettings, section(TrainStatusStrings.sectionNavigation, StartSettings.startTab))

    execute {
        val departuresTabId = resources.id("id", "texthorarios40")?.toInt() ?: error("The app has no id/texthorarios40")

        // The tab's own click listener switches the layouts and sets the time to now
        mainActivityCreate.after {
            whenChosen(StartSettings.startTab, START_TAB_DEPARTURES) {
                val tab = thisObject.callVirtual("android.app.Activity", "findViewById", "(I)Landroid/view/View;", int(departuresTabId))
                whenNotNull(tab) {
                    tab.callVirtual("android.view.View", "callOnClick", "()Z")
                }
            }
        }
    }
}
