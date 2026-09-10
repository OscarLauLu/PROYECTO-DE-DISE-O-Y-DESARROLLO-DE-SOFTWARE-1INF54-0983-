package com.paqrap.logistics.planificacion.algoritmo;

import com.paqrap.logistics.flota.model.UnidadTransporte;
import com.paqrap.logistics.pedidos.model.Pedido;
import com.paqrap.logistics.planificacion.model.Ruta;
import com.paqrap.logistics.redvial.model.RedVial;

import java.util.List;
import java.util.Map;

/**
 * Patrón Strategy: interfaz para algoritmos metaheurísticos de optimización de rutas (RF-06).
 * Permite la experimentación y comparación ACO vs. ALNS exigida por el proyecto.
 */
public interface AlgoritmoRuteo {

    /**
     * Construye un conjunto de rutas factibles para los pedidos pendientes minimizando costo.
     *
     * @param pedidos Lista de pedidos a planificar
     * @param flota Lista de unidades de transporte disponibles
     * @param red Retícula vial ortogonal con estado de tramos
     * @return Lista de rutas construidas
     */
    List<Ruta> construirSolucion(List<Pedido> pedidos, List<UnidadTransporte> flota, RedVial red);

    /**
     * Configura hiperparámetros del algoritmo.
     */
    void configurarParametros(Map<String, Double> params);

    /**
     * Nombre descriptivo del algoritmo.
     */
    String obtenerNombre();

    /**
     * Costo total de la solución obtenida.
     */
    double obtenerCostoSolucion();
}
