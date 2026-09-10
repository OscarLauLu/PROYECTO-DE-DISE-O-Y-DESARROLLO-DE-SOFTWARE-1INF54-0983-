package com.odiparpack.logistics.flota.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa la tipología y parámetros operacionales de un tipo de vehículo (RF-39, RF-40, RF-41, RF-46).
 * Admite actualización en caliente durante la ejecución del sistema:
 * - Auto: 24 u | 40 km/h | S/8.00/km
 * - Moto: 8 u | 25 km/h | S/6.00/km
 * - Bicicleta: 4 u | 12 km/h | S/3.00/km
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tipos_vehiculo")
public class TipoVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "capacidad_maxima")
    private int capacidadMaxima;

    @Column(name = "velocidad_promedio_km_h")
    private double velocidadPromedioKmH;

    @Column(name = "costo_por_km")
    private double costoPorKm;

    public TipoVehiculo(String nombre, int capacidadMaxima, double velocidadPromedioKmH, double costoPorKm) {
        this.nombre = nombre;
        this.capacidadMaxima = capacidadMaxima;
        this.velocidadPromedioKmH = velocidadPromedioKmH;
        this.costoPorKm = costoPorKm;
    }

    /**
     * Actualiza la velocidad promedio configurada (cambio en caliente, RF-40).
     */
    public void actualizarVelocidad(double nueva) {
        if (nueva <= 0) {
            throw new IllegalArgumentException("La velocidad debe ser mayor a cero");
        }
        this.velocidadPromedioKmH = nueva;
    }

    /**
     * Actualiza el costo por kilómetro configurado (cambio en caliente, RF-41).
     */
    public void actualizarCosto(double nuevo) {
        if (nuevo < 0) {
            throw new IllegalArgumentException("El costo no puede ser negativo");
        }
        this.costoPorKm = nuevo;
    }

    /**
     * Actualiza la capacidad máxima del tipo de vehículo (RF-46).
     */
    public void actualizarCapacidad(int nueva) {
        if (nueva <= 0) {
            throw new IllegalArgumentException("La capacidad máxima debe ser un entero mayor a cero");
        }
        this.capacidadMaxima = nueva;
    }
}
