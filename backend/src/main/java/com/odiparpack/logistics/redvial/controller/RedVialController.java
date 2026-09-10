package com.odiparpack.logistics.redvial.controller;

import com.odiparpack.logistics.redvial.model.Bloqueo;
import com.odiparpack.logistics.redvial.model.Nodo;
import com.odiparpack.logistics.redvial.model.Tramo;
import com.odiparpack.logistics.redvial.service.RedVialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/redvial")
@RequiredArgsConstructor
@Tag(name = "Red Vial y Geografía", description = "Endpoints de la red vial ortogonal de 70x50 km y gestión de bloqueos (RF-01, RF-10, RF-11, RF-12)")
public class RedVialController {

    private final RedVialService redVialService;

    @GetMapping("/nodo")
    @Operation(summary = "Obtener nodo de la retícula por coordenadas (x, y)")
    public ResponseEntity<Nodo> obtenerNodo(@RequestParam int x, @RequestParam int y) {
        return ResponseEntity.ok(redVialService.obtenerNodo(x, y));
    }

    @GetMapping("/distancia")
    @Operation(summary = "Calcular distancia ortogonal en kilómetros entre dos puntos (RF-01)")
    public ResponseEntity<Double> calcularDistancia(
            @RequestParam int x1, @RequestParam int y1,
            @RequestParam int x2, @RequestParam int y2) {
        return ResponseEntity.ok(redVialService.calcularDistancia(x1, y1, x2, y2));
    }

    @GetMapping("/ruta-minima")
    @Operation(summary = "Calcular ruta mínima de tramos evitando bloqueos (RF-01, RF-12)")
    public ResponseEntity<List<Tramo>> calcularRutaMinima(
            @RequestParam int x1, @RequestParam int y1,
            @RequestParam int x2, @RequestParam int y2,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime instante) {
        return ResponseEntity.ok(redVialService.calcularRutaMinima(x1, y1, x2, y2, instante));
    }

    @GetMapping("/bloqueos")
    @Operation(summary = "Listar bloqueos viales registrados (RF-10, RF-11)")
    public ResponseEntity<List<Bloqueo>> listarBloqueos(@RequestParam(defaultValue = "false") boolean soloActivos) {
        if (soloActivos) {
            return ResponseEntity.ok(redVialService.listarBloqueosActivos());
        }
        return ResponseEntity.ok(redVialService.listarTodosBloqueos());
    }

    @PostMapping("/bloqueos/cargar")
    @Operation(summary = "Cargar archivo mensual de bloqueos (aaaamm.bloqueadas) (RF-11)")
    public ResponseEntity<List<Bloqueo>> cargarArchivoBloqueos(@RequestParam String rutaArchivo) {
        return ResponseEntity.ok(redVialService.cargarArchivoBloqueos(rutaArchivo));
    }
}
