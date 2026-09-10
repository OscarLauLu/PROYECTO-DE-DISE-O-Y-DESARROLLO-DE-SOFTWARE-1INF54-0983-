package com.odiparpack.logistics.pedidos.model;

import lombok.Getter;

/**
 * Niveles de criticidad tipo semáforo según holgura temporal del pedido (RF-03, RF-53).
 */
@Getter
public enum NivelCriticidad {
    VERDE("Verde - Holgura amplia", "#28A745"),
    AMBAR("Ámbar - Holgura moderada", "#FFC107"),
    ROJO("Rojo - Holgura crítica / Plazo próximo", "#DC3545");

    private final String descripcion;
    private final String codigoColor;

    NivelCriticidad(String descripcion, String codigoColor) {
        this.descripcion = descripcion;
        this.codigoColor = codigoColor;
    }
}
