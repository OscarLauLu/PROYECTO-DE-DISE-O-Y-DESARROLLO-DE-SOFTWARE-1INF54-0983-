package com.odiparpack.logistics.flota.repository;

import com.odiparpack.logistics.flota.model.AsignacionTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AsignacionTurnoRepository extends JpaRepository<AsignacionTurno, Long> {
    List<AsignacionTurno> findByFecha(LocalDate fecha);
    List<AsignacionTurno> findByUnidadId(Long unidadId);
    List<AsignacionTurno> findByConductorId(Long conductorId);
}
