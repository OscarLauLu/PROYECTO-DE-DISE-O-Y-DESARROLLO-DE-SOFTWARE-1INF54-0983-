#!/bin/sh
set -e

# Modo 1: Benchmark nativo Java (ACO vs ALNS)
if [ "$1" = "benchmark" ] || [ "$1" = "bench" ]; then
    shift
    MES="${1:-202601}"
    PEDIDOS="${2:-30}"
    echo "================================================================================"
    echo "  EJECUTANDO BENCHMARK NATIVO JAVA EN CONTENEDOR DOCKER (Mes: $MES, Pedidos: $PEDIDOS)"
    echo "================================================================================"
    exec java -jar /app/app.jar --benchmark --mes="$MES" --pedidos="$PEDIDOS"

# Modo 2: Benchmark Python (interactivo / con detalle)
elif [ "$1" = "python" ] || [ "$1" = "python-benchmark" ]; then
    shift
    exec python3 /app/probar_metaheuristicas.py "$@"

# Modo 3: Shell interactivo
elif [ "$1" = "sh" ] || [ "$1" = "bash" ]; then
    exec "$@"

# Modo 4: Servidor Web Spring Boot por defecto
elif [ "$1" = "web" ]; then
    echo "================================================================================"
    echo "  INICIANDO SERVIDOR WEB SPRING BOOT: ODIPARPACK LOGISTICS API                 "
    echo "  Swagger UI disponible en: http://localhost:8080/swagger-ui.html              "
    echo "================================================================================"
    exec java -jar /app/app.jar

# Fallback: ejecutar java pasándole todos los argumentos
else
    exec java -jar /app/app.jar "$@"
fi
