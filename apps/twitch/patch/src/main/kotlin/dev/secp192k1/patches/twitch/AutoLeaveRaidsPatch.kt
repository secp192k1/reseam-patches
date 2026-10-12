package dev.secp192k1.patches.twitch

import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.before
import app.reseam.patch.field
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.whenOn

// Follows the raids of the channel being watched, and moves a joined viewer along when the raid goes
private val raidViewModel = klass("raidViewModel") { strings("getObserveRaidsJob()Lkotlinx/coroutines/Job;") }

@Suppress("unused")
val autoLeaveRaids = patch("Auto leave raids") {
    description("Stays on the channel when a raid is started. The raid banner still offers Join.")
    compatibleWith(TWITCH)
    settings(twitchSettings, section(chatPage, "Raids", TwitchSettings.autoLeaveRaids))

    execute {
        // A viewer starts out joined, so a raid moves them along unless they leave it
        val joinedState = raidViewModel.method("<init>").point("joined state") { field { name("JOINED") } }

        joinedState.captureAs("state").after {
            whenOn(TwitchSettings.autoLeaveRaids) { capture("state").assign(
                staticField(klass(joinedState.field().owner).field("NOT_JOINED"))
            ) }
        }

        // Joins a raid in the background, once, when it starts
        method("joinStartedRaid") {
            inClass(raidViewModel)
            params("tv.twitch.android.shared.raids.pubsub.RaidEventInfo")
            returns(Type.Void)
        }.point("join") {
            invokeStatic { hasParam("kotlin.jvm.functions.Function2") }
        }.before {
            whenOn(TwitchSettings.autoLeaveRaids) { returnVoid() }
        }
    }
}
