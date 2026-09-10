package com.odiparpack.logistics.planificacion.algoritmo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Prueba unitaria y benchmark ejecutable con JUnit / Maven (100% Java).
 * Permite probar y comparar los algoritmos ACO y ALNS directamente
 * con 'mvn test' o desde cualquier IDE Java.
 */
class BenchmarkMetaheuristicasTest {

    @Test
    @DisplayName("Evaluar y comparar algoritmos ACO vs ALNS con datos reales de prueba")
    void probarYCompararMetaheuristicas() {
        // Ejecuta el benchmark de ruteo con 30 pedidos del archivo 202601
        String[] args = new String[]{"202601", "", "30"};
        BenchmarkMetaheuristicas.main(args);
    }
}
