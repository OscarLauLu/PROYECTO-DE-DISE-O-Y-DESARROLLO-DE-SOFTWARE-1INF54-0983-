package com.odiparpack.logistics.planificacion.algoritmo.alns;

import com.odiparpack.logistics.flota.model.EstadoOperativo;
import com.odiparpack.logistics.flota.model.UnidadTransporte;
import com.odiparpack.logistics.pedidos.model.Pedido;
import com.odiparpack.logistics.planificacion.model.ParadaRuta;
import com.odiparpack.logistics.planificacion.model.Ruta;

import java.util.ArrayList;
import java.util.List;

/**
 * Operador de Destrucción Específico de Dominio: Atiende contingencias por averías
 * mecánicas de la flota (RF-14, RF-15, RF-16). Libera todos los pedidos de unidades
 * averiadas o con fallas operativas para su reasignación a flota disponible.
 */
public class AveriaDestroyOperator extends DestroyOperator {

    private final List<UnidadTransporte> flota;

    public AveriaDestroyOperator(List<UnidadTransporte> flota) {
        this.flota = flota != null ? flota : new ArrayList<>();
    }

    @Override
    public String getNombre() {
        return "AveriaDestroyOperator";
    }

    @Override
    public List<Pedido> destruir(PlanSolution solucion, double factorDestruccion) {
        List<Pedido> liberados = new ArrayList<>();
        if (solucion == null || solucion.getRutas().isEmpty()) return liberados;

        // 1. Buscar rutas asignadas a unidades averiadas o no operativas (RF-14, RF-15)
        for (Ruta r : solucion.getRutas()) {
            UnidadTransporte unidad = r.getUnidadTransporte();
            boolean esAveriada = (unidad != null &&
                    (unidad.getEstadoOperativo() == EstadoOperativo.AVERIADA ||
                     unidad.getEstadoOperativo() == EstadoOperativo.EN_MANTENIMIENTO ||
                     !unidad.isActivo()));

            if (esAveriada) {
                // Liberar todos los pedidos de la unidad averiada
                for (ParadaRuta p : r.getParadas()) {
                    if (p.getPedido() != null) {
                        liberados.add(p.getPedido());
                    }
                }
                r.getParadas().clear();
            }
        }

        // 2. Si no hay unidades averiadas, descargar parcialmente la unidad con mayor saturación de carga
        if (liberados.isEmpty()) {
            Ruta rutaMasCargada = null;
            int maxCarga = -1;
            for (Ruta r : solucion.getRutas()) {
                int carga = r.getParadas().stream()
                        .mapToInt(p -> p.getPedido() != null ? p.getPedido().getCantidadUnidades() : 0)
                        .sum();
                if (carga > maxCarga) {
                    maxCarga = carga;
                    rutaMasCargada = r;
                }
            }

            if (rutaMasCargada != null && !rutaMasCargada.getParadas().isEmpty()) {
                int cantARemover = Math.max(1, (int) Math.round(rutaMasCargada.getParadas().size() * factorDestruccion));
                for (int i = 0; i < cantARemover && !rutaMasCargada.getParadas().isEmpty(); i++) {
                    ParadaRuta parada = rutaMasCargada.getParadas().remove(rutaMasCargada.getParadas().size() - 1);
                    if (parada.getPedido() != null) {
                        liberados.add(parada.getPedido());
                    }
                }
            }
        }

        return liberados;
    }
}
