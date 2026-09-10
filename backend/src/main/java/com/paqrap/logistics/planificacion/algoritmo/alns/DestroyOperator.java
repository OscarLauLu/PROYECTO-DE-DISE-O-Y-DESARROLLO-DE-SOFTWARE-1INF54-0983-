package com.paqrap.logistics.planificacion.algoritmo.alns;

import com.paqrap.logistics.pedidos.model.Pedido;

import java.util.List;

/**
 * Interfaz base para los operadores de destrucción en ALNS (sección 3.3).
 */
public abstract class DestroyOperator extends PonderableOperator {

    /**
     * Remueve pedidos de la solución candidata según la heurística del operador.
     *
     * @param solucion Solución a destruir parcialmente
     * @param factorDestruccion Porcentaje de destrucción (entre 0.10 y 0.25)
     * @return Lista de pedidos liberados/removidos
     */
    public abstract List<Pedido> destruir(PlanSolution solucion, double factorDestruccion);
}
