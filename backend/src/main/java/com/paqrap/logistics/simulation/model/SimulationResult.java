package com.paqrap.logistics.simulation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing the result of a simulation run.
 */
@Entity
@Table(name = "simulation_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String simulationId;

    @Column(nullable = false)
    private String scenario;

    @Column(nullable = false)
    private LocalDateTime executionStart;

    @Column(nullable = false)
    private LocalDateTime executionEnd;

    private int totalOrders;
    private int onTimeDeliveries;
    private int lateDeliveries;
    private double onTimePercentage;
    private BigDecimal totalCost;

    @Column(nullable = true)
    private String collapsePoint;

    @Column(columnDefinition = "TEXT")
    private String configJson;

    @Column(columnDefinition = "TEXT")
    private String metricsJson;
}
