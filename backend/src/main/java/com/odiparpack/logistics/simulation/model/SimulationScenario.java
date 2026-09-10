package com.odiparpack.logistics.simulation.model;

/**
 * Scenarios for the simulation.
 */
public enum SimulationScenario {
    DIA_A_DIA("Día a Día"),
    CINCO_DIAS("5 Días"),
    COLAPSO("Colapso Logístico");

    private final String description;

    SimulationScenario(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
