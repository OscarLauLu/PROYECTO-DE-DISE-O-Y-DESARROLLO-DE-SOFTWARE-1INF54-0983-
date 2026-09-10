package com.odiparpack.logistics.planificacion.algoritmo;

import com.odiparpack.logistics.almacen.model.Almacen;
import com.odiparpack.logistics.almacen.model.AlmacenCentral;
import com.odiparpack.logistics.almacen.repository.AlmacenRepository;
import com.odiparpack.logistics.flota.model.EstadoOperativo;
import com.odiparpack.logistics.flota.model.TipoVehiculo;
import com.odiparpack.logistics.flota.model.UnidadTransporte;
import com.odiparpack.logistics.pedidos.model.EstadoPedido;
import com.odiparpack.logistics.pedidos.model.Pedido;
import com.odiparpack.logistics.pedidos.model.TipoEntrega;
import com.odiparpack.logistics.planificacion.model.Ruta;
import com.odiparpack.logistics.redvial.model.RedVial;
import com.odiparpack.logistics.redvial.model.Ubicacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class AlgoritmosTest {

    private RedVial redVial;
    private List<UnidadTransporte> flota;
    private List<Pedido> pedidos;
    private List<Almacen> almacenes;
    private AlmacenRepository almacenRepository;

    @BeforeEach
    void setUp() {
        redVial = new RedVial();
        redVial.inicializarRed();

        TipoVehiculo tipoAuto = TipoVehiculo.builder()
                .id(1L)
                .nombre("Auto")
                .capacidadMaxima(24)
                .velocidadPromedioKmH(40.0)
                .costoPorKm(8.0)
                .build();

        flota = new ArrayList<>();
        flota.add(UnidadTransporte.builder()
                .id(1L)
                .codigo("VEH-001")
                .tipo(tipoAuto)
                .estadoOperativo(EstadoOperativo.DISPONIBLE)
                .ubicacionActual(new Ubicacion(10, 10))
                .activo(true)
                .build());

        flota.add(UnidadTransporte.builder()
                .id(2L)
                .codigo("VEH-002")
                .tipo(tipoAuto)
                .estadoOperativo(EstadoOperativo.DISPONIBLE)
                .ubicacionActual(new Ubicacion(10, 10))
                .activo(true)
                .build());

        LocalDateTime ahora = LocalDateTime.now();
        pedidos = new ArrayList<>();
        pedidos.add(Pedido.builder()
                .id(101L)
                .codigo("PED-001")
                .cantidadUnidades(5)
                .fechaHoraRegistro(ahora)
                .plazoLimiteEntrega(ahora.plusHours(6))
                .estado(EstadoPedido.REGISTRADO)
                .tipoEntrega(TipoEntrega.REGULAR_24H)
                .destino(new Ubicacion(12, 14))
                .build());

        pedidos.add(Pedido.builder()
                .id(102L)
                .codigo("PED-002")
                .cantidadUnidades(8)
                .fechaHoraRegistro(ahora)
                .plazoLimiteEntrega(ahora.plusHours(4))
                .estado(EstadoPedido.REGISTRADO)
                .tipoEntrega(TipoEntrega.EXPRESS_4H)
                .destino(new Ubicacion(15, 12))
                .build());

        pedidos.add(Pedido.builder()
                .id(103L)
                .codigo("PED-003")
                .cantidadUnidades(6)
                .fechaHoraRegistro(ahora)
                .plazoLimiteEntrega(ahora.plusHours(8))
                .estado(EstadoPedido.REGISTRADO)
                .tipoEntrega(TipoEntrega.REGULAR_24H)
                .destino(new Ubicacion(18, 16))
                .build());

        almacenes = new ArrayList<>();
        almacenes.add(new AlmacenCentral("ALM-CEN", "Almacén Central", new Ubicacion(10, 10)));

        almacenRepository = Mockito.mock(AlmacenRepository.class);
        when(almacenRepository.findAll()).thenReturn(almacenes);
    }

    @Test
    void testAlgoritmoACO_ConstruyeSolucionValida() {
        AlgoritmoACO aco = new AlgoritmoACO();
        aco.getConfig().setIterations(10);
        aco.getConfig().setMinAnts(5);
        aco.getConfig().setMaxAnts(10);

        List<Ruta> rutas = aco.construirSolucion(pedidos, flota, redVial);

        assertNotNull(rutas);
        assertFalse(rutas.isEmpty(), "ACO debe generar al menos una ruta");
        assertTrue(aco.obtenerCostoSolucion() > 0.0, "El costo de la solución debe ser positivo");

        for (Ruta r : rutas) {
            assertFalse(r.capacidadExcedida(), "Ninguna ruta debe exceder la capacidad máxima");
            assertTrue(r.cumplePlazos(), "Todas las paradas deben cumplir los plazos comprometidos");
        }
    }

    @Test
    void testAlgoritmoALNS_ConstruyeSolucionValida() {
        AlgoritmoALNS alns = new AlgoritmoALNS(almacenRepository);
        alns.setLimiteMillis(800);

        List<Ruta> rutas = alns.construirSolucion(pedidos, flota, redVial);

        assertNotNull(rutas);
        assertFalse(rutas.isEmpty(), "ALNS debe generar al menos una ruta");
        assertTrue(alns.obtenerCostoSolucion() > 0.0, "El costo de la solución debe ser positivo");

        for (Ruta r : rutas) {
            assertFalse(r.capacidadExcedida(), "Ninguna ruta debe exceder la capacidad máxima");
            assertTrue(r.cumplePlazos(), "Todas las paradas deben cumplir los plazos");
        }
    }
}
