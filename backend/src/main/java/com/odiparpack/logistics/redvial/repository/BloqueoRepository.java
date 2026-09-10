package com.odiparpack.logistics.redvial.repository;

import com.odiparpack.logistics.redvial.model.Bloqueo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BloqueoRepository extends JpaRepository<Bloqueo, Long> {

    Optional<Bloqueo> findByCodigo(String codigo);

    List<Bloqueo> findByActivoTrue();

    List<Bloqueo> findByFechaHoraInicioBeforeAndFechaHoraFinAfter(LocalDateTime t1, LocalDateTime t2);
}
