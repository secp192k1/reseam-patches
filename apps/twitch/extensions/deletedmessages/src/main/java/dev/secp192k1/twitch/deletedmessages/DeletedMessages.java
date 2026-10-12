package dev.secp192k1.twitch.deletedmessages;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;

public final class DeletedMessages {
    private static final int USERNAME_COLOR = 0xFFADADB8;

    private DeletedMessages() {}

    public static int deletedLabelId() {
        return 0;
    }

    // Marks a message this class crossed out, so a second delete event leaves it as it is.
    private static final class DeletedSpan extends StrikethroughSpan {}

    public static Spanned crossOutMessage(SpannedString message, Context context) {
        if (isCrossedOut(message)) return message;
        SpannableStringBuilder builder = crossOut(message, context.getString(deletedLabelId()));
        int usernameEnd = usernameEnd(message);
        if (usernameEnd > 0) builder.setSpan(new ForegroundColorSpan(USERNAME_COLOR), 0, usernameEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return new SpannedString(builder);
    }

    public static SpannedString crossOutBody(SpannedString body, SpannedString placeholder) {
        if (body == null) return placeholder;
        if (isCrossedOut(body)) return body;
        return new SpannedString(crossOut(body, label(placeholder)));
    }

    private static SpannableStringBuilder crossOut(CharSequence message, CharSequence label) {
        SpannableStringBuilder builder = new SpannableStringBuilder(message);
        int messageEnd = builder.length();
        builder.setSpan(new DeletedSpan(), 0, messageEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(" (").append(label).append(')');
        builder.setSpan(new StyleSpan(Typeface.ITALIC), messageEnd, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    private static boolean isCrossedOut(Spanned message) {
        return message.getSpans(0, message.length(), DeletedSpan.class).length > 0;
    }

    // Twitch shows a deleted message as "<label>", and only the label goes into the suffix.
    private static String label(SpannedString placeholder) {
        String text = placeholder.toString();
        int length = text.length();
        if (length > 1 && text.charAt(0) == '<' && text.charAt(length - 1) == '>') return text.substring(1, length - 1);
        return text;
    }

    // The username is the first clickable span; later ones are links and mentions inside the message.
    private static int usernameEnd(Spanned message) {
        ClickableSpan[] clickableSpans = message.getSpans(0, message.length(), ClickableSpan.class);
        int usernameStart = Integer.MAX_VALUE;
        int usernameEnd = -1;
        for (ClickableSpan clickableSpan : clickableSpans) {
            int spanStart = message.getSpanStart(clickableSpan);
            if (spanStart < usernameStart) {
                usernameStart = spanStart;
                usernameEnd = message.getSpanEnd(clickableSpan);
            }
        }
        return usernameEnd;
    }
}
