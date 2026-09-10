package com.paqrap.logistics.planificacion.service;

import com.paqrap.logistics.common.exception.ResourceNotFoundException;
import com.paqrap.logistics.planificacion.algoritmo.AlgoritmoACO;
import com.paqrap.logistics.planificacion.algoritmo.AlgoritmoALNS;
import com.paqrap.logistics.planificacion.dto.ParadaRutaDTO;
import com.paqrap.logistics.planificacion.dto.PlanificacionResultDTO;
import com.paqrap.logistics.planificacion.dto.RutaDTO;
import com.paqrap.logistics.planificacion.model.EstadoRuta;
import com.paqrap.logistics.planificacion.model.ParadaRuta;
import com.paqrap.logistics.planificacion.model.Planificador;
import com.paqrap.logistics.planificacion.model.Ruta;
import com.paqrap.logistics.planificacion.repository.RutaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de negocio para la Planificación de Rutas (RF-01 a RF-16, RF-55).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanificacionService {

    private final Planificador planificador;
    private final RutaRepository rutaRepository;
    private final AlgoritmoACO algoritmoACO;
    private final AlgoritmoALNS algoritmoALNS;

    @Transactional
    public PlanificacionResultDTO ejecutarCiclo(LocalDateTime instante) {
        LocalDateTime t = instante != null ? instante : LocalDateTime.now();
        List<Ruta> rutas = planificador.ejecutarCicloPlanificacion(t);

        int totalPedidos = rutas.stream().mapToInt(r -> r.getParadas().size()).sum();
        double costoTotal = rutas.stream().mapToDouble(Ruta::getCostoTotal).sum();

        return PlanificacionResultDTO.builder()
                .algoritmoUtilizado(planificador.getAlgoritmoRuteo().obtenerNombre())
                .instanteEjecucion(t)
                .totalRutasPlanificadas(rutas.size())
                .totalPedidosAtendidos(totalPedidos)
                .costoTotalEstimado(Math.round(costoTotal * 100.0) / 100.0)
                .rutas(rutas.stream().map(this::mapearRutaADTO).collect(Collectors.toList()))
                .build();
    }

    @Transactional(readOnly = true)
    public List<RutaDTO> listarRutas(EstadoRuta estado) {
        List<Ruta> rutas = (estado != null) ? rutaRepository.findByEstado(estado) : rutaRepository.findAll();
        return rutas.stream().map(this::mapearRutaADTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RutaDTO obtenerRuta(Long id) {
        Ruta r = rutaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ruta", "id", id));
        return mapearRutaADTO(r);
    }

    public String cambiarAlgoritmo(String nombre) {
        if ("ALNS".equalsIgnoreCase(nombre)) {
            planificador.setAlgoritmoRuteo(algoritmoALNS);
        } else {
            planificador.setAlgoritmoRuteo(algoritmoACO);
        }
        log.info("Algoritmo de ruteo configurado a: {}", planificador.getAlgoritmoRuteo().obtenerNombre());
        return planificador.getAlgoritmoRuteo().obtenerNombre();
    }

    private RutaDTO mapearRutaADTO(Ruta r) {
        List<ParadaRutaDTO> paradasDTO = r.getParadas().stream().map(this::mapearParadaADTO).collect(Collectors.toList());

        String tipoVehiculo = (r.getUnidadTransporte() != null && r.getUnidadTransporte().getTipo() != null)
                ? r.getUnidadTransporte().getTipo().getNombre() : null;

        return RutaDTO.builder()
                .id(r.getId())
                .codigo(r.getCodigo())
                .unidadCodigo(r.getUnidadTransporte() != null ? r.getUnidadTransporte().getCodigo() : null)
                .tipoVehiculo(tipoVehiculo)
                .almacenOrigenCodigo(r.getAlmacenOrigen() != null ? r.getAlmacenOrigen().getCodigo() : null)
                .almacenOrigenNombre(r.getAlmacenOrigen() != null ? r.getAlmacenOrigen().getNombre() : null)
                .fechaHoraGeneracion(r.getFechaHoraGeneracion())
                .distanciaTotalKm(r.getDistanciaTotalKm())
                .tiempoEstimadoMin(r.getTiempoEstimadoMin())
                .costoTotal(r.getCostoTotal())
                .estado(r.getEstado())
                .paradas(paradasDTO)
                .build();
    }

    private ParadaRutaDTO mapearParadaADTO(ParadaRuta p) {
        return ParadaRutaDTO.builder()
                .id(p.getId())
                .orden(p.getOrden())
                .pedidoId(p.getPedido() != null ? p.getPedido().getId() : null)
                .codigoPedido(p.getPedido() != null ? p.getPedido().getCodigo() : null)
                .nombreCliente(p.getPedido() != null && p.getPedido().getCliente() != null ? p.getPedido().getCliente().getNombre() : null)
                .cantidadUnidades(p.getPedido() != null ? p.getPedido().getCantidadUnidades() : 0)
                .destino(p.getPedido() != null ? p.getPedido().getDestino() : null)
                .horaEstimadaLlegada(p.getHoraEstimadaLlegada())
                .plazoLimiteEntrega(p.getPedido() != null ? p.getPedido().getPlazoLimiteEntrega() : null)
                .tiempoServicioMin(p.getTiempoServicioMin())
                .entregada(p.isEntregada())
                .build();
    }
}
