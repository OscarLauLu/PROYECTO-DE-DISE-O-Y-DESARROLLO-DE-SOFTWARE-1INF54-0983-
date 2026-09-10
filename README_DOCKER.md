# Guía de Ejecución con Docker: OdiparPack Logistics

Esta guía explica cómo compilar y ejecutar todo el sistema logístico y el benchmark de algoritmos metaheurísticos (**ACO** vs **ALNS**) usando **Docker** y **Docker Compose**.

---

## 1. Requisitos Previos
- Docker y Docker Compose instalados en tu sistema (`docker --version`, `docker compose version`).

---

## 2. Ejecución con Docker Compose (Recomendado)

### A. Ejecutar el Benchmark de Metaheurísticos en Java (ACO vs ALNS)
Para correr la prueba comparativa directamente en el contenedor Docker:
```bash
docker compose run --rm benchmark
```
> Esto cargará los pedidos y bloqueos de `datos/`, ejecutará ACO y ALNS, y mostrará la tabla de resultados en tu terminal.

#### Personalizar mes o cantidad de pedidos en el benchmark:
```bash
# Evaluar mes 202602 con 50 pedidos:
docker compose run --rm benchmark benchmark 202602 50

# Evaluar mes de alta demanda 202609 con 80 pedidos:
docker compose run --rm benchmark benchmark 202609 80
```

---

### B. Iniciar la Aplicación Web (Spring Boot API + Swagger UI)
Para levantar el servidor backend en segundo plano:
```bash
docker compose up -d app
```
- **API Swagger UI**: Abre tu navegador en [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Ver logs en tiempo real**: `docker compose logs -f app`
- **Detener la aplicación**: `docker compose down`

---

### C. Ejecutar el Benchmark con desglose detallado de paradas (Python)
Si deseas ver la lista detallada de paradas de cada vehículo con horas de llegada y cumplimiento:
```bash
docker compose run --rm benchmark python --mes 202601 --pedidos 25 --algoritmo ambos --detalle
```

---

## 3. Ejecución Directa con Docker (sin Compose)

### Construir la imagen:
```bash
docker build -t odiparpack .
```

### Ejecutar el benchmark:
```bash
# Benchmark rápido (30 pedidos, mes 202601)
docker run --rm -v $(pwd)/datos:/app/datos:ro odiparpack benchmark 202601 30

# Benchmark con 60 pedidos en 202602
docker run --rm -v $(pwd)/datos:/app/datos:ro odiparpack benchmark 202602 60
```

### Iniciar el servidor API web:
```bash
docker run -d --name odiparpack-api -p 8080:8080 -v $(pwd)/datos:/app/datos:ro odiparpack
```
Accede a la documentación OpenAPI en: `http://localhost:8080/swagger-ui.html`
