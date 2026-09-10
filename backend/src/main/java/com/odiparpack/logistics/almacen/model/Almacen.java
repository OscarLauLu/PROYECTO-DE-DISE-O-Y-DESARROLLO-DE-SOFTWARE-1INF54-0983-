package com.odiparpack.logistics.almacen.model;

import com.odiparpack.logistics.redvial.model.Ubicacion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase base abstracta para los almacenes del sistema logístico (RF-17 a RF-25).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "almacenes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_almacen", discriminatorType = DiscriminatorType.STRING)
public abstract class Almacen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;

    private String nombre;

    @Embedded
    private Ubicacion ubicacion;

    @Column(name = "stock_actual")
    private int stockActual;

    @OneToMany(mappedBy = "almacen", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<MovimientoInventario> movimientos = new ArrayList<>();

    public Almacen(String codigo, String nombre, Ubicacion ubicacion, int stockActual) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.stockActual = stockActual;
        this.movimientos = new ArrayList<>();
    }

    /**
     * Valida si el almacén cuenta con stock suficiente para atender la cantidad solicitada (RF-22).
     */
    public abstract boolean tieneStockSuficiente(int cantidad);

    /**
     * Descuenta stock del almacén registrando el movimiento respectivo (RF-21).
     */
    public abstract MovimientoInventario descontarStock(int cantidad, String codigoRuta);

    /**
     * Registra un movimiento en el historial del almacén (RF-23).
     */
    public void registrarMovimiento(MovimientoInventario mov) {
        if (mov != null) {
            mov.setAlmacen(this);
            if (this.movimientos == null) {
                this.movimientos = new ArrayList<>();
            }
            this.movimientos.add(mov);
        }
    }

    /**
     * Obtiene el historial de movimientos de los últimos N días simulados (RF-23).
     */
    public List<MovimientoInventario> obtenerHistorial(int dias) {
        if (movimientos == null) {
            return new ArrayList<>();
        }
        LocalDateTime corte = LocalDateTime.now().minusDays(dias);
        return movimientos.stream()
                .filter(m -> m.getFechaHora() == null || !m.getFechaHora().isBefore(corte))
                .collect(Collectors.toList());
    }
}
