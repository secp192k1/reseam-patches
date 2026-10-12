package dev.secp192k1.twitch.embeddedads;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

public final class EmbeddedAds {
    private static final String LIVE_PLAYLIST_PREFIX = "https://usher.ttvnw.net/api/";
    private static final String CHANNEL_PATH = "/channel/hls/";
    private static final String PLAYLIST_EXTENSION = ".m3u8";
    // The proxy reads the playlist query encoded into its path.
    private static final String PROXY_QUERY = "%3Fallow_source%3Dtrue%26allow_audio_only%3Dtrue%26fast_bread%3Dtrue";
    private static final long FALLBACK_MILLISECONDS = 5 * 60 * 1000;
    private static final int NOT_FOUND = 404;
    private static final String DEFAULT_PROXY = "https://eu.luminous.dev";
    // An https base URL without a query, the form the proxy setting asks for.
    private static final Pattern PROXY_URL = Pattern.compile("https://[A-Za-z0-9.-]+(:[0-9]+)?(/[A-Za-z0-9._~/-]*)?");

    // Playlists load from Twitch until this time, so the player's retry after a proxy failure succeeds.
    private static volatile long proxyPausedUntil;

    private EmbeddedAds() {}

    public static String proxyUrl() {
        return "";
    }

    // The class loader builds the prefix on first use, so later requests take no lock.
    private static final class Proxy {
        static final String PLAYLIST_PREFIX = proxyBase() + "/playlist/";
    }

    // The proxy setting without trailing slashes, or the default proxy when the setting is not a valid base URL.
    private static String proxyBase() {
        String proxy = proxyUrl().trim();
        int end = proxy.length();
        while (end > 0 && proxy.charAt(end - 1) == '/') end--;
        proxy = proxy.substring(0, end);
        return PROXY_URL.matcher(proxy).matches() ? proxy : DEFAULT_PROXY;
    }

    public static String playlistUrl(String url) {
        int channelStart = proxiedChannelStart(url);
        if (channelStart < 0) return url;
        String channel = url.substring(channelStart, url.indexOf(PLAYLIST_EXTENSION, channelStart));
        return Proxy.PLAYLIST_PREFIX + channel + PLAYLIST_EXTENSION + PROXY_QUERY;
    }

    public static Map<String, String> playlistHeaders(String url, Map<String, String> headers) {
        if (proxiedChannelStart(url) < 0) return headers;
        return Collections.emptyMap();
    }

    public static void onResponse(int status, String url) {
        if (status >= 400 && status != NOT_FOUND && isProxied(url)) pauseProxy();
    }

    public static void onFailure(String url) {
        if (isProxied(url)) pauseProxy();
    }

    // Start of the channel name in a live playlist URL the proxy should serve, or a negative index to leave the URL alone.
    private static int proxiedChannelStart(String url) {
        if (url == null || !url.startsWith(LIVE_PLAYLIST_PREFIX)) return -1;
        int channelPath = url.indexOf(CHANNEL_PATH, LIVE_PLAYLIST_PREFIX.length() - 1);
        if (channelPath < 0) return -1;
        int channelStart = channelPath + CHANNEL_PATH.length();
        if (url.indexOf(PLAYLIST_EXTENSION, channelStart) <= channelStart) return -1;
        if (System.currentTimeMillis() < proxyPausedUntil) return -1;
        return channelStart;
    }

    private static boolean isProxied(String url) {
        return url != null && url.startsWith(Proxy.PLAYLIST_PREFIX);
    }

    private static void pauseProxy() {
        proxyPausedUntil = System.currentTimeMillis() + FALLBACK_MILLISECONDS;
    }
}
