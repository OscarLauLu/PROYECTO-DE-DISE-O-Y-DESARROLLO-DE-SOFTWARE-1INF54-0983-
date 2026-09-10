package com.odiparpack.logistics.pedidos.repository;

import com.odiparpack.logistics.pedidos.model.EstadoPedido;
import com.odiparpack.logistics.pedidos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByCodigo(String codigo);

    List<Pedido> findByEstado(EstadoPedido estado);

    List<Pedido> findByEstadoIn(List<EstadoPedido> estados);

    List<Pedido> findByClienteIdCliente(String idCliente);

    List<Pedido> findByEstadoOrderByPlazoLimiteEntregaAsc(EstadoPedido estado);

    List<Pedido> findByFechaHoraRegistroBetween(LocalDateTime desde, LocalDateTime hasta);
}
