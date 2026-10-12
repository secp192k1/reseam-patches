package dev.secp192k1.patches.twitch

import app.reseam.patch.Type
import app.reseam.patch.before
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.whenOn

@Suppress("unused")
val hideTurboUpsells = patch("Hide Turbo upsells") {
    description("Removes the Turbo offers: the Go Ad-Free buttons, the offer at app start and after sign up, and the GO AD FREE badge in the account menu.")
    compatibleWith(TWITCH)
    settings(twitchSettings, section(adsPage, "Turbo", TwitchSettings.hideTurboUpsells))

    execute {
        // Reads every string experiment, from the experiment name and its default value
        method("stringExperimentValue") {
            strings("mobile_mandatory_email_verification")
            returns(Type.Object)
            paramCount(5)
        }.before {
            // Its control group gets no Go Ad-Free button in Following, Home or the profile
            whenTrue(string("Android-Turbo-Ingress").callVirtual(Type.String, "equals", "(Ljava/lang/Object;)Z", param(1))) {
                whenOn(TwitchSettings.hideTurboUpsells) { returnValue(param(4)) }
            }
        }

        // A suspend function may return its result at once instead of suspending
        // Decides by free trial eligibility whether the Turbo offer opens at app start and after sign up
        method("turboFreeTrialEligibility") {
            hasParam("tv.twitch.android.models.commerce.TurboPurchaseScreen")
            returns(Type.Object)
            paramCount(2)
        }.before {
            whenOn(TwitchSettings.hideTurboUpsells) { returnValue(callStatic("java.lang.Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", bool(false))) }
        }

        // The account menu draws the badge only when its first parameter is set
        val turboBadge = resources.id("string", "viewer_account_menu_turbo_upsell")?.toLong() ?: error("The app has no viewer_account_menu_turbo_upsell string")
        method("accountMenuTurboItem") { literals(turboBadge) }.before {
            whenOn(TwitchSettings.hideTurboUpsells) { param(0).assign(bool(false)) }
        }
    }
}
