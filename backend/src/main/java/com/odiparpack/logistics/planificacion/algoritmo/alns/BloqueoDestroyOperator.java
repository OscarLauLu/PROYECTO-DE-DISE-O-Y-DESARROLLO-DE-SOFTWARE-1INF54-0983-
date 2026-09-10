package com.odiparpack.logistics.planificacion.algoritmo.alns;

import com.odiparpack.logistics.pedidos.model.Pedido;
import com.odiparpack.logistics.planificacion.model.ParadaRuta;
import com.odiparpack.logistics.planificacion.model.Ruta;
import com.odiparpack.logistics.redvial.model.Nodo;
import com.odiparpack.logistics.redvial.model.RedVial;
import com.odiparpack.logistics.redvial.model.Ubicacion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Operador de Destrucción Específico de Dominio: Identifica tramos viales afectados
 * por bloqueos temporales (RF-12) y remueve los pedidos cuyas rutas atraviesan dichos tramos
 * para permitir su replanificación libre de bloqueos.
 */
public class BloqueoDestroyOperator extends DestroyOperator {

    private final RedVial redVial;

    public BloqueoDestroyOperator(RedVial redVial) {
        this.redVial = redVial;
    }

    @Override
    public String getNombre() {
        return "BloqueoDestroyOperator";
    }

    @Override
    public List<Pedido> destruir(PlanSolution solucion, double factorDestruccion) {
        List<Pedido> liberados = new ArrayList<>();
        if (solucion == null || solucion.getRutas().isEmpty() || redVial == null) {
            return liberados;
        }

        LocalDateTime ahora = LocalDateTime.now();

        for (Ruta r : solucion.getRutas()) {
            List<ParadaRuta> paradas = r.getParadas();
            if (paradas.isEmpty()) continue;

            Ubicacion posActual = (r.getAlmacenOrigen() != null && r.getAlmacenOrigen().getUbicacion() != null)
                    ? r.getAlmacenOrigen().getUbicacion()
                    : (r.getUnidadTransporte() != null && r.getUnidadTransporte().getUbicacionActual() != null
                    ? r.getUnidadTransporte().getUbicacionActual() : new Ubicacion(35, 25));

            Nodo nodoActual = redVial.obtenerNodo(posActual.getPosX(), posActual.getPosY());

            List<ParadaRuta> paradasARemover = new ArrayList<>();
            for (ParadaRuta p : paradas) {
                if (p.getPedido() == null) continue;
                Ubicacion dest = p.getPedido().getDestino() != null ? p.getPedido().getDestino() : new Ubicacion(35, 25);
                Nodo nodoDest = redVial.obtenerNodo(dest.getPosX(), dest.getPosY());

                // Verificar si la distancia libre de bloqueos difiere de la directa o si está bloqueado
                double distOrtogonal = (nodoActual != null && nodoDest != null)
                        ? redVial.calcularDistancia(nodoActual, nodoDest) : 0.0;
                double distReal = (nodoActual != null && nodoDest != null)
                        ? redVial.distanciaMinima(nodoActual, nodoDest, ahora) : distOrtogonal;

                if (distReal > distOrtogonal || distReal == Double.MAX_VALUE) {
                    paradasARemover.add(p);
                }
                nodoActual = nodoDest;
            }

            // Remover las paradas afectadas
            int limite = Math.max(1, (int) Math.round(paradas.size() * factorDestruccion));
            for (int i = 0; i < Math.min(limite, paradasARemover.size()); i++) {
                ParadaRuta afectada = paradasARemover.get(i);
                r.getParadas().remove(afectada);
                liberados.add(afectada.getPedido());
            }
        }

        return liberados;
    }
}
