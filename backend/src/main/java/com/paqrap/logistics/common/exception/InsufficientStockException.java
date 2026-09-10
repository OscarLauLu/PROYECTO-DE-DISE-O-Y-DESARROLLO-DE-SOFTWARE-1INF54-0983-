package com.paqrap.logistics.common.exception;

import lombok.Getter;

/**
 * Exception thrown when there is insufficient stock.
 */
@Getter
public class InsufficientStockException extends BusinessException {
    private final Long warehouseId;
    private final int requested;
    private final int available;

    public InsufficientStockException(Long warehouseId, int requested, int available) {
        super(String.format("Stock insuficiente en almacén %d. Solicitado: %d, Disponible: %d", warehouseId, requested, available), "INSUFFICIENT_STOCK");
        this.warehouseId = warehouseId;
        this.requested = requested;
        this.available = available;
    }
}
