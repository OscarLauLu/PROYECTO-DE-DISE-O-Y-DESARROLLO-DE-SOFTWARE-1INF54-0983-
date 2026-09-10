package com.paqrap.logistics.simulacion.model;

import lombok.Getter;

/**
 * Escenarios de simulación soportados por el motor logístico (RF-64, RF-65, RF-66, RF-67).
 */
@Getter
public enum TipoEscenario {
    DIA_A_DIA("Operación día a día en tiempo real (1s real = 1s simulado)"),
    SIMULACION_5D("Simulación acelerada de 5 días (ejecutada en 30 a 60 minutos)"),
    COLAPSO_LOGISTICO("Escenario de colapso logístico con incremento progresivo de demanda");

    private final String descripcion;

    TipoEscenario(String descripcion) {
        this.descripcion = descripcion;
    }
}
