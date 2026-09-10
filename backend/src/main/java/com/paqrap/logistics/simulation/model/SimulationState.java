package com.paqrap.logistics.simulation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * State of the simulation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationState {
    private String id;
    private SimulationScenario scenario;
    private LocalDateTime simulatedTime;
    private LocalDateTime startTime;
    private SimulationStatus status;
    private SimulationMetrics metrics;
}
