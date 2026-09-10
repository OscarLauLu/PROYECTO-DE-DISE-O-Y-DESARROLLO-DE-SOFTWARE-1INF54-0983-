package com.paqrap.logistics.common.enums;

import java.math.BigDecimal;
import lombok.Getter;

/**
 * Enum for vehicle types.
 */
@Getter
public enum VehicleType {
    AUTO(40, new BigDecimal("8.00"), 24, "Auto"),
    MOTO(25, new BigDecimal("6.00"), 8, "Moto"),
    BICICLETA(12, new BigDecimal("3.00"), 4, "Bicicleta");

    private final int defaultSpeed;
    private final BigDecimal defaultCostPerKm;
    private final int defaultCapacity;
    private final String displayName;

    VehicleType(int defaultSpeed, BigDecimal defaultCostPerKm, int defaultCapacity, String displayName) {
        this.defaultSpeed = defaultSpeed;
        this.defaultCostPerKm = defaultCostPerKm;
        this.defaultCapacity = defaultCapacity;
        this.displayName = displayName;
    }
}
