package com.odiparpack.logistics.simulation.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SimulationResultDTO {
    private Long id;
    private String simulationId;
    private String scenario;
    private LocalDateTime executionStart;
    private LocalDateTime executionEnd;
    private int totalOrders;
    private double onTimePercentage;
    private BigDecimal totalCost;
    private String collapsePoint;
}
