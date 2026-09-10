package com.paqrap.logistics.common.util;

/**
 * Utility class for grid operations.
 */
public class GridUtils {
    
    private GridUtils() {}

    /**
     * Calcula la distancia Manhattan entre dos puntos en la grilla.
     */
    public static int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    /**
     * Calcula el tiempo estimado de viaje en minutos.
     */
    public static double estimatedTravelTime(int distanceKm, int speedKmh) {
        if (speedKmh <= 0) return Double.MAX_VALUE;
        return ((double) distanceKm / speedKmh) * 60.0;
    }

    /**
     * Verifica si una coordenada es válida en la grilla.
     */
    public static boolean isValidCoordinate(int x, int y, int gridWidth, int gridHeight) {
        return x >= 0 && x < gridWidth && y >= 0 && y < gridHeight;
    }

    /**
     * Convierte coordenadas (x, y) a un ID de nodo único.
     */
    public static int coordinateToNodeId(int x, int y, int gridWidth) {
        return y * gridWidth + x;
    }

    /**
     * Convierte un ID de nodo único a la coordenada X.
     */
    public static int nodeIdToX(int nodeId, int gridWidth) {
        return nodeId % gridWidth;
    }

    /**
     * Convierte un ID de nodo único a la coordenada Y.
     */
    public static int nodeIdToY(int nodeId, int gridWidth) {
        return nodeId / gridWidth;
    }
}
