package com.amazonaws.ivs.player;

import java.util.Set;

public interface Player {
    boolean isAutoQualityMode();

    Set<Quality> getQualities();

    String getSessionId();

    void setQuality(Quality quality, boolean adaptive);
}
