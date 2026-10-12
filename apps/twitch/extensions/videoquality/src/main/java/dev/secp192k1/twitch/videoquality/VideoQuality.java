package dev.secp192k1.twitch.videoquality;

import com.amazonaws.ivs.player.Player;
import com.amazonaws.ivs.player.Quality;
import java.util.Map;
import java.util.WeakHashMap;

public final class VideoQuality {
    // The state number the native player reports for playing, the one CorePlayerImpl maps to Player.State.PLAYING.
    private static final int PLAYING = 3;

    // The playback session each player last started; weak keys let released players go.
    private static final Map<Player, String> startedSessions = new WeakHashMap<>();

    private VideoQuality() {}

    public static String quality() {
        return "";
    }

    // The class loader reads the setting on first use, so later state changes take no lock for it.
    private static final class Preference {
        static final int MAXIMUM_HEIGHT = maximumHeight(quality());
    }

    // Auto leaves the choice to the player; anything unknown counts as the source quality.
    private static int maximumHeight(String quality) {
        switch (quality) {
            case "auto":
                return 0;
            case "1080p":
                return 1080;
            case "720p":
                return 720;
            case "480p":
                return 480;
            case "360p":
                return 360;
            case "160p":
                return 160;
            default:
                return Integer.MAX_VALUE;
        }
    }

    public static void onStateChanged(Player player, int state) {
        if (state != PLAYING || Preference.MAXIMUM_HEIGHT == 0) return;
        // A pause and resume keeps the session, so only a newly loaded stream gets the preferred quality.
        String session = player.getSessionId();
        synchronized (startedSessions) {
            if (session == null || session.equals(startedSessions.get(player))) return;
            startedSessions.put(player, session);
        }
        // A quality the app applied itself, such as one picked earlier, turns auto mode off and stays.
        if (!player.isAutoQualityMode()) return;
        Quality preferred = preferredQuality(player);
        if (preferred != null) player.setQuality(preferred, true);
    }

    // The best quality up to the preferred height, or the lowest one when every quality is higher.
    private static Quality preferredQuality(Player player) {
        int maximumHeight = Preference.MAXIMUM_HEIGHT;
        Quality best = null;
        Quality lowest = null;
        for (Quality quality : player.getQualities()) {
            int height = quality.getHeight();
            // Audio only has no height.
            if (height <= 0) continue;
            if (lowest == null || height < lowest.getHeight()) lowest = quality;
            if (height <= maximumHeight && (best == null || isBetter(quality, best))) best = quality;
        }
        return best != null ? best : lowest;
    }

    private static boolean isBetter(Quality candidate, Quality current) {
        if (candidate.getHeight() != current.getHeight()) return candidate.getHeight() > current.getHeight();
        if (candidate.getFramerate() != current.getFramerate()) return candidate.getFramerate() > current.getFramerate();
        return candidate.getBitrate() > current.getBitrate();
    }
}
