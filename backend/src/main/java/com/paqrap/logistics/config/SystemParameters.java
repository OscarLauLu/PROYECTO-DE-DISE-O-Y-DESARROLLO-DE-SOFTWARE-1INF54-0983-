package com.paqrap.logistics.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Parámetros de configuración del sistema de logística.
 */
@Component
@ConfigurationProperties(prefix = "logistics.config")
@Getter
@Setter
public class SystemParameters {
    
    private volatile int criticalSlackHours = 4;
    private volatile int serviceTimeMinutes = 60;
    private volatile int gridWidth = 70;
    private volatile int gridHeight = 50;
    private volatile int warehouseMaxCapacity = 1000;
    private volatile double warehouseAlertThreshold = 90.0;
    private volatile int semaphoreGreenHours = 12;
    private volatile int semaphoreAmberHours = 4;
    private volatile int planningTimeoutSeconds = 30;
    private volatile LocalTime dailyRestockTime = LocalTime.of(23, 59, 59);
    
    private VehiclesConfig vehicles = new VehiclesConfig();

    @Data
    public static class VehiclesConfig {
        private VehicleConfig auto = new VehicleConfig();
        private VehicleConfig moto = new VehicleConfig();
        private VehicleConfig bicicleta = new VehicleConfig();
    }

    @Data
    public static class VehicleConfig {
        private int speed;
        private BigDecimal cost;
        private int capacity;
    }
}
