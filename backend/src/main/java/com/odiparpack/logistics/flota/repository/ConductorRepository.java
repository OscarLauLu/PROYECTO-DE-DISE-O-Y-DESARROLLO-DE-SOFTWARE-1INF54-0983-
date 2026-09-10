package com.odiparpack.logistics.flota.repository;

import com.odiparpack.logistics.flota.model.Conductor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConductorRepository extends JpaRepository<Conductor, Long> {
    Optional<Conductor> findByCodigo(String codigo);
    List<Conductor> findByActivoTrue();
}
