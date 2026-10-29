package dev.secp192k1.patches.speedtest.ads

import app.reseam.patch.Type
import app.reseam.patch.alwaysReturn
import app.reseam.patch.method
import app.reseam.patch.patch
import dev.secp192k1.patches.speedtest.SPEEDTEST

// The ads manager reports 1 for an ad free user and 2 when ads may show
private const val ADS_FREE = 1

// The banner, the ad after each test and the ad SDK startup all follow the state this method works out
val adsState = method("adsState") {
    strings("isCoreAdsFree:Boolean")
    returns(Type.Int)
}

@Suppress("unused")
val hideAds = patch("Hide ads") {
    description("Removes the banner ad and the ad shown with each test result.")
    compatibleWith(SPEEDTEST)

    execute {
        adsState.alwaysReturn(ADS_FREE)
    }
}
