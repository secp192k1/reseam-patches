package dev.secp192k1.patches.twitch

import app.reseam.patch.Type
import app.reseam.patch.before
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.methods
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.whenOn

private val BRANCH_SERVERS = listOf("https://api2.branch.io/", "https://cdn.branch.io/")

@Suppress("unused")
val blockTrackers = patch("Block trackers") {
    compatibleWith(TWITCH)
    settings(twitchSettings, section(privacyPage, "Trackers", TwitchSettings.blockTrackers))

    execute {
        klass("com.comscore.Analytics").method("start") { params(Type.Context) }
            .before { whenOn(TwitchSettings.blockTrackers) { returnVoid() } }

        // Sentry skips its whole setup, native and React Native included, when its options report it disabled
        klass("io.sentry.SentryOptions").method("isEnabled") { paramCount(0) }
            .before { whenOn(TwitchSettings.blockTrackers) { returnFalse() } }

        // Crashlytics and Firebase Sessions keep their reports on the device while the data collection switch reads off
        klass("com.google.firebase.crashlytics.FirebaseCrashlytics").method("isCrashlyticsCollectionEnabled")
            .point("data collection switch") { invokeVirtual { returns(Type.Boolean) } }.callee()
            .before { whenOn(TwitchSettings.blockTrackers) { returnFalse() } }

        methods("sprigConfigure") {
            inClass(klass("com.userleap.UserLeap"))
            name("configure")
        }.forEach { before { whenOn(TwitchSettings.blockTrackers) { returnVoid() } } }

        // Sends Twitch's Nielsen audience pings
        method("nielsenPing") {
            calls {
                owner($$"tv.twitch.android.shared.nielsen.NielsenS2SApi$NielsenS2SService")
                name("firePing")
            }
        }.before { whenOn(TwitchSettings.blockTrackers) { returnVoid() } }

        val cutOffUrls = BRANCH_SERVERS.sumOf { server -> cutOffServer(server, TwitchSettings.blockTrackers) }
        require(cutOffUrls > 0) { "The app no longer references the Branch servers" }
        log.info("Blocked comScore, Nielsen, Sentry, Crashlytics, Sprig and $cutOffUrls Branch URLs")
    }
}
