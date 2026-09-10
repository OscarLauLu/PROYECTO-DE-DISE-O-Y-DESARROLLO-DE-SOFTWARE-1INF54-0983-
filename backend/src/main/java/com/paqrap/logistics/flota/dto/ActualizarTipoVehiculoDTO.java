package com.paqrap.logistics.flota.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarTipoVehiculoDTO {
    private Double nuevaVelocidadKmH;
    private Double nuevoCostoPorKm;
    private Integer nuevaCapacidadMaxima;
}
