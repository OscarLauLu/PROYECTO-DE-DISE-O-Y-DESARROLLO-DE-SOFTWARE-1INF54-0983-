#!/usr/bin/env python3
"""
================================================================================
SCRIPT DE BENCHMARK Y EVALUACIÓN DE METAHEURÍSTICOS: ACO vs. ALNS
Proyecto de Diseño y Desarrollo de Software (1INF54-0983) - PaqRap
================================================================================
Prueba los algoritmos metaheurísticos Ant Colony Optimization (ACO) y
Adaptive Large Neighborhood Search (ALNS) sobre la red ortogonal de 70x50 km,
respetando:
  - Capacidades de flota (Autos: 24 u, Motos: 8 u, Bicis: 4 u)
  - Velocidades y costos (Auto: 40 km/h, S/8/km; Moto: 25 km/h, S/6/km; Bici: 12 km/h, S/3/km)
  - Tiempo de servicio de 60 min por parada de entrega
  - Ventanas de tiempo y plazos comprometidos (4h, 8h, 12h, 18h, 36h)
  - Bloqueos viales dinámicos en la malla ortogonal
================================================================================
"""

import os
import sys
import math
import time
import random
import argparse
from datetime import datetime, timedelta
from collections import deque
import json

# ==============================================================================
# CONFIGURACIÓN DEL SISTEMA
# ==============================================================================
ANCHO_KM = 70
ALTO_KM = 50
ALMACEN_CENTRAL = (35, 25)
TIEMPO_SERVICIO_MIN = 60

TIPOS_VEHICULO = {
    "Auto": {"capacidad": 24, "velocidad": 40.0, "costo_km": 8.0, "cantidad": 4},
    "Moto": {"capacidad": 8, "velocidad": 25.0, "costo_km": 6.0, "cantidad": 3},
    "Bicicleta": {"capacidad": 4, "velocidad": 12.0, "costo_km": 3.0, "cantidad": 3},
}

# ==============================================================================
# MODELOS DE DATOS
# ==============================================================================
class Pedido:
    def __init__(self, id_num, codigo, tiempo_reg, x, y, cliente_id, cantidad, plazo_horas):
        self.id = id_num
        self.codigo = codigo
        self.tiempo_reg = tiempo_reg
        self.x = x
        self.y = y
        self.cliente_id = cliente_id
        self.cantidad = cantidad
        self.plazo_horas = plazo_horas
        self.fecha_limite = tiempo_reg + timedelta(hours=plazo_horas)

    def holgura(self, t_actual):
        segundos = (self.fecha_limite - t_actual).total_seconds()
        return max(0.0, segundos / 60.0)

    def __repr__(self):
        return f"Pedido({self.codigo}, cant={self.cantidad}, dest=({self.x},{self.y}), plazo={self.plazo_horas}h)"

class Vehiculo:
    def __init__(self, codigo, tipo_nombre, cfg):
        self.codigo = codigo
        self.tipo = tipo_nombre
        self.capacidad_max = cfg["capacidad"]
        self.velocidad = cfg["velocidad"]
        self.costo_km = cfg["costo_km"]
        self.pos_x, self.pos_y = ALMACEN_CENTRAL

    def clonar(self):
        v = Vehiculo(self.codigo, self.tipo, {
            "capacidad": self.capacidad_max,
            "velocidad": self.velocidad,
            "costo_km": self.costo_km
        })
        v.pos_x, v.pos_y = self.pos_x, self.pos_y
        return v

class Parada:
    def __init__(self, pedido, hora_llegada):
        self.pedido = pedido
        self.hora_llegada = hora_llegada
        self.hora_salida = hora_llegada + timedelta(minutes=TIEMPO_SERVICIO_MIN)
        self.en_plazo = hora_llegada <= pedido.fecha_limite

class Ruta:
    def __init__(self, vehiculo, hora_inicio):
        self.vehiculo = vehiculo
        self.hora_inicio = hora_inicio
        self.paradas = []
        self.distancia_km = 0.0
        self.costo_total = 0.0
        self.tiempo_total_min = 0

    def carga_actual(self):
        return sum(p.pedido.cantidad for p in self.paradas)

    def capacidad_disponible(self):
        return self.vehiculo.capacidad_max - self.carga_actual()

# ==============================================================================
# RED VIAL Y MANEJO DE BLOQUEOS
# ==============================================================================
class RedVial:
    def __init__(self):
        self.bloqueos = []  # lista de dict: {"inicio", "fin", "aristas": set de claves}

    @staticmethod
    def clave_arista(x1, y1, x2, y2):
        if (x1, y1) <= (x2, y2):
            return f"{x1},{y1}-{x2},{y2}"
        return f"{x2},{y2}-{x1},{y1}"

    def agregar_bloqueo(self, inicio, fin, coords_lista):
        aristas = set()
        for i in range(0, len(coords_lista) - 3, 2):
            x1, y1 = coords_lista[i], coords_lista[i+1]
            x2, y2 = coords_lista[i+2], coords_lista[i+3]
            cx, cy = x1, y1
            step_x = 1 if x2 > x1 else (-1 if x2 < x1 else 0)
            step_y = 1 if y2 > y1 else (-1 if y2 < y1 else 0)
            while cx != x2:
                nx = cx + step_x
                aristas.add(self.clave_arista(cx, cy, nx, cy))
                cx = nx
            while cy != y2:
                ny = cy + step_y
                aristas.add(self.clave_arista(cx, cy, cx, ny))
                cy = ny
        self.bloqueos.append({"inicio": inicio, "fin": fin, "aristas": aristas})

    def aristas_bloqueadas_en(self, t):
        bloqueadas = set()
        for b in self.bloqueos:
            if b["inicio"] <= t <= b["fin"]:
                bloqueadas.update(b["aristas"])
        return bloqueadas

    def distancia_minima(self, p1, p2, t):
        if p1 == p2:
            return 0.0
        bloqueadas = self.aristas_bloqueadas_en(t)
        dist_manhattan = abs(p1[0] - p2[0]) + abs(p1[1] - p2[1])
        if not bloqueadas:
            return float(dist_manhattan)

        # BFS para camino mínimo evitando tramos bloqueados
        cola = deque([(p1[0], p1[1], 0)])
        visitados = {(p1[0], p1[1])}
        dest_x, dest_y = p2

        while cola:
            x, y, d = cola.popleft()
            if (x, y) == (dest_x, dest_y):
                return float(d)

            for dx, dy in ((1,0), (-1,0), (0,1), (0,-1)):
                nx, ny = x + dx, y + dy
                if 0 <= nx <= ANCHO_KM and 0 <= ny <= ALTO_KM:
                    if (nx, ny) not in visitados:
                        clave = self.clave_arista(x, y, nx, ny)
                        if clave not in bloqueadas:
                            visitados.add((nx, ny))
                            cola.append((nx, ny, d + 1))
        # Si no hay camino libre por bloqueos, retorna penalización alta
        return float(dist_manhattan * 1.5)

# ==============================================================================
# CARGA DE ARCHIVOS
# ==============================================================================
def parsear_tiempo(texto, anio=2026, mes=9):
    try:
        # Formato: ##d##h##m
        d = int(texto[0:2])
        h = int(texto[3:5])
        m = int(texto[6:8])
        return datetime(anio, mes, max(1, d), h, m, 0)
    except Exception:
        return datetime(anio, mes, 1, 0, 0, 0)

def extraer_anio_mes(ruta_archivo):
    base = os.path.basename(ruta_archivo)
    import re
    m = re.search(r"(\d{4})(\d{2})", base)
    if m:
        return int(m.group(1)), int(m.group(2))
    m2 = re.search(r"(\d{2})(\d{2})", base)
    if m2:
        return 2000 + int(m2.group(1)), int(m2.group(2))
    return 2026, 9

def cargar_pedidos(ruta_archivo, max_pedidos=None):
    pedidos = []
    if not os.path.exists(ruta_archivo):
        print(f"[ERROR] Archivo no encontrado: {ruta_archivo}")
        return pedidos

    anio, mes = extraer_anio_mes(ruta_archivo)
    with open(ruta_archivo, "r", encoding="utf-8", errors="ignore") as f:
        num = 0
        for linea in f:
            linea = linea.strip()
            if not linea or linea.startswith("#"):
                continue
            # Normalizar delimitador: ##d##h##m:x,y... a ##d##h##m,x,y...
            if ":" in linea and ("," not in linea or linea.find(":") < linea.find(",")):
                linea = linea.replace(":", ",", 1)
            partes = linea.split(",")
            if len(partes) >= 6:
                try:
                    num += 1
                    t_str = partes[0].strip()
                    x = int(partes[1].strip())
                    y = int(partes[2].strip())
                    cid = partes[3].strip()
                    cant = int(partes[4].strip())
                    plazo = int(partes[5].strip())

                    t_reg = parsear_tiempo(t_str, anio, mes)
                    ped = Pedido(num, f"PED-{num:04d}", t_reg, x, y, cid, cant, plazo)
                    pedidos.append(ped)
                    if max_pedidos and len(pedidos) >= max_pedidos:
                        break
                except Exception:
                    continue
    return pedidos

def cargar_bloqueos(ruta_archivo, red):
    if not os.path.exists(ruta_archivo):
        return 0
    anio, mes = extraer_anio_mes(ruta_archivo)
    cargados = 0
    with open(ruta_archivo, "r", encoding="utf-8", errors="ignore") as f:
        for linea in f:
            linea = linea.strip()
            if not linea or linea.startswith("#"):
                continue
            if ":" in linea:
                ventana, coords_str = linea.split(":", 1)
                tiempos = ventana.split("-")
                if len(tiempos) == 2:
                    try:
                        t_ini = parsear_tiempo(tiempos[0].strip(), anio, mes)
                        t_fin = parsear_tiempo(tiempos[1].strip(), anio, mes)
                        coords = [int(c.strip()) for c in coords_str.split(",") if c.strip()]
                        red.agregar_bloqueo(t_ini, t_fin, coords)
                        cargados += 1
                    except Exception:
                        continue
    return cargados

def instanciar_flota():
    flota = []
    idx = 1
    for tipo, cfg in TIPOS_VEHICULO.items():
        for i in range(cfg["cantidad"]):
            codigo = f"{tipo[:3].upper()}-{i+1:02d}"
            flota.append(Vehiculo(codigo, tipo, cfg))
            idx += 1
    return flota

# ==============================================================================
# ALGORITMO 1: ACO (ANT COLONY OPTIMIZATION)
# ==============================================================================
class MetaheuristicoACO:
    def __init__(self, alpha=1.0, beta=2.0, rho=0.1, iteraciones=15, hormigas=10):
        self.alpha = alpha
        self.beta = beta
        self.rho = rho
        self.iteraciones = iteraciones
        self.num_hormigas = hormigas
        self.feromonas = {}

    def _get_feromona(self, p1, p2):
        k = (p1, p2)
        return self.feromonas.get(k, 1.0)

    def _add_feromona(self, p1, p2, delta):
        k = (p1, p2)
        self.feromonas[k] = self.feromonas.get(k, 1.0) + delta

    def _evaporar(self):
        for k in self.feromonas:
            self.feromonas[k] = max(0.01, self.feromonas[k] * (1.0 - self.rho))

    def resolver(self, pedidos, flota, red, t_inicio):
        random.seed(42)
        flota_disp = [v.clonar() for v in flota]
        pedidos_pendientes = list(pedidos)
        rutas_finales = []

        for vehiculo in flota_disp:
            if not pedidos_pendientes:
                break

            mejor_ruta_vehiculo = None
            mejor_costo_vehiculo = float("inf")
            mejor_atendidos_count = -1

            for it in range(self.iteraciones):
                rutas_iter = []
                for h in range(self.num_hormigas):
                    ruta = self._construir_ruta_hormiga(vehiculo, pedidos_pendientes, red, t_inicio)
                    rutas_iter.append(ruta)

                # Evaluar mejor hormiga de la iteración
                for r in rutas_iter:
                    count_atendidos = len(r.paradas)
                    if count_atendidos > mejor_atendidos_count or (count_atendidos == mejor_atendidos_count and r.costo_total < mejor_costo_vehiculo):
                        mejor_ruta_vehiculo = r
                        mejor_costo_vehiculo = r.costo_total
                        mejor_atendidos_count = count_atendidos

                # Evaporación y refuerzo de feromonas
                self._evaporar()
                if mejor_ruta_vehiculo and mejor_ruta_vehiculo.paradas:
                    deposito = 1.0 / (mejor_ruta_vehiculo.costo_total + 1.0)
                    pos_ant = ALMACEN_CENTRAL
                    for p in mejor_ruta_vehiculo.paradas:
                        pos_act = (p.pedido.x, p.pedido.y)
                        self._add_feromona(pos_ant, pos_act, deposito)
                        pos_ant = pos_act

            if mejor_ruta_vehiculo and mejor_ruta_vehiculo.paradas:
                rutas_finales.append(mejor_ruta_vehiculo)
                atendidos_set = {p.pedido.id for p in mejor_ruta_vehiculo.paradas}
                pedidos_pendientes = [p for p in pedidos_pendientes if p.id not in atendidos_set]

        return rutas_finales

    def _construir_ruta_hormiga(self, vehiculo, pedidos, red, t_inicio):
        ruta = Ruta(vehiculo, t_inicio)
        pos_actual = ALMACEN_CENTRAL
        t_actual = t_inicio
        pendientes = list(pedidos)

        while pendientes:
            candidatos = []
            for p in pendientes:
                if ruta.carga_actual() + p.cantidad > vehiculo.capacidad_max:
                    continue
                d = red.distancia_minima(pos_actual, (p.x, p.y), t_actual)
                horas_viaje = d / vehiculo.velocidad
                llegada = t_actual + timedelta(minutes=math.ceil(horas_viaje * 60))
                # Ventana de entrega
                if llegada <= p.fecha_limite:
                    candidatos.append((p, d, llegada))

            if not candidatos:
                break

            # Selección probabilística por ruleta (ACO)
            pesos = []
            for p, d, llegada in candidatos:
                holgura_min = max(1.0, (p.fecha_limite - llegada).total_seconds() / 60.0)
                urgencia = 1.0 / holgura_min
                costo_tramo = d * vehiculo.costo_km
                heuristica = urgencia / (costo_tramo + 1.0)
                tau = self._get_feromona(pos_actual, (p.x, p.y))
                atractivo = (tau ** self.alpha) * (heuristica ** self.beta)
                pesos.append(atractivo)

            suma_pesos = sum(pesos)
            if suma_pesos <= 0:
                elegido_idx = random.randint(0, len(candidatos) - 1)
            else:
                r = random.uniform(0, suma_pesos)
                acum = 0.0
                elegido_idx = len(candidatos) - 1
                for i, w in enumerate(pesos):
                    acum += w
                    if acum >= r:
                        elegido_idx = i
                        break

            elegido, dist_tramo, llegada = candidatos[elegido_idx]
            parada = Parada(elegido, llegada)
            ruta.paradas.append(parada)
            ruta.distancia_km += dist_tramo
            ruta.costo_total += dist_tramo * vehiculo.costo_km

            pos_actual = (elegido.x, elegido.y)
            t_actual = parada.hora_salida  # +60 min de servicio
            pendientes.remove(elegido)

        if ruta.paradas:
            # Retorno al almacén central
            dist_retorno = red.distancia_minima(pos_actual, ALMACEN_CENTRAL, t_actual)
            ruta.distancia_km += dist_retorno
            ruta.costo_total += dist_retorno * vehiculo.costo_km
            minutos_retorno = math.ceil((dist_retorno / vehiculo.velocidad) * 60)
            t_retorno = t_actual + timedelta(minutes=minutos_retorno)
            ruta.tiempo_total_min = int((t_retorno - t_inicio).total_seconds() / 60)

        return ruta

# ==============================================================================
# ALGORITMO 2: ALNS (ADAPTIVE LARGE NEIGHBORHOOD SEARCH)
# ==============================================================================
class MetaheuristicoALNS:
    def __init__(self, t_inicial=100.0, enfriamiento=0.95, factor_destruccion=0.25, limite_segundos=2.0):
        self.t_inicial = t_inicial
        self.enfriamiento = enfriamiento
        self.factor_destruccion = factor_destruccion
        self.limite_segundos = limite_segundos

    def resolver(self, pedidos, flota, red, t_inicio):
        random.seed(42)
        flota_disp = [v.clonar() for v in flota]

        # 1. Solución Inicial Voraz (ordenada por urgencia/holgura)
        solucion_actual = self._solucion_voraz(pedidos, flota_disp, red, t_inicio)
        mejor_solucion = self._clonar_solucion(solucion_actual)

        # Operadores de destrucción
        destructores = ["random", "costo", "bloqueos"]
        pesos_destructores = [1.0, 1.0, 1.0]

        t_inicio_cpu = time.time()
        temperatura = self.t_inicial

        while (time.time() - t_inicio_cpu) < self.limite_segundos and temperatura > 0.1:
            # Seleccionar operador de destrucción
            op_idx = self._seleccionar_ruleta(pesos_destructores)
            op_nombre = destructores[op_idx]

            candidata = self._clonar_solucion(solucion_actual)
            liberados = self._destruir(candidata, op_nombre, red, t_inicio)
            self._reparar_voraz(candidata, liberados, red, t_inicio)

            costo_act = self._costo_solucion(solucion_actual)
            costo_cand = self._costo_solucion(candidata)

            # Criterio de aceptación por Recocido Simulado
            delta = costo_cand - costo_act
            aceptar = False
            if delta < 0:
                aceptar = True
            else:
                prob = math.exp(-delta / max(0.001, temperatura))
                if random.random() < prob:
                    aceptar = True

            if aceptar:
                solucion_actual = candidata
                if costo_cand < self._costo_solucion(mejor_solucion):
                    mejor_solucion = self._clonar_solucion(candidata)
                    pesos_destructores[op_idx] += 3.0  # Gran recompensa
                else:
                    pesos_destructores[op_idx] += 1.0
            else:
                pesos_destructores[op_idx] += 0.1

            temperatura *= self.enfriamiento

        return [r for r in mejor_solucion if r.paradas]

    def _solucion_voraz(self, pedidos, flota, red, t_inicio):
        ordenados = sorted(pedidos, key=lambda p: p.holgura(t_inicio))
        rutas = [Ruta(v, t_inicio) for v in flota]
        pendientes = list(ordenados)

        for p in pendientes:
            mejor_ruta = None
            mejor_pos = -1
            menor_inc_costo = float("inf")

            for r in rutas:
                if r.carga_actual() + p.cantidad <= r.vehiculo.capacidad_max:
                    # Probar inserción
                    for pos in range(len(r.paradas) + 1):
                        factible, inc = self._evaluar_insercion(r, p, pos, red, t_inicio)
                        if factible and inc < menor_inc_costo:
                            menor_inc_costo = inc
                            mejor_ruta = r
                            mejor_pos = pos

            if mejor_ruta is not None:
                self._insertar_parada(mejor_ruta, p, mejor_pos, red, t_inicio)

        return rutas

    def _evaluar_insercion(self, ruta, pedido, pos, red, t_inicio):
        # Simula ruta con inserción
        paradas_sim = list(ruta.paradas)
        paradas_sim.insert(pos, Parada(pedido, t_inicio))

        pos_act = ALMACEN_CENTRAL
        t_act = t_inicio
        dist_total = 0.0

        for parada in paradas_sim:
            d = red.distancia_minima(pos_act, (parada.pedido.x, parada.pedido.y), t_act)
            minutos = math.ceil((d / ruta.vehiculo.velocidad) * 60)
            llegada = t_act + timedelta(minutes=minutos)
            if llegada > parada.pedido.fecha_limite:
                return False, float("inf")
            dist_total += d
            pos_act = (parada.pedido.x, parada.pedido.y)
            t_act = llegada + timedelta(minutes=TIEMPO_SERVICIO_MIN)

        dist_ret = red.distancia_minima(pos_act, ALMACEN_CENTRAL, t_act)
        dist_total += dist_ret
        nuevo_costo = dist_total * ruta.vehiculo.costo_km
        inc_costo = nuevo_costo - ruta.costo_total
        return True, inc_costo

    def _insertar_parada(self, ruta, pedido, pos, red, t_inicio):
        ruta.paradas.insert(pos, Parada(pedido, t_inicio))
        self._recalcular_ruta(ruta, red, t_inicio)

    def _recalcular_ruta(self, ruta, red, t_inicio):
        pos_act = ALMACEN_CENTRAL
        t_act = t_inicio
        dist_total = 0.0

        for parada in ruta.paradas:
            d = red.distancia_minima(pos_act, (parada.pedido.x, parada.pedido.y), t_act)
            minutos = math.ceil((d / ruta.vehiculo.velocidad) * 60)
            llegada = t_act + timedelta(minutes=minutos)
            parada.hora_llegada = llegada
            parada.hora_salida = llegada + timedelta(minutes=TIEMPO_SERVICIO_MIN)
            parada.en_plazo = (llegada <= parada.pedido.fecha_limite)
            dist_total += d
            pos_act = (parada.pedido.x, parada.pedido.y)
            t_act = parada.hora_salida

        if ruta.paradas:
            d_ret = red.distancia_minima(pos_act, ALMACEN_CENTRAL, t_act)
            dist_total += d_ret
            min_ret = math.ceil((d_ret / ruta.vehiculo.velocidad) * 60)
            t_fin = t_act + timedelta(minutes=min_ret)
            ruta.tiempo_total_min = int((t_fin - t_inicio).total_seconds() / 60)
        else:
            ruta.tiempo_total_min = 0

        ruta.distancia_km = dist_total
        ruta.costo_total = dist_total * ruta.vehiculo.costo_km

    def _destruir(self, solucion, metodo, red, t_inicio):
        liberados = []
        asignados = []
        for r in solucion:
            for p in r.paradas:
                asignados.append((r, p.pedido))

        if not asignados:
            return liberados

        num_quitar = max(1, int(len(asignados) * self.factor_destruccion))

        if metodo == "random":
            quitar = random.sample(asignados, min(num_quitar, len(asignados)))
            for r, ped in quitar:
                r.paradas = [p for p in r.paradas if p.pedido.id != ped.id]
                liberados.append(ped)

        elif metodo == "costo":
            # Ordenar por costo/distancia del pedido
            candidatos_costo = []
            for r in solucion:
                pos_ant = ALMACEN_CENTRAL
                for p in r.paradas:
                    d = abs(pos_ant[0] - p.pedido.x) + abs(pos_ant[1] - p.pedido.y)
                    candidatos_costo.append((d * r.vehiculo.costo_km, r, p.pedido))
                    pos_ant = (p.pedido.x, p.pedido.y)
            candidatos_costo.sort(reverse=True, key=lambda x: x[0])
            for _, r, ped in candidatos_costo[:num_quitar]:
                r.paradas = [p for p in r.paradas if p.pedido.id != ped.id]
                liberados.append(ped)

        elif metodo == "bloqueos":
            # Remover pedidos cerca de zonas con bloqueos activos
            bloqueadas = red.aristas_bloqueadas_en(t_inicio)
            candidatos_bloq = []
            for r in solucion:
                for p in r.paradas:
                    # Chequeo si el nodo destino está en alguna arista bloqueada
                    es_afectado = any(f"{p.pedido.x},{p.pedido.y}" in a for a in bloqueadas)
                    if es_afectado:
                        candidatos_bloq.append((r, p.pedido))
            if not candidatos_bloq:
                candidatos_bloq = asignados
            quitar = random.sample(candidatos_bloq, min(num_quitar, len(candidatos_bloq)))
            for r, ped in quitar:
                r.paradas = [p for p in r.paradas if p.pedido.id != ped.id]
                liberados.append(ped)

        for r in solucion:
            self._recalcular_ruta(r, red, t_inicio)
        return liberados

    def _reparar_voraz(self, solucion, liberados, red, t_inicio):
        ordenados = sorted(liberados, key=lambda p: p.holgura(t_inicio))
        for p in ordenados:
            mejor_ruta = None
            mejor_pos = -1
            menor_inc = float("inf")

            for r in solucion:
                if r.carga_actual() + p.cantidad <= r.vehiculo.capacidad_max:
                    for pos in range(len(r.paradas) + 1):
                        factible, inc = self._evaluar_insercion(r, p, pos, red, t_inicio)
                        if factible and inc < menor_inc:
                            menor_inc = inc
                            mejor_ruta = r
                            mejor_pos = pos

            if mejor_ruta is not None:
                self._insertar_parada(mejor_ruta, p, mejor_pos, red, t_inicio)

    def _costo_solucion(self, solucion):
        costo = sum(r.costo_total for r in solucion)
        # Penalizar pedidos no atendidos
        total_paradas = sum(len(r.paradas) for r in solucion)
        return costo - (total_paradas * 1000.0)

    def _clonar_solucion(self, solucion):
        nueva = []
        for r in solucion:
            nr = Ruta(r.vehiculo.clonar(), r.hora_inicio)
            nr.paradas = [Parada(p.pedido, p.hora_llegada) for p in r.paradas]
            nr.distancia_km = r.distancia_km
            nr.costo_total = r.costo_total
            nr.tiempo_total_min = r.tiempo_total_min
            nueva.append(nr)
        return nueva

    def _seleccionar_ruleta(self, pesos):
        s = sum(pesos)
        if s <= 0:
            return random.randint(0, len(pesos) - 1)
        r = random.uniform(0, s)
        acum = 0.0
        for i, w in enumerate(pesos):
            acum += w
            if acum >= r:
                return i
        return len(pesos) - 1

# ==============================================================================
# FORMATEO Y REPORTE DE RESULTADOS
# ==============================================================================
def calcular_metricas(pedidos_total, rutas, tiempo_ms):
    pedidos_atendidos = sum(len(r.paradas) for r in rutas)
    tasa_atencion = (pedidos_atendidos * 100.0 / len(pedidos_total)) if pedidos_total else 0.0
    paradas_en_plazo = sum(1 for r in rutas for p in r.paradas if p.en_plazo)
    pct_en_plazo = (paradas_en_plazo * 100.0 / pedidos_atendidos) if pedidos_atendidos else 100.0
    distancia_total = sum(r.distancia_km for r in rutas)
    costo_total = sum(r.costo_total for r in rutas)
    tiempo_prom_ruta = (sum(r.tiempo_total_min for r in rutas) / len(rutas)) if rutas else 0.0

    return {
        "pedidos_evaluados": len(pedidos_total),
        "pedidos_atendidos": pedidos_atendidos,
        "tasa_atencion_pct": round(tasa_atencion, 1),
        "entregas_en_plazo_pct": round(pct_en_plazo, 1),
        "num_rutas": len(rutas),
        "distancia_total_km": round(distancia_total, 2),
        "costo_total_soles": round(costo_total, 2),
        "tiempo_ejecucion_ms": tiempo_ms,
        "tiempo_prom_ruta_min": round(tiempo_prom_ruta, 1)
    }

def imprimir_tabla_comparativa(m_aco, m_alns):
    print("\n" + "="*85)
    print(f"{'INDICADOR / MÉTRICA':<36} | {'ACO (Ant Colony)':<20} | {'ALNS (Neighborhood)':<22}")
    print("-" * 85)
    print(f"{'Pedidos evaluados':<36} | {m_aco['pedidos_evaluados']:<20} | {m_alns['pedidos_evaluados']:<22}")
    print(f"{'Pedidos atendidos':<36} | {m_aco['pedidos_atendidos']:<20} | {m_alns['pedidos_atendidos']:<22}")
    print(f"{'Tasa de atención':<36} | {m_aco['tasa_atencion_pct']:<19}% | {m_alns['tasa_atencion_pct']:<21}%")
    print(f"{'Entregas en plazo (On-Time)':<36} | {m_aco['entregas_en_plazo_pct']:<19}% | {m_alns['entregas_en_plazo_pct']:<21}%")
    print(f"{'Rutas construidas':<36} | {m_aco['num_rutas']:<20} | {m_alns['num_rutas']:<22}")
    print(f"{'Distancia total':<36} | {m_aco['distancia_total_km']:<17} km | {m_alns['distancia_total_km']:<19} km")
    print(f"{'Costo operativo total':<36} | S/ {m_aco['costo_total_soles']:<17} | S/ {m_alns['costo_total_soles']:<19}")
    print(f"{'Tiempo de ejecución':<36} | {m_aco['tiempo_ejecucion_ms']:<17} ms | {m_alns['tiempo_ejecucion_ms']:<19} ms")
    print(f"{'Duración prom. por ruta':<36} | {m_aco['tiempo_prom_ruta_min']:<16} min | {m_alns['tiempo_prom_ruta_min']:<18} min")
    print("=" * 85)

    # Análisis de ganador
    costo_dif = m_aco['costo_total_soles'] - m_alns['costo_total_soles']
    print("\n[ANÁLISIS COMPARATIVO]:")
    if abs(costo_dif) < 0.01:
        print("  -> Ambos algoritmos alcanzaron costos operativos equivalentes.")
    elif costo_dif > 0:
        pct = (costo_dif / m_aco['costo_total_soles']) * 100 if m_aco['costo_total_soles'] else 0
        print(f"  -> ALNS obtuvo una solución S/ {costo_dif:.2f} ({pct:.1f}%) más económica que ACO.")
    else:
        pct = (-costo_dif / m_alns['costo_total_soles']) * 100 if m_alns['costo_total_soles'] else 0
        print(f"  -> ACO obtuvo una solución S/ {-costo_dif:.2f} ({pct:.1f}%) más económica que ALNS.")

    if m_aco['tiempo_ejecucion_ms'] < m_alns['tiempo_ejecucion_ms']:
        print(f"  -> ACO fue más rápido ({m_aco['tiempo_ejecucion_ms']} ms vs {m_alns['tiempo_ejecucion_ms']} ms).")
    else:
        print(f"  -> ALNS fue más rápido ({m_alns['tiempo_ejecucion_ms']} ms vs {m_aco['tiempo_ejecucion_ms']} ms).")

def imprimir_tabla_individual(nombre, m):
    print("\n" + "="*60)
    print(f" RESULTADOS: {nombre.upper()}")
    print("="*60)
    for k, v in m.items():
        etiqueta = k.replace("_", " ").capitalize()
        print(f"  {etiqueta:<30}: {v}")
    print("="*60)

def imprimir_detalle_rutas(rutas, nombre_algo):
    print(f"\n--- DESGLOSE DE RUTAS GENERADAS ({nombre_algo.upper()}) ---")
    if not rutas:
        print("  (Sin rutas construidas)")
        return
    for r in rutas:
        print(f"\n  Vehículo {r.vehiculo.codigo} ({r.vehiculo.tipo}) | "
              f"Carga: {r.carga_actual()}/{r.vehiculo.capacidad_max} u | "
              f"Dist: {r.distancia_km:.1f} km | Costo: S/ {r.costo_total:.2f} | Tiempo: {r.tiempo_total_min} min")
        for i, p in enumerate(r.paradas, 1):
            estado_plazo = "[A TIEMPO]" if p.en_plazo else "[TARDE]"
            print(f"    {i:2d}. {p.pedido.codigo} -> Dest: ({p.pedido.x:2d},{p.pedido.y:2d}) | "
                  f"Cant: {p.pedido.cantidad:2d} u | Plazo: {p.pedido.fecha_limite.strftime('%d/%m %H:%M')} | "
                  f"Llegada: {p.hora_llegada.strftime('%d/%m %H:%M')} {estado_plazo}")

# ==============================================================================
# MAIN / CLI
# ==============================================================================
def main():
    parser = argparse.ArgumentParser(description="Benchmark de Algoritmos Metaheurísticos (ACO vs ALNS)")
    parser.add_argument("--mes", type=str, default="202601", help="Mes en formato AAAAMM (ej. 202601, 202602)")
    parser.add_argument("--pedidos", type=str, default="30", help="Cantidad de pedidos a evaluar (ej. 20, 50, all)")
    parser.add_argument("--algoritmo", type=str, choices=["ambos", "aco", "alns"], default="ambos", help="Algoritmo a ejecutar")
    parser.add_argument("--iteraciones", type=int, default=15, help="Iteraciones para ACO")
    parser.add_argument("--hormigas", type=int, default=10, help="Cantidad de hormigas para ACO")
    parser.add_argument("--detalle", action="store_true", help="Mostrar desglose de paradas por ruta")
    parser.add_argument("--exportar", type=str, default=None, help="Ruta de archivo JSON para exportar resultados")

    args = parser.parse_args()

    # Resolver rutas de archivos
    base_dir = os.path.dirname(os.path.abspath(__file__))
    datos_dir = os.path.join(base_dir, "datos")

    mes_corto = args.mes[2:] if len(args.mes) == 6 else args.mes
    ruta_ventas = os.path.join(datos_dir, "ventas.v20260909", f"ventas.{args.mes}.txt")
    ruta_bloqueos = os.path.join(datos_dir, "bloqueos", f"bloqueo.{mes_corto}.txt")

    print("\n" + "="*85)
    print("      SISTEMA LOGÍSTICO PAQRAP - EVALUADOR DE METAHEURÍSTICOS         ")
    print("="*85)
    print(f"  Mes de evaluación       : {args.mes}")
    print(f"  Archivo de pedidos      : {os.path.relpath(ruta_ventas, base_dir)}")
    print(f"  Archivo de bloqueos     : {os.path.relpath(ruta_bloqueos, base_dir)}")

    max_ped = None if args.pedidos.lower() == "all" else int(args.pedidos)

    # 1. Cargar datos
    red = RedVial()
    num_bloq = cargar_bloqueos(ruta_bloqueos, red)
    print(f"  Bloqueos viales activos : {num_bloq}")

    pedidos = cargar_pedidos(ruta_ventas, max_ped)
    print(f"  Pedidos cargados        : {len(pedidos)}")

    if not pedidos:
        print("[ERROR] No hay pedidos cargados para evaluar. Finalizando.")
        sys.exit(1)

    flota = instanciar_flota()
    t_inicio = pedidos[0].tiempo_reg

    print(f"  Flota disponible        : {len(flota)} unidades (4 Autos, 3 Motos, 3 Bicicletas)")
    print(f"  Instante simulado inicio: {t_inicio.strftime('%Y-%m-%d %H:%M:%S')}")
    print("="*85)

    res_aco = None
    res_alns = None
    rutas_aco = []
    rutas_alns = []

    # 2. Ejecución ACO
    if args.algoritmo in ("ambos", "aco"):
        print("\n>>> Ejecutando Algoritmo ACO (Ant Colony Optimization)...")
        aco = MetaheuristicoACO(iteraciones=args.iteraciones, hormigas=args.hormigas)
        t0 = time.time()
        rutas_aco = aco.resolver(pedidos, flota, red, t_inicio)
        duracion_aco_ms = int((time.time() - t0) * 1000)
        res_aco = calcular_metricas(pedidos, rutas_aco, duracion_aco_ms)

    # 3. Ejecución ALNS
    if args.algoritmo in ("ambos", "alns"):
        print(">>> Ejecutando Algoritmo ALNS (Adaptive Large Neighborhood Search)...")
        alns = MetaheuristicoALNS()
        t0 = time.time()
        rutas_alns = alns.resolver(pedidos, flota, red, t_inicio)
        duracion_alns_ms = int((time.time() - t0) * 1000)
        res_alns = calcular_metricas(pedidos, rutas_alns, duracion_alns_ms)

    # 4. Mostrar Resultados
    if args.algoritmo == "ambos":
        imprimir_tabla_comparativa(res_aco, res_alns)
    elif args.algoritmo == "aco":
        imprimir_tabla_individual("ACO", res_aco)
    elif args.algoritmo == "alns":
        imprimir_tabla_individual("ALNS", res_alns)

    # 5. Detalle si fue solicitado
    if args.detalle:
        if rutas_aco:
            imprimir_detalle_rutas(rutas_aco, "ACO")
        if rutas_alns:
            imprimir_detalle_rutas(rutas_alns, "ALNS")

    # 6. Exportar reporte
    if args.exportar:
        reporte = {
            "fecha_ejecucion": datetime.now().isoformat(),
            "mes": args.mes,
            "pedidos_evaluados": len(pedidos),
            "resultados": {
                "aco": res_aco,
                "alns": res_alns
            }
        }
        with open(args.exportar, "w", encoding="utf-8") as f:
            json.dump(reporte, f, indent=2)
        print(f"\n[OK] Resultados exportados a: {args.exportar}")

if __name__ == "__main__":
    main()
