package dev.secp192k1.patches.twitch.ads

import app.reseam.patch.patch
import app.reseam.patch.settings.section
import dev.secp192k1.patches.twitch.TWITCH
import dev.secp192k1.patches.twitch.settings.twitchSettings
import dev.secp192k1.patches.views.keepGoneById
import dev.secp192k1.twitch.settings.AdsSettings
import dev.secp192k1.twitch.settings.TwitchStrings

// The containers a sponsor's channel skin is drawn into, its brand view, and the sponsored stream banners
private val sponsoredViewIds = listOf(
    "channelskins_container",
    "channelskins_overlay_container",
    "channelskins_top_background_container",
    "channelskins_bottom_background_container",
    "channel_skins_creator_brand_view",
    "sponsored_stream_container",
    "sponsored_streams_container_portrait",
    "sponsored_streams_container_landscape",
)

@Suppress("unused")
val hideSponsoredChannelSkins = patch("Hide sponsored channel skins") {
    description("Removes the brand skins and sponsored stream banners that sponsors add to a channel.")
    compatibleWith(TWITCH)
    enabledByDefault(false)
    settings(twitchSettings, section(TwitchStrings.sectionAds, AdsSettings.hideSponsoredChannelSkins))

    execute {
        sponsoredViewIds.forEach { idName -> keepGoneById(idName, AdsSettings.hideSponsoredChannelSkins) }
    }
}
