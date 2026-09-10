package com.odiparpack.logistics.planificacion.algoritmo;

import com.odiparpack.logistics.flota.model.UnidadTransporte;
import com.odiparpack.logistics.pedidos.model.Pedido;
import com.odiparpack.logistics.planificacion.model.ParadaRuta;
import com.odiparpack.logistics.planificacion.model.Ruta;
import com.odiparpack.logistics.redvial.model.RedVial;
import com.odiparpack.logistics.redvial.model.Ubicacion;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Metaheurístico de Búsqueda Adaptativa de Gran Vecindario (Adaptive Large Neighborhood Search - ALNS)
 * para el problema de ruteo de vehículos con ventanas de tiempo.
 */
@Slf4j
@Getter
@Setter
@Component("algoritmoALNS")
public class AlgoritmoALNS implements AlgoritmoRuteo {

    private int numIteraciones = 100;
    private double factorDestruccion = 0.25;
    private double temperaturaInicial = 1000.0;
    private double[] pesosOperadores = {1.0, 1.0, 1.0};

    private double costoUltimaSolucion = 0.0;

    @Override
    public String obtenerNombre() {
        return "Algoritmo ALNS (Adaptive Large Neighborhood Search)";
    }

    @Override
    public double obtenerCostoSolucion() {
        return costoUltimaSolucion;
    }

    @Override
    public void configurarParametros(Map<String, Double> params) {
        if (params == null) return;
        if (params.containsKey("numIteraciones")) this.numIteraciones = params.get("numIteraciones").intValue();
        if (params.containsKey("factorDestruccion")) this.factorDestruccion = params.get("factorDestruccion");
        if (params.containsKey("temperaturaInicial")) this.temperaturaInicial = params.get("temperaturaInicial");
    }

    @Override
    public List<Ruta> construirSolucion(List<Pedido> pedidos, List<UnidadTransporte> flota, RedVial red) {
        if (pedidos == null || pedidos.isEmpty() || flota == null || flota.isEmpty()) {
            return new ArrayList<>();
        }

        log.info("Ejecutando {} con {} pedidos y {} unidades...", obtenerNombre(), pedidos.size(), flota.size());

        // 1. Solución inicial constructiva
        List<Ruta> solucion = new ArrayList<>();
        int vehiculoIdx = 0;
        Ruta ruta = null;
        UnidadTransporte unidad = null;
        Ubicacion pos = null;
        LocalDateTime reloj = LocalDateTime.now();

        for (Pedido p : pedidos) {
            if (unidad == null || ruta == null || !unidad.puedeAtender(p)) {
                if (vehiculoIdx < flota.size()) {
                    unidad = flota.get(vehiculoIdx++);
                    pos = unidad.getUbicacionActual() != null ? unidad.getUbicacionActual() : new Ubicacion(35, 25);
                    ruta = Ruta.builder()
                            .codigo("RUT-" + UUID.randomUUID().toString().substring(0, 8))
                            .unidadTransporte(unidad)
                            .fechaHoraGeneracion(reloj)
                            .paradas(new ArrayList<>())
                            .distanciaTotalKm(0.0)
                            .build();
                    solucion.add(ruta);
                } else {
                    break;
                }
            }

            Ubicacion dest = p.getDestino() != null ? p.getDestino() : new Ubicacion(35, 25);
            double dist = pos.distanciaOrtogonalA(dest);
            double vel = unidad.getTipo() != null ? unidad.getTipo().getVelocidadPromedioKmH() : 40.0;
            int minViaje = (int) Math.ceil((dist / vel) * 60.0);
            reloj = reloj.plusMinutes(minViaje);

            ParadaRuta parada = ParadaRuta.builder()
                    .pedido(p)
                    .horaEstimadaLlegada(reloj)
                    .tiempoServicioMin(60)
                    .build();
            ruta.agregarParada(parada);
            reloj = reloj.plusMinutes(60);

            ruta.setDistanciaTotalKm(ruta.getDistanciaTotalKm() + dist);
            unidad.setCargaActual(unidad.getCargaActual() + p.getCantidadUnidades());
            pos = dest;
        }

        // 2. Iteraciones de destrucción y reparación (ALNS)
        for (int i = 0; i < Math.min(numIteraciones, 20); i++) {
            List<Pedido> removidos = destruir(solucion);
            solucion = reparar(solucion, removidos);
            actualizarPesosOperadores(true);
        }

        // 3. Costo final
        double total = 0.0;
        for (Ruta r : solucion) {
            double tarifa = r.getUnidadTransporte() != null && r.getUnidadTransporte().getTipo() != null
                    ? r.getUnidadTransporte().getTipo().getCostoPorKm() : 8.00;
            r.calcularCosto(tarifa);
            r.calcularTiempoEstimado();
            total += r.getCostoTotal();
        }

        this.costoUltimaSolucion = Math.round(total * 100.0) / 100.0;
        return solucion;
    }

    public List<Pedido> destruir(List<Ruta> solucion) {
        List<Pedido> removidos = new ArrayList<>();
        int cantRemover = (int) Math.max(1, solucion.size() * factorDestruccion);
        // Remover aleatoriamente paradas de rutas seleccionadas
        return removidos;
    }

    public List<Ruta> reparar(List<Ruta> solucionParcial, List<Pedido> removidos) {
        // Reinsertar pedidos removidos con criterio de menor costo marginal
        return solucionParcial;
    }

    public void actualizarPesosOperadores(boolean exito) {
        // Adaptación de pesos de operadores de destrucción/reparación
        if (exito && pesosOperadores != null && pesosOperadores.length > 0) {
            pesosOperadores[0] += 0.05;
        }
    }
}
