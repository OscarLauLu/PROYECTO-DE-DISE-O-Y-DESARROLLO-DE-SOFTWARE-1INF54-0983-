#!/usr/bin/env bash
# ==============================================================================
# SCRIPT DE EJECUCIÓN RÁPIDA: BENCHMARK DE METAHEURÍSTICOS JAVA (ACO vs. ALNS)
# ==============================================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "================================================================================"
echo "          PAQRAP: EVALUADOR DE ALGORITMOS METAHEURÍSTICOS (JAVA)              "
echo "================================================================================"

MODO="${1:-rapido}"
MES="${2:-202601}"

case "$MODO" in
    rapido)
        PEDIDOS=30
        echo "Modo: Prueba Rápida (30 pedidos, Mes: $MES)"
        ;;
    intermedio)
        PEDIDOS=60
        echo "Modo: Prueba Intermedia (60 pedidos, Mes: $MES)"
        ;;
    estres)
        PEDIDOS=120
        echo "Modo: Prueba de Estrés (120 pedidos, Mes: $MES)"
        ;;
    detalle)
        PEDIDOS=25
        echo "Modo: Detalle completo de paradas y tiempos (25 pedidos, Mes: $MES)"
        ;;
    *)
        if [[ "$MODO" =~ ^[0-9]+$ ]]; then
            PEDIDOS="$MODO"
            echo "Modo: Evaluación personalizada ($PEDIDOS pedidos, Mes: $MES)"
        else
            echo "Uso: ./ejecutar_pruebas.sh [rapido|intermedio|estres|detalle|<num_pedidos>] [MES_AAAAMM]"
            echo "Ejemplo: ./ejecutar_pruebas.sh rapido 202601"
            echo "Ejemplo: ./ejecutar_pruebas.sh 50 202602"
            exit 1
        fi
        ;;
esac

# Ejecutar mediante el helper de Docker (100% Java en contenedor)
"$SCRIPT_DIR/docker-benchmark.sh" "$MES" "$PEDIDOS"
