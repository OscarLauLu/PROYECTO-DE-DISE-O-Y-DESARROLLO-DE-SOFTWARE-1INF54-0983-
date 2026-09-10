package com.odiparpack.logistics.flota.repository;

import com.odiparpack.logistics.flota.model.Averia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AveriaRepository extends JpaRepository<Averia, Long> {
    Optional<Averia> findByCodigo(String codigo);
    List<Averia> findByResueltaFalse();
    List<Averia> findByUnidadId(Long unidadId);
}
