package dev.secp192k1.patches.trainstatus

import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.methods
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.whenChosen

private const val TWO_MINUTES = 120000L
private val trackingIntervals = mapOf("5" to 300000L, "10" to 600000L)

@Suppress("unused")
val trackingInterval = patch("Tracking interval") {
    description("Sets how often a followed train is checked.")
    compatibleWith(PACKAGE)
    settings(trainStatusSettings, section("Train tracking", TrainStatusSettings.trackingInterval))

    execute {
        // The foreground service the train screen starts, and the older service, each sleep two minutes between checks
        val trackingLoops = methods("trackingLoops") {
            literals(TWO_MINUTES)
            calls { name("sleep"); params(Type.Long) }
        }
        require(trackingLoops.all.isNotEmpty()) { "The app no longer waits two minutes between train checks" }

        trackingLoops.forEach {
            point("check interval") { literal(TWO_MINUTES) }
                .captureAs("milliseconds", Type.Long)
                .after {
                    trackingIntervals.forEach { (choice, milliseconds) ->
                        whenChosen(TrainStatusSettings.trackingInterval, choice) { capture("milliseconds").assign(long(milliseconds)) }
                    }
                }
        }
        log.info("Tracking loops hooked: ${trackingLoops.all.size}")
    }
}
