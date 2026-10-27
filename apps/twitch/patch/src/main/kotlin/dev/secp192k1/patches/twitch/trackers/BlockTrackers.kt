package dev.secp192k1.patches.twitch.trackers

import app.reseam.patch.Type
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.methods
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.points
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import dev.secp192k1.patches.twitch.TWITCH
import dev.secp192k1.patches.twitch.settings.twitchSettings
import dev.secp192k1.patches.twitch.shared.cutOffServer
import dev.secp192k1.twitch.settings.PrivacySettings
import dev.secp192k1.twitch.settings.TwitchStrings

private const val BRANCH_API = "https://api2.branch.io/"
private const val BRANCH_CDN = "https://cdn.branch.io/"

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
