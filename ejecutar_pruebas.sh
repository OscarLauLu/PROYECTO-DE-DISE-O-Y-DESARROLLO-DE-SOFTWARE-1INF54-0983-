#!/usr/bin/env bash
# ==============================================================================
# SCRIPT DE EJECUCIÓN RÁPIDA: BENCHMARK DE METAHEURÍSTICOS (ACO vs. ALNS)
# ==============================================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

chmod +x "$SCRIPT_DIR/probar_metaheuristicas.py"

echo "================================================================================"
echo "          PAQRAP: EVALUADOR DE ALGORITMOS METAHEURÍSTICOS                     "
echo "================================================================================"

MODO="${1:-rapido}"
MES="${2:-202601}"

case "$MODO" in
    rapido)
        echo "Modo: Prueba Rápida (30 pedidos, Mes: $MES)"
        python3 "$SCRIPT_DIR/probar_metaheuristicas.py" --mes "$MES" --pedidos 30 --algoritmo ambos
        ;;
    intermedio)
        echo "Modo: Prueba Intermedia (60 pedidos, Mes: $MES)"
        python3 "$SCRIPT_DIR/probar_metaheuristicas.py" --mes "$MES" --pedidos 60 --algoritmo ambos
        ;;
    estres)
        echo "Modo: Prueba de Estrés (120 pedidos, Mes: $MES)"
        python3 "$SCRIPT_DIR/probar_metaheuristicas.py" --mes "$MES" --pedidos 120 --algoritmo ambos
        ;;
    detalle)
        echo "Modo: Detalle completo de paradas y tiempos (25 pedidos, Mes: $MES)"
        python3 "$SCRIPT_DIR/probar_metaheuristicas.py" --mes "$MES" --pedidos 25 --algoritmo ambos --detalle
        ;;
    aco)
        echo "Modo: Solo ACO (40 pedidos, Mes: $MES)"
        python3 "$SCRIPT_DIR/probar_metaheuristicas.py" --mes "$MES" --pedidos 40 --algoritmo aco --detalle
        ;;
    alns)
        echo "Modo: Solo ALNS (40 pedidos, Mes: $MES)"
        python3 "$SCRIPT_DIR/probar_metaheuristicas.py" --mes "$MES" --pedidos 40 --algoritmo alns --detalle
        ;;
    exportar)
        ARCHIVO_OUT="reporte_metaheuristicas_${MES}.json"
        echo "Modo: Exportar resultados a $ARCHIVO_OUT (50 pedidos)"
        python3 "$SCRIPT_DIR/probar_metaheuristicas.py" --mes "$MES" --pedidos 50 --algoritmo ambos --exportar "$ARCHIVO_OUT"
        ;;
    java)
        echo "Modo: Ejecución mediante Runner Nativo Java..."
        cd "$SCRIPT_DIR/backend"
        mvn compile exec:java -Dexec.mainClass="com.paqrap.logistics.planificacion.algoritmo.BenchmarkMetaheuristicas" -Dexec.args="$MES"
        ;;
    *)
        echo "Uso: ./ejecutar_pruebas.sh [rapido|intermedio|estres|detalle|aco|alns|exportar|java] [MES_AAAAMM]"
        echo "Ejemplo: ./ejecutar_pruebas.sh rapido 202601"
        echo "Ejemplo: ./ejecutar_pruebas.sh detalle 202602"
        ;;
esac
