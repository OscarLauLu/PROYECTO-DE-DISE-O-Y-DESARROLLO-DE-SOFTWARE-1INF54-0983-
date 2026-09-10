package com.odiparpack.logistics.planificacion.dto;

import com.odiparpack.logistics.planificacion.model.EstadoRuta;
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
public class RutaDTO {
    private Long id;
    private String codigo;
    private String unidadCodigo;
    private String tipoVehiculo;
    private String almacenOrigenCodigo;
    private String almacenOrigenNombre;
    private LocalDateTime fechaHoraGeneracion;
    private double distanciaTotalKm;
    private int tiempoEstimadoMin;
    private double costoTotal;
    private EstadoRuta estado;
    private List<ParadaRutaDTO> paradas;
}
