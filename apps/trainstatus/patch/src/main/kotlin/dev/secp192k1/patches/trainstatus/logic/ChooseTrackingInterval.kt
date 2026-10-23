package dev.secp192k1.patches.trainstatus.logic

import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.methods
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.settings.section
import dev.secp192k1.patches.trainstatus.TRAINSTATUS_PINNED
import dev.secp192k1.patches.trainstatus.settings.trainStatusSettings
import dev.secp192k1.patches.trainstatus.shared.whenChosen
import dev.secp192k1.trainstatus.settings.TRACKING_INTERVAL_FIVE
import dev.secp192k1.trainstatus.settings.TRACKING_INTERVAL_TEN
import dev.secp192k1.trainstatus.settings.TrackingSettings
import dev.secp192k1.trainstatus.settings.TrainStatusStrings

private const val TWO_MINUTES = 120_000L
private val trackingIntervals = mapOf(
    TRACKING_INTERVAL_FIVE to 300_000L,
    TRACKING_INTERVAL_TEN to 600_000L,
)

// The foreground service the train screen starts, and the older service, each sleep two minutes between checks
val trackingLoops = methods("trackingLoops") {
    literals(TWO_MINUTES)
    calls { name("sleep"); params(Type.Long) }
}

val chooseTrackingInterval = patch("Choose tracking interval") {
    description("Sets how often a followed train is checked.")
    // Pinned because a wrong match would rewrite a constant without any error
    compatibleWith(TRAINSTATUS_PINNED)
    settings(trainStatusSettings, section(TrainStatusStrings.sectionTrainTracking, TrackingSettings.trackingInterval))

    execute {
        val loops = trackingLoops.all
        require(loops.isNotEmpty()) { "The app no longer waits two minutes between train checks" }

        loops.forEach { loop ->
            loop.point("checkInterval") { literal(TWO_MINUTES) }.captureAs("milliseconds", Type.Long).after {
                trackingIntervals.forEach { (choice, milliseconds) ->
                    whenChosen(TrackingSettings.trackingInterval, choice) { capture("milliseconds").assign(long(milliseconds)) }
                }
            }
        }
        log.info("Tracking loops hooked: ${loops.size}")
    }
}
