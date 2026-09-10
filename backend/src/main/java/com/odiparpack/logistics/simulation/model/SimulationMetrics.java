package com.odiparpack.logistics.simulation.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Metrics tracking for the simulation.
 */
@Data
public class SimulationMetrics {
    private int totalOrdersProcessed;
    private int ordersDeliveredOnTime;
    private int ordersDeliveredLate;
    private int ordersPending;
    private double onTimeDeliveryPercentage;
    
    private BigDecimal totalCostAuto = BigDecimal.ZERO;
    private BigDecimal totalCostMoto = BigDecimal.ZERO;
    private BigDecimal totalCostBicicleta = BigDecimal.ZERO;
    private BigDecimal totalCostAll = BigDecimal.ZERO;
    
    private int deliveriesByAuto;
    private int deliveriesByMoto;
    private int deliveriesByBicicleta;
    
    private int totalBlockages;
    private int totalBreakdowns;
    private LocalDateTime lastUpdated;

    /**
     * Updates the on-time delivery percentage.
     */
    public void updateOnTimePercentage() {
        int totalDelivered = getTotalDelivered();
        if (totalDelivered > 0) {
            this.onTimeDeliveryPercentage = (this.ordersDeliveredOnTime * 100.0) / totalDelivered;
        } else {
            this.onTimeDeliveryPercentage = 0.0;
        }
    }

    /**
     * Gets the total number of delivered orders.
     * @return sum of on-time and late orders.
     */
    public int getTotalDelivered() {
        return this.ordersDeliveredOnTime + this.ordersDeliveredLate;
    }

    /**
     * Adds cost for a specific vehicle type and updates the total.
     * @param type vehicle type
     * @param cost cost to add
     */
    public void addCost(String type, BigDecimal cost) {
        if (cost == null) return;
        
        if ("AUTO".equalsIgnoreCase(type)) {
            this.totalCostAuto = this.totalCostAuto.add(cost);
        } else if ("MOTO".equalsIgnoreCase(type)) {
            this.totalCostMoto = this.totalCostMoto.add(cost);
        } else if ("BICICLETA".equalsIgnoreCase(type)) {
            this.totalCostBicicleta = this.totalCostBicicleta.add(cost);
        }
        
        this.totalCostAll = this.totalCostAuto.add(this.totalCostMoto).add(this.totalCostBicicleta);
    }
}
