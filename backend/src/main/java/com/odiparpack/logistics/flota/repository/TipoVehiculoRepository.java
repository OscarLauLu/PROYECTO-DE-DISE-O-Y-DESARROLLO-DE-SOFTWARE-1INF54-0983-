package com.odiparpack.logistics.flota.repository;

import com.odiparpack.logistics.flota.model.TipoVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoVehiculoRepository extends JpaRepository<TipoVehiculo, Long> {
    Optional<TipoVehiculo> findByNombreIgnoreCase(String nombre);
}
