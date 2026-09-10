package com.odiparpack.logistics.planificacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanificacionResultDTO {
    private String algoritmoUtilizado;
    private LocalDateTime instanteEjecucion;
    private int totalRutasPlanificadas;
    private int totalPedidosAtendidos;
    private double costoTotalEstimado;
    private List<RutaDTO> rutas;
}
