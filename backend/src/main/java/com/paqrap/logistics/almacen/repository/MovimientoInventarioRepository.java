package com.paqrap.logistics.almacen.repository;

import com.paqrap.logistics.almacen.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findByAlmacenIdOrderByFechaHoraDesc(Long almacenId);
    List<MovimientoInventario> findByAlmacenIdAndFechaHoraBetweenOrderByFechaHoraDesc(
            Long almacenId, LocalDateTime desde, LocalDateTime hasta);
}
