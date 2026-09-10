package com.odiparpack.logistics.planificacion.algoritmo.aco;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Hiperparámetros configurables para la metaheurística ACO (RF-06).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigACO {
    @Builder.Default
    private int minAnts = 10;

    @Builder.Default
    private int maxAnts = 30;

    @Builder.Default
    private int iterations = 40;

    @Builder.Default
    private double alpha = 1.0; // Exponente de influencia de la feromona

    @Builder.Default
    private double beta = 2.0;  // Exponente de influencia de la heurística

    @Builder.Default
    private double rho = 0.1;   // Tasa de evaporación de feromonas (0 < rho < 1)

    @Builder.Default
    private double umbralCriticidadMin = 240.0; // 4 horas en minutos (RF-04)
}
