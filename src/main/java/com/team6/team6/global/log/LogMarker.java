package com.team6.team6.global.log;

import com.github.loki4j.slf4j.marker.StructuredMetadataMarker;

public enum LogMarker {
    REQUEST("REQUEST"),
    OPEN_AI("OPEN_AI");

    private final StructuredMetadataMarker marker;

    LogMarker(String name) {
        this.marker = StructuredMetadataMarker.of("type", () -> name);
    }

    public StructuredMetadataMarker getMarker() {
        return marker;
    }
}
