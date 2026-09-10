package com.paqrap.logistics.simulacion.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Representa una alerta emitida en el panel de control (RF-16, RF-25, RF-45).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "alertas")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;

    @Enumerated(EnumType.STRING)
    private TipoAlerta tipo;

    private String mensaje;

    private LocalDateTime fechaHoraGeneracion;

    @Builder.Default
    private boolean atendida = false;

    /**
     * Marca la alerta como atendida o resuelta por el operador.
     */
    public void marcarAtendida() {
        this.atendida = true;
    }
}
