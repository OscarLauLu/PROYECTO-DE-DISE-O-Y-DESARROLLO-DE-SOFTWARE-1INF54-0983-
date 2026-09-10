package com.odiparpack.logistics.simulation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationComparisonDTO {
    private SimulationResultDTO result1;
    private SimulationResultDTO result2;
    private String onTimeComparison;
    private String costComparison;
}
