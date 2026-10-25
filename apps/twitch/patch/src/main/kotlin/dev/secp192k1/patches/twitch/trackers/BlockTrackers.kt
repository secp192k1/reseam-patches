package dev.secp192k1.patches.twitch.trackers

import app.reseam.patch.PointsTarget
import app.reseam.patch.Type
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.methods
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.points
import app.reseam.patch.settings.GateScope
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import dev.secp192k1.patches.twitch.TWITCH
import dev.secp192k1.patches.twitch.settings.twitchSettings
import dev.secp192k1.twitch.settings.PrivacySettings
import dev.secp192k1.twitch.settings.TwitchStrings

private const val BRANCH_API = "https://api2.branch.io/"
private const val BRANCH_CDN = "https://cdn.branch.io/"
// Nothing listens on the discard port, so a request sent here fails at once
private const val DISCARD_SERVER = "https://127.0.0.1:9/"

// R8 keeps the names of these public SDK classes
val comScore = klass("com.comscore.Analytics")
val sentryOptions = klass("io.sentry.SentryOptions")
val crashlytics = klass("com.google.firebase.crashlytics.FirebaseCrashlytics")
val sprig = klass("com.userleap.UserLeap")

val comScoreStart = comScore.method("start") { params(Type.Context) }

// Sentry skips its whole setup, native and React Native included, when its options report it disabled
val sentryEnabled = sentryOptions.method("isEnabled") { paramCount(0) }

// Crashlytics and Firebase Sessions keep their reports on the device while the data collection switch reads off
val crashlyticsCollectionSwitch = crashlytics.method("isCrashlyticsCollectionEnabled").point("crashlyticsCollectionSwitch") {
    invokeVirtual { returns(Type.Boolean) }
}.callee()

val sprigConfigureOverloads = sprig.methods("sprigConfigureOverloads") { name("configure") }

// Twitch's R8 rules keep the names of its Retrofit services and their calls, unlike most of its classes
val nielsenPing = method("nielsenPing") {
    calls { owner($$"tv.twitch.android.shared.nielsen.NielsenS2SApi$NielsenS2SService"); name("firePing") }
}

val branchApiUrls = methods("branchApiUsers") { stringsStartingWith(BRANCH_API) }.points("branchApiUrls") { stringContains(BRANCH_API) }
val branchCdnUrls = methods("branchCdnUsers") { stringsStartingWith(BRANCH_CDN) }.points("branchCdnUrls") { stringContains(BRANCH_CDN) }

// Points every URL on the server at the discard port
private fun GateScope.cutOffServer(urls: PointsTarget, server: String): Int {
    val points = urls.all
    require(points.isNotEmpty()) { "The app no longer references $server" }

    points.forEach { url ->
        url.captureAs("url").after {
            val cutOff = capture("url").callVirtual("java.lang.String", "replace", "(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;", string(server), string(DISCARD_SERVER))
            capture("url").assign(cutOff)
        }
    }
    return points.size
}

@Suppress("unused")
val blockTrackers = patch("Block trackers") {
    description("Turns off comScore, Nielsen, Sentry, Crashlytics, Sprig and Branch.")
    compatibleWith(TWITCH)
    settings(twitchSettings, section(TwitchStrings.sectionPrivacy, PrivacySettings.blockTrackers))

    execute {
        gate(PrivacySettings.blockTrackers) {
            comScoreStart.alwaysReturn()
            sentryEnabled.alwaysReturn(false)
            crashlyticsCollectionSwitch.alwaysReturn(false)

            val configureOverloads = sprigConfigureOverloads.all
            require(configureOverloads.isNotEmpty()) { "The app no longer configures Sprig" }
            configureOverloads.forEach { configure -> configure.alwaysReturn() }

            nielsenPing.alwaysReturn()

            val branchUrls = cutOffServer(branchApiUrls, BRANCH_API) + cutOffServer(branchCdnUrls, BRANCH_CDN)
            log.info("Blocked comScore, Nielsen, Sentry, Crashlytics, Sprig and $branchUrls Branch URLs")
        }
    }
}
