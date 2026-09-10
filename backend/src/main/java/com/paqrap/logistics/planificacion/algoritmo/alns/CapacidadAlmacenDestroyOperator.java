package com.paqrap.logistics.planificacion.algoritmo.alns;

import com.paqrap.logistics.almacen.model.Almacen;
import com.paqrap.logistics.almacen.model.AlmacenIntermedio;
import com.paqrap.logistics.pedidos.model.Pedido;
import com.paqrap.logistics.planificacion.model.ParadaRuta;
import com.paqrap.logistics.planificacion.model.Ruta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Operador de Destrucción Específico de Dominio: Remueve pedidos de almacenes
 * intermedios que superen el umbral de alerta de ocupación (>90%, RF-24, RF-25)
 * o con stock crítico para permitir su reabastecimiento o reasignación hacia el almacén central (RF-02).
 */
public class CapacidadAlmacenDestroyOperator extends DestroyOperator {

    private final List<Almacen> almacenes;

    public CapacidadAlmacenDestroyOperator(List<Almacen> almacenes) {
        this.almacenes = almacenes != null ? almacenes : new ArrayList<>();
    }

    @Override
    public String getNombre() {
        return "CapacidadAlmacenDestroyOperator";
    }

    @Override
    public List<Pedido> destruir(PlanSolution solucion, double factorDestruccion) {
        List<Pedido> liberados = new ArrayList<>();
        if (solucion == null || solucion.getRutas().isEmpty()) return liberados;

        // Identificar almacenes sobreocupados o con alta demanda acumulada en la solución
        Map<Almacen, Integer> demandaPorAlmacen = new HashMap<>();
        for (Ruta r : solucion.getRutas()) {
            if (r.getAlmacenOrigen() != null) {
                int sumCarga = r.getParadas().stream()
                        .mapToInt(p -> p.getPedido() != null ? p.getPedido().getCantidadUnidades() : 0)
                        .sum();
                demandaPorAlmacen.merge(r.getAlmacenOrigen(), sumCarga, Integer::sum);
            }
        }

        Set<Almacen> almacenesCriticos = new HashSet<>();
        for (Map.Entry<Almacen, Integer> entry : demandaPorAlmacen.entrySet()) {
            Almacen a = entry.getKey();
            int demanda = entry.getValue();
            if (a instanceof AlmacenIntermedio intermedio) {
                double pctOcupacion = ((intermedio.getStockActual() - demanda) * 100.0) / intermedio.getCapacidadMaxima();
                if (pctOcupacion > 90.0 || demanda >= intermedio.getStockActual() * 0.8) {
                    almacenesCriticos.add(a);
                }
            }
        }

        // Si no se detectaron específicamente críticos, tomar el almacén intermedio con menor stock remanente
        if (almacenesCriticos.isEmpty()) {
            almacenes.stream()
                    .filter(a -> a instanceof AlmacenIntermedio)
                    .min((a1, a2) -> Integer.compare(a1.getStockActual(), a2.getStockActual()))
                    .ifPresent(almacenesCriticos::add);
        }

        // Remover pedidos pertenecientes a las rutas de dichos almacenes
        for (Ruta r : solucion.getRutas()) {
            if (r.getAlmacenOrigen() != null && almacenesCriticos.contains(r.getAlmacenOrigen())) {
                List<ParadaRuta> paradas = new ArrayList<>(r.getParadas());
                int limiteARemover = Math.max(1, (int) Math.round(paradas.size() * factorDestruccion));
                for (int i = 0; i < Math.min(limiteARemover, paradas.size()); i++) {
                    ParadaRuta paradaRemovida = paradas.get(i);
                    r.getParadas().remove(paradaRemovida);
                    if (paradaRemovida.getPedido() != null) {
                        liberados.add(paradaRemovida.getPedido());
                    }
                }
            }
        }

        return liberados;
    }
}
