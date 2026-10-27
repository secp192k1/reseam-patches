@file:JvmName("AdEvents")

package dev.secp192k1.twitch.adevents

import app.reseam.runtime.Export
import org.json.JSONException
import org.json.JSONObject

// The HLS metadata classes for stitched ads, other ad formats, ad tracking, and the picture by picture ad preflight
private val adMetadataClasses = setOf("twitch-stitched-ad", "twitch-maf-ad", "twitch-ad-quartile", "live-video-net-metadata")

@Suppress("unused")
@Export
fun isAdMetadata(metadata: JSONObject?): Boolean = metadata != null && metadata.optString("CLASS") in adMetadataClasses

// The React Native player gets the same metadata as JSON text, which may be malformed
@Suppress("unused")
@Export
fun isAdCue(cue: String?): Boolean {
    if (cue == null) return false
    if (adMetadataClasses.none { adClass -> cue.contains(adClass) }) return false

    return try {
        isAdMetadata(JSONObject(cue))
    } catch (exception: JSONException) {
        false
    }
}
