package com.odiparpack.logistics.simulation.dto;

import com.odiparpack.logistics.simulation.model.SimulationScenario;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SimulationConfigDTO {
    @Positive
    private int autoCount;
    @Positive
    private int motoCount;
    @Positive
    private int bicycletaCount;
    @Positive
    private int warehouseCapacity;
    @Positive
    private double semaphoreGreenHours;
    @Positive
    private double semaphoreAmberHours;
    private double collapseDemandFactor;
    private String ordersFilePath;
    
    @NotNull
    private SimulationScenario scenario;
    private double speedFactor;
}
