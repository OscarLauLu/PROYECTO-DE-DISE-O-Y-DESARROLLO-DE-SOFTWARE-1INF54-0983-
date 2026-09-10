package com.paqrap.logistics.flota.model;

import lombok.Getter;

/**
 * Estados operativos de una unidad de transporte (RF-42, RF-51).
 */
@Getter
public enum EstadoOperativo {
    DISPONIBLE("Disponible", "#28A745"),
    EN_RUTA("En Ruta", "#007BFF"),
    EN_MANTENIMIENTO("En Mantenimiento", "#FD7E14"),
    AVERIADA("Averiada", "#DC3545");

    private final String descripcion;
    private final String codigoColor;

    EstadoOperativo(String descripcion, String codigoColor) {
        this.descripcion = descripcion;
        this.codigoColor = codigoColor;
    }
}
