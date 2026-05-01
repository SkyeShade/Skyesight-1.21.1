package com.skyeshade.skyesight.api;

public enum SkyesightViewStatus {
    CREATED,
    RENDERING,
    READY,
    UNAVAILABLE,
    CLOSED;

    public boolean isRenderable() {
        return this == READY || this == RENDERING;
    }
}