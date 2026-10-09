package dev.secp192k1.patches.views

import app.reseam.patch.ExtClass
import app.reseam.patch.PatchRuntime
import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.methods
import app.reseam.patch.points
import app.reseam.patch.settings.ToggleSetting
import dev.secp192k1.patches.settings.whenOn

private object HiddenViews : ExtClass("dev.secp192k1.views.HiddenViews") {
    val keepGone by static(Type.View)
}

fun PatchRuntime.keepGoneById(idName: String, setting: ToggleSetting) {
    val id = resources.id("id", idName)?.toLong() ?: error("The app has no id/$idName")

    val lookups = methods("id/$idName users") { literals(id) }.points("id/$idName lookup") {
        literal(id)
        then { invokeVirtual { name("findViewById") } }
    }.all

    require(lookups.isNotEmpty()) { "The app never looks up id/$idName with findViewById" }

    lookups.forEach { lookup ->
        lookup.next { resultOf(Type.View) }.captureAs("view").after {
            whenOn(setting) { call(HiddenViews.keepGone, capture("view")) }
        }
    }

    log.info("Keeps id/$idName hidden, lookups hooked: ${lookups.size}")
}
