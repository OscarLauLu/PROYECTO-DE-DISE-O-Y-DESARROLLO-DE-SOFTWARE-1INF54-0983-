package com.paqrap.logistics.flota.model;

import lombok.Getter;

/**
 * Tipos de avería mecánica de unidades de transporte y sus reglas de reincorporación (RF-14).
 */
@Getter
public enum TipoAveria {
    TIPO_1(1, "Avería Menor: Reparación rápida de 2 horas en el lugar."),
    TIPO_2(2, "Avería Moderada: Lo que ocurra primero entre el siguiente cambio de turno o máx. 4 horas."),
    TIPO_3(3, "Avería Grave: Al menos 2 días de taller; se reincorpora al inicio del turno 15:00-23:00.");

    private final int tipo;
    private final String descripcion;

    TipoAveria(int tipo, String descripcion) {
        this.tipo = tipo;
        this.descripcion = descripcion;
    }
}
