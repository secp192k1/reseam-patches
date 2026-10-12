package dev.secp192k1.patches.twitch

import app.reseam.patch.before
import app.reseam.patch.field
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.whenOn

private const val DISCOVERY_FEED_PAGE = "tv.twitch.android.models.feed.DiscoveryFeedPage"

@Suppress("unused")
val startOnFollowing = patch("Start on Following") {
    description("Opens Home on the Following page instead of the Live feed.")
    compatibleWith(TWITCH)
    settings(twitchSettings, section(homePage, "Start page", TwitchSettings.startOnFollowing))

    execute {
        // The page Home opens on: Live, or the saved one for viewers in Twitch's default page test
        method("defaultHomePage") {
            strings("default_home_page")
            returns(DISCOVERY_FEED_PAGE)
            paramCount(0)
        }.before {
            whenOn(TwitchSettings.startOnFollowing) {
                returnValue(staticField(klass($$"$$DISCOVERY_FEED_PAGE$FollowingPage").field("INSTANCE")))
            }
        }
    }
}
