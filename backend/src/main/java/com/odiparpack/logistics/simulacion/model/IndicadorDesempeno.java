package com.odiparpack.logistics.simulacion.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Representa un indicador clave de desempeño (KPI) recalculado durante la simulación (RF-68, RF-69, RF-70, RF-72).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicadorDesempeno {

    private String nombre;

    private double valor;

    private String unidad;

    private LocalDateTime fechaHoraCalculo;

    public void recalcular() {
        this.fechaHoraCalculo = LocalDateTime.now();
    }
}
