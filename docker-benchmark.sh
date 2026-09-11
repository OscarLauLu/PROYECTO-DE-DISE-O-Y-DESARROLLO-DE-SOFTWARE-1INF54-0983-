#!/usr/bin/env bash
# ==============================================================================
# Helper para ejecutar el Benchmark de Metaheurísticos en Docker
# Detecta automáticamente si se dispone de docker-compose o docker estándar
# ==============================================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Auto-detectar si docker requiere sudo para acceder al socket
DOCKER_CMD="docker"
COMPOSE_CMD="docker-compose"
if [ "$EUID" -ne 0 ] && ! docker info >/dev/null 2>&1; then
    DOCKER_CMD="sudo docker"
    COMPOSE_CMD="sudo docker-compose"
fi

# 1. Probar con docker-compose (con guion)
if command -v docker-compose >/dev/null 2>&1; then
    echo "-> Ejecutando en Docker Compose..."
    $COMPOSE_CMD run --build --rm benchmark benchmark "$@"

# 2. Probar con docker compose (con espacio, v2 plugin)
elif docker compose version >/dev/null 2>&1 || $DOCKER_CMD compose version >/dev/null 2>&1; then
    echo "-> Ejecutando en Docker Compose..."
    $DOCKER_CMD compose run --build --rm benchmark benchmark "$@"

# 3. Fallback universal: docker build + docker run
else
    echo "-> Ejecutando con Docker..."
    $DOCKER_CMD build -t paqrap .
    $DOCKER_CMD run --rm -v "$SCRIPT_DIR/datos:/app/datos:ro" paqrap benchmark "$@"
fi
