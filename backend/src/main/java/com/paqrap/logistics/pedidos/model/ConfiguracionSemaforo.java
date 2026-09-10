package com.paqrap.logistics.pedidos.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Configuración dinámica de los rangos del semáforo de criticidad (RF-03, RF-04, RF-53, RF-59).
 * Permite cambio en caliente sin reiniciar el sistema.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
public class ConfiguracionSemaforo {

    /**
     * Holgura por encima de este valor es clasificado como VERDE (por defecto >= 12 horas).
     */
    @Builder.Default
    private Duration umbralVerdeMin = Duration.ofHours(12);

    /**
     * Holgura entre umbralAmbarMin y umbralVerdeMin es clasificado como AMBAR (por defecto >= 4 horas).
     */
    @Builder.Default
    private Duration umbralAmbarMin = Duration.ofHours(4);

    /**
     * Umbral de holgura crítica para priorizar asignación (por defecto 4 horas, RF-04).
     */
    @Builder.Default
    private Duration holguraCritica = Duration.ofHours(4);

    /**
     * Clasifica la holgura en un nivel de criticidad (VERDE, AMBAR, ROJO).
     *
     * @param holgura Duración restante hasta el vencimiento del plazo
     * @return NivelCriticidad correspondiente
     */
    public NivelCriticidad clasificar(Duration holgura) {
        if (holgura == null || holgura.isNegative() || holgura.compareTo(umbralAmbarMin) < 0) {
            return NivelCriticidad.ROJO;
        }
        if (holgura.compareTo(umbralVerdeMin) >= 0) {
            return NivelCriticidad.VERDE;
        }
        return NivelCriticidad.AMBAR;
    }

    /**
     * Actualiza los rangos en caliente sin reiniciar el sistema (RF-59).
     */
    public void actualizarRangos(Duration verde, Duration ambar) {
        if (verde != null) {
            this.umbralVerdeMin = verde;
        }
        if (ambar != null) {
            this.umbralAmbarMin = ambar;
            this.holguraCritica = ambar;
        }
    }
}
