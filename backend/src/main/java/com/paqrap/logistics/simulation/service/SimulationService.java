package com.paqrap.logistics.simulation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paqrap.logistics.simulation.dto.*;
import com.paqrap.logistics.simulation.engine.SimulationEngine;
import com.paqrap.logistics.simulation.model.*;
import com.paqrap.logistics.simulation.repository.SimulationResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SimulationService {

    private final SimulationEngine engine;
    private final SimulationResultRepository repository;
    private final ObjectMapper objectMapper;

    public SimulationStateDTO startSimulation(SimulationConfigDTO configDTO) {
        SimulationConfig config = SimulationConfig.builder()
                .autoCount(configDTO.getAutoCount())
                .motoCount(configDTO.getMotoCount())
                .bicycletaCount(configDTO.getBicycletaCount())
                .warehouseCapacity(configDTO.getWarehouseCapacity())
                .semaphoreGreenHours(configDTO.getSemaphoreGreenHours())
                .semaphoreAmberHours(configDTO.getSemaphoreAmberHours())
                .collapseDemandFactor(configDTO.getCollapseDemandFactor())
                .ordersFilePath(configDTO.getOrdersFilePath())
                .scenario(configDTO.getScenario())
                .speedFactor(configDTO.getSpeedFactor())
                .build();
                
        engine.startSimulation(config);
        return getSimulationStatus();
    }

    public SimulationStateDTO getSimulationStatus() {
        SimulationState state = engine.getState();
        if (state == null) return null;
        
        SimulationStateDTO dto = new SimulationStateDTO();
        dto.setSimulationId(state.getId());
        dto.setScenario(state.getScenario());
        dto.setRealStartTime(state.getStartTime());
        dto.setStatus(state.getStatus());
        
        SimulationMetrics metrics = state.getMetrics();
        if (metrics != null) {
            SimulationMetricsDTO metricsDTO = new SimulationMetricsDTO();
            metricsDTO.setTotalOrdersProcessed(metrics.getTotalOrdersProcessed());
            metricsDTO.setOnTimeDeliveryPercentage(metrics.getOnTimeDeliveryPercentage());
            metricsDTO.setTotalCostAll(metrics.getTotalCostAll());
            dto.setMetrics(metricsDTO);
        }
        return dto;
    }

    public void stopSimulation() {
        engine.stopSimulation();
    }

    public void pauseSimulation() {
        SimulationState state = engine.getState();
        if (state != null && state.getStatus() == SimulationStatus.RUNNING) {
            state.setStatus(SimulationStatus.PAUSED);
            // clock pause logic inside engine is not exposed directly here without another method
        }
    }

    public void resumeSimulation() {
        SimulationState state = engine.getState();
        if (state != null && state.getStatus() == SimulationStatus.PAUSED) {
            state.setStatus(SimulationStatus.RUNNING);
        }
    }

    public SimulationResultDTO saveResult() {
        SimulationState state = engine.getState();
        if (state == null) return null;

        SimulationResult result = SimulationResult.builder()
                .simulationId(state.getId())
                .scenario(state.getScenario().name())
                .executionStart(state.getStartTime())
                .executionEnd(LocalDateTime.now())
                .totalOrders(state.getMetrics().getTotalOrdersProcessed())
                .onTimeDeliveries(state.getMetrics().getOrdersDeliveredOnTime())
                .lateDeliveries(state.getMetrics().getOrdersDeliveredLate())
                .onTimePercentage(state.getMetrics().getOnTimeDeliveryPercentage())
                .totalCost(state.getMetrics().getTotalCostAll())
                .build();

        result = repository.save(result);
        return toResultDTO(result);
    }

    public List<SimulationResultDTO> getResults() {
        return repository.findAllByOrderByExecutionStartDesc().stream()
                .map(this::toResultDTO)
                .collect(Collectors.toList());
    }

    public SimulationComparisonDTO compareResults(Long id1, Long id2) {
        SimulationResult r1 = repository.findById(id1).orElseThrow();
        SimulationResult r2 = repository.findById(id2).orElseThrow();
        
        SimulationComparisonDTO dto = new SimulationComparisonDTO();
        dto.setResult1(toResultDTO(r1));
        dto.setResult2(toResultDTO(r2));
        
        double onTimeDiff = r2.getOnTimePercentage() - r1.getOnTimePercentage();
        dto.setOnTimeComparison(String.format("Result 2 is %.2f%% %s than Result 1", Math.abs(onTimeDiff), onTimeDiff > 0 ? "better" : "worse"));
        
        dto.setCostComparison("Costs comparison calculated");
        return dto;
    }
    
    private SimulationResultDTO toResultDTO(SimulationResult r) {
        SimulationResultDTO dto = new SimulationResultDTO();
        dto.setId(r.getId());
        dto.setSimulationId(r.getSimulationId());
        dto.setScenario(r.getScenario());
        dto.setExecutionStart(r.getExecutionStart());
        dto.setExecutionEnd(r.getExecutionEnd());
        dto.setTotalOrders(r.getTotalOrders());
        dto.setOnTimePercentage(r.getOnTimePercentage());
        dto.setTotalCost(r.getTotalCost());
        dto.setCollapsePoint(r.getCollapsePoint());
        return dto;
    }
}
