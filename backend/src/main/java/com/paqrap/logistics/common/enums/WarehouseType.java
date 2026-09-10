package com.paqrap.logistics.common.enums;

import lombok.Getter;

/**
 * Enum for warehouse types.
 */
@Getter
public enum WarehouseType {
    CENTRAL(Integer.MAX_VALUE),
    INTERMEDIO(1000);

    private final int maxCapacity;

    WarehouseType(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
}
