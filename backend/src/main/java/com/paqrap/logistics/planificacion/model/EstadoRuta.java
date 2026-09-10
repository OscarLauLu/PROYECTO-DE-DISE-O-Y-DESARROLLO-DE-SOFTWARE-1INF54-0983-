package com.paqrap.logistics.planificacion.model;

import lombok.Getter;

/**
 * Estados operacionales de una ruta de entrega.
 */
@Getter
public enum EstadoRuta {
    PLANIFICADA("Planificada"),
    EN_EJECUCION("En Ejecución"),
    REPLANIFICADA("Replanificada por contingencia"),
    COMPLETADA("Completada"),
    CON_ALERTA("Con Alerta de retraso o bloqueo");

    private final String descripcion;

    EstadoRuta(String descripcion) {
        this.descripcion = descripcion;
    }
}
