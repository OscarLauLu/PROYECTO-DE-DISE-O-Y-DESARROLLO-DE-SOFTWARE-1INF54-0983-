package com.odiparpack.logistics.planificacion.repository;

import com.odiparpack.logistics.planificacion.model.ParadaRuta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParadaRutaRepository extends JpaRepository<ParadaRuta, Long> {
    List<ParadaRuta> findByRutaIdOrderByOrdenAsc(Long rutaId);
    List<ParadaRuta> findByPedidoId(Long pedidoId);
}
