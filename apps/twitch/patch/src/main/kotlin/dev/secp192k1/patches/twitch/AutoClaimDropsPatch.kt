package dev.secp192k1.patches.twitch

import app.reseam.patch.after
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.reserveLocal
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.whenOn

private const val DROPS_CLAIM = "tv.twitch.android.shared.drops.model.DropsClaim"

@Suppress("unused")
val autoClaimDrops = patch("Auto claim drops") {
    description("Claims the Drops as soon as you earn them while watching.")
    compatibleWith(TWITCH)
    settings(twitchSettings, section(rewardsPage, "Drops", TwitchSettings.autoClaimDrops))

    execute {
        val earnedDrop = resources.id("string", "you_earned_drop")?.toLong() ?: error("The app has no you_earned_drop string")
        // Draws the notification that offers an earned Drop to claim, and what the claim leads to
        val dropNotification = method("dropNotification") { literals(earnedDrop) }

        // Tapping Claim, instead of sending the claim request, lets Twitch show the Drop as claimed
        val drop = dropNotification.reserveLocal("drop", DROPS_CLAIM)
        val claimableDrop = dropNotification.point("claimable drop") { field { type(DROPS_CLAIM) } }
        claimableDrop.captureAs("drop").after { local(drop).assign(capture("drop")) }
        claimableDrop.next { invokeVirtual { name("setOnClickListener") } }
            .captureArgumentAs("claimButton", 0)
            .captureArgumentAs("claimListener", 1)
            .after {
                whenOn(TwitchSettings.autoClaimDrops) {
                    call(
                        AutoClaim.claimDrop,
                        capture("claimButton"),
                        capture("claimListener"),
                        local(drop).callVirtual(DROPS_CLAIM, "getDropInstanceId", "()Ljava/lang/String;"),
                    )
                }
            }
    }
}
