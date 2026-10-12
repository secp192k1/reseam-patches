package dev.secp192k1.patches.twitch

import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.whenOn

@Suppress("unused")
val showEveryAutomaticReward = patch("Show every automatic reward") {
    description("Lists every automatic Channel Points reward a channel offers, such as Highlight My Message and the emote unlocks, also those Twitch's remote config leaves out of the app.")
    compatibleWith(TWITCH)
    settings(twitchSettings, section(rewardsPage, "Automatic rewards", TwitchSettings.everyAutomaticReward))

    execute {
        // Keeps the automatic rewards of a channel whose types the comma separated remote config list enables
        method("automaticRewardsFilter") {
            calls {
                owner($$"tv.twitch.android.models.communitypoints.CommunityPointsReward$Automatic")
                name("getType")
            }
            strings(",")
        }.point("enabled reward types") { field { type(Type.String) } }
            .captureAs("enabledTypes")
            .after {
                whenOn(TwitchSettings.everyAutomaticReward) {
                    // The remote config list stays, so a type Twitch enables later keeps working; a missing list reads as "null"
                    val remoteTypes = callStatic(Type.String, "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;", capture("enabledTypes"))
                    capture("enabledTypes").assign(string(
                        // Every automatic reward type the app has screens for
                        // The April Fools one is left to Twitch, which switches that event on itself
                        "SINGLE_MESSAGE_BYPASS_SUB_MODE,SEND_HIGHLIGHTED_MESSAGE,RANDOM_SUB_EMOTE_UNLOCK,CHOSEN_SUB_EMOTE_UNLOCK,CHOSEN_MODIFIED_SUB_EMOTE_UNLOCK,"
                    ).callVirtual(Type.String, "concat", "(Ljava/lang/String;)Ljava/lang/String;", remoteTypes))
                }
            }
    }
}
