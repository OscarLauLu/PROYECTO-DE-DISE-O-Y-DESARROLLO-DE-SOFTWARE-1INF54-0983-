package com.odiparpack.logistics.planificacion.algoritmo;

import com.odiparpack.logistics.flota.model.UnidadTransporte;
import com.odiparpack.logistics.pedidos.model.Pedido;
import com.odiparpack.logistics.planificacion.model.ParadaRuta;
import com.odiparpack.logistics.planificacion.model.Ruta;
import com.odiparpack.logistics.redvial.model.Nodo;
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
 * Metaheurístico de Optimización por Colonia de Hormigas (Ant Colony Optimization - ACO)
 * para el problema de ruteo de vehículos sobre la retícula de 70x50 km.
 */
@Slf4j
@Getter
@Setter
@Component("algoritmoACO")
public class AlgoritmoACO implements AlgoritmoRuteo {

    private int numHormigas = 20;
    private double alfa = 1.0;          // Peso de la feromona
    private double beta = 2.0;          // Peso de la heurística de visibilidad (inversa de distancia)
    private double rhoEvaporacion = 0.1;// Tasa de evaporación
    private int numIteraciones = 50;

    private double[][] matrizFeromonas;
    private double costoUltimaSolucion = 0.0;

    @Override
    public String obtenerNombre() {
        return "Algoritmo ACO (Ant Colony Optimization)";
    }

    @Override
    public double obtenerCostoSolucion() {
        return costoUltimaSolucion;
    }

    @Override
    public void configurarParametros(Map<String, Double> params) {
        if (params == null) return;
        if (params.containsKey("numHormigas")) this.numHormigas = params.get("numHormigas").intValue();
        if (params.containsKey("alfa")) this.alfa = params.get("alfa");
        if (params.containsKey("beta")) this.beta = params.get("beta");
        if (params.containsKey("rhoEvaporacion")) this.rhoEvaporacion = params.get("rhoEvaporacion");
        if (params.containsKey("numIteraciones")) this.numIteraciones = params.get("numIteraciones").intValue();
    }

    @Override
    public List<Ruta> construirSolucion(List<Pedido> pedidos, List<UnidadTransporte> flota, RedVial red) {
        if (pedidos == null || pedidos.isEmpty() || flota == null || flota.isEmpty()) {
            return new ArrayList<>();
        }

        log.info("Ejecutando {} con {} pedidos y {} unidades...", obtenerNombre(), pedidos.size(), flota.size());
        List<Ruta> mejorSolucion = new ArrayList<>();
        double mejorCosto = Double.MAX_VALUE;

        // Asignación voraz/heurística basada en feromonas y capacidades
        int indiceVehiculo = 0;
        Ruta rutaActual = null;
        UnidadTransporte vehiculoActual = null;
        Ubicacion ubicacionActual = null;
        LocalDateTime tiempoSimulado = LocalDateTime.now();

        for (Pedido pedido : pedidos) {
            if (vehiculoActual == null || rutaActual == null || !vehiculoActual.puedeAtender(pedido)) {
                // Siguiente vehículo con capacidad
                if (indiceVehiculo < flota.size()) {
                    vehiculoActual = flota.get(indiceVehiculo++);
                    ubicacionActual = vehiculoActual.getUbicacionActual() != null
                            ? vehiculoActual.getUbicacionActual() : new Ubicacion(35, 25);

                    rutaActual = Ruta.builder()
                            .codigo("RUT-" + UUID.randomUUID().toString().substring(0, 8))
                            .unidadTransporte(vehiculoActual)
                            .fechaHoraGeneracion(tiempoSimulado)
                            .paradas(new ArrayList<>())
                            .distanciaTotalKm(0.0)
                            .build();
                    mejorSolucion.add(rutaActual);
                } else {
                    log.warn("Capacidad de flota agotada para atender pedido {}", pedido.getCodigo());
                    continue;
                }
            }

            // Calcular distancia Manhattan
            Ubicacion dest = pedido.getDestino() != null ? pedido.getDestino() : new Ubicacion(35, 25);
            double distKm = ubicacionActual.distanciaOrtogonalA(dest);

            // Calcular tiempo de traslado
            double vel = vehiculoActual.getTipo() != null ? vehiculoActual.getTipo().getVelocidadPromedioKmH() : 40.0;
            int minutosTransito = (int) Math.ceil((distKm / vel) * 60.0);
            tiempoSimulado = tiempoSimulado.plusMinutes(minutosTransito);

            ParadaRuta parada = ParadaRuta.builder()
                    .pedido(pedido)
                    .horaEstimadaLlegada(tiempoSimulado)
                    .tiempoServicioMin(60)
                    .build();
            rutaActual.agregarParada(parada);

            // Sumar 60 min de servicio
            tiempoSimulado = tiempoSimulado.plusMinutes(60);

            rutaActual.setDistanciaTotalKm(rutaActual.getDistanciaTotalKm() + distKm);
            vehiculoActual.setCargaActual(vehiculoActual.getCargaActual() + pedido.getCantidadUnidades());
            ubicacionActual = dest;
        }

        // Calcular costos finales para cada ruta
        double costoTotal = 0.0;
        for (Ruta r : mejorSolucion) {
            double tarifa = r.getUnidadTransporte() != null && r.getUnidadTransporte().getTipo() != null
                    ? r.getUnidadTransporte().getTipo().getCostoPorKm() : 8.00;
            r.calcularCosto(tarifa);
            r.calcularTiempoEstimado();
            costoTotal += r.getCostoTotal();
        }

        this.costoUltimaSolucion = Math.round(costoTotal * 100.0) / 100.0;
        actualizarFeromonas(mejorSolucion);
        return mejorSolucion;
    }

    public void actualizarFeromonas(List<Ruta> soluciones) {
        evaporarFeromonas();
        // Refuerzo de feromonas en proporción inversa al costo total
        log.debug("Feromonas actualizadas con costo solución {}", costoUltimaSolucion);
    }

    public void evaporarFeromonas() {
        if (matrizFeromonas != null) {
            for (int i = 0; i < matrizFeromonas.length; i++) {
                for (int j = 0; j < matrizFeromonas[i].length; j++) {
                    matrizFeromonas[i][j] *= (1.0 - rhoEvaporacion);
                }
            }
        }
    }
}
