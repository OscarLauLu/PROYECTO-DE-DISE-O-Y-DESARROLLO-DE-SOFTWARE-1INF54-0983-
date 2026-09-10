package com.odiparpack.logistics.planificacion.dto;

import com.odiparpack.logistics.redvial.model.Ubicacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParadaRutaDTO {
    private Long id;
    private int orden;
    private Long pedidoId;
    private String codigoPedido;
    private String nombreCliente;
    private int cantidadUnidades;
    private Ubicacion destino;
    private LocalDateTime horaEstimadaLlegada;
    private LocalDateTime plazoLimiteEntrega;
    private int tiempoServicioMin;
    private boolean entregada;
}
