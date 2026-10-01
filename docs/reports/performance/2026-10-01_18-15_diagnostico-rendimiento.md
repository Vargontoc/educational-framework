# Diagnóstico de Rendimiento WebSocket — 01/10/2026 18:15

**Skill aplicado:** WebSocket Performance Analyser, Trend Diagnostics & History Delta
**Ventana de muestreo:** 01/10/2026 18:11:32 → 18:15:32 (~4 minutos)
**Fuentes:** `reporte_actual.txt` (18:15:32) vs `reporte_anterior_1.txt` (18:11:32)

---

## 📊 COMPARATIVA E HISTORIAL DE TRÁFICO

### Volumen de Mensajes Recientes (Deltas)

| Tipo de mensaje | Count anterior | Count actual | Δ Mensajes |
|---|---|---|---|
| `auth` | 5 | 5 | **0** (sin tráfico nuevo) |
| `game_abandon` | 2 | 2 | **0** (sin tráfico nuevo) |
| `game_action` | 2 | 7 | **+5** ▲ |
| `game_ready` | 2 | 3 | **+1** |
| `game_start` | 2 | 3 | **+1** |
| `heartbeat` | 52 | 58 | **+6** (fondo esperado) |
| `world_discovery_interacted` | 2 | 3 | **+1** |
| `world_heartbeat` | 686 | 718 | **+32** (fondo esperado) |

### Evolución de Sesiones Activas

| Gauge | Anterior | Actual | Tendencia |
|---|---|---|---|
| `ws_sessions_open` | NaN | NaN | ⚠️ **Sin datos** |
| `ws_games_active` | NaN | NaN | ⚠️ Sin datos |
| `ws_games_locks` | NaN | NaN | ⚠️ Sin datos |
| `ws_world_states` | NaN | NaN | ⚠️ Sin datos |

> **Nota:** Los cuatro gauges de estado global reportan `NaN` en ambas muestras. No es posible evaluar concurrencia ni contención desde esta fuente.

---

## 🚨 ALERTAS DE RENDIMIENTO RECIENTE (DELTAS)

Cálculo: `Latencia_Periodo = Δ_Sum / Δ_Count`

| Tipo | Δ Msgs | Δ Tiempo (s) | **Latencia Periodo** | SLA | Estado |
|---|---|---|---|---|---|
| `game_action` | 5 | 0.1876 | **37.53 ms** | — (sin SLA) | ⚠️ **ADVERTENCIA** |
| `game_ready` | 1 | 0.0185 | **18.46 ms** | 100 ms | ✅ OK |
| `game_start` | 1 | 0.0002 | **0.23 ms** | 100 ms | ✅ OK |
| `heartbeat` | 6 | 0.0188 | **3.14 ms** | 25 ms | ✅ OK |
| `world_heartbeat` | 32 | 0.1355 | **4.23 ms** | 25 ms | ✅ OK |
| `world_discovery_interacted` | 1 | 0.0162 | **16.18 ms** | — (sin SLA) | ✅ OK |
| `auth` | 0 | 0 | N/A (sin tráfico) | 250 ms | — |
| `game_abandon` | 0 | 0 | N/A (sin tráfico) | — | — |

### Comportamiento respecto al reporte anterior (tendencia de picos `_max`)

| Tipo | `_max` anterior | `_max` actual | Tendencia |
|---|---|---|---|
| `game_action` | 0.0 ms | **132.59 ms** | 🔴 **Pico nuevo — primera aparición** |
| `game_ready` | 0.0 ms | **18.46 ms** | 🟡 Pico nuevo |
| `game_start` | 0.0 ms | 0.23 ms | 🟢 Despreciable |
| `heartbeat` | 3.62 ms | 3.74 ms | 🟢 Estable |
| `world_heartbeat` | 6.10 ms | 8.91 ms | 🟡 Leve subida (+46%) |
| `world_discovery_interacted` | 0.0 ms | **16.18 ms** | 🟡 Pico nuevo |

> **Conclusión de alertas:** Todos los tipos con SLA definido están **dentro de tolerancia**. Sin embargo, `game_action` muestra la latencia más alta del periodo (37.53 ms promedio, pico de 132.59 ms) y es la **única señal de degradación activa**.

---

## 🔍 ANÁLISIS DE CAUSA RAÍZ

### Fase Culpable — Desglose de `game_action` (Δ 5 mensajes)

| Fase | Δ Count | Δ Sum (s) | **Latencia fase** | % del total |
|---|---|---|---|---|
| **`db`** | 5 | 0.1711 | **34.22 ms** | **91.2 %** |
| `send` | 5 | 0.0162 | 3.24 ms | 8.6 % |
| `audio` | 4 | 0.000007 | 0.002 ms | ≈ 0 % |

> **La fase `db` absorbe el 91% del tiempo de procesamiento de `game_action`.** El pico máximo de la fase `db` alcanzó **129.79 ms** (anteriormente 0.0 — primera aparición).

### Fase Culpable — Desglose de `game_ready` (Δ 1 mensaje)

| Fase | Latencia | % del total |
|---|---|---|
| `db` | 10.32 ms | 55.9 % |
| `audio` | 5.51 ms | 29.9 % |
| `send` | 2.59 ms | 14.0 % |

> Distribución equilibrada; sin fase dominante. Dentro de SLA.

### Ratio de Consultas SQL por Mensaje (Periodo vs. Histórico)

| Tipo | Δ SQL | Δ Msgs | **SQL/msg periodo** | SQL/msg histórico | Δ Ratio |
|---|---|---|---|---|---|
| `game_action` | 94 | 5 | **18.8** | 5.0 | 🔴 **+276 %** |
| `game_ready` | 6 | 1 | 6.0 | 7.0 | 🟢 -14 % |
| `heartbeat` | 12 | 6 | 2.0 | 2.0 | 🟢 estable |
| `world_discovery_interacted` | 10 | 1 | 10.0 | 10.0 | 🟢 estable |
| `world_heartbeat` | 64 | 32 | 2.0 | 2.0 | 🟢 estable |

> **`game_action` ejecuta 18.8 sentencias SQL por mensaje frente a las 5.0 del histórico.** Esto indica un patrón de consultas desproporcionado — posible bucle, N+1 query o carga de datos innecesaria activada con la concurrencia nueva.

### Caché de Audio y TTS (Periodo)

| Métrica | Δ Periodo |
|---|---|
| Cache `hit` | +0 |
| Cache `miss` | +5 |
| **Hit rate del periodo** | **0 %** |
| Síntesis TTS nuevas | 5 |
| Latencia media TTS | 0.126 ms |
| Max TTS anterior → actual | 0.0 → 0.143 ms |

> La caché no está ayudando: todas las peticiones de audio del periodo fueron `miss`. Afortunadamente la síntesis TTS es muy rápida (0.126 ms media), por lo que el impacto en latencia total es mínimo.

---

## 💡 ACCIONES CORRECTIVAS PROPUESTAS

### 1. 🔴 Optimización Inmediata — `game_action` fase `db`

- **Problema:** El ratio SQL/msg pasó de 5.0 a 18.8 (+276%). La fase `db` concentra el 91% de la latencia (34.22 ms media, pico 129.79 ms).
- **Diagnóstico probable:** Con la llegada de tráfico concurrente real (5 `game_action` en 4 minutos vs. 2 acumulados), se ha activado un patrón de consulta ineficiente — posiblemente un **problema N+1**, una carga de estado de juego sin índice adecuado, o una lectura de estado de jugadores que escala linealmente con participantes.
- **Acción:** Revisar el handler de `game_action` en el backend. Identificar las 18.8 sentencias SQL promedio y reducir a ≤ 6 mediante:
  - Consolidación de consultas en un JOIN o batch.
  - Verificación de índices en tablas de estado de juego.
  - Eliminación de lecturas redundantes por jugador.

### 2. 🟡 Estrategia de Caché de Audio

- **Problema:** 0% hit rate en el periodo. 5 misses generaron 5 síntesis TTS nuevas.
- **Acción:** Aunque el coste TTS es bajo (0.126 ms), si el volumen crece la caché debería absorber repeticiones. Verificar que la clave de caché incluye el contexto correcto y que las entradas no están expirando prematuramente.

### 3. 🟡 Observación — Gauges en NaN

- **Problema:** Los 4 gauges globales (`ws_sessions_open`, `ws_games_active`, `ws_games_locks`, `ws_world_states`) reportan `NaN` consistentemente.
- **Acción:** Verificar que el registro de gauges se inicializa correctamente al arrancar la aplicación. Sin estos datos no es posible diagnosticar contención de hilos ni fugas de sesiones. Esto es una **ceguera operacional** para el equipo SRE.

### 4. 🟢 Monitoreo Continuo

- `heartbeat` y `world_heartbeat` se mantienen estables y dentro de SLA.
- No hay evidencia de degradación progresiva en los canales de keep-alive.
- Programar una tercera muestra en 5-10 minutos para confirmar si el pico de `game_action` (132.59 ms `_max`) fue aislado o es tendencia.

---

## 📋 RESUMEN EJECUTIVO

| Área | Veredicto |
|---|---|
| SLAs de latencia | ✅ **Todos dentro de tolerancia** |
| Degradación progresiva | ⚠️ `game_action` muestra pico nuevo (132.59 ms) y ratio SQL x276% |
| Fase crítica | 🔴 **`db` en `game_action`** — 91% del tiempo, 18.8 SQL/msg |
| Caché audio | 🟡 0% hit rate (TTS compensa por velocidad) |
| Gauges de estado | 🔴 **NaN** — sin visibilidad de concurrencia |
| **Veredicto global** | **ADVERTENCIA** — No hay ruptura de SLA, pero `game_action` requiere intervención antes de que escale con más carga. |
