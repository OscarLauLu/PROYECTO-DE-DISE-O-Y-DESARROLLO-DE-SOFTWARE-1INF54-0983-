package com.paqrap.logistics.redvial.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Representa un bloqueo temporal de tramos viales (RF-10, RF-11, RF-12, RF-13).
 * Se alimenta del archivo mensual aaaamm.bloqueadas con formato:
 * ##d##h##m-##d##h##m:x1,y1,...,xn,yn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bloqueos")
public class Bloqueo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(nullable = false)
    private LocalDateTime fechaHoraFin;

    @Builder.Default
    private boolean activo = false;

    /**
     * Secuencia de coordenadas de nodos que conforman el o los tramos bloqueados:
     * formato "x1,y1,x2,y2,...,xn,yn"
     */
    @Column(columnDefinition = "TEXT")
    private String coordenadasNodos;

    private String archivoOrigen;

    /**
     * Verifica si el bloqueo está vigente en un instante temporal específico.
     *
     * @param instante Momento temporal a consultar
     * @return true si el instante se encuentra dentro del rango [fechaHoraInicio, fechaHoraFin]
     */
    public boolean estaVigente(LocalDateTime instante) {
        if (instante == null) {
            return activo;
        }
        return !instante.isBefore(fechaHoraInicio) && !instante.isAfter(fechaHoraFin);
    }

    /**
     * Activa formalmente el bloqueo.
     */
    public void activar() {
        this.activo = true;
    }

    /**
     * Desactiva formalmente el bloqueo.
     */
    public void desactivar() {
        this.activo = false;
    }
}
