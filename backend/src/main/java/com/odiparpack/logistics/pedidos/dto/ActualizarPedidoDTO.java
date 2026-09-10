package com.odiparpack.logistics.pedidos.dto;

import com.odiparpack.logistics.pedidos.model.TipoEntrega;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la modificación de un pedido existente en estado REGISTRADO (RF-35).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarPedidoDTO {

    @Positive(message = "La cantidad debe ser mayor a cero")
    private Integer cantidadUnidades;

    private Integer destinoX;

    private Integer destinoY;

    private TipoEntrega tipoEntrega;
}
