package com.odiparpack.logistics.planner.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un nodo en la cuadrícula de la ciudad (70x50).
 */
@Data
@AllArgsConstructor
public class GridNode {
    private int x;
    private int y;
    private boolean blocked;

    /**
     * Obtiene un identificador único para el nodo.
     * @param gridWidth ancho de la cuadrícula
     * @return ID único
     */
    public int getId(int gridWidth) {
        return y * gridWidth + x;
    }

    /**
     * Obtiene los vecinos ortogonales (hasta 4) de este nodo.
     * @param gridWidth ancho de la cuadrícula
     * @param gridHeight alto de la cuadrícula
     * @return lista de nodos vecinos
     */
    public List<GridNode> getNeighbors(int gridWidth, int gridHeight) {
        List<GridNode> neighbors = new ArrayList<>();
        if (x > 0) neighbors.add(new GridNode(x - 1, y, false));
        if (x < gridWidth - 1) neighbors.add(new GridNode(x + 1, y, false));
        if (y > 0) neighbors.add(new GridNode(x, y - 1, false));
        if (y < gridHeight - 1) neighbors.add(new GridNode(x, y + 1, false));
        return neighbors;
    }
}
