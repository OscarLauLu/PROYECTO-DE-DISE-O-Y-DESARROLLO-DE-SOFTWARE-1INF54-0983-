package com.odiparpack.logistics.planificacion.algoritmo.aco;

import com.odiparpack.logistics.pedidos.model.Pedido;
import com.odiparpack.logistics.redvial.model.Nodo;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Representa una hormiga que construye probabilísticamente un itinerario de paradas (RF-06).
 */
@Getter
@Setter
public class Hormiga {

    private final List<Pedido> pedidosAtendidos = new ArrayList<>();
    private final Set<Pedido> visitados = new HashSet<>();
    private final Map<Pedido, LocalDateTime> horaLlegada = new HashMap<>();
    private final Map<Pedido, List<Nodo>> tramoHaciaPedido = new HashMap<>();

    private int cargaAcumulada = 0;
    private double costoAcumulado = 0.0;
    private double distanciaAcumuladaKm = 0.0;
    private int tiempoAcumuladoMin = 0;

    public boolean yaAtendio(Pedido p) {
        return visitados.contains(p);
    }

    public boolean cumpleTodosLosPlazos() {
        for (Pedido p : pedidosAtendidos) {
            LocalDateTime llegada = horaLlegada.get(p);
            if (llegada != null && p.getPlazoLimiteEntrega() != null && llegada.isAfter(p.getPlazoLimiteEntrega())) {
                return false;
            }
        }
        return true;
    }
}
