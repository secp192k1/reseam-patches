package dev.secp192k1.patches.views

import app.reseam.patch.PatchRuntime
import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.methods
import app.reseam.patch.points
import app.reseam.patch.settings.ToggleSetting
import app.reseam.patch.settings.whenEnabled
import dev.secp192k1.views.bindings.HiddenViews

// The id literal exists only once resources.id resolves it, so these targets are built at patch time
fun PatchRuntime.keepGoneById(idName: String, setting: ToggleSetting) {
    val id = resources.id("id", idName)?.toLong() ?: error("The app has no id/$idName")
    val users = methods("id/$idName users") { literals(id) }

    // R8 may load the id once for several lookups, so the argument is traced back to it
    val findViewByIdLookups = users.points("id/$idName findViewById lookup") {
        invokeVirtual { name("findViewById") }
        argument(1) { literal(id) }
    }.all
    val bindingLookups = users.points("id/$idName binding lookup") {
        invokeStatic { params(Type.View, Type.Int); returns(Type.View) }
        argument(1) { literal(id) }
    }.all
    // Twitch's ViewBinding helper takes the id before the root view
    val idFirstBindingLookups = users.points("id/$idName id first binding lookup") {
        invokeStatic { params(Type.Int, Type.View); returns(Type.View) }
        argument(0) { literal(id) }
    }.all
    val lookups = findViewByIdLookups + bindingLookups + idFirstBindingLookups
    require(lookups.isNotEmpty()) { "The app never looks up id/$idName" }

    lookups.forEach { lookup ->
        lookup.next { resultOf(Type.View) }.captureAs("view").after {
            whenEnabled(setting) { HiddenViews.keepGone(capture("view")) }
        }
    }
    log.info("Keeps id/$idName hidden, lookups hooked: ${lookups.size}")
}
