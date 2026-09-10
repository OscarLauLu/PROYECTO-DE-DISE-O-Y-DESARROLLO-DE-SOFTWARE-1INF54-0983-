package com.paqrap.logistics.pedidos.repository;

import com.paqrap.logistics.pedidos.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByIdCliente(String idCliente);
}
