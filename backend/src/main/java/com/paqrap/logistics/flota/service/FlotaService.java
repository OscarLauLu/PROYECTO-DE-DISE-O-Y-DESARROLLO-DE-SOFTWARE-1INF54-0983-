package com.paqrap.logistics.flota.service;

import com.paqrap.logistics.common.exception.BusinessException;
import com.paqrap.logistics.common.exception.ResourceNotFoundException;
import com.paqrap.logistics.flota.dto.ActualizarTipoVehiculoDTO;
import com.paqrap.logistics.flota.dto.RegistrarAveriaDTO;
import com.paqrap.logistics.flota.dto.UnidadTransporteDTO;
import com.paqrap.logistics.flota.model.AsignacionTurno;
import com.paqrap.logistics.flota.model.Averia;
import com.paqrap.logistics.flota.model.Conductor;
import com.paqrap.logistics.flota.model.EstadoOperativo;
import com.paqrap.logistics.flota.model.TipoVehiculo;
import com.paqrap.logistics.flota.model.UnidadTransporte;
import com.paqrap.logistics.flota.repository.AsignacionTurnoRepository;
import com.paqrap.logistics.flota.repository.AveriaRepository;
import com.paqrap.logistics.flota.repository.ConductorRepository;
import com.paqrap.logistics.flota.repository.TipoVehiculoRepository;
import com.paqrap.logistics.flota.repository.UnidadTransporteRepository;
import com.paqrap.logistics.redvial.model.Ubicacion;
import com.paqrap.logistics.simulacion.model.Alerta;
import com.paqrap.logistics.simulacion.model.TipoAlerta;
import com.paqrap.logistics.simulacion.repository.AlertaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de negocio para la Gestión de Flota y Conductores (RF-39 a RF-50).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlotaService {

    private final UnidadTransporteRepository unidadRepository;
    private final TipoVehiculoRepository tipoVehiculoRepository;
    private final ConductorRepository conductorRepository;
    private final AsignacionTurnoRepository asignacionTurnoRepository;
    private final AveriaRepository averiaRepository;
    private final AlertaRepository alertaRepository;

    @Transactional(readOnly = true)
    public List<UnidadTransporteDTO> listarUnidades(String tipo, EstadoOperativo estado) {
        return unidadRepository.findAll().stream()
                .filter(u -> tipo == null || (u.getTipo() != null && u.getTipo().getNombre().equalsIgnoreCase(tipo)))
                .filter(u -> estado == null || u.getEstadoOperativo() == estado)
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UnidadTransporteDTO> listarUnidadesDisponibles(String tipo) {
        return listarUnidades(tipo, EstadoOperativo.DISPONIBLE).stream()
                .filter(UnidadTransporteDTO::isActivo)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UnidadTransporteDTO obtenerUnidad(Long id) {
        UnidadTransporte u = unidadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UnidadTransporte", "id", id));
        return mapearADTO(u);
    }

    @Transactional
    public UnidadTransporteDTO cambiarEstadoOperativo(Long id, EstadoOperativo nuevoEstado) {
        UnidadTransporte u = unidadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UnidadTransporte", "id", id));

        u.cambiarEstado(nuevoEstado);
        UnidadTransporte guardada = unidadRepository.save(u);
        log.info("Estado de unidad {} actualizado a {}", u.getCodigo(), nuevoEstado);
        return mapearADTO(guardada);
    }

    /**
     * Registra un evento de avería y genera una alerta visible (RF-14, RF-45).
     */
    @Transactional
    public Averia registrarAveria(Long unidadId, RegistrarAveriaDTO dto) {
        UnidadTransporte u = unidadRepository.findById(unidadId)
                .orElseThrow(() -> new ResourceNotFoundException("UnidadTransporte", "id", unidadId));

        LocalDateTime ahora = LocalDateTime.now();
        Ubicacion ubicacion = new Ubicacion(dto.getUbicacionX(), dto.getUbicacionY());

        Averia averia = Averia.builder()
                .codigo("AVE-" + System.currentTimeMillis())
                .unidad(u)
                .tipo(dto.getTipo())
                .fechaHoraEvento(ahora)
                .ubicacionFalla(ubicacion)
                .origenManual(dto.isOrigenManual())
                .resuelta(false)
                .build();

        LocalDateTime reincorporacion = averia.calcularReincorporacion(null);
        averia.setHoraReincorporacion(reincorporacion);

        Averia guardada = averiaRepository.save(averia);
        u.registrarAveria(guardada);
        unidadRepository.save(u);

        // Generar alerta de avería (RF-45)
        Alerta alerta = Alerta.builder()
                .codigo("ALT-AVE-" + UUID.randomUUID().toString().substring(0, 6))
                .tipo(TipoAlerta.UNIDAD_AVERIADA)
                .mensaje("Unidad " + u.getCodigo() + " en estado AVERIADA en " + ubicacion
                        + ". Tipo: " + dto.getTipo() + ". Reincorporación estimada: " + reincorporacion)
                .fechaHoraGeneracion(ahora)
                .atendida(false)
                .build();
        alertaRepository.save(alerta);

        log.warn("Avería registrada en unidad {}: reincorporación estimada para {}", u.getCodigo(), reincorporacion);
        return guardada;
    }

    /**
     * Actualiza parámetros en caliente para un tipo de vehículo sin reiniciar (RF-40, RF-41, RF-46).
     */
    @Transactional
    public TipoVehiculo actualizarConfiguracionTipoVehiculo(String nombreTipo, ActualizarTipoVehiculoDTO dto) {
        TipoVehiculo tipo = tipoVehiculoRepository.findByNombreIgnoreCase(nombreTipo)
                .orElseThrow(() -> new ResourceNotFoundException("TipoVehiculo", "nombre", nombreTipo));

        if (dto.getNuevaVelocidadKmH() != null) {
            tipo.actualizarVelocidad(dto.getNuevaVelocidadKmH()); // RF-40
        }
        if (dto.getNuevoCostoPorKm() != null) {
            tipo.actualizarCosto(dto.getNuevoCostoPorKm()); // RF-41
        }
        if (dto.getNuevaCapacidadMaxima() != null) {
            tipo.actualizarCapacidad(dto.getNuevaCapacidadMaxima()); // RF-46
        }

        TipoVehiculo actualizado = tipoVehiculoRepository.save(tipo);
        log.info("Parámetros en caliente actualizados para tipo de vehículo: {}", nombreTipo);
        return actualizado;
    }

    /**
     * Asigna un conductor a una unidad validando turno y hora de alimentación (RF-43, RF-44).
     */
    @Transactional
    public AsignacionTurno asignarConductor(Long unidadId, Long conductorId, LocalDate fecha, LocalTime inicioAlimentacion) {
        UnidadTransporte unidad = unidadRepository.findById(unidadId)
                .orElseThrow(() -> new ResourceNotFoundException("UnidadTransporte", "id", unidadId));
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor", "id", conductorId));

        AsignacionTurno asignacion = AsignacionTurno.builder()
                .unidad(unidad)
                .conductor(conductor)
                .turno(conductor.getTurno())
                .fecha(fecha != null ? fecha : LocalDate.now())
                .horaInicioAlimentacion(inicioAlimentacion != null ? inicioAlimentacion : conductor.getTurno().getHoraInicio().plusHours(3))
                .duracionAlimentacionMin(60)
                .build();

        if (!asignacion.validarHorario()) {
            throw new BusinessException("El turno asignado no corresponde a los horarios estándar permitidos (RF-44)");
        }
        if (!asignacion.validarAlimentacion()) {
            throw new BusinessException("La pausa de alimentación debe ser de 60 min y estar al menos 60 min después del inicio y antes del fin del turno (RF-44)");
        }

        unidad.setConductorAsignado(conductor);
        unidadRepository.save(unidad);
        return asignacionTurnoRepository.save(asignacion);
    }

    @Transactional
    public void darDeBajaUnidad(Long id) {
        UnidadTransporte u = unidadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UnidadTransporte", "id", id));
        u.darDeBaja();
        unidadRepository.save(u);
        log.info("Unidad {} dada de baja exitosamente (RF-47).", u.getCodigo());
    }

    private UnidadTransporteDTO mapearADTO(UnidadTransporte u) {
        int capMax = u.getTipo() != null ? u.getTipo().getCapacidadMaxima() : 0;
        double pctCarga = (capMax > 0) ? (u.getCargaActual() * 100.0) / capMax : 0.0;

        return UnidadTransporteDTO.builder()
                .id(u.getId())
                .codigo(u.getCodigo())
                .tipoNombre(u.getTipo() != null ? u.getTipo().getNombre() : null)
                .capacidadMaxima(capMax)
                .cargaActual(u.getCargaActual())
                .capacidadDisponible(u.capacidadDisponible())
                .porcentajeCarga(Math.round(pctCarga * 10.0) / 10.0)
                .velocidadPromedioKmH(u.getTipo() != null ? u.getTipo().getVelocidadPromedioKmH() : 0.0)
                .costoPorKm(u.getTipo() != null ? u.getTipo().getCostoPorKm() : 0.0)
                .estadoOperativo(u.getEstadoOperativo())
                .colorEstado(u.getEstadoOperativo().getCodigoColor())
                .ubicacionActual(u.getUbicacionActual())
                .nombreConductor(u.getConductorAsignado() != null ? u.getConductorAsignado().getNombre() : "Sin asignar")
                .activo(u.isActivo())
                .build();
    }
}
