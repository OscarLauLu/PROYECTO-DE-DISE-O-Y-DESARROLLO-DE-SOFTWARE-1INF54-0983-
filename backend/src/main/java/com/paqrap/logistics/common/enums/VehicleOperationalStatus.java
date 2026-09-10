package com.paqrap.logistics.common.enums;

import lombok.Getter;

/**
 * Enum for vehicle operational status.
 */
@Getter
public enum VehicleOperationalStatus {
    DISPONIBLE("Disponible", "green"),
    EN_RUTA("En Ruta", "blue"),
    EN_MANTENIMIENTO("En Mantenimiento", "orange"),
    AVERIADA("Averiada", "red");

    private final String displayName;
    private final String color;

    VehicleOperationalStatus(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }
}
