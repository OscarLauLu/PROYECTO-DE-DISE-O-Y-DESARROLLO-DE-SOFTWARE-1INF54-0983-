package com.odiparpack.logistics.pedidos.dto;

import com.odiparpack.logistics.pedidos.model.EstadoPedido;
import com.odiparpack.logistics.pedidos.model.NivelCriticidad;
import com.odiparpack.logistics.pedidos.model.TipoEntrega;
import com.odiparpack.logistics.redvial.model.Ubicacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta con información completa de un pedido.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {

    private Long id;
    private String codigo;
    private String clienteId;
    private String nombreCliente;
    private Ubicacion destino;
    private int cantidadUnidades;
    private TipoEntrega tipoEntrega;
    private EstadoPedido estado;
    private NivelCriticidad nivelCriticidad;
    private LocalDateTime fechaHoraRegistro;
    private LocalDateTime plazoLimiteEntrega;
    private LocalDateTime fechaHoraEntrega;
    private double holguraHoras;
    private Long unidadAsignadaId;
    private Long rutaAsignadaId;
}
