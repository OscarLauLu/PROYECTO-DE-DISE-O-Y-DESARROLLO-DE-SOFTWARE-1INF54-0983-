package com.odiparpack.logistics.common.util;

import com.odiparpack.logistics.common.enums.CriticalityLevel;
import com.odiparpack.logistics.common.enums.DeliveryType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Utility class for time operations.
 */
public class TimeUtils {
    
    private TimeUtils() {}

    /**
     * Calcula la fecha límite de entrega según el tiempo de registro y tipo de entrega.
     */
    public static LocalDateTime calculateDeadline(LocalDateTime registrationTime, DeliveryType type) {
        if (registrationTime == null || type == null) return null;
        return registrationTime.plusHours(type.getHours());
    }

    /**
     * Calcula la holgura (slack) de tiempo entre el límite y la hora actual.
     */
    public static Duration calculateSlack(LocalDateTime deadline, LocalDateTime currentTime) {
        if (deadline == null || currentTime == null) return Duration.ZERO;
        return Duration.between(currentTime, deadline);
    }

    /**
     * Calcula la holgura en horas.
     */
    public static double calculateSlackHours(LocalDateTime deadline, LocalDateTime currentTime) {
        Duration slack = calculateSlack(deadline, currentTime);
        return slack.toMillis() / (1000.0 * 60.0 * 60.0);
    }

    /**
     * Determina el nivel de criticidad en función de la holgura.
     */
    public static CriticalityLevel determineCriticality(double slackHours, double greenThreshold, double amberThreshold) {
        if (slackHours >= greenThreshold) {
            return CriticalityLevel.VERDE;
        } else if (slackHours >= amberThreshold) {
            return CriticalityLevel.AMBAR;
        } else {
            return CriticalityLevel.ROJO;
        }
    }

    /**
     * Verifica si una hora está dentro de un turno específico, manejando turnos nocturnos.
     */
    public static boolean isWithinShift(LocalTime time, LocalTime shiftStart, LocalTime shiftEnd) {
        if (time == null || shiftStart == null || shiftEnd == null) return false;
        if (shiftStart.isBefore(shiftEnd)) {
            return !time.isBefore(shiftStart) && time.isBefore(shiftEnd);
        } else {
            return !time.isBefore(shiftStart) || time.isBefore(shiftEnd);
        }
    }

    /**
     * Formatea una duración en un string legible.
     */
    public static String formatDuration(Duration d) {
        if (d == null) return "0h 0m";
        long hours = d.toHours();
        long minutes = d.minusHours(hours).toMinutes();
        if (d.isNegative()) {
            return String.format("-%dh %dm", Math.abs(hours), Math.abs(minutes));
        }
        return String.format("%dh %dm", hours, minutes);
    }
}
