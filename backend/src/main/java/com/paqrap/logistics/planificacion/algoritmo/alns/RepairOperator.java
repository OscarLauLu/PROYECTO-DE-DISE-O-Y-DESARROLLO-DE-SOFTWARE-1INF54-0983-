package com.paqrap.logistics.planificacion.algoritmo.alns;

import com.paqrap.logistics.pedidos.model.Pedido;

import java.util.List;

/**
 * Interfaz base para los operadores de reparación en ALNS (sección 3.3).
 */
public abstract class RepairOperator extends PonderableOperator {

    /**
     * Reinserta los pedidos liberados dentro de la solución parcial.
     *
     * @param solucion Solución parcial a reconstruir
     * @param liberados Lista de pedidos desasignados por el operador de destrucción
     */
    public abstract void reparar(PlanSolution solucion, List<Pedido> liberados);
}
