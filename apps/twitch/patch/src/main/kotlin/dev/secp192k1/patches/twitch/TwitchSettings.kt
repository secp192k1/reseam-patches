package dev.secp192k1.patches.twitch

import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.settings.Choice
import app.reseam.patch.settings.SettingsPage
import app.reseam.patch.settings.choice
import app.reseam.patch.settings.settingsHost
import app.reseam.patch.settings.text
import app.reseam.patch.settings.toggle
import dev.secp192k1.patches.settings.installSettingsEntry

// The settings screen lists its pages by order, and leaves out pages no running patch fills
internal val adsPage = SettingsPage("ads", "Ads", order = 10)
internal val chatPage = SettingsPage("chat", "Chat", order = 20)
internal val rewardsPage = SettingsPage("rewards", "Rewards", order = 25)
internal val playbackPage = SettingsPage("playback", "Playback", order = 30)
internal val homePage = SettingsPage("home", "Home", order = 40)
internal val privacyPage = SettingsPage("privacy", "Privacy", order = 50)
internal val developerPage = SettingsPage("developer", "Developer", order = 60)

// The keys are pinned, so renaming a property keeps what users chose
internal object TwitchSettings {
    val blockVideoAds by toggle(
        "Block video ads",
        summary = "Blocks the video, audio and display ads Twitch requests itself.",
        default = true,
        key = "twitch.block_video_ads",
    )
    val blockEmbeddedAds by toggle(
        "Block embedded ads",
        summary = "Loads live streams through a proxy in a region without ads.",
        default = true,
        key = "twitch.block_embedded_ads",
    )
    val embeddedAdsProxy by text(
        "Ad-free proxy",
        summary = "Base URL of a Luminous or TTV LOL compatible playlist proxy.",
        default = "https://eu.luminous.dev",
        key = "twitch.embedded_ads_proxy",
    )
    val hideTurboUpsells by toggle(
        "Hide Turbo offers",
        summary = "Removes the Go Ad-Free buttons, the Turbo offers and the GO AD FREE badge.",
        default = true,
        key = "twitch.hide_turbo_upsells",
    )
    val deletedMessages by choice(
        "Deleted messages",
        summary = "How chat shows a message a moderator deleted.",
        default = "cross-out",
        choices = listOf(
            Choice("hidden", "Hidden, like Twitch"),
            Choice("cross-out", "Crossed out"),
            Choice("spoiler", "Shown on tap"),
        ),
        key = "twitch.deleted_messages",
    )
    val autoClaimChannelPoints by toggle(
        "Auto claim channel points",
        summary = "Claims the bonus chest as soon as it appears.",
        default = true,
        key = "twitch.auto_claim_channel_points",
    )
    val exactPointsBalance by toggle(
        "Exact points balance",
        summary = "Shows your Channel Points balance and the points you gain in full, such as 12,345 instead of 12.3K.",
        default = true,
        key = "twitch.exact_points_balance",
    )
    val everyAutomaticReward by toggle(
        "Every automatic reward",
        summary = "Lists every automatic Channel Points reward a channel offers, also those Twitch's remote config leaves out.",
        default = true,
        key = "twitch.every_automatic_reward",
    )
    val autoClaimDrops by toggle(
        "Auto claim drops",
        summary = "Claims a Drop as soon as you earn it while watching.",
        default = true,
        key = "twitch.auto_claim_drops",
    )
    val sevenTvEmotes by toggle(
        "7TV emotes",
        summary = "Loads the emotes of each channel from 7tv.io.",
        default = false,
        key = "twitch.emotes_7tv",
    )
    val betterTtvEmotes by toggle(
        "BetterTTV emotes",
        summary = "Loads the emotes of each channel from api.betterttv.net.",
        default = false,
        key = "twitch.emotes_bttv",
    )
    val frankerFaceZEmotes by toggle(
        "FrankerFaceZ emotes",
        summary = "Loads the emotes of each channel through api.betterttv.net.",
        default = false,
        key = "twitch.emotes_ffz",
    )
    val hideHypeTrain by toggle(
        "Hide Hype Train",
        summary = "Removes the Hype Train banner above chat.",
        default = false,
        key = "twitch.hide_hype_train",
    )
    val autoLeaveRaids by toggle(
        "Auto leave raids",
        summary = "Stays on the channel when it raids another one.",
        default = false,
        key = "twitch.auto_leave_raids",
    )
    val videoQuality by choice(
        "Default video quality",
        summary = "The quality a stream, video or clip switches to when it starts on Auto.",
        default = "auto",
        choices = listOf(
            Choice("auto", "Auto"),
            Choice("source", "Source"),
            Choice("1080p", "1080p"),
            Choice("720p", "720p"),
            Choice("480p", "480p"),
            Choice("360p", "360p"),
            Choice("160p", "160p"),
        ),
        key = "twitch.default_video_quality",
    )
    val startOnFollowing by toggle(
        "Start on Following",
        summary = "Opens Home on the Following page.",
        default = true,
        key = "twitch.start_on_following",
    )
    val hideStories by toggle(
        "Hide stories",
        summary = "Removes the stories shelf and the stories button from Home.",
        default = true,
        key = "twitch.hide_stories",
    )
    val blockTrackers by toggle(
        "Block trackers",
        summary = "Turns off comScore, Nielsen, Sentry, Crashlytics, Sprig and Branch.",
        default = true,
        key = "twitch.block_trackers",
    )
    val disableUpdatePrompt by toggle(
        "Hide update prompt",
        summary = "Google Play reports no update to Twitch.",
        default = true,
        key = "twitch.disable_update_prompt",
    )
    val debugMode by toggle(
        "Debug menu",
        summary = "Shows Twitch's staff debug menu in settings.",
        default = false,
        key = "twitch.debug_mode",
    )
}

val twitchSettings = settingsHost("twitch") {
    compatibleWith(TWITCH)

    install { installSettingsEntry(
        // Twitch builds its main settings screen in Compose, from a fixed set of rows
        klass("tv.twitch.android.settings.main.MainSettingsFragmentV2").method("onCreateView")
    ) }
}
