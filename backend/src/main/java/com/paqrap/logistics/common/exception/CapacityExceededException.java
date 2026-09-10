package com.paqrap.logistics.common.exception;

import lombok.Getter;

/**
 * Exception thrown when vehicle capacity is exceeded.
 */
@Getter
public class CapacityExceededException extends BusinessException {
    private final Long vehicleId;
    private final int currentLoad;
    private final int requestedLoad;
    private final int maxCapacity;

    public CapacityExceededException(Long vehicleId, int currentLoad, int requestedLoad, int maxCapacity) {
        super(String.format("Capacidad excedida para vehículo %d. Carga actual: %d, Solicitado: %d, Capacidad máxima: %d", vehicleId, currentLoad, requestedLoad, maxCapacity), "CAPACITY_EXCEEDED");
        this.vehicleId = vehicleId;
        this.currentLoad = currentLoad;
        this.requestedLoad = requestedLoad;
        this.maxCapacity = maxCapacity;
    }
}
