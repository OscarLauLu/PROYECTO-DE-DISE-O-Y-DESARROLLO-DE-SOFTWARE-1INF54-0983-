package com.odiparpack.logistics.flota.repository;

import com.odiparpack.logistics.flota.model.EstadoOperativo;
import com.odiparpack.logistics.flota.model.UnidadTransporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnidadTransporteRepository extends JpaRepository<UnidadTransporte, Long> {

    Optional<UnidadTransporte> findByCodigo(String codigo);

    List<UnidadTransporte> findByActivoTrue();

    List<UnidadTransporte> findByEstadoOperativo(EstadoOperativo estado);

    List<UnidadTransporte> findByActivoTrueAndEstadoOperativo(EstadoOperativo estado);

    List<UnidadTransporte> findByTipoNombreIgnoreCase(String tipoNombre);

    List<UnidadTransporte> findByTipoNombreIgnoreCaseAndEstadoOperativo(String tipoNombre, EstadoOperativo estado);
}
