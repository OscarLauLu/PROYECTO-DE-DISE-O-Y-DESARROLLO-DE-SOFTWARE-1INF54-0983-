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

# Pasa todos los argumentos directamente al ejecutor de Docker sin intermediarios ni valores forzados
"$SCRIPT_DIR/docker-benchmark.sh" "$@"
