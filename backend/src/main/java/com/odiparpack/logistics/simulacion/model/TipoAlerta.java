package com.odiparpack.logistics.simulacion.model;

import lombok.Getter;

/**
 * Tipos de alerta generadas por el sistema (RF-16, RF-25, RF-45).
 */
@Getter
public enum TipoAlerta {
    UNIDAD_AVERIADA("Unidad de transporte averiada"),
    OCUPACION_ALMACEN("Ocupación de almacén intermedio superó umbral crítico (90%)"),
    PLAZO_INCUMPLIDO("Riesgo o incumplimiento de ventana de entrega de pedido"),
    VIA_BLOQUEADA("Tramo vial cerrado por bloqueo programado");

    private final String descripcion;

    TipoAlerta(String descripcion) {
        this.descripcion = descripcion;
    }
}
