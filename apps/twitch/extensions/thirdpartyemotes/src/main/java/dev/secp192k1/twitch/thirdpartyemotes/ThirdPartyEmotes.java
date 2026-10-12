package dev.secp192k1.twitch.thirdpartyemotes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class ThirdPartyEmotes {
    // Marks the emote id of a third-party emote; the URL of its image follows.
    private static final String EMOTE_ID_PREFIX = "dev.secp192k1.emote:";
    private static final int TIMEOUT_MILLISECONDS = 10_000;

    // Global and channel emotes of the current channel, the channel ones winning; null until the first load.
    private static volatile EmoteTable emotes;
    private static volatile String currentRoom;
    private static volatile Map<String, String> globalEmotes;

    private ThirdPartyEmotes() {}

    public static boolean sevenTvEnabled() {
        return false;
    }

    public static boolean betterTtvEnabled() {
        return false;
    }

    public static boolean frankerFaceZEnabled() {
        return false;
    }

    // The services in order of priority, read once from the settings.
    private static final class Providers {
        static final String[] ENABLED = enabledProviders();

        private static String[] enabledProviders() {
            ArrayList<String> providers = new ArrayList<>(3);
            if (sevenTvEnabled()) providers.add("7tv");
            if (betterTtvEnabled()) providers.add("bttv");
            if (frankerFaceZEnabled()) providers.add("ffz");
            return providers.toArray(new String[0]);
        }
    }

    public static boolean isTextToken(Object token) {
        return false;
    }

    public static String textOf(Object textToken) {
        return null;
    }

    public static Object textToken(String text, Object original) {
        return null;
    }

    public static Object emoteToken(String code, String id) {
        return null;
    }

    // Every chat message carries its channel in room-id, so the emotes follow the channel being watched.
    public static void onMessageTags(Map<String, String> tags) {
        String room = tags.get("room-id");
        if (room == null || room.equals(currentRoom) || Providers.ENABLED.length == 0) return;
        currentRoom = room;
        Thread loader = new Thread(() -> load(room), "ThirdPartyEmotes");
        loader.setDaemon(true);
        loader.start();
    }

    // Splits the text tokens of a chat message around third-party emote codes.
    public static void addEmotes(ArrayList<Object> tokens) {
        EmoteTable table = emotes;
        if (table == null) return;
        for (int index = 0; index < tokens.size(); index++) {
            Object token = tokens.get(index);
            if (!isTextToken(token)) continue;
            String text = textOf(token);
            int length = text.length();
            ArrayList<Object> pieces = null;
            int pieceStart = 0;
            int wordStart = 0;
            while (wordStart < length) {
                int wordEnd = text.indexOf(' ', wordStart);
                if (wordEnd < 0) wordEnd = length;
                String url = wordEnd > wordStart ? table.find(text, wordStart, wordEnd) : null;
                if (url != null) {
                    if (pieces == null) pieces = new ArrayList<>();
                    if (wordStart > pieceStart) pieces.add(textToken(text.substring(pieceStart, wordStart), token));
                    pieces.add(emoteToken(text.substring(wordStart, wordEnd), EMOTE_ID_PREFIX + url));
                    pieceStart = wordEnd;
                }
                wordStart = wordEnd + 1;
            }
            if (pieces == null) continue;
            if (pieceStart < length) pieces.add(textToken(text.substring(pieceStart), token));
            tokens.remove(index);
            tokens.addAll(index, pieces);
            index += pieces.size() - 1;
        }
    }

    public static boolean isThirdPartyEmote(String emoteId) {
        return emoteId != null && emoteId.startsWith(EMOTE_ID_PREFIX);
    }

    public static String urlOf(String emoteId) {
        return emoteId.substring(EMOTE_ID_PREFIX.length());
    }

    private static void load(String room) {
        String[] providers = Providers.ENABLED;
        Map<String, String> global = globalEmotes;
        if (global == null) {
            global = new HashMap<>();
            // The first provider in the list wins, so it is put last.
            for (int index = providers.length - 1; index >= 0; index--) putGlobalEmotes(providers[index], global);
            globalEmotes = global;
        }
        Map<String, String> merged = new HashMap<>(global);
        for (int index = providers.length - 1; index >= 0; index--) putChannelEmotes(providers[index], room, merged);
        if (room.equals(currentRoom)) emotes = new EmoteTable(merged);
    }

    private static void putGlobalEmotes(String provider, Map<String, String> emotes) {
        try {
            switch (provider) {
                case "7tv": {
                    String response = download("https://7tv.io/v3/emote-sets/global");
                    if (response != null) putSevenTvEmotes(new JSONObject(response).optJSONArray("emotes"), emotes);
                    break;
                }
                case "bttv": {
                    String response = download("https://api.betterttv.net/3/cached/emotes/global");
                    if (response != null) putBetterTtvEmotes(new JSONArray(response), emotes);
                    break;
                }
                case "ffz": {
                    String response = download("https://api.betterttv.net/3/cached/frankerfacez/emotes/global");
                    if (response != null) putFrankerFaceZEmotes(new JSONArray(response), emotes);
                    break;
                }
                default:
                    break;
            }
        } catch (IOException | JSONException ignored) {
            // A provider that fails leaves the others in place.
        }
    }

    private static void putChannelEmotes(String provider, String room, Map<String, String> emotes) {
        try {
            switch (provider) {
                case "7tv": {
                    String response = download("https://7tv.io/v3/users/twitch/" + room);
                    if (response == null) break;
                    JSONObject emoteSet = new JSONObject(response).optJSONObject("emote_set");
                    if (emoteSet != null) putSevenTvEmotes(emoteSet.optJSONArray("emotes"), emotes);
                    break;
                }
                case "bttv": {
                    String response = download("https://api.betterttv.net/3/cached/users/twitch/" + room);
                    if (response == null) break;
                    JSONObject user = new JSONObject(response);
                    putBetterTtvEmotes(user.optJSONArray("sharedEmotes"), emotes);
                    putBetterTtvEmotes(user.optJSONArray("channelEmotes"), emotes);
                    break;
                }
                case "ffz": {
                    String response = download("https://api.betterttv.net/3/cached/frankerfacez/users/twitch/" + room);
                    if (response != null) putFrankerFaceZEmotes(new JSONArray(response), emotes);
                    break;
                }
                default:
                    break;
            }
        } catch (IOException | JSONException ignored) {
            // A provider that fails leaves the others in place.
        }
    }

    // Glide animates GIF but not WebP, so animated emotes load as GIF.
    private static void putSevenTvEmotes(JSONArray list, Map<String, String> emotes) {
        if (list == null) return;
        for (int index = 0; index < list.length(); index++) {
            JSONObject emote = list.optJSONObject(index);
            JSONObject data = emote == null ? null : emote.optJSONObject("data");
            JSONObject host = data == null ? null : data.optJSONObject("host");
            String name = emote == null ? "" : emote.optString("name");
            if (host == null || name.isEmpty()) continue;
            String file = sevenTvFile(host.optJSONArray("files"), data.optBoolean("animated") ? "2x.gif" : "2x.png");
            if (file != null) emotes.put(name, "https:" + host.optString("url") + "/" + file);
        }
    }

    private static String sevenTvFile(JSONArray files, String preferred) {
        if (files == null) return null;
        String fallback = null;
        for (int index = 0; index < files.length(); index++) {
            JSONObject file = files.optJSONObject(index);
            String name = file == null ? "" : file.optString("name");
            if (name.equals(preferred)) return name;
            if (name.equals("2x.webp")) fallback = name;
        }
        return fallback;
    }

    private static void putBetterTtvEmotes(JSONArray list, Map<String, String> emotes) {
        if (list == null) return;
        for (int index = 0; index < list.length(); index++) {
            JSONObject emote = list.optJSONObject(index);
            String id = emote == null ? "" : emote.optString("id");
            String code = emote == null ? "" : emote.optString("code");
            if (id.isEmpty() || code.isEmpty()) continue;
            boolean animated = emote.optBoolean("animated", "gif".equals(emote.optString("imageType")));
            emotes.put(code, "https://cdn.betterttv.net/emote/" + id + (animated ? "/2x.gif" : "/2x.png"));
        }
    }

    private static void putFrankerFaceZEmotes(JSONArray list, Map<String, String> emotes) {
        if (list == null) return;
        for (int index = 0; index < list.length(); index++) {
            JSONObject emote = list.optJSONObject(index);
            JSONObject images = emote == null ? null : emote.optJSONObject("images");
            String code = emote == null ? "" : emote.optString("code");
            if (images == null || code.isEmpty()) continue;
            String url = images.optString("2x");
            if (url.isEmpty() || url.equals("null")) url = images.optString("1x");
            if (!url.isEmpty() && !url.equals("null")) emotes.put(code, url);
        }
    }

    private static String download(String address) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(TIMEOUT_MILLISECONDS);
        connection.setReadTimeout(TIMEOUT_MILLISECONDS);
        try {
            // A channel without emotes on a provider answers 404.
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return null;
            try (InputStream input = connection.getInputStream()) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[16 * 1024];
                for (int read = input.read(buffer); read != -1; read = input.read(buffer)) output.write(buffer, 0, read);
                return output.toString("UTF-8");
            }
        } finally {
            connection.disconnect();
        }
    }

    // Open addressing from emote code to image URL, looked up by a range of the message so no word is copied.
    private static final class EmoteTable {
        private final String[] codes;
        private final String[] urls;
        private final int mask;

        EmoteTable(Map<String, String> emotes) {
            int capacity = Integer.highestOneBit(Math.max(emotes.size(), 1) * 2) << 1;
            codes = new String[capacity];
            urls = new String[capacity];
            mask = capacity - 1;
            for (Map.Entry<String, String> emote : emotes.entrySet()) {
                String code = emote.getKey();
                int slot = hash(code, 0, code.length()) & mask;
                while (codes[slot] != null) slot = (slot + 1) & mask;
                codes[slot] = code;
                urls[slot] = emote.getValue();
            }
        }

        String find(String text, int start, int end) {
            int length = end - start;
            for (int slot = hash(text, start, end) & mask; ; slot = (slot + 1) & mask) {
                String code = codes[slot];
                if (code == null) return null;
                if (code.length() == length && text.regionMatches(start, code, 0, length)) return urls[slot];
            }
        }

        private static int hash(String text, int start, int end) {
            int hash = 0;
            for (int index = start; index < end; index++) hash = 31 * hash + text.charAt(index);
            return hash ^ (hash >>> 16);
        }
    }
}
