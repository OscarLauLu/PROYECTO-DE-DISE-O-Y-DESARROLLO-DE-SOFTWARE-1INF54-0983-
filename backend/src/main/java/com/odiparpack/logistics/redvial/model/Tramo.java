package com.odiparpack.logistics.redvial.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Representa un tramo vial entre dos nodos adyacentes de la retícula.
 * En la ciudad ortogonal, cada tramo tiene 1 km de longitud por defecto y es de doble sentido.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tramo {

    private Nodo nodoOrigen;
    private Nodo nodoDestino;
    @Builder.Default
    private double longitudKm = 1.0;
    @Builder.Default
    private boolean bidireccional = true;

    private boolean bloqueado;
    private LocalDateTime inicioBloqueo;
    private LocalDateTime finBloqueo;

    public Tramo(Nodo origen, Nodo destino) {
        this.nodoOrigen = origen;
        this.nodoDestino = destino;
        this.longitudKm = 1.0;
        this.bidireccional = true;
        this.bloqueado = false;
    }

    /**
     * Verifica si el tramo está disponible para el tránsito vehicular en un instante dado.
     *
     * @param instante Momento en el tiempo simulado
     * @return true si el tramo no está bloqueado en dicho instante
     */
    public boolean estaDisponible(LocalDateTime instante) {
        if (!bloqueado) {
            return true;
        }
        if (instante == null) {
            return !bloqueado;
        }
        if (inicioBloqueo != null && finBloqueo != null) {
            boolean dentroDeVentana = !instante.isBefore(inicioBloqueo) && !instante.isAfter(finBloqueo);
            return !dentroDeVentana;
        }
        return !bloqueado;
    }

    public String getClaveCanonica() {
        if (nodoOrigen == null || nodoDestino == null) return "";
        int x1 = nodoOrigen.getX();
        int y1 = nodoOrigen.getY();
        int x2 = nodoDestino.getX();
        int y2 = nodoDestino.getY();
        if (x1 < x2 || (x1 == x2 && y1 <= y2)) {
            return x1 + "," + y1 + "-" + x2 + "," + y2;
        } else {
            return x2 + "," + y2 + "-" + x1 + "," + y1;
        }
    }
}
