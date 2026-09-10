package com.odiparpack.logistics.planificacion.repository;

import com.odiparpack.logistics.planificacion.model.EstadoRuta;
import com.odiparpack.logistics.planificacion.model.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {
    Optional<Ruta> findByCodigo(String codigo);
    List<Ruta> findByEstado(EstadoRuta estado);
    List<Ruta> findByUnidadTransporteId(Long unidadId);
}
