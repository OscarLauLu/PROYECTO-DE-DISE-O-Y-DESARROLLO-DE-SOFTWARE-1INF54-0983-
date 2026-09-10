package com.odiparpack.logistics.flota.dto;

import com.odiparpack.logistics.flota.model.EstadoOperativo;
import com.odiparpack.logistics.redvial.model.Ubicacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnidadTransporteDTO {
    private Long id;
    private String codigo;
    private String tipoNombre;
    private int capacidadMaxima;
    private int cargaActual;
    private int capacidadDisponible;
    private double porcentajeCarga;
    private double velocidadPromedioKmH;
    private double costoPorKm;
    private EstadoOperativo estadoOperativo;
    private String colorEstado;
    private Ubicacion ubicacionActual;
    private String nombreConductor;
    private boolean activo;
}
