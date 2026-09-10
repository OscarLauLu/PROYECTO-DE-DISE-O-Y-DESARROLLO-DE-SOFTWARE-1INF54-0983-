package com.odiparpack.logistics.simulation.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SimulationMetricsDTO {
    private int totalOrdersProcessed;
    private int ordersDeliveredOnTime;
    private int ordersDeliveredLate;
    private int ordersPending;
    private double onTimeDeliveryPercentage;
    
    private BigDecimal totalCostAuto;
    private BigDecimal totalCostMoto;
    private BigDecimal totalCostBicicleta;
    private BigDecimal totalCostAll;
    
    private int deliveriesByAuto;
    private int deliveriesByMoto;
    private int deliveriesByBicicleta;
    
    private int totalBlockages;
    private int totalBreakdowns;
    private LocalDateTime lastUpdated;
}
