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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
        String rutaMantenimiento = datosDir + "/mant.preventivo.09.10.txt";
        int maxPedidos = 30;

        Integer customAutos = null;
        Integer customMotos = null;
        Integer customBicis = null;

        for (String arg : args) {
            if (arg.startsWith("--flota=")) {
                String[] fParts = arg.substring(8).split(",");
                if (fParts.length >= 3) {
                    try {
                        customAutos = Integer.parseInt(fParts[0].trim());
                        customMotos = Integer.parseInt(fParts[1].trim());
                        customBicis = Integer.parseInt(fParts[2].trim());
                    } catch (Exception ignored) {}
                }
            } else if (arg.startsWith("--mant=")) {
                rutaMantenimiento = arg.substring(7).trim();
            }
        }

        if (args.length > 0 && !args[0].trim().isEmpty() && !args[0].startsWith("--")) {
            if (new File(args[0]).exists()) {
                rutaVentas = args[0];
            } else if (args[0].matches("\\d{6}")) {
                rutaVentas = datosDir + "/ventas.v20260909/ventas." + args[0] + ".txt";
                rutaBloqueos = datosDir + "/bloqueos/bloqueo." + args[0].substring(2) + ".txt";
            }
        }
        if (args.length > 1 && !args[1].trim().isEmpty() && !args[1].startsWith("--") && new File(args[1]).exists()) {
            rutaBloqueos = args[1];
        }
        if (args.length > 2 && !args[2].trim().isEmpty() && !args[2].startsWith("--")) {
            try {
                maxPedidos = Integer.parseInt(args[2].trim());
            } catch (Exception ignored) {}
        }

        System.out.println("Archivo de ventas   : " + rutaVentas);
        System.out.println("Archivo de bloqueos : " + rutaBloqueos);
        System.out.println("Plan de mantenim.   : " + (new File(rutaMantenimiento).exists() ? rutaMantenimiento : "(No disponible)"));
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

        // 4. Crear flota dinámica (descubierta desde archivo de mantenimiento o por parámetros)
        List<UnidadTransporte> flotaAco = crearFlotaDinamica(rutaMantenimiento, customAutos, customMotos, customBicis);
        List<UnidadTransporte> flotaAlns = crearFlotaDinamica(rutaMantenimiento, customAutos, customMotos, customBicis);
        System.out.println("Flota total creada  : " + flotaAco.size() + " unidades");

        // 4.1. Aplicar Mantenimiento Preventivo según la fecha de los pedidos (RF-42 / Pregunta 19 FAQ)
        Map<LocalDate, List<String>> mantenimientos = cargador.cargarMantenimientosPreventivos(rutaMantenimiento);
        LocalDate fechaSimulada = pedidosPrueba.get(0).getFechaHoraRegistro().toLocalDate();
        List<String> vehiculosEnMant = mantenimientos.get(fechaSimulada);
        if (vehiculosEnMant != null && !vehiculosEnMant.isEmpty()) {
            System.out.println("\n" + "-".repeat(80));
            System.out.println("  [MANTENIMIENTO PREVENTIVO RF-42] Restricción de flota para fecha " + fechaSimulada + ":");
            for (String cod : vehiculosEnMant) {
                System.out.println("  -> Unidad " + cod + ": En mantenimiento programado -> Inhabilitada 00:00 - 23:59");
                for (UnidadTransporte u : flotaAco) {
                    if (cod.equalsIgnoreCase(u.getCodigo())) {
                        u.cambiarEstado(EstadoOperativo.EN_MANTENIMIENTO);
                        u.setActivo(false);
                    }
                }
                for (UnidadTransporte u : flotaAlns) {
                    if (cod.equalsIgnoreCase(u.getCodigo())) {
                        u.cambiarEstado(EstadoOperativo.EN_MANTENIMIENTO);
                        u.setActivo(false);
                    }
                }
            }
            System.out.println("-".repeat(80));
        }

        // 4.2. Evaluar si se solicita contingencia por averías mecánicas (RF-14, RF-15)
        boolean conAveria = false;
        for (String a : args) {
            if ("averia".equalsIgnoreCase(a) || "--averia".equalsIgnoreCase(a) || "--averias".equalsIgnoreCase(a)) {
                conAveria = true;
                break;
            }
        }
        if (conAveria) {
            System.out.println("\n" + "!".repeat(80));
            System.out.println("  [CONTINGENCIA RF-14 / RF-15] INYECCIÓN DE AVERÍAS EN LA FLOTA:");
            System.out.println("  -> TA02 (Auto): Falla mecánica en motor -> Estado: AVERIADA (Inhabilitado)");
            System.out.println("  -> TM01 (Moto): Falla en transmisión   -> Estado: AVERIADA (Inhabilitado)");
            System.out.println("!".repeat(80));

            for (UnidadTransporte u : flotaAco) {
                if ("TA02".equals(u.getCodigo()) || "TM01".equals(u.getCodigo())) {
                    u.cambiarEstado(EstadoOperativo.AVERIADA);
                    u.setActivo(false);
                }
            }
            for (UnidadTransporte u : flotaAlns) {
                if ("TA02".equals(u.getCodigo()) || "TM01".equals(u.getCodigo())) {
                    u.cambiarEstado(EstadoOperativo.AVERIADA);
                    u.setActivo(false);
                }
            }
        }

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
        return crearFlotaDinamica(null, 4, 3, 3);
    }

    public static List<UnidadTransporte> crearFlotaDinamica(String rutaMantenimiento, Integer nAutos, Integer nMotos, Integer nBicis) {
        List<UnidadTransporte> flota = new ArrayList<>();
        TipoVehiculo auto = TipoVehiculo.builder().id(1L).nombre("Auto").capacidadMaxima(24).velocidadPromedioKmH(40.0).costoPorKm(8.0).build();
        TipoVehiculo moto = TipoVehiculo.builder().id(2L).nombre("Moto").capacidadMaxima(8).velocidadPromedioKmH(25.0).costoPorKm(6.0).build();
        TipoVehiculo bici = TipoVehiculo.builder().id(3L).nombre("Bicicleta").capacidadMaxima(4).velocidadPromedioKmH(12.0).costoPorKm(3.0).build();

        Set<String> codigosEncontrados = new TreeSet<>();
        if (rutaMantenimiento != null && new File(rutaMantenimiento).exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(rutaMantenimiento))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    linea = linea.trim();
                    if (linea.isEmpty() || linea.startsWith("#")) continue;
                    String[] partes = linea.split(":");
                    if (partes.length == 2) {
                        codigosEncontrados.add(partes[1].trim());
                    }
                }
            } catch (Exception ignored) {}
        }

        // Si se especificaron cantidades por parámetro (ej. --flota=4,3,3)
        if (nAutos != null || nMotos != null || nBicis != null) {
            int cAutos = nAutos != null ? nAutos : 4;
            int cMotos = nMotos != null ? nMotos : 3;
            int cBicis = nBicis != null ? nBicis : 3;
            long id = 1;
            for (int i = 1; i <= cAutos; i++) {
                flota.add(UnidadTransporte.builder().id(id++).codigo(String.format("TA%02d", i)).tipo(auto).estadoOperativo(EstadoOperativo.DISPONIBLE).ubicacionActual(new Ubicacion(27, 14)).activo(true).build());
            }
            for (int i = 1; i <= cMotos; i++) {
                flota.add(UnidadTransporte.builder().id(id++).codigo(String.format("TM%02d", i)).tipo(moto).estadoOperativo(EstadoOperativo.DISPONIBLE).ubicacionActual(new Ubicacion(27, 14)).activo(true).build());
            }
            for (int i = 1; i <= cBicis; i++) {
                flota.add(UnidadTransporte.builder().id(id++).codigo(String.format("TB%02d", i)).tipo(bici).estadoOperativo(EstadoOperativo.DISPONIBLE).ubicacionActual(new Ubicacion(27, 14)).activo(true).build());
            }
            return flota;
        }

        // Si se encontraron vehículos en el archivo de mantenimiento, se construye la flota oficial dinámicamente
        if (!codigosEncontrados.isEmpty()) {
            long id = 1;
            for (String codigo : codigosEncontrados) {
                TipoVehiculo tipo;
                if (codigo.startsWith("TA")) tipo = auto;
                else if (codigo.startsWith("TM")) tipo = moto;
                else tipo = bici;

                flota.add(UnidadTransporte.builder()
                        .id(id++)
                        .codigo(codigo)
                        .tipo(tipo)
                        .estadoOperativo(EstadoOperativo.DISPONIBLE)
                        .ubicacionActual(new Ubicacion(27, 14))
                        .activo(true)
                        .build());
            }
            return flota;
        }

        // Fallback por defecto: 4 Autos, 3 Motos, 3 Bicis
        return crearFlotaDinamica(null, 4, 3, 3);
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
            int capMax = (r.getUnidadTransporte() != null && r.getUnidadTransporte().getTipo() != null)
                    ? r.getUnidadTransporte().getTipo().getCapacidadMaxima() : 0;
            System.out.println(String.format("  [%s] Vehículo: %s (%s) | Paradas: %d | Carga: %d/%d u. | Dist: %.1f km | Costo: S/ %.2f | Tiempo: %d min",
                    r.getCodigo(), vehiculo, tipo, r.getParadas().size(), cargaTotal, capMax, r.getDistanciaTotalKm(), r.getCostoTotal(), r.getTiempoEstimadoMin()));

            Ubicacion origen = (r.getAlmacenOrigen() != null && r.getAlmacenOrigen().getUbicacion() != null)
                    ? r.getAlmacenOrigen().getUbicacion() : new Ubicacion(27, 14);

            LocalDateTime tSalida = r.getFechaHoraGeneracion() != null ? r.getFechaHoraGeneracion()
                    : (!r.getParadas().isEmpty() && r.getParadas().get(0).getPedido() != null
                    ? r.getParadas().get(0).getPedido().getFechaHoraRegistro() : LocalDateTime.of(2026, 9, 1, 0, 0));
            LocalDateTime tRetorno = tSalida.plusMinutes(r.getTiempoEstimadoMin());

            System.out.println(String.format("     -> [SALIDA]                   Almacén Central (%d,%d) | Hora Salida: %s",
                    origen.getPosX(), origen.getPosY(), tSalida));

            int idx = 1;
            for (ParadaRuta p : r.getParadas()) {
                if (p.getPedido() != null) {
                    Ubicacion dest = p.getPedido().getDestino();
                    String coords = dest != null ? "(" + dest.getPosX() + "," + dest.getPosY() + ")" : "N/A";
                    System.out.println(String.format("     -> [ENTREGA %02d] Pedido: %-12s | Destino: %-9s | Cant: %2d u. | Plazo: %s | Llegada: %s",
                            idx++, p.getPedido().getCodigo(), coords, p.getPedido().getCantidadUnidades(),
                            p.getPedido().getPlazoLimiteEntrega(), p.getHoraEstimadaLlegada()));
                }
            }

            System.out.println(String.format("     -> [RETORNO/REABASTECIMIENTO] Almacén Central (%d,%d) | Llegada Estimada: %s | Reabastecido y disponible",
                    origen.getPosX(), origen.getPosY(), tRetorno));
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
