package com.paqrap.logistics.common.enums;

import lombok.Getter;

/**
 * Enum for inventory movement types.
 */
@Getter
public enum MovementType {
    CARGA("Carga"),
    DESCARGA("Descarga");

    private final String displayName;

    MovementType(String displayName) {
        this.displayName = displayName;
    }
}
