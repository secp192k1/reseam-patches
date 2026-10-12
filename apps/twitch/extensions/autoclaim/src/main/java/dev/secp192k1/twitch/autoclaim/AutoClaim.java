package dev.secp192k1.twitch.autoclaim;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;

public final class AutoClaim {
    // A claim that failed is sent again on a later update after this long.
    private static final long RETRY_MILLISECONDS = 60 * 1000;

    private static final Handler mainThread = new Handler(Looper.getMainLooper());

    private static String lastClaimId;
    private static long lastClaimTime;
    private static String lastDropInstanceId;
    private static long lastDropClaimTime;

    private AutoClaim() {}

    // Both chat layouts recompute the button on every update, so the same bonus arrives many times.
    public static synchronized boolean shouldClaim(String claimId) {
        long now = SystemClock.elapsedRealtime();
        if (claimId.equals(lastClaimId) && now - lastClaimTime < RETRY_MILLISECONDS) return false;
        lastClaimId = claimId;
        lastClaimTime = now;
        return true;
    }

    // Called on the main thread, where the Drop notification draws itself, so it needs no lock.
    public static void claimDrop(View claimButton, View.OnClickListener claimListener, String dropInstanceId) {
        long now = SystemClock.elapsedRealtime();
        if (dropInstanceId.equals(lastDropInstanceId) && now - lastDropClaimTime < RETRY_MILLISECONDS) return;
        lastDropInstanceId = dropInstanceId;
        lastDropClaimTime = now;
        // The notification may start listening for the tap only after it is drawn, so the tap runs once drawing is done.
        mainThread.post(() -> claimListener.onClick(claimButton));
    }
}
