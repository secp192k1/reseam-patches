package dev.secp192k1.patches.trainstatus

import app.reseam.patch.after
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.whenChosen

@Suppress("unused")
val startTab = patch("Start tab") {
    description("Pick which screen to open on app start.")
    compatibleWith(PACKAGE)
    settings(trainStatusSettings, section("Start", TrainStatusSettings.startTab))

    execute {
        val departuresTab = resources.id("id", "texthorarios40")?.toInt() ?: error("The app has no id/texthorarios40")

        // The tab's own click listener switches the layouts and sets the time to now
        mainActivity.method("onCreate").after {
            whenChosen(TrainStatusSettings.startTab, "departures") {
                thisObject.callVirtual("android.app.Activity", "findViewById", "(I)Landroid/view/View;", int(departuresTab))
                    .callVirtual("android.view.View", "callOnClick", "()Z")
            }
        }
    }
}
