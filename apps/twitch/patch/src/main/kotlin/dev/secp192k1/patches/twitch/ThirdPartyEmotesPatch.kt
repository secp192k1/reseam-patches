package dev.secp192k1.patches.twitch

import app.reseam.patch.ExtClass
import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.before
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.valueOf

private const val EVENT_DISPATCHER = "tv.twitch.android.core.mvp.viewdelegate.EventDispatcher"

private object ThirdPartyEmotes : ExtClass("dev.secp192k1.twitch.thirdpartyemotes.ThirdPartyEmotes") {
    val sevenTvEnabled by static(returns = Type.Boolean)
    val betterTtvEnabled by static(returns = Type.Boolean)
    val frankerFaceZEnabled by static(returns = Type.Boolean)
    val isTextToken by static(Type.Object, returns = Type.Boolean)
    val textOf by static(Type.Object, returns = Type.String)
    val textToken by static(Type.String, Type.Object, returns = Type.Object)
    val emoteToken by static(Type.String, Type.String, returns = Type.Object)
    val onMessageTags by static(Type.Map)
    val addEmotes by static("java.util.ArrayList")
    val isThirdPartyEmote by static(Type.String, returns = Type.Boolean)
    val urlOf by static(Type.String, returns = Type.String)
}

// Parses a chat message from its IRC tags, which name the channel in room-id
private val chatMessageParser = method("chatMessageParser") {
    strings("Could not parse automod flag: ", "emotes")
}

// The token of a Twitch emote in a chat message, before the message is laid out
private val chatEmoteToken = klass("chatEmoteToken") { strings("EmoteToken(text=", ", id=") }

// The emote of a laid out chat message, which the chat draws
private val chatEmoteModel = klass("chatEmoteModel") { strings("EmoteToken(id=") }

// Builds the image URL of an emote from its id
private val emoteUrl = method("emoteUrl") {
    strings("https://static-cdn.jtvnw.net/emoticons/v2/")
    hasParam(Type.Context)
    paramCount(4)
}

// Draws an emote in chat, and makes a tap on it open the Twitch emote card
private val emoteSpan = method("emoteSpan") {
    hasParam(Type.Context)
    hasParam("tv.twitch.android.models.Tuid")
    hasParam("java.lang.Integer")
    hasParam(EVENT_DISPATCHER)
    returns("android.text.SpannableString")
    paramCount(7)
}

@Suppress("unused")
val thirdPartyEmotes = patch("Third-party emotes") {
    description("Shows BetterTTV, FrankerFaceZ and 7TV emotes in chat.")
    compatibleWith(TWITCH)
    settings(
        twitchSettings,
        section(chatPage, "Emotes", TwitchSettings.sevenTvEmotes, TwitchSettings.betterTtvEmotes, TwitchSettings.frankerFaceZEmotes),
    )

    execute {
        ThirdPartyEmotes.sevenTvEnabled.implement { returnValue(valueOf(TwitchSettings.sevenTvEmotes)) }
        ThirdPartyEmotes.betterTtvEnabled.implement { returnValue(valueOf(TwitchSettings.betterTtvEmotes)) }
        ThirdPartyEmotes.frankerFaceZEnabled.implement { returnValue(valueOf(TwitchSettings.frankerFaceZEmotes)) }

        // Several classes print as TextToken; the chat one shares its base class with the emote token
        val tokenBase = chatEmoteToken.classDef.superclass ?: error("The chat emote token has no base class")
        val textTokenClass = klass("chatTextToken") {
            strings("TextToken(text=")
            extends(tokenBase)
        }
        val textTokenConstructor = textTokenClass.method("<init>") { paramCount(2) }
        val emoteTokenConstructor = chatEmoteToken.method("<init>") { paramCount(2) }
        ThirdPartyEmotes.isTextToken.implement {
            whenInstanceOf(param(0), textTokenClass.descriptor) { returnTrue() }
            returnFalse()
        }
        ThirdPartyEmotes.textOf.implement { returnValue(param(0).cast(textTokenClass.descriptor).fieldOfType(Type.String)) }
        ThirdPartyEmotes.textToken.implement {
            // The pieces of a split text keep its AutoMod flags
            val flags = param(1).cast(textTokenClass.descriptor).fieldOfType(textTokenConstructor.parameterTypes[1])
            returnValue(newInstance(textTokenClass.descriptor, textTokenConstructor.proto, param(0), flags))
        }
        ThirdPartyEmotes.emoteToken.implement { returnValue(newInstance(chatEmoteToken.descriptor, emoteTokenConstructor.proto, param(0), param(1))) }

        chatMessageParser.before { call(ThirdPartyEmotes.onMessageTags, paramOfType(Type.Map)) }
        // The tokenizer marks Twitch emotes from the message tags and leaves every other word as text
        val tokenizer = chatMessageParser.point("tokenizer") { invokeVirtual { returns("java.util.ArrayList"); hasParam(Type.String) } }.callee()
        tokenizer.after { call(ThirdPartyEmotes.addEmotes, capture("result")) }

        val emoteUrlDetails = klass(emoteUrl.returnType).method("<init>") { paramCount(2) }
        emoteUrl.after {
            whenTrue(call(ThirdPartyEmotes.isThirdPartyEmote, param(1))) {
                val animation = capture("result").fieldOfType(emoteUrlDetails.parameterTypes[1])
                returnValue(newInstance(emoteUrl.returnType, emoteUrlDetails.proto, call(ThirdPartyEmotes.urlOf, param(1)), animation))
            }
        }

        // The emote card looks emotes up on Twitch, which knows none of these
        val emoteModelId = emoteSpan.point("emote id") { field { owner(chatEmoteModel.descriptor); type(Type.String) } }.field()
        emoteSpan.before {
            whenTrue(call(ThirdPartyEmotes.isThirdPartyEmote, paramOfType(chatEmoteModel.descriptor).field(emoteModelId))) {
                paramOfType(EVENT_DISPATCHER).assign(nullObject)
            }
        }
    }
}
