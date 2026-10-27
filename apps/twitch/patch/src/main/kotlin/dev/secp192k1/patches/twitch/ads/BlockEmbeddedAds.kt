package dev.secp192k1.patches.twitch.ads

import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.before
import app.reseam.patch.field
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import app.reseam.patch.settings.valueOf
import dev.secp192k1.patches.twitch.TWITCH
import dev.secp192k1.patches.twitch.settings.twitchSettings
import dev.secp192k1.twitch.embeddedads.bindings.EmbeddedAds
import dev.secp192k1.twitch.settings.AdsSettings
import dev.secp192k1.twitch.settings.TwitchStrings

// The player loads streams through the IVS network classes, which keep their names because native code calls them
val ivsRequest = klass("com.amazonaws.ivs.net.Request")
val ivsResponse = klass("com.amazonaws.ivs.net.Response")
val ivsResponseCallback = klass($$"com.amazonaws.ivs.net.OkHttpClient$OkHttpResponseCallback")

val requestUrl = ivsRequest.method("getUrl")
val requestHeaders = ivsRequest.method("getHeaders")
val requestUrlField = ivsRequest.field("url")
val responseCreate = ivsResponse.method("<init>") { params(Type.Int, Type.String) }
val requestFailure = ivsResponseCallback.method("onFailure")
val callbackRequest = ivsResponseCallback.field("request")

@Suppress("unused")
val blockEmbeddedAds = patch("Block embedded ads") {
    description("Loads streams through a proxy in a region without ads, and from Twitch for a few minutes after the proxy fails.")
    compatibleWith(TWITCH)
    settings(twitchSettings, section(TwitchStrings.sectionAds, AdsSettings.blockEmbeddedAds, AdsSettings.embeddedAdsProxy))

    execute {
        EmbeddedAds.proxyUrl.implement { returnValue(valueOf(AdsSettings.embeddedAdsProxy)) }

        gate(AdsSettings.blockEmbeddedAds) {
            requestUrl.after { returnValue(EmbeddedAds.playlistUrl(result)) }

            // The proxy gets none of the player's headers, the same way it gets none of its tokens
            requestHeaders.after { returnValue(EmbeddedAds.playlistHeaders(thisObject.field(requestUrlField), result)) }
        }

        // Only proxied requests count, so these stay quiet while the setting is off
        responseCreate.after { EmbeddedAds.onResponse(param(0), param(1)) }
        requestFailure.before { EmbeddedAds.onFailure(thisObject.field(callbackRequest).call(requestUrl)) }
    }
}
