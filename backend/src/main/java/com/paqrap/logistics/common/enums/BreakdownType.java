package com.paqrap.logistics.common.enums;

import lombok.Getter;

/**
 * Enum for vehicle breakdown types.
 */
@Getter
public enum BreakdownType {
    TIPO_1(1, "Avería menor, reparación en 2h"),
    TIPO_2(2, "Avería media, reparación hasta en 4h o siguiente turno"),
    TIPO_3(3, "Avería mayor, reparación al menos 2 días");

    private final int type;
    private final String description;

    BreakdownType(int type, String description) {
        this.type = type;
        this.description = description;
    }
}
