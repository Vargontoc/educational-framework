# Sprint 076 - frontend
# -----------------------------------------------

## Goal
Externalizar el tamaño táctil mínimo de los elementos del minijuego para permitir ajustes sin recompilar, consumiendo la configuración enviada por backend al iniciar sesión.

## Contexto

Verificado por análisis técnico (`analyser-frontend`, 2026-09-15):

- **ADR-028 §4 R6**: "Se propone como punto de partida una zona táctil de aproximadamente 88–96 px de lado, equivalente de forma orientativa a 15–16 mm físicos. No queda fijada como estándar definitivo hasta validarla en dispositivos reales."
- **FEAT-014 §4 R2**: "Las opciones deben ser táctiles, amplias, distinguibles y aptas para pantalla móvil y tableta; el rango de 88–96 px es una referencia no definitiva pendiente de validación."
- **SPRINT-073**: Ya extrajo `MIN_ELEMENT_HIT_SIZE` de constante a campo de instancia con default 80.
- **Estado actual**:
  - `MIN_ELEMENT_HIT_SIZE` es un campo de instancia en `RecognitionGameScene` con valor por defecto 80.
  - Backend SPRINT-101 creará `RecognitionProperties` con `touchTargetSizePx` (default 92, rango 88-96).
  - No hay mecanismo para recibir configuración de backend al iniciar sesión.
- **Gap crítico**:
  - El tamaño táctil no es configurable sin recompilar.
  - ADR-028 exige validación en dispositivos reales; sin configuración dinámica, los ajustes requieren nueva compilación.

## Status
status: pending
started_at:
closed_at:
blocked_by: SPRINT-073
waiting_for: Backend SPRINT-101 (configuración dinámica)

## Decisiones confirmadas (2026-09-15)

1. **Configuración recibida al iniciar sesión infantil.** Confirmado — el tamaño táctil se recibe como parte de la configuración inicial de sesión (via WebSocket o store global), no como endpoint REST separado.
2. **Rango válido: 88-96 px.** Confirmado — valores fuera de rango usan el valor por defecto (92 px).
3. **Fallback a valor por defecto si no se recibe configuración.** Confirmado — si backend no envía `touchTargetSizePx`, frontend usa 92 px.

## Diseño propuesto

### 1. Recepción de configuración

**Mecanismo**:
- Backend envía `touchTargetSizePx` como parte de la configuración de sesión infantil (en el evento `AUTH_ACK` o en un evento de configuración inicial).
- Frontend almacena el valor en `registry` de Phaser o en un store global (`stores/session.ts`).

**Alternativa considerada y descartada**:
- Endpoint REST `GET /api/v1/recognition/config`: descartado porque añade una llamada HTTP adicional. La configuración puede viajar en el WebSocket existente.

### 2. Consumo en `RecognitionGameScene`

**Lógica en `init()` o `create()`**:
- Leer `touchTargetSizePx` del registry/store.
- Validar rango: si está entre 88 y 96, usar el valor; si no, usar 92 (default).
- Asignar al campo de instancia `minElementHitSize`.

**Cambio respecto a SPRINT-073**:
- SPRINT-073 extrajo la constante a campo de instancia con default 80.
- SPRINT-076 cambia el default a 92 (coherente con backend) y lo hace configurable.

### 3. Propagación a otros componentes

**`RoundProgressBar`**:
- No usa `minElementHitSize`, no se ve afectado.

**`MinigameNubiLayer`**:
- El tamaño de Nubi (100x100px) es independiente del tamaño táctil de los elementos del minijuego.
- No se ve afectado.

## Contratos y dependencias externas

| Contrato | Estado | Notas |
|----------|--------|-------|
| `touchTargetSizePx` en configuración de sesión | Pendiente (Backend SPRINT-101) | Valor entero, rango 88-96 |
| SPRINT-073 (campo de instancia) | Requerida | `minElementHitSize` ya existe como campo |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | El valor recibido es fuera de rango y se usa default sin aviso | BAJA | Log de advertencia si el valor está fuera de rango. |
| R2 | La configuración no llega a tiempo (race condition) | BAJA | Si el valor no está en registry al iniciar la escena, usar default 92. |

## Tareas del sprint

### Recepción de configuración
- [ ] Definir mecanismo de recepción de `touchTargetSizePx` (evento WebSocket o store).
- [ ] Almacenar `touchTargetSizePx` en registry de Phaser o store global.
- [ ] Validar rango 88-96; si fuera de rango, usar default 92.
- [ ] Log de advertencia si el valor está fuera de rango.

### Consumo en RecognitionGameScene
- [ ] Leer `touchTargetSizePx` del registry/store en `init()` o `create()`.
- [ ] Asignar al campo de instancia `minElementHitSize`.
- [ ] Cambiar default de 80 a 92 (coherente con backend).
- [ ] Verificar que el cálculo de escala usa el nuevo valor.

### Pruebas
- [ ] Test: `touchTargetSizePx=88` se aplica correctamente.
- [ ] Test: `touchTargetSizePx=96` se aplica correctamente.
- [ ] Test: `touchTargetSizePx=50` (fuera de rango) usa default 92.
- [ ] Test: sin configuración recibida, se usa default 92.
- [ ] Test: los elementos táctiles respetan el tamaño configurado.

## Manual Tests
- Iniciar sesión infantil con `touchTargetSizePx=88`: verificar que los elementos táctiles tienen tamaño mínimo 88x88px.
- Iniciar sesión con `touchTargetSizePx=96`: verificar tamaño 96x96px.
- Iniciar sesión sin configuración: verificar default 92x92px.
- Verificar en logs que valores fuera de rango generan advertencia.

## Dependencies
- SPRINT-073 (campo de instancia) — `minElementHitSize` ya existe.
- Backend SPRINT-101 (configuración dinámica) — envía `touchTargetSizePx`.

## Agent Instruction
- No modificar la lógica de feedback visual (SPRINT-070), Nubi (SPRINT-071), pista/hint (SPRINT-072), celebración (SPRINT-072), ladder visual (SPRINT-074) ni accesibilidad cromática (SPRINT-075).
- Código, comentarios y nombres en inglés.
- El tamaño táctil es solo para los elementos del minijuego, no para Nubi ni otros componentes.

## Notes
- Este sprint cierra la externalización de configuración del minijuego.
- SPRINT-077 verificará la accesibilidad completa y añadirá pruebas E2E.
- El valor de `touchTargetSizePx` debe validarse en dispositivos reales (ADR-028).
