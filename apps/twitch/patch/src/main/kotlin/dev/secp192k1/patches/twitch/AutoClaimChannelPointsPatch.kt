package dev.secp192k1.patches.twitch

import app.reseam.patch.ExtClass
import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.dex.AccessFlags
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.whenOn

internal object AutoClaim : ExtClass("dev.secp192k1.twitch.autoclaim.AutoClaim") {
    val shouldClaim by static(Type.String, returns = Type.Boolean)
    val claimDrop by static(Type.View, $$"android.view.View$OnClickListener", Type.String)
}

// Keeps the Channel Points state that both the classic chat button and the One Chat input draw
private val buttonStateProvider = klass("channelPointsButtonStateProvider") {
    strings($$"processStateUpdate(Ltv/twitch/android/shared/community/points/presenters/CommunityPointsButtonStateProvider$State;Ltv/twitch/android/shared/community/points/presenters/CommunityPointsButtonStateProvider$UpdateEvent;)Ltv/twitch/android/core/mvp/presenter/StateAndAction;")
}
internal val enabledButton = klass("enabledChannelPointsButton") { strings("Enabled(balance=") }
private val activeClaim = klass("activeClaim") { strings("ActiveClaimModel(id=") }

// Sends the claim request, the same one a tap on the chest sends
private val claimBonus = method("claimBonus") {
    params(Type.String, "tv.twitch.android.shared.one.chat.pub.ChatModeMetadata")
    returns(Type.Void)
    flags(AccessFlags.ABSTRACT)
}

@Suppress("unused")
val autoClaimChannelPoints = patch("Auto claim channel points") {
    description("Claims the Channel Points bonus as soon as they're available.")
    compatibleWith(TWITCH)
    settings(twitchSettings, section(rewardsPage, "Channel points", TwitchSettings.autoClaimChannelPoints))

    execute {
        // Turns the state into what the button shows, on every Channel Points update
        method("channelPointsButtonState") {
            inClass(buttonStateProvider)
            calls(enabledButton.method("<init>"))
        }.after {
            // Claiming from the state instead of the button covers both chat layouts and bonuses already waiting when chat opens
            val button = capture("result")
            whenInstanceOf(button, enabledButton.descriptor) {
                val claim = button.cast(enabledButton.descriptor).fieldOfType(activeClaim.descriptor)
                whenNotNull(claim) {
                    whenOn(TwitchSettings.autoClaimChannelPoints) {
                        val claimId = claim.fieldOfType(Type.String)
                        whenTrue(call(AutoClaim.shouldClaim, claimId)) {
                            // The chat mode only labels analytics, and the request accepts none
                            thisObject.fieldOfType(claimBonus.owner).call(claimBonus, claimId, nullObject)
                        }
                    }
                }
            }
        }
    }
}
