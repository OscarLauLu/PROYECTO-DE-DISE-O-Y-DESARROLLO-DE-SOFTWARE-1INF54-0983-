package com.odiparpack.logistics.common.enums;

import lombok.Getter;

/**
 * Enum for criticality levels.
 */
@Getter
public enum CriticalityLevel {
    VERDE("#00FF00", "Verde"),
    AMBAR("#FFA500", "Ámbar"),
    ROJO("#FF0000", "Rojo");

    private final String color;
    private final String displayName;

    CriticalityLevel(String color, String displayName) {
        this.color = color;
        this.displayName = displayName;
    }
}
