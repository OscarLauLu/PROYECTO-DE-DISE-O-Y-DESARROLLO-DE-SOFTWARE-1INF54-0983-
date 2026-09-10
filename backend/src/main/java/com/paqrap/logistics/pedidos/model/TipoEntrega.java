package com.paqrap.logistics.pedidos.model;

import lombok.Getter;

/**
 * Tipos de entrega admitidos por el sistema (RF-28).
 * Únicamente 5 valores válidos:
 * - 36 horas para entrega regular
 * - 4, 8, 12 o 18 horas para entrega priorizada
 */
@Getter
public enum TipoEntrega {
    REGULAR_36H(36, "Entrega Regular (36h)"),
    PRIORIZADA_18H(18, "Entrega Priorizada (18h)"),
    PRIORIZADA_12H(12, "Entrega Priorizada (12h)"),
    PRIORIZADA_8H(8, "Entrega Priorizada (8h)"),
    PRIORIZADA_4H(4, "Entrega Priorizada (4h)");

    private final int horasPlazo;
    private final String descripcion;

    TipoEntrega(int horasPlazo, String descripcion) {
        this.horasPlazo = horasPlazo;
        this.descripcion = descripcion;
    }

    public boolean esPriorizada() {
        return this != REGULAR_36H;
    }
}
