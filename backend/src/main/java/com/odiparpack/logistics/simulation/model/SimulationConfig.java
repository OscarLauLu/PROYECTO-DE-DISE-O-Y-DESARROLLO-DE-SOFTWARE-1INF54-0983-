package com.odiparpack.logistics.simulation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for the simulation engine.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationConfig {
    private int autoCount;
    private int motoCount;
    private int bicycletaCount;
    private int warehouseCapacity;
    private double semaphoreGreenHours;
    private double semaphoreAmberHours;
    private double collapseDemandFactor;
    private String ordersFilePath;
    private SimulationScenario scenario;
    private double speedFactor;
}
