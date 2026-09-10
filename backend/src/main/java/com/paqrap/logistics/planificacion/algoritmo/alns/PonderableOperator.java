package com.paqrap.logistics.planificacion.algoritmo.alns;

import lombok.Getter;
import lombok.Setter;

/**
 * Clase base para operadores de ALNS con peso adaptativo (sección 3.3).
 * Soporta actualización dinámica de pesos mediante aprendizaje por refuerzo.
 */
@Getter
@Setter
public abstract class PonderableOperator {

    protected double peso = 1.0;
    protected double puntajeAcumulado = 0.0;
    protected int vecesUsado = 0;

    /**
     * Refuerza el operador acumulando puntos de desempeño en la iteración:
     * - +3.0: nueva mejor solución global
     * - +1.0: solución aceptada por recocido simulado
     * - +0.1: solución rechazada o infactible
     */
    public void reforzar(double puntos) {
        this.puntajeAcumulado += puntos;
        this.vecesUsado++;
    }

    /**
     * Actualiza el peso del operador usando la fórmula adaptativa con factor de reacción lambda (0.3).
     */
    public void actualizarPeso(double lambda) {
        if (vecesUsado > 0) {
            double rendimiento = puntajeAcumulado / vecesUsado;
            this.peso = Math.max(0.1, lambda * rendimiento + (1.0 - lambda) * this.peso);
        }
        this.puntajeAcumulado = 0.0;
        this.vecesUsado = 0;
    }

    public abstract String getNombre();
}
