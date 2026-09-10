package com.paqrap.logistics.common.enums;

import lombok.Getter;

/**
 * Enum for order status.
 */
@Getter
public enum OrderStatus {
    REGISTRADO("Registrado"),
    PLANIFICADO("Planificado"),
    EN_RUTA("En Ruta"),
    ENTREGADO("Entregado"),
    REASIGNADO("Reasignado"),
    CANCELADO("Cancelado");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Valida si se puede transicionar a otro estado.
     */
    public boolean canTransitionTo(OrderStatus target) {
        if (target == null) return false;
        switch (this) {
            case REGISTRADO: return target == PLANIFICADO || target == CANCELADO;
            case PLANIFICADO: return target == EN_RUTA || target == REASIGNADO;
            case EN_RUTA: return target == ENTREGADO || target == REASIGNADO;
            case REASIGNADO: return target == PLANIFICADO;
            default: return false;
        }
    }
}
