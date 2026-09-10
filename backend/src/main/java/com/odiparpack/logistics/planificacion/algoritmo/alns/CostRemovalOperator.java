package com.odiparpack.logistics.planificacion.algoritmo.alns;

import com.odiparpack.logistics.pedidos.model.Pedido;
import com.odiparpack.logistics.planificacion.model.ParadaRuta;
import com.odiparpack.logistics.planificacion.model.Ruta;
import com.odiparpack.logistics.redvial.model.Ubicacion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Operador de Destrucción General: Remueve los pedidos que aportan el mayor costo marginal
 * o distancia relativa a su ruta actual (sección 3.3).
 */
public class CostRemovalOperator extends DestroyOperator {

    @Override
    public String getNombre() {
        return "CostRemovalOperator";
    }

    private static class CandidatoCosto {
        final Pedido pedido;
        final double costoMarginal;

        CandidatoCosto(Pedido pedido, double costoMarginal) {
            this.pedido = pedido;
            this.costoMarginal = costoMarginal;
        }
    }

    @Override
    public List<Pedido> destruir(PlanSolution solucion, double factorDestruccion) {
        List<Pedido> liberados = new ArrayList<>();
        if (solucion == null || solucion.getRutas().isEmpty()) {
            return liberados;
        }

        List<CandidatoCosto> candidatos = new ArrayList<>();

        for (Ruta r : solucion.getRutas()) {
            List<ParadaRuta> paradas = r.getParadas();
            double tarifa = (r.getUnidadTransporte() != null && r.getUnidadTransporte().getTipo() != null)
                    ? r.getUnidadTransporte().getTipo().getCostoPorKm() : 8.0;

            Ubicacion origen = (r.getAlmacenOrigen() != null && r.getAlmacenOrigen().getUbicacion() != null)
                    ? r.getAlmacenOrigen().getUbicacion()
                    : (r.getUnidadTransporte() != null && r.getUnidadTransporte().getUbicacionActual() != null
                    ? r.getUnidadTransporte().getUbicacionActual() : new Ubicacion(35, 25));

            for (int i = 0; i < paradas.size(); i++) {
                ParadaRuta p = paradas.get(i);
                if (p.getPedido() == null) continue;

                Ubicacion prev = (i == 0) ? origen : paradas.get(i - 1).getPedido().getDestino();
                Ubicacion curr = p.getPedido().getDestino();
                double distAnterior = (prev != null && curr != null) ? prev.distanciaOrtogonalA(curr) : 0.0;

                double costoMarginal = distAnterior * tarifa;
                candidatos.add(new CandidatoCosto(p.getPedido(), costoMarginal));
            }
        }

        if (candidatos.isEmpty()) return liberados;

        // Ordenar de mayor a menor costo marginal
        candidatos.sort(Comparator.comparingDouble((CandidatoCosto c) -> c.costoMarginal).reversed());

        int numARemover = Math.max(1, (int) Math.round(candidatos.size() * factorDestruccion));
        for (int i = 0; i < Math.min(numARemover, candidatos.size()); i++) {
            Pedido pedido = candidatos.get(i).pedido;
            removerPedido(solucion, pedido);
            liberados.add(pedido);
        }

        return liberados;
    }

    private void removerPedido(PlanSolution solucion, Pedido pedido) {
        for (Ruta r : solucion.getRutas()) {
            boolean removed = r.getParadas().removeIf(p -> p.getPedido() != null && p.getPedido().getId().equals(pedido.getId()));
            if (removed) break;
        }
    }
}
