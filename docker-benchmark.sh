#!/usr/bin/env bash
# ==============================================================================
# Helper para ejecutar el Benchmark de Metaheurísticos en Docker
# Detecta automáticamente si se dispone de docker-compose o docker estándar
# ==============================================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

MES="${1:-202601}"
PEDIDOS="${2:-30}"

echo "================================================================================"
echo "          EJECUTANDO BENCHMARK EN DOCKER (Mes: $MES, Pedidos: $PEDIDOS)        "
echo "================================================================================"

# 1. Probar con docker-compose (con guion)
if command -v docker-compose >/dev/null 2>&1; then
    echo "-> Usando docker-compose..."
    docker-compose run --rm benchmark benchmark "$MES" "$PEDIDOS"

# 2. Probar con docker compose (con espacio, v2 plugin)
elif docker compose version >/dev/null 2>&1; then
    echo "-> Usando docker compose..."
    docker compose run --rm benchmark benchmark "$MES" "$PEDIDOS"

# 3. Fallback universal: docker build + docker run
else
    echo "-> Usando docker directo (construyendo imagen si no existe)..."
    docker build -t paqrap .
    docker run --rm -v "$SCRIPT_DIR/datos:/app/datos:ro" paqrap benchmark "$MES" "$PEDIDOS"
fi
