package dev.secp192k1.patches.trainstatus.ads

import app.reseam.patch.Type
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import dev.secp192k1.patches.trainstatus.TRAINSTATUS
import dev.secp192k1.patches.trainstatus.settings.trainStatusSettings
import dev.secp192k1.patches.trainstatus.shared.mainActivityCreate
import dev.secp192k1.trainstatus.settings.AdsSettings
import dev.secp192k1.trainstatus.settings.TrainStatusStrings

private const val VIEW_GONE = 8
private const val AD_VIEW = "Lcom/google/android/gms/ads/AdView;"

// Every screen loads its banner right after it finds its AdView, so the load method hides them all
val adViewCast = mainActivityCreate.point("adViewCast") {
    checkCast(AD_VIEW)
}

// The request type is obfuscated, so the load is the call made on the AdView field
val bannerLoad = adViewCast.next {
    invokeVirtual { paramCount(1); returns(Type.Void) }
    argument(0) { field { type(AD_VIEW) } }
}.callee()

// R8 keeps the name of this public ads SDK class
val mobileAds = klass("com.google.android.gms.ads.MobileAds")

val adSdkStart = method("adSdkStart") {
    inClass(mobileAds)
    paramCount(2)
}

@Suppress("unused")
val hideAds = patch("Hide ads") {
    description("Removes the ad banners from the search, train and station screens.")
    compatibleWith(TRAINSTATUS)
    settings(trainStatusSettings, section(TrainStatusStrings.sectionAds, AdsSettings.hideAds))

    execute {
        gate(AdsSettings.hideAds) {
            bannerLoad.before {
                thisObject.callVirtual("android.view.View", "setVisibility", "(I)V", int(VIEW_GONE))
                returnVoid()
            }
            adSdkStart.alwaysReturn()
        }
    }
}
