package com.paqrap.logistics.flota.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un conductor o repartidor asignado a la flota (RF-43, RF-44).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "conductores")
public class Conductor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;

    private String nombre;

    @Embedded
    private Turno turno;

    @Builder.Default
    private boolean activo = true;

    public Conductor(String codigo, String nombre, Turno turno) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.turno = turno;
        this.activo = true;
    }

    /**
     * Verifica si el conductor está disponible para operar en un turno determinado (RF-43).
     */
    public boolean estaDisponible(Turno turnoConsultado) {
        if (!activo || this.turno == null || turnoConsultado == null) {
            return false;
        }
        return this.turno.getHoraInicio().equals(turnoConsultado.getHoraInicio())
                && this.turno.getHoraFin().equals(turnoConsultado.getHoraFin());
    }
}
