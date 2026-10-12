package dev.secp192k1.patches.twitch

import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.points
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.forceTrue

private const val AD_SERVER = "https://edge.ads.twitch.tv/"

// Applies Twitch's staff ad debug switches before a stream decides whether to request an ad
private val adEligibilityOverride = method("adEligibilityOverride") {
    strings("skip_preroll", "decline_all_ads")
}

@Suppress("unused")
val blockVideoAds = patch("Block video ads") {
    description("Blocks the video, audio and display ads the app requests itself. Ads stitched into the stream need Block embedded ads.")
    compatibleWith(TWITCH)
    settings(twitchSettings, section(adsPage, "Video ads", TwitchSettings.blockVideoAds))

    execute {
        // The staff switch that declines every ad makes the stream skip its ad request
        adEligibilityOverride.points("staff check") { calls(isStaff) }.forEach { forceTrue(TwitchSettings.blockVideoAds) }
        adEligibilityOverride.point("decline all ads") { string("decline_all_ads") }
            .next { invokeInterface { name("getBoolean") } }
            .forceTrue(TwitchSettings.blockVideoAds)

        // Feed, browse and remaining stream ad requests fail before they reach the ad server
        val cutOffUrls = cutOffServer(AD_SERVER, TwitchSettings.blockVideoAds)
        require(cutOffUrls > 0) { "The app no longer references $AD_SERVER" }
        log.info("Declined ad eligibility and cut off $cutOffUrls ad server URLs")
    }
}
