package com.odiparpack.logistics.simulation.engine;

import com.odiparpack.logistics.simulation.model.*;
import com.odiparpack.logistics.simulation.repository.SimulationResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main engine for running simulations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SimulationEngine {

    private final SimulationClock clock;
    private final SimulationResultRepository resultRepository;
    
    private SimulationState currentState;
    private SimulationConfig config;
    private ScheduledExecutorService executor;
    private List<SimulationEvent> pendingEvents = new ArrayList<>();

    public String startSimulation(SimulationConfig config) {
        this.config = config;
        this.currentState = SimulationState.builder()
                .id(UUID.randomUUID().toString())
                .scenario(config.getScenario())
                .startTime(LocalDateTime.now())
                .simulatedTime(LocalDateTime.now())
                .status(SimulationStatus.CONFIGURED)
                .metrics(new SimulationMetrics())
                .build();

        double speed = config.getSpeedFactor();
        if (config.getScenario() == SimulationScenario.CINCO_DIAS) {
            speed = 120.0; // Example: 1 hour real = 5 days simulated (120x)
        }
        
        clock.initialize(LocalDateTime.now(), speed);
        loadOrdersFromFile(config.getOrdersFilePath());
        // loadBlockagesFromFile(...)
        
        this.currentState.setStatus(SimulationStatus.RUNNING);
        clock.resume();
        
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::simulationLoop, 0, 100, TimeUnit.MILLISECONDS);
        
        log.info("Started simulation {} with scenario {}", currentState.getId(), config.getScenario());
        return currentState.getId();
    }

    private void simulationLoop() {
        try {
            if (currentState.getStatus() != SimulationStatus.RUNNING) {
                return;
            }
            
            clock.tick();
            LocalDateTime now = clock.getCurrentTime();
            currentState.setSimulatedTime(now);
            
            processEvents(now);
            
            if (config.getScenario() == SimulationScenario.COLAPSO) {
                checkCollapseCondition();
            }
            
            if (pendingEvents.isEmpty()) {
                stopSimulation();
            }
        } catch (Exception e) {
            log.error("Error in simulation loop", e);
            stopSimulation();
        }
    }

    private void processEvents(LocalDateTime currentTime) {
        List<SimulationEvent> dueEvents = new ArrayList<>();
        pendingEvents.removeIf(event -> {
            if (!event.getScheduledTime().isAfter(currentTime)) {
                dueEvents.add(event);
                return true;
            }
            return false;
        });
        
        for (SimulationEvent event : dueEvents) {
            // Process event (mock)
            log.debug("Processing event {} at {}", event.getType(), currentTime);
        }
    }

    private void checkCollapseCondition() {
        if (currentState.getMetrics().getOnTimeDeliveryPercentage() < 60.0 && currentState.getMetrics().getTotalDelivered() > 100) {
            log.warn("Collapse condition reached!");
            stopSimulation();
        }
    }

    public void stopSimulation() {
        if (currentState == null || currentState.getStatus() == SimulationStatus.STOPPED) return;
        
        currentState.setStatus(SimulationStatus.STOPPED);
        clock.stop();
        if (executor != null) {
            executor.shutdown();
        }
        log.info("Simulation {} stopped", currentState.getId());
    }

    public SimulationState getState() {
        return currentState;
    }

    public void loadOrdersFromFile(String filePath) {
        // Mock parsing logic
        log.info("Loading orders from {}", filePath);
        Collections.sort(pendingEvents);
    }
    
    public void loadBlockagesFromFile(String filePath) {
        log.info("Loading blockages from {}", filePath);
    }
}
