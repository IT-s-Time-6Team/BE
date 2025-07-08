package com.team6.team6.global.log;

import com.github.loki4j.slf4j.marker.LabelMarker;

public enum LogMarker {
    REQUEST("REQUEST"),
    OPEN_AI("OPEN_AI");

    private final LabelMarker marker;

    LogMarker(String name) {
        this.marker = LabelMarker.of("type", () -> name);
    }

    public LabelMarker getMarker() {
        return marker;
    }
}
