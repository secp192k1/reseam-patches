package dev.secp192k1.patches.trainstatus

import app.reseam.patch.Type
import app.reseam.patch.before
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.whenOn

@Suppress("unused")
val hideAds = patch("Hide ads") {
    compatibleWith(PACKAGE)
    settings(trainStatusSettings, section("Ads", TrainStatusSettings.hideAds))

    execute {
        // Every screen loads its banner right after it finds its AdView, so the load method hides them all
        mainActivity.method("onCreate").point("banner") { checkCast("com.google.android.gms.ads.AdView") }
            .next { invokeVirtual { paramCount(1); returns(Type.Void) } }
            .callee()
            .before {
                whenOn(TrainStatusSettings.hideAds) {
                    // GONE = 8
                    thisObject.callVirtual("android.view.View", "setVisibility", "(I)V", int(8))
                    returnVoid()
                }
            }

        method("adSdkStart") {
            inClass(klass("com.google.android.gms.ads.MobileAds"))
            paramCount(2)
        }.before { whenOn(TrainStatusSettings.hideAds) { returnVoid() } }
    }
}
