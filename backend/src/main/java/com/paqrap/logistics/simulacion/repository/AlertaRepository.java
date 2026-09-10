package com.paqrap.logistics.simulacion.repository;

import com.paqrap.logistics.simulacion.model.Alerta;
import com.paqrap.logistics.simulacion.model.TipoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    List<Alerta> findByAtendidaFalse();
    List<Alerta> findByTipo(TipoAlerta tipo);
}
