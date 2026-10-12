package dev.secp192k1.patches.twitch

import app.reseam.patch.ExtClass
import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.before
import app.reseam.patch.field
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.valueOf
import dev.secp192k1.patches.settings.whenOn

private const val IVS_REQUEST = "com.amazonaws.ivs.net.Request"
private const val IVS_RESPONSE = "com.amazonaws.ivs.net.Response"

private object EmbeddedAds : ExtClass("dev.secp192k1.twitch.embeddedads.EmbeddedAds") {
    val proxyUrl by static(returns = Type.String)
    val playlistUrl by static(Type.String, returns = Type.String)
    val playlistHeaders by static(Type.String, Type.Map, returns = Type.Map)
    val onResponse by static(Type.Int, Type.String)
    val onFailure by static(Type.String)
}

// The player loads streams through the IVS network classes, which keep their names because native code calls them
private val ivsRequest = klass(IVS_REQUEST)
private val requestUrl = ivsRequest.method("getUrl")
private val requestHeaders = ivsRequest.method("getHeaders")
private val originalUrl = ivsRequest.field("url")
private val responseConstructor = klass(IVS_RESPONSE).method("<init>") { params(Type.Int, Type.String) }
private val responseCallback = klass($$"com.amazonaws.ivs.net.OkHttpClient$OkHttpResponseCallback")
private val requestFailure = responseCallback.method("onFailure")
private val failedRequest = responseCallback.field("request")

@Suppress("unused")
val blockEmbeddedAds = patch("Block embedded ads") {
    description("Loads streams through a proxy in a region without ads. Fallbacks to normal stream for a few minutes when the proxy fails.")
    compatibleWith(TWITCH)
    settings(twitchSettings, section(adsPage, "Embedded ads", TwitchSettings.blockEmbeddedAds, TwitchSettings.embeddedAdsProxy))

    execute {
        EmbeddedAds.proxyUrl.implement { returnValue(valueOf(TwitchSettings.embeddedAdsProxy)) }

        requestUrl.after {
            whenOn(TwitchSettings.blockEmbeddedAds) { returnValue(call(EmbeddedAds.playlistUrl, capture("result"))) }
        }

        // The proxy gets none of the player's headers, the same way it gets none of its tokens
        requestHeaders.after {
            whenOn(TwitchSettings.blockEmbeddedAds) {
                returnValue(call(EmbeddedAds.playlistHeaders, thisObject.field(originalUrl), capture("result")))
            }
        }

        // Only proxied requests count, so these stay quiet while the setting is off
        responseConstructor.after { call(EmbeddedAds.onResponse, param(0), param(1)) }
        requestFailure.before { call(EmbeddedAds.onFailure, thisObject.field(failedRequest).call(requestUrl)) }
    }
}
