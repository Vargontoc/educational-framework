# SPRINT-119: Rendimiento WebSocket de juego - Heartbeats sin escritura por latido

## Objetivo
Hacer que los latidos sean baratos: sin escritura en base de datos en cada latido y sin reenviar datos que no han cambiado.

## Contexto
Hallazgos de la auditoría 2026-10-01:
- `heartbeat`: SELECT + UPDATE de `last_activity_at` por latido, con un commit cada vez.
- `world_heartbeat` con posición: unas 2 SELECT y 2 UPDATE en transacciones distintas (`persistExplorationState` más `recordHeartbeat`).
- El handler reenvía el destino completo (con todos los elementos de descubrimiento) en cada latido si el estado es ACTIVE.

## Decisión aprobada (usuario, 2026-10-01)
Se acepta que `last_activity_at` se persista con un retraso de pocos segundos.

## Tareas
- [ ] Mantener la última actividad en memoria por sesión y volcarla a BD de forma diferida (intervalo configurable, p. ej. 10-15 s) y siempre al cerrar la sesión
- [ ] La expiración de sesión (`SessionExpirationJob`) y la inactividad de mundo deben leer la actividad en memoria o tolerar el retraso configurado; documentar el margen para que el tiempo de expiración efectivo no cambie de forma observable
- [ ] `world_heartbeat`: persistir `WorldExplorationState` solo cuando cambia el bioma (y a intervalo/cierre para la posición, según SPRINT-093); sin SELECT por latido
- [ ] Un único acceso a BD por intervalo, no por latido; evitar el `merge` completo reconstruyendo la entidad (`toJpa`) para actualizar una sola columna (update dirigido)
- [ ] Reenviar `WORLD_STATE_SYNC` con destino solo cuando cambie el destino o el estado; en el resto, ack ligero compatible con el contrato actual (si el frontend depende del destino en cada sync, se mantiene y se cachea el payload ya construido; confirmar con el analista de frontend antes de eliminar nada)
- [ ] Cachear el `WorldDestinationPayload` construido por destino para no reconstruir mapas en cada latido

### Tests
- [ ] 60 latidos en un minuto producen como mucho `ceil(60 / intervalo)` escrituras
- [ ] Al cerrar la sesión se vuelca la última actividad
- [ ] La expiración por inactividad sigue ocurriendo en el tiempo definido (± margen documentado)
- [ ] Cambio de bioma persiste inmediatamente
- [ ] El payload de `WORLD_STATE_SYNC` conserva su forma según contrato

### Pruebas manuales
- [ ] Dejar el niño sin actividad: la sesión se cierra en el tiempo esperado
- [ ] Jugar 5 minutos con latidos: comprobar con el contador SQL de SPRINT-114 que las escrituras bajan de ~1 por latido a ~1 por intervalo
- [ ] Viajar entre biomas y reconectar: se retoma en el bioma correcto

## Criterios de Aceptación
1. Un latido no ejecuta ninguna sentencia SQL en el camino normal
2. p95 de `heartbeat` y `world_heartbeat` < 20 ms
3. La expiración de sesión y la inactividad se comportan igual dentro del margen documentado
4. El bioma y la posición persistidos entre sesiones siguen funcionando (SPRINT-093)
5. Sin cambios en el contrato salvo confirmación explícita con frontend

## Contratos y dependencias
- `docs/contracts` (mensajes `world_heartbeat` y `WORLD_STATE_SYNC`): sin cambios previstos; cualquier cambio requiere confirmación con frontend.
- Familia: la actividad es un dato de sesión de juego, no se comparte ni se analiza.

## Riesgos
- Si el proceso cae, se pierden como mucho unos segundos de `last_activity_at`: aceptado.
- Doble fuente de verdad (memoria/BD) si no se centraliza: un único componente de actividad.

## Dependencias
- SPRINT-114 completado; independiente de 116-118.

## Estimación
- **Tamaño:** M | **Riesgo:** Medio
