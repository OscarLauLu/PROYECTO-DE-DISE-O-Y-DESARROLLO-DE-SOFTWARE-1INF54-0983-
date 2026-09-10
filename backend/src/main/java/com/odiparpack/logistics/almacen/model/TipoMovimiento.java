package com.odiparpack.logistics.almacen.model;

import lombok.Getter;

/**
 * Tipos de movimiento de inventario en los almacenes (RF-21, RF-23).
 */
@Getter
public enum TipoMovimiento {
    CARGA("Carga de inventario"),
    DESCARGA("Descarga / Despacho hacia ruta"),
    RECARGA_DIARIA("Recarga diaria automática (23:59:59)"),
    LIBERACION_RESERVA("Liberación de stock por cancelación");

    private final String descripcion;

    TipoMovimiento(String descripcion) {
        this.descripcion = descripcion;
    }
}
