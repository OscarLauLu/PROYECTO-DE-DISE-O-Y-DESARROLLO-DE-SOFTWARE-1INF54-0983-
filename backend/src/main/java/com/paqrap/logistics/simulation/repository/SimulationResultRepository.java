package com.paqrap.logistics.simulation.repository;

import com.paqrap.logistics.simulation.model.SimulationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SimulationResultRepository extends JpaRepository<SimulationResult, Long> {
    Optional<SimulationResult> findBySimulationId(String simulationId);
    List<SimulationResult> findByScenarioOrderByExecutionStartDesc(String scenario);
    List<SimulationResult> findAllByOrderByExecutionStartDesc();
}
