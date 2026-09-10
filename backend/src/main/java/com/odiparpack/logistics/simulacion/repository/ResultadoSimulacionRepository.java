package com.odiparpack.logistics.simulacion.repository;

import com.odiparpack.logistics.simulacion.model.ResultadoSimulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResultadoSimulacionRepository extends JpaRepository<ResultadoSimulacion, Long> {
    Optional<ResultadoSimulacion> findByCodigo(String codigo);
}
