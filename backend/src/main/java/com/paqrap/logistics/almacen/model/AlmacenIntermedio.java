package com.paqrap.logistics.almacen.model;

import com.paqrap.logistics.redvial.model.Ubicacion;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Almacén intermedio con capacidad máxima de 1,000 unidades y recarga diaria a las 23:59:59 (RF-18, RF-19, RF-20, RF-24, RF-25).
 */
@Entity
@DiscriminatorValue("INTERMEDIO")
@Getter
@Setter
@NoArgsConstructor
public class AlmacenIntermedio extends Almacen {

    @Column(name = "capacidad_maxima")
    private int capacidadMaxima = 1000;

    @Column(name = "umbral_alerta_ocupacion")
    private double umbralAlertaOcupacion = 90.0;

    @Column(name = "hora_recarga_diaria")
    private LocalTime horaRecargaDiaria = LocalTime.of(23, 59, 59);

    public AlmacenIntermedio(String codigo, String nombre, Ubicacion ubicacion, int stockInicial) {
        super(codigo, nombre, ubicacion, Math.min(stockInicial, 1000));
        this.capacidadMaxima = 1000;
        this.umbralAlertaOcupacion = 90.0;
        this.horaRecargaDiaria = LocalTime.of(23, 59, 59);
    }

    @Override
    public boolean tieneStockSuficiente(int cantidad) {
        return getStockActual() >= cantidad;
    }

    @Override
    public MovimientoInventario descontarStock(int cantidad, String codigoRuta) {
        if (!tieneStockSuficiente(cantidad)) {
            throw new IllegalStateException("Stock insuficiente en " + getNombre() + ". Disponible: "
                    + getStockActual() + ", Requerido: " + cantidad);
        }
        setStockActual(getStockActual() - cantidad);

        MovimientoInventario mov = MovimientoInventario.builder()
                .codigo("DES-" + UUID.randomUUID().toString().substring(0, 8))
                .fechaHora(LocalDateTime.now())
                .tipo(TipoMovimiento.DESCARGA)
                .cantidad(cantidad)
                .almacen(this)
                .codigoRuta(codigoRuta)
                .descripcion("Despacho hacia ruta " + codigoRuta + " desde " + getNombre())
                .build();
        registrarMovimiento(mov);
        return mov;
    }

    /**
     * Ejecuta la recarga diaria a las 23:59:59 completando hasta la capacidad máxima de 1,000 unidades (RF-20).
     */
    public MovimientoInventario recargarDiario(LocalDateTime instante) {
        int faltante = Math.max(0, capacidadMaxima - getStockActual());
        setStockActual(capacidadMaxima);

        MovimientoInventario mov = MovimientoInventario.builder()
                .codigo("REC-" + UUID.randomUUID().toString().substring(0, 8))
                .fechaHora(instante != null ? instante : LocalDateTime.now())
                .tipo(TipoMovimiento.RECARGA_DIARIA)
                .cantidad(faltante)
                .almacen(this)
                .descripcion("Recarga diaria automática a las 23:59:59 completando 1,000 unidades")
                .build();
        registrarMovimiento(mov);
        return mov;
    }

    /**
     * Calcula el porcentaje de ocupación respecto a la capacidad máxima de 1,000 unidades,
     * con 1 decimal de precisión (RF-24).
     */
    public double porcentajeOcupacion() {
        if (capacidadMaxima <= 0) return 0.0;
        double pct = (getStockActual() * 100.0) / capacidadMaxima;
        return Math.round(pct * 10.0) / 10.0;
    }

    /**
     * Valida si la ocupación supera el umbral porcentual configurable (90% por defecto, RF-25).
     */
    public boolean superaUmbralAlerta() {
        return porcentajeOcupacion() > umbralAlertaOcupacion;
    }
}
