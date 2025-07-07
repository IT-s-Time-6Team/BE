package com.team6.team6.global.log;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public enum LogMarker {
    REQUEST("REQUEST"),
    OPEN_AI("OPEN_AI"),

    KEYWORD_ANALYSIS("KEYWORD_ANALYSIS", OPEN_AI),
    QUESTION_GENERATION("QUESTION_GENERATION", OPEN_AI);

    private final Marker marker;

    LogMarker(String name) {
        this.marker = MarkerFactory.getMarker(name);
    }

    LogMarker(String name, LogMarker parent) {
        this.marker = MarkerFactory.getMarker(name);
        this.marker.add(parent.getMarker());
    }

    public Marker getMarker() {
        return marker;
    }
}
