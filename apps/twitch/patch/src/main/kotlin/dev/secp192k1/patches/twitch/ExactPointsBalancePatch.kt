package dev.secp192k1.patches.twitch

import app.reseam.patch.ExtClass
import app.reseam.patch.Type
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.methods
import app.reseam.patch.patch
import app.reseam.patch.points
import app.reseam.patch.redirectTo
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.valueOf

private object ExactPoints : ExtClass("dev.secp192k1.twitch.exactpoints.ExactPoints") {
    val enabled by static(returns = Type.Boolean)
    val compact by static(Type.Long, Type.Boolean, returns = Type.String)
    val format by static(Type.Long, Type.Boolean, returns = Type.String)
}

// Shortens a count the way Twitch shows it, such as 12.3K for 12,345
private val compactNumber = method("compactNumber") {
    inClass(klass("tv.twitch.android.util.NumberFormatUtil"))
    params(Type.Long, Type.Boolean)
    returns(Type.String)
}

@Suppress("unused")
val exactPointsBalance = patch("Exact points balance") {
    description("Shows the balance on the chat button in full, such as 12,345 instead of 12.3K.")
    compatibleWith(TWITCH)
    settings(twitchSettings, section(rewardsPage, "Points balance", TwitchSettings.exactPointsBalance))

    execute {
        ExactPoints.enabled.implement { returnValue(valueOf(TwitchSettings.exactPointsBalance)) }
        ExactPoints.compact.implement { returnValue(call(compactNumber, param(0), param(1))) }

        // The balance on the classic chat button; One Chat shows it only in the rewards list, which Twitch prints in full
        val balanceText = method("channelPointsBalanceText") {
            calls(compactNumber)
            params(enabledButton.descriptor)
            returns(Type.String)
        }
        val compactPoints = balanceText.points("compact balance") { calls(compactNumber) }.all +
            // Show the points a viewer just gained next to the Channel Points button, in both chat layouts
            methods("pointGainText") {
                calls(compactNumber)
                calls { owner("tv.twitch.android.models.communitypoints.PointGain"); name("getTotalPoints") }
            }.points("compact point gain") { calls(compactNumber) }.all

        require(compactPoints.isNotEmpty()) { "Found no shortened Channel Points" }

        compactPoints.forEach { point -> point.redirectTo(ExactPoints.format) }
        log.info("Printed Channel Points in full at ${compactPoints.size} places")
    }
}
