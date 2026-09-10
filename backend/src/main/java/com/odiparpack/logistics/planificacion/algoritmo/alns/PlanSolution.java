package com.odiparpack.logistics.planificacion.algoritmo.alns;

import com.odiparpack.logistics.almacen.model.Almacen;
import com.odiparpack.logistics.almacen.model.AlmacenCentral;
import com.odiparpack.logistics.almacen.model.AlmacenIntermedio;
import com.odiparpack.logistics.flota.model.EstadoOperativo;
import com.odiparpack.logistics.flota.model.UnidadTransporte;
import com.odiparpack.logistics.pedidos.model.Pedido;
import com.odiparpack.logistics.planificacion.model.ParadaRuta;
import com.odiparpack.logistics.planificacion.model.Ruta;
import com.odiparpack.logistics.redvial.model.Nodo;
import com.odiparpack.logistics.redvial.model.RedVial;
import com.odiparpack.logistics.redvial.model.Ubicacion;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Representa el estado de una solución completa de planificación para ALNS (RF-06).
 * Modela la asignación de pedidos a rutas de vehículos y almacenes de origen,
 * permitiendo evaluar restricciones duras y costos totales.
 */
@Getter
@Setter
public class PlanSolution {

    private List<Ruta> rutas = new ArrayList<>();
    private List<Pedido> pedidosNoAsignados = new ArrayList<>();

    public PlanSolution() {
    }

    /**
     * Construye una solución inicial voraz factible (RF-02, RF-04, RF-07, RF-08).
     */
    public static PlanSolution asignacionVoraz(List<Pedido> pedidos, List<UnidadTransporte> flota,
                                              List<Almacen> almacenes, RedVial red, LocalDateTime tiempoInicio) {
        PlanSolution plan = new PlanSolution();
        if (pedidos == null || pedidos.isEmpty() || flota == null || flota.isEmpty()) {
            return plan;
        }

        HolguraGreedyRepairOperator repair = new HolguraGreedyRepairOperator(red, almacenes, flota);
        repair.reparar(plan, pedidos);

        plan.rutas.removeIf(r -> r.getParadas().isEmpty());
        return plan;
    }

    private static Ruta crearNuevaRuta(UnidadTransporte vehiculo, Almacen almacen, LocalDateTime tiempoInicio) {
        return Ruta.builder()
                .codigo("RUT-" + UUID.randomUUID().toString().substring(0, 8))
                .unidadTransporte(vehiculo)
                .almacenOrigen(almacen)
                .fechaHoraGeneracion(tiempoInicio)
                .distanciaTotalKm(0.0)
                .costoTotal(0.0)
                .tiempoEstimadoMin(0)
                .paradas(new ArrayList<>())
                .build();
    }

    private static Nodo obtenerNodoOrigen(UnidadTransporte vehiculo, Almacen almacen, RedVial red) {
        if (almacen != null && almacen.getUbicacion() != null) {
            return red.obtenerNodo(almacen.getUbicacion().getPosX(), almacen.getUbicacion().getPosY());
        }
        if (vehiculo != null && vehiculo.getUbicacionActual() != null) {
            return red.obtenerNodo(vehiculo.getUbicacionActual().getPosX(), vehiculo.getUbicacionActual().getPosY());
        }
        return red.obtenerNodo(35, 25);
    }

    /**
     * Evalúa el costo total de la solución: suma de costos por distancia + penalización por pedidos no atendidos.
     */
    public double costoTotal() {
        double total = 0.0;
        for (Ruta r : rutas) {
            total += r.getCostoTotal();
        }
        // Penalización por pedido no atendido para forzar mayor cobertura
        total += pedidosNoAsignados.size() * 1000.0;
        return total;
    }

    /**
     * Valida restricciones duras: capacidades de vehículos (RF-07), plazos de entrega (RF-08),
     * stock y umbrales de almacén (RF-22, RF-25), y operatividad de la flota (RF-14).
     */
    public boolean cumpleRestriccionesDuras(List<UnidadTransporte> flota, List<Almacen> almacenes) {
        // 1. Validar cada ruta
        for (Ruta r : rutas) {
            if (r.capacidadExcedida()) return false;
            if (!r.cumplePlazos()) return false;
            if (r.getUnidadTransporte() != null && r.getUnidadTransporte().getEstadoOperativo() == EstadoOperativo.AVERIADA) {
                return false;
            }
        }

        // 2. Validar capacidad y stock de almacenes
        Map<Almacen, Integer> demandaPorAlmacen = new HashMap<>();
        for (Ruta r : rutas) {
            if (r.getAlmacenOrigen() != null) {
                int demandaRuta = r.getParadas().stream()
                        .mapToInt(p -> p.getPedido() != null ? p.getPedido().getCantidadUnidades() : 0)
                        .sum();
                demandaPorAlmacen.merge(r.getAlmacenOrigen(), demandaRuta, Integer::sum);
            }
        }

        for (Map.Entry<Almacen, Integer> entry : demandaPorAlmacen.entrySet()) {
            Almacen a = entry.getKey();
            int demanda = entry.getValue();
            if (!a.tieneStockSuficiente(demanda)) {
                return false;
            }
            if (a instanceof AlmacenIntermedio intermedio) {
                double ocupacionFutura = ((intermedio.getStockActual() - demanda) * 100.0) / intermedio.getCapacidadMaxima();
                if (ocupacionFutura < 0) return false;
            }
        }

        return true;
    }

    /**
     * Recalcula distancias, tiempos de llegada y costos de todas las rutas de la solución.
     */
    public void recalcularMetricas(RedVial red, LocalDateTime tiempoInicio) {
        for (Ruta r : rutas) {
            if (r.getParadas().isEmpty()) {
                r.setDistanciaTotalKm(0.0);
                r.setCostoTotal(0.0);
                r.setTiempoEstimadoMin(0);
                continue;
            }

            Nodo actual = (r.getAlmacenOrigen() != null && r.getAlmacenOrigen().getUbicacion() != null)
                    ? red.obtenerNodo(r.getAlmacenOrigen().getUbicacion().getPosX(), r.getAlmacenOrigen().getUbicacion().getPosY())
                    : (r.getUnidadTransporte() != null && r.getUnidadTransporte().getUbicacionActual() != null
                    ? red.obtenerNodo(r.getUnidadTransporte().getUbicacionActual().getPosX(), r.getUnidadTransporte().getUbicacionActual().getPosY())
                    : red.obtenerNodo(35, 25));

            LocalDateTime reloj = tiempoInicio;
            double distTotal = 0.0;
            double velocidad = (r.getUnidadTransporte() != null && r.getUnidadTransporte().getTipo() != null)
                    ? r.getUnidadTransporte().getTipo().getVelocidadPromedioKmH() : 40.0;
            double tarifa = (r.getUnidadTransporte() != null && r.getUnidadTransporte().getTipo() != null)
                    ? r.getUnidadTransporte().getTipo().getCostoPorKm() : 8.0;

            int orden = 1;
            for (ParadaRuta parada : r.getParadas()) {
                parada.setOrden(orden++);
                Ubicacion dest = parada.getPedido().getDestino() != null ? parada.getPedido().getDestino() : new Ubicacion(35, 25);
                Nodo nodoDest = red.obtenerNodo(dest.getPosX(), dest.getPosY());

                double d = red.distanciaMinima(actual, nodoDest, reloj);
                if (d == Double.MAX_VALUE) {
                    d = actual.aUbicacion().distanciaOrtogonalA(dest);
                }
                distTotal += d;

                long minViaje = (long) Math.ceil((d / velocidad) * 60.0);
                LocalDateTime llegada = reloj.plusMinutes(minViaje);
                parada.setHoraEstimadaLlegada(llegada);

                reloj = llegada.plusMinutes(parada.getTiempoServicioMin());
                actual = nodoDest;
            }

            if (!r.getParadas().isEmpty()) {
                Nodo origen = (r.getAlmacenOrigen() != null && r.getAlmacenOrigen().getUbicacion() != null)
                        ? red.obtenerNodo(r.getAlmacenOrigen().getUbicacion().getPosX(), r.getAlmacenOrigen().getUbicacion().getPosY())
                        : red.obtenerNodo(35, 25);
                double distRetorno = red.distanciaMinima(actual, origen, reloj);
                if (distRetorno == Double.MAX_VALUE) {
                    distRetorno = actual.aUbicacion().distanciaOrtogonalA(new Ubicacion(35, 25));
                }
                distTotal += distRetorno;
                long minRetorno = (long) Math.ceil((distRetorno / velocidad) * 60.0);
                reloj = reloj.plusMinutes(minRetorno);
            }

            r.setDistanciaTotalKm(Math.round(distTotal * 100.0) / 100.0);
            r.setCostoTotal(Math.round((distTotal * tarifa) * 100.0) / 100.0);
            r.setTiempoEstimadoMin((int) Duration.between(tiempoInicio, reloj).toMinutes());
        }
    }

    /**
     * Crea un clon profundo e independiente de la solución.
     */
    public PlanSolution clonar() {
        PlanSolution copia = new PlanSolution();
        for (Ruta r : this.rutas) {
            Ruta rCopia = Ruta.builder()
                    .codigo(r.getCodigo())
                    .unidadTransporte(r.getUnidadTransporte())
                    .almacenOrigen(r.getAlmacenOrigen())
                    .fechaHoraGeneracion(r.getFechaHoraGeneracion())
                    .distanciaTotalKm(r.getDistanciaTotalKm())
                    .tiempoEstimadoMin(r.getTiempoEstimadoMin())
                    .costoTotal(r.getCostoTotal())
                    .estado(r.getEstado())
                    .paradas(new ArrayList<>())
                    .build();

            for (ParadaRuta p : r.getParadas()) {
                ParadaRuta pCopia = ParadaRuta.builder()
                        .pedido(p.getPedido())
                        .orden(p.getOrden())
                        .horaEstimadaLlegada(p.getHoraEstimadaLlegada())
                        .tiempoServicioMin(p.getTiempoServicioMin())
                        .entregada(p.isEntregada())
                        .build();
                rCopia.agregarParada(pCopia);
            }
            copia.rutas.add(rCopia);
        }
        copia.pedidosNoAsignados.addAll(this.pedidosNoAsignados);
        return copia;
    }
}
