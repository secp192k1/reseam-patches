package dev.secp192k1.patches.twitch

import app.reseam.patch.ExtClass
import app.reseam.patch.PointMatch
import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.before
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.reserveLocal
import app.reseam.patch.settings.section
import dev.secp192k1.patches.settings.whenChosen

private const val SPANNED = "android.text.Spanned"
private const val SPANNED_STRING = "android.text.SpannedString"
private const val CROSS_OUT = "cross-out"
private const val SPOILER = "spoiler"

private object DeletedMessages : ExtClass("dev.secp192k1.twitch.deletedmessages.DeletedMessages") {
    val deletedLabelId by static(returns = Type.Int)
    val crossOutMessage by static(SPANNED_STRING, Type.Context, returns = SPANNED)
    val crossOutBody by static(SPANNED_STRING, SPANNED_STRING, returns = SPANNED_STRING)
}

val showDeletedMessages = patch("Show deleted messages") {
    description("Shows chat messages that moderators deleted.")
    compatibleWith(TWITCH)
    settings(twitchSettings, section(chatPage, "Deleted messages", TwitchSettings.deletedMessages))

    execute {
        val deletedLabelId = resources.id("string", "chat_message_deleted")?.toLong() ?: error("The app has no chat_message_deleted string")
        // Replaces a message on screen when a moderator deletes it
        val deletedMessageFactory = method("deletedMessageFactory") {
            literals(deletedLabelId)
            returns(SPANNED)
        }
        // Formats a message that arrives already deleted, such as one from chat history
        val chatMessageFormatter = method("chatMessageFormatter") {
            literals(deletedLabelId)
            hasParam("java.lang.Long")
        }

        // Twitch reveals a deleted message on tap only when the span records moderator access
        val deletedMessageSpan = deletedMessageFactory.point("deleted message span") { deletedMessageSpanConstructor() }.callee()
        val moderatorAccess = deletedMessageSpan.point("moderator access") { field { type(Type.Boolean) } }.field()
        deletedMessageSpan.after {
            whenChosen(TwitchSettings.deletedMessages, SPOILER) { thisObject.set(moderatorAccess, bool(true)) }
        }

        DeletedMessages.deletedLabelId.implement { returnValue(int(deletedLabelId.toInt())) }
        deletedMessageFactory.before {
            whenChosen(TwitchSettings.deletedMessages, CROSS_OUT) {
                returnValue(call(DeletedMessages.crossOutMessage, paramOfType(SPANNED_STRING), paramOfType(Type.Context)))
            }
        }

        val originalMessage = chatMessageFormatter.reserveLocal("originalMessage", SPANNED_STRING)
        val formattedMessageSpan = chatMessageFormatter.point("deleted message span") { deletedMessageSpanConstructor() }
        formattedMessageSpan.captureArgumentAs("originalMessage", 2).before {
            local(originalMessage).assign(capture("originalMessage"))
        }
        formattedMessageSpan.next { invokeDirect { owner(SPANNED_STRING); name("<init>") } }
            .captureArgumentAs("placeholder", 0, SPANNED_STRING)
            .after {
                whenChosen(TwitchSettings.deletedMessages, CROSS_OUT) {
                    capture("placeholder").assign(call(DeletedMessages.crossOutBody, local(originalMessage), capture("placeholder")))
                }
            }
    }
}

// The clickable span Twitch puts on a deleted message: message ID, original message, moderator access, event dispatcher
private fun PointMatch.deletedMessageSpanConstructor() = invokeDirect {
    name("<init>")
    params(Type.String, SPANNED_STRING, Type.Boolean, "tv.twitch.android.core.mvp.viewdelegate.EventDispatcher")
}
