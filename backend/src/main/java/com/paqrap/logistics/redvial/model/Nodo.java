package com.paqrap.logistics.redvial.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Representa un nodo en la retícula vial ortogonal (70x50 km, nodos cada 1 km).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Nodo {

    private int x;
    private int y;

    public Ubicacion aUbicacion() {
        return new Ubicacion(x, y);
    }

    public static Nodo deUbicacion(Ubicacion ubicacion) {
        if (ubicacion == null) return null;
        return new Nodo(ubicacion.getPosX(), ubicacion.getPosY());
    }

    public String getClave() {
        return x + "," + y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nodo nodo = (Nodo) o;
        return x == nodo.x && y == nodo.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Nodo(" + x + ", " + y + ")";
    }
}
