package dev.secp192k1.patches.views

import app.reseam.patch.ExtClass
import app.reseam.patch.PatchRuntime
import app.reseam.patch.PointTarget
import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.methods
import app.reseam.patch.points
import app.reseam.patch.settings.ToggleSetting
import dev.secp192k1.patches.settings.whenOn

private object HiddenViews : ExtClass("dev.secp192k1.views.HiddenViews") {
    val keepGone by static(Type.View)
    val keepAncestorGone by static(Type.View, Type.Int, Type.Int)
}

// Every lookup of the id, with findViewById or with the ViewBinding helper that takes the root view and the id.
fun PatchRuntime.viewLookups(idName: String): List<PointTarget> {
    val id = resources.id("id", idName)?.toLong() ?: error("The app has no id/$idName")
    val users = methods("id/$idName users") { literals(id) }

    // R8 may load the id once for several lookups, so the argument is traced back to it.
    val lookups = users.points("id/$idName lookup") {
        invokeVirtual { name("findViewById") }
        argument(1) { literal(id) }
    }.all + users.points("id/$idName binding lookup") {
        invokeStatic { params(Type.View, Type.Int); returns(Type.View) }
        argument(1) { literal(id) }
    }.all

    require(lookups.isNotEmpty()) { "The app never looks up id/$idName" }
    return lookups
}

// levelsUp hides a container of the looked up view instead, such as the card around a row, and siblingsBefore the views right before that, such as a divider.
fun PatchRuntime.keepGoneById(idName: String, setting: ToggleSetting, levelsUp: Int = 0, siblingsBefore: Int = 0) {
    val lookups = viewLookups(idName)

    lookups.forEach { lookup ->
        lookup.next { resultOf(Type.View) }.captureAs("view").after {
            whenOn(setting) {
                if (levelsUp == 0 && siblingsBefore == 0) {
                    call(HiddenViews.keepGone, capture("view"))
                } else {
                    call(HiddenViews.keepAncestorGone, capture("view"), int(levelsUp), int(siblingsBefore))
                }
            }
        }
    }

    var hidden = if (levelsUp == 0) "id/$idName" else "the view $levelsUp up from id/$idName"
    if (siblingsBefore > 0) hidden += " and the $siblingsBefore views before it"
    log.info("Keeps $hidden hidden, lookups hooked: ${lookups.size}")
}
