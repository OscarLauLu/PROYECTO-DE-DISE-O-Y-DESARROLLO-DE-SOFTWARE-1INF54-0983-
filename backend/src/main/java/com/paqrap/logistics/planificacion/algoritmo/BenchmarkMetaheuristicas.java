package com.paqrap.logistics.planificacion.algoritmo;

import com.paqrap.logistics.flota.model.EstadoOperativo;
import com.paqrap.logistics.flota.model.TipoVehiculo;
import com.paqrap.logistics.flota.model.UnidadTransporte;
import com.paqrap.logistics.pedidos.model.Pedido;
import com.paqrap.logistics.planificacion.model.ParadaRuta;
import com.paqrap.logistics.planificacion.model.Ruta;
import com.paqrap.logistics.redvial.model.Bloqueo;
import com.paqrap.logistics.redvial.model.RedVial;
import com.paqrap.logistics.redvial.model.Ubicacion;
import com.paqrap.logistics.simulacion.model.CargadorArchivos;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Runner ejecutable independiente para realizar benchmarks y pruebas comparativas
 * de AlgoritmoACO vs AlgoritmoALNS utilizando los datos reales de prueba (RF-06).
 */
public class BenchmarkMetaheuristicas {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("     BENCHMARK DE ALGORITMOS METAHEURÍSTICOS: ACO vs. ALNS (PaqRap)        ");
        System.out.println("================================================================================");

        File dirDatos = new File("datos");
        if (!dirDatos.exists()) {
            dirDatos = new File("../datos");
        }
        if (!dirDatos.exists()) {
            dirDatos = new File("/home/sandbox/Documents/DP1/PROYECTO-DE-DISE-O-Y-DESARROLLO-DE-SOFTWARE-1INF54-0983-/datos");
        }
        String datosDir = dirDatos.getAbsolutePath();

        String rutaVentas = datosDir + "/ventas.v20260909/ventas.202601.txt";
        String rutaBloqueos = datosDir + "/bloqueos/bloqueo.2601.txt";
        int maxPedidos = 30;

        if (args.length > 0 && !args[0].trim().isEmpty()) {
            if (new File(args[0]).exists()) {
                rutaVentas = args[0];
            } else if (args[0].matches("\\d{6}")) {
                rutaVentas = datosDir + "/ventas.v20260909/ventas." + args[0] + ".txt";
                rutaBloqueos = datosDir + "/bloqueos/bloqueo." + args[0].substring(2) + ".txt";
            }
        }
        if (args.length > 1 && !args[1].trim().isEmpty() && new File(args[1]).exists()) {
            rutaBloqueos = args[1];
        }
        if (args.length > 2 && !args[2].trim().isEmpty()) {
            try {
                maxPedidos = Integer.parseInt(args[2].trim());
            } catch (Exception ignored) {}
        }

        System.out.println("Archivo de ventas   : " + rutaVentas);
        System.out.println("Archivo de bloqueos : " + rutaBloqueos);
        System.out.println("Límite de pedidos   : " + (maxPedidos > 0 ? maxPedidos : "Todos"));

        // 1. Inicializar Red Vial
        RedVial red = new RedVial();
        red.inicializarRed();

        // 2. Cargar Bloqueos
        CargadorArchivos cargador = new CargadorArchivos();
        List<Bloqueo> bloqueos = cargador.cargarBloqueos(rutaBloqueos);
        System.out.println("Bloqueos cargados   : " + bloqueos.size());
        for (Bloqueo b : bloqueos) {
            red.aplicarBloqueo(b);
        }

        // 3. Cargar Pedidos
        List<Pedido> pedidosTodos = cargador.cargarPedidos(rutaVentas);
        System.out.println("Pedidos en archivo  : " + pedidosTodos.size());
        if (pedidosTodos.isEmpty()) {
            System.err.println("No se pudieron cargar pedidos. Verifique la ruta del archivo.");
            return;
        }

        List<Pedido> pedidosPrueba = new ArrayList<>();
        int count = 0;
        for (Pedido p : pedidosTodos) {
            pedidosPrueba.add(p);
            count++;
            if (maxPedidos > 0 && count >= maxPedidos) break;
        }
        System.out.println("Pedidos a evaluar   : " + pedidosPrueba.size());

        // 4. Crear flota estándar (4 Autos, 3 Motos, 3 Bicicletas)
        List<UnidadTransporte> flotaAco = crearFlotaEstandar();
        List<UnidadTransporte> flotaAlns = crearFlotaEstandar();

        // 5. Ejecutar ACO
        System.out.println("\n>>> Ejecutando Algoritmo ACO (Ant Colony Optimization)...");
        AlgoritmoACO aco = new AlgoritmoACO();
        long tInicioAco = System.currentTimeMillis();
        List<Ruta> rutasAco = aco.construirSolucion(pedidosPrueba, flotaAco, red);
        long tFinAco = System.currentTimeMillis();
        long duracionAco = tFinAco - tInicioAco;

        // 6. Ejecutar ALNS
        System.out.println(">>> Ejecutando Algoritmo ALNS (Adaptive Large Neighborhood Search)...");
        AlgoritmoALNS alns = new AlgoritmoALNS();
        long tInicioAlns = System.currentTimeMillis();
        List<Ruta> rutasAlns = alns.construirSolucion(pedidosPrueba, flotaAlns, red);
        long tFinAlns = System.currentTimeMillis();
        long duracionAlns = tFinAlns - tInicioAlns;

        // 7. Imprimir tabla comparativa
        imprimirResultados(pedidosPrueba.size(), "ACO (Ant Colony)", rutasAco, duracionAco,
                           "ALNS (Large Neighborhood)", rutasAlns, duracionAlns);
    }

    public static List<UnidadTransporte> crearFlotaEstandar() {
        List<UnidadTransporte> flota = new ArrayList<>();
        TipoVehiculo auto = TipoVehiculo.builder().id(1L).nombre("Auto").capacidadMaxima(24).velocidadPromedioKmH(40.0).costoPorKm(8.0).build();
        TipoVehiculo moto = TipoVehiculo.builder().id(2L).nombre("Moto").capacidadMaxima(8).velocidadPromedioKmH(25.0).costoPorKm(6.0).build();
        TipoVehiculo bici = TipoVehiculo.builder().id(3L).nombre("Bicicleta").capacidadMaxima(4).velocidadPromedioKmH(12.0).costoPorKm(3.0).build();

        // 4 Autos
        for (int i = 1; i <= 4; i++) {
            flota.add(UnidadTransporte.builder().id((long) i).codigo("AUT-0" + i).tipo(auto).estadoOperativo(EstadoOperativo.DISPONIBLE).ubicacionActual(new Ubicacion(35, 25)).activo(true).build());
        }
        // 3 Motos
        for (int i = 1; i <= 3; i++) {
            flota.add(UnidadTransporte.builder().id((long) (4 + i)).codigo("MOT-0" + i).tipo(moto).estadoOperativo(EstadoOperativo.DISPONIBLE).ubicacionActual(new Ubicacion(35, 25)).activo(true).build());
        }
        // 3 Bicicletas
        for (int i = 1; i <= 3; i++) {
            flota.add(UnidadTransporte.builder().id((long) (7 + i)).codigo("BIC-0" + i).tipo(bici).estadoOperativo(EstadoOperativo.DISPONIBLE).ubicacionActual(new Ubicacion(35, 25)).activo(true).build());
        }
        return flota;
    }

    private static void imprimirResultados(int totalPedidos,
                                           String nombre1, List<Ruta> rutas1, long tiempo1,
                                           String nombre2, List<Ruta> rutas2, long tiempo2) {
        MetricasSolucion m1 = calcularMetricas(totalPedidos, rutas1, tiempo1);
        MetricasSolucion m2 = calcularMetricas(totalPedidos, rutas2, tiempo2);

        System.out.println("\n" + "=".repeat(80));
        System.out.println(String.format("%-35s | %-20s | %-20s", "MÉTRICA / INDICADOR", nombre1, nombre2));
        System.out.println("-".repeat(80));
        System.out.println(String.format("%-35s | %-20d | %-20d", "Total pedidos evaluados", totalPedidos, totalPedidos));
        System.out.println(String.format("%-35s | %-20d | %-20d", "Pedidos atendidos", m1.pedidosAtendidos, m2.pedidosAtendidos));
        System.out.println(String.format("%-35s | %-19.1f%% | %-19.1f%%", "Tasa de atención", m1.tasaAtencion, m2.tasaAtencion));
        System.out.println(String.format("%-35s | %-19.1f%% | %-19.1f%%", "Entregas en plazo (On-Time)", m1.pctEnPlazo, m2.pctEnPlazo));
        System.out.println(String.format("%-35s | %-20d | %-20d", "Rutas vehiculares construidas", m1.numRutas, m2.numRutas));
        System.out.println(String.format("%-35s | %-17.2f km | %-17.2f km", "Distancia total recorrida", m1.distanciaTotalKm, m2.distanciaTotalKm));
        System.out.println(String.format("%-35s | S/ %-17.2f | S/ %-17.2f", "Costo operativo total", m1.costoTotal, m2.costoTotal));
        System.out.println(String.format("%-35s | %-17d ms | %-17d ms", "Tiempo de ejecución de CPU", m1.tiempoMs, m2.tiempoMs));
        System.out.println(String.format("%-35s | %-17.1f min | %-17.1f min", "Tiempo prom. por ruta", m1.tiempoPromedioMin, m2.tiempoPromedioMin));
        System.out.println("=".repeat(80));

        // Detalle de rutas
        System.out.println("\n--- DETALLE DE RUTAS GENERADAS POR " + nombre1.toUpperCase() + " ---");
        imprimirDetalleRutas(rutas1);

        System.out.println("\n--- DETALLE DE RUTAS GENERADAS POR " + nombre2.toUpperCase() + " ---");
        imprimirDetalleRutas(rutas2);
    }

    private static void imprimirDetalleRutas(List<Ruta> rutas) {
        if (rutas == null || rutas.isEmpty()) {
            System.out.println("  (No se generaron rutas)");
            return;
        }
        for (Ruta r : rutas) {
            String vehiculo = r.getUnidadTransporte() != null ? r.getUnidadTransporte().getCodigo() : "N/A";
            String tipo = (r.getUnidadTransporte() != null && r.getUnidadTransporte().getTipo() != null)
                    ? r.getUnidadTransporte().getTipo().getNombre() : "N/A";
            int cargaTotal = r.getParadas().stream()
                    .mapToInt(p -> p.getPedido() != null ? p.getPedido().getCantidadUnidades() : 0).sum();
            System.out.println(String.format("  [%s] Vehículo: %s (%s) | Paradas: %d | Carga: %d u. | Dist: %.1f km | Costo: S/ %.2f | Tiempo: %d min",
                    r.getCodigo(), vehiculo, tipo, r.getParadas().size(), cargaTotal, r.getDistanciaTotalKm(), r.getCostoTotal(), r.getTiempoEstimadoMin()));

            for (ParadaRuta p : r.getParadas()) {
                if (p.getPedido() != null) {
                    Ubicacion dest = p.getPedido().getDestino();
                    String coords = dest != null ? "(" + dest.getPosX() + "," + dest.getPosY() + ")" : "N/A";
                    System.out.println(String.format("     -> Pedido: %s | Destino: %-9s | Cant: %2d u. | Plazo: %s | Estimada: %s",
                            p.getPedido().getCodigo(), coords, p.getPedido().getCantidadUnidades(),
                            p.getPedido().getPlazoLimiteEntrega(), p.getHoraEstimadaLlegada()));
                }
            }
        }
    }

    private static MetricasSolucion calcularMetricas(int totalPedidos, List<Ruta> rutas, long tiempoMs) {
        MetricasSolucion m = new MetricasSolucion();
        m.tiempoMs = tiempoMs;
        if (rutas == null) return m;

        m.numRutas = rutas.size();
        int aTiempo = 0;
        for (Ruta r : rutas) {
            m.distanciaTotalKm += r.getDistanciaTotalKm();
            m.costoTotal += r.getCostoTotal();
            m.tiempoPromedioMin += r.getTiempoEstimadoMin();
            for (ParadaRuta p : r.getParadas()) {
                m.pedidosAtendidos++;
                if (p.getPedido() != null && p.getHoraEstimadaLlegada() != null && p.getPedido().getPlazoLimiteEntrega() != null) {
                    if (!p.getHoraEstimadaLlegada().isAfter(p.getPedido().getPlazoLimiteEntrega())) {
                        aTiempo++;
                    }
                } else {
                    aTiempo++;
                }
            }
        }
        if (m.numRutas > 0) {
            m.tiempoPromedioMin /= m.numRutas;
        }
        m.tasaAtencion = totalPedidos > 0 ? (m.pedidosAtendidos * 100.0) / totalPedidos : 0.0;
        m.pctEnPlazo = m.pedidosAtendidos > 0 ? (aTiempo * 100.0) / m.pedidosAtendidos : 100.0;
        return m;
    }

    private static class MetricasSolucion {
        int pedidosAtendidos = 0;
        double tasaAtencion = 0.0;
        double pctEnPlazo = 0.0;
        int numRutas = 0;
        double distanciaTotalKm = 0.0;
        double costoTotal = 0.0;
        long tiempoMs = 0;
        double tiempoPromedioMin = 0.0;
    }
}
