package com.paqrap.logistics.pedidos.model;

import lombok.Getter;

/**
 * Estados del ciclo de vida de un pedido (RF-33).
 */
@Getter
public enum EstadoPedido {
    REGISTRADO("Registrado"),
    PLANIFICADO("Planificado en Ruta"),
    EN_RUTA("En Ruta de Entrega"),
    ENTREGADO("Entregado con Éxito"),
    CANCELADO("Cancelado");

    private final String descripcion;

    EstadoPedido(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean esModificable() {
        return this == REGISTRADO;
    }

    public boolean esFinal() {
        return this == ENTREGADO || this == CANCELADO;
    }
}
