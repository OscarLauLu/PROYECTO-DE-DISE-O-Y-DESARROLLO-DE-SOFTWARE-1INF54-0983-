package com.odiparpack.logistics.simulation.dto;

import com.odiparpack.logistics.simulation.model.SimulationScenario;
import com.odiparpack.logistics.simulation.model.SimulationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SimulationStateDTO {
    private String simulationId;
    private SimulationScenario scenario;
    private String simulatedTimeFormatted;
    private LocalDateTime realStartTime;
    private SimulationStatus status;
    private SimulationMetricsDTO metrics;
}
