package com.odiparpack.logistics.redvial.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa una ubicación geográfica en la retícula ortogonal de la ciudad (coordenadas x, y en km).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Ubicacion {

    @Column(name = "pos_x")
    private int posX;

    @Column(name = "pos_y")
    private int posY;

    /**
     * Calcula la distancia ortogonal (Manhattan) en kilómetros hacia otra ubicación.
     * En la retícula de 70x50 km, los desplazamientos son estrictamente horizontales y verticales.
     *
     * @param otra Ubicación destino
     * @return distancia en kilómetros
     */
    public double distanciaOrtogonalA(Ubicacion otra) {
        if (otra == null) {
            return 0.0;
        }
        return Math.abs(this.posX - otra.getPosX()) + Math.abs(this.posY - otra.getPosY());
    }

    @Override
    public String toString() {
        return "(" + posX + ", " + posY + ")";
    }
}
