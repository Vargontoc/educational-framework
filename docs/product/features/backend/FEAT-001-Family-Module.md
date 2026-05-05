# Feat-001 - Family Module

## Status

state: finished
user_history: Configuración módulo familia
depends_on:
owned_by: backend
test: Solo se puede crear una unica familia
sprints:
    - backend/sprints/history/003-exception-handler-cors-2026-05-01.md (Sprint 003 — completed)
    - backend/sprints/history/004-family-module-domain-2026-05-01.md (Sprint 004 — completed)
    - backend/sprints/history/005-family-module-web-2026-05-01.md (Sprint 005 — completed)

# Description

El objetivo de esta feature es definir la configuración de la familia que va a utilizar la aplicación. Es una aplicación monousuario por tanto solo habrá una familia, y a partir de esta se desgloda el resto como perfil de niñ@ y perdil de adulto. 

La familia tiene un PIN hasheada, de 4 digitos. Este PIN se usará como contraseña para que los adultos puedan entrar en su panel de control. El PIN no viene por defecto ni tiene validaciones, es seguridad muy básica (inexistente) pero como es privado y no tiene ninguna forma de autenticación adicional, se considera suficiente. 


# Schema proposal:

Family
    - Id
    - Name
    - PIN
    - tts_enabled
    - agent_enabled

ChildProfile
    - Id  (long)
    - family_id (long) (references Family[id])
    - Name (string)(required)
    - active (boolean)(required | default true) 
    - Birthday (date)(required - importante para el cálculo de la edad)
    - Avatar (string)(opcional, por defecto default-child)
    - tts_enabled
    - agent_enabled

AdultProfile
    - Id (long)
    - family_id (long) (references Family[id]) 
    - Name (string)(required)
    - Birthday (date)(required)
    - Avatar (string)(opcional, por defecto default-adult)

Podemos observar que tanto Family como ChildProfile tienen tts_enabled y agent_enabled. ChildProdile depende de Family por tanto, si Family tiene tts_enabled = false, entonces ChildProfile.tts_enabled también debe ser false. Lo mismo para agent_enabled.

---

## Análisis y recomendaciones

A continuación se incluyen errores potenciales, mitigaciones por capas y acciones recomendadas para implementar antes de mergear esta feature.

### Base de datos / Esquema
- Riesgo: existencia de más de una fila `Family` (rompe la premisa monousuario).
    - Mitigación: impedir insert adicionales a nivel BD (constraint o trigger). Crear la familia inicial en migration y devolver error en intentos de creación adicional. [Practicamente nula posibilidad, usuario final no va a tener acceso a la base de datos] [Y es un desarrollo individual]
  - Riesgo: `ChildProfile.family_id` nulo o sin FK.
    - Mitigación: `NOT NULL` + FK a `Family(id)` con `ON DELETE RESTRICT` y `INDEX` en `family_id`.
- Riesgo: inconsistencia de flags (`tts_enabled`, `agent_enabled`).
    - Mitigación: trigger/constraint que fuerce `child.tts_enabled = false` si `family.tts_enabled = false`. Alternativa: gestionar flags sólo desde `Family` y prohibir updates directos en child. [La familia puede deshabilitar la caracteristica a nivel global o a uno de sus hijos en especifico, por tanto es control a nivel lógica de negocio]
- Riesgo: `birthday` inválida.
    - Mitigación: CHECK en BD (`birthday <= CURRENT_DATE` y rango plausible, p. ej. > CURRENT_DATE - INTERVAL '120 years').

### Backend / API
- Almacenamiento de PIN: nunca en claro.
    - Mitigación: usar hashing fuerte (Argon2, bcrypt o scrypt) con salt por entrada. Documentar que, por tener sólo 4 dígitos, la entropía es baja, y aplicar protecciones adicionales (throttling, lockout). [No hace falta tanta seguridad para este escenario, monousuario y de uso privado]
- Brute-force del PIN.
    - Mitigación: bloqueo temporal tras N intentos (ej. 5 intentos → 15 min), rate-limiting por IP y por cuenta, registro de intentos y alertas. [No es necesario, a lo sumo que se podría aplicar rate-limiting]
- Condiciones de carrera al crear la familia.
    - Mitigación: transacción + lock (p. ej. `SELECT FOR UPDATE`) o constraint único en BD que haga fallar concurrencia.
- Propagación de flags al actualizar `Family`.
    - Mitigación: operación en transacción que actualice `ChildProfile` en la misma operación; crear pruebas de integración.
- Auditoría.
    - Mitigación: registrar cambios críticos (PIN, flags) en tabla de auditoría o logs estructurados.

Tener en cuenta que como mucho va a ver dos o tres personas a la vez, todos del mismo grupo familiar. Es una apliación privada, no es necesario tanta seguridad.

### Frontend / UX
- Mostrar feedback claro en login por PIN (intentos restantes, bloqueo temporal).
- Validar inputs en frontend y repetir validación en backend. Preferir validación compartida (JSON Schema o librería común).
- Confirmar en UI cuando la activación de `tts`/`agent` esté prohibida por la configuración de la `Family`.

### Integraciones (TTS / Agent)
- Consistencia runtime: cuando `Family.tts_enabled` cambie, emitir evento (mensajería interna) para que TTS/Agent refresquen configuración.
- Falla de servicios externos: usar circuit-breaker, reintentos con backoff y degradado elegante (p. ej. mostrar mensaje al usuario o usar fallback de audio local).
- Reconciliación periódica: job que verifique que los perfiles respetan las políticas del padre y corrija/avise discrepancias.

### Pruebas recomendadas
- Unitarias: validaciones DTOs, hashing y verificación de PIN, reglas de edad.
- Integración: transacción que actualiza `Family` y propaga flags a `ChildProfile`; FK y constraint tests.
- E2E: flujo creación de familia (única), login con PIN y bloqueo por intentos, cambio de flags y verificación en servicios TTS.
- Seguridad: test de throttling (staging) para validar bloqueo.

### Operaciones y runbook mínimo
- Métricas a recolectar: intentos de PIN (success/fail), bloqueos, cambios de flags, errores de constraint.
- Alertas: picos de intentos fallidos, intentos de creación de familia adicional, discrepancias TTS.
- Runbook rápido: pasos para desbloquear cuenta (admin), rotar secret del servidor si hay sospecha de exposición, restaurar desde backup probado.

### Acciones obligatorias a corto plazo
1. Implementar hashing seguro de PIN (Argon2/bcrypt) y no almacenar PIN en claro.
2. Añadir throttling/lockout (ej. 5 intentos → 15 minutos) y registro de intentos.
3. Añadir FK `ChildProfile.family_id NOT NULL` + índice, y constraint/trigger que impida más de una `Family`.
4. Validaciones de `birthday` (no futuro, rango plausible) en backend y BD.

### Cambios recomendados
- Considerar permitir PIN más largo o contraseña alfanumérica.
- Emitir evento cuando `Family` cambie flags para sincronizar TTS/Agent.
- Añadir tabla de auditoría para cambios en `Family`.

### Tests mínimos exigidos antes de merge
- Unit tests de validación y hashing.
- Integration test: toggle de `Family` → sincronización de `ChildProfile`.
- Test de bloqueo por intentos de PIN.

---

Si quieres, puedo aplicar estos cambios en forma de migration SQL y pruebas de ejemplo en el backend. ¿Deseas que los implemente ahora?