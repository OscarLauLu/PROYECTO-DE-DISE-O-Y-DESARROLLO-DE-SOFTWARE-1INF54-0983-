package com.paqrap.logistics.common.enums;

import lombok.Getter;

/**
 * Enum for delivery types.
 */
@Getter
public enum DeliveryType {
    REGULAR_36H(36, "Regular 36h"),
    PRIORIZADA_4H(4, "Priorizada 4h"),
    PRIORIZADA_8H(8, "Priorizada 8h"),
    PRIORIZADA_12H(12, "Priorizada 12h"),
    PRIORIZADA_18H(18, "Priorizada 18h");

    private final int hours;
    private final String displayName;

    DeliveryType(int hours, String displayName) {
        this.hours = hours;
        this.displayName = displayName;
    }

    /**
     * Verifica si la entrega es priorizada.
     */
    public boolean isPrioritized() {
        return this != REGULAR_36H;
    }
}
