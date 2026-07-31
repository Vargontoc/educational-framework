# Propuesta tecnica backend — FEAT-006: Gestion parental de perfiles infantiles

## 1. Capa principal

**Backend** — Spring Boot (Hexagonal Architecture) + PostgreSQL + Liquibase

## 2. Objetivo tecnico

Evolucionar el modelo de dominio, persistencia, DTOs y contratos OpenAPI del perfil infantil para:

- Alinear la nomenclatura del perfil infantil con la nomenclatura global de familia (`ttsEnabled` -> `npcVoiceEnabled`, `agentEnabled` -> `npcEnabled`).
- Anadir el campo `npcVoiceVolume` (integer, 0-100) al perfil infantil, con restriccion de techo (ceiling) por el volumen global familiar.
- Ampliar el enum `ColorVisionMode` con tres nuevos valores: `PROTANOMALY`, `TRITANOMALY`, `ACHROMATOMALY`.
- Documentar la semantica toggle del endpoint `PUT /family/children/activation/{id}`.
- Confirmar que `GET /sessions/children?familyId={id}` devuelve sesiones activas con `startedAt`.

**Todos los cambios de esta propuesta son BLOQUEANTES para los sprints frontend de FEAT-006.** El frontend no puede iniciar implementacion hasta que los contratos y el modelo backend esten completos y verificados.

## 3. Diseno API/reglas/persistencia

### 3.1 Analisis del estado actual

El modelo de familia (`Family.java`, `FamilyJpaEntity.java`, migracion 025) ya utiliza la nomenclatura nueva (`npcVoiceEnabled`, `npcEnabled`, `npcVoiceVolume`). Sin embargo, el perfil infantil (`ChildProfile`, `ChildProfileJpaEntity`, DTOs, contratos) mantiene la nomenclatura legacy (`ttsEnabled`, `agentEnabled`) y carece de `npcVoiceVolume`.

Esta asimetria genera:
- Incoherencia semantica entre la config global y la individual.
- Imposibilidad de que el frontend consuma `npcVoiceVolume` individual.
- Nombres de columnas DB desalineados (`tts_enabled`/`agent_enabled` en `child_profile` vs `npc_voice_enabled`/`npc_enabled` en `family`).

### 3.2 Cambios en modelo de dominio

#### `ChildProfile.java`

| Campo actual | Campo nuevo | Tipo | Notas |
|---|---|---|---|
| `ttsEnabled` (boolean) | `npcVoiceEnabled` (boolean) | boolean | Renombrar campo, getter y setter |
| `agentEnabled` (boolean) | `npcEnabled` (boolean) | boolean | Renombrar campo, getter y setter |
| *(no existe)* | `npcVoiceVolume` (int) | int | Nuevo campo. Default 100. Rango 0-100 |

Getter/setter nuevos:
- `isNpcVoiceEnabled()` / `setNpcVoiceEnabled(boolean)`
- `isNpcEnabled()` / `setNpcEnabled(boolean)`
- `getNpcVoiceVolume()` / `setNpcVoiceVolume(int)`

#### `ColorVisionMode.java`

Valores actuales: `NONE`, `PROTANOPIA`, `DEUTERANOMALY`, `DEUTERANOPIA`, `TRITANOPIA`, `ACHROMATOPSIA`.

Valores a anadir:

```java
public enum ColorVisionMode {
    NONE,
    PROTANOPIA,
    PROTANOMALY,
    DEUTERANOMALY,
    DEUTERANOPIA,
    TRITANOPIA,
    TRITANOMALY,
    ACHROMATOMALY,
    ACHROMATOPSIA
}
```

### 3.3 Cambios en entidad JPA

#### `ChildProfileJpaEntity.java`

| Columna actual | Columna nueva | Tipo DB | Notas |
|---|---|---|---|
| `tts_enabled` | `npc_voice_enabled` | BOOLEAN NOT NULL DEFAULT TRUE | Renombrar `@Column(name=...)` y campo |
| `agent_enabled` | `npc_enabled` | BOOLEAN NOT NULL DEFAULT TRUE | Renombrar `@Column(name=...)` y campo |
| *(no existe)* | `npc_voice_volume` | INT NOT NULL DEFAULT 100 | Nueva columna |

### 3.4 Cambios en DTOs

#### `ChildProfileResponse.java`

```java
public record ChildProfileResponse(
    Long id,
    Long familyId,
    String name,
    boolean active,
    LocalDate birthday,
    String avatar,
    boolean npcVoiceEnabled,
    boolean npcEnabled,
    int npcVoiceVolume,
    ColorVisionMode colorVisionMode,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

#### `CreateChildProfileRequest.java`

```java
public record CreateChildProfileRequest(
    String name,
    LocalDate birthday,
    String avatar,
    boolean npcVoiceEnabled,
    boolean npcEnabled,
    int npcVoiceVolume,
    ColorVisionMode colorVisionMode
) {}
```

#### `UpdateChildProfileRequest.java`

```java
public record UpdateChildProfileRequest(
    String name,
    LocalDate birthday,
    String avatar,
    Boolean npcVoiceEnabled,
    Boolean npcEnabled,
    Integer npcVoiceVolume,
    ColorVisionMode colorVisionMode
) {}
```

### 3.5 Cambios en puertos de entrada

#### `ChildProfileUseCase.java`

Firmas actualizadas:

```java
ChildProfile createChild(
    Long familyId,
    String name,
    LocalDate birthday,
    String avatar,
    boolean npcVoiceEnabled,
    boolean npcEnabled,
    int npcVoiceVolume,
    ColorVisionMode colorVisionMode
);

ChildProfile updateChild(
    Long id,
    String name,
    LocalDate birthday,
    String avatar,
    boolean npcVoiceEnabled,
    boolean npcEnabled,
    int npcVoiceVolume,
    ColorVisionMode colorVisionMode
);
```

### 3.6 Cambios en logica de negocio

#### `ChildProfileService.java`

**`createChild()`:**
- Renombrar parametros `ttsEnabled` -> `npcVoiceEnabled`, `agentEnabled` -> `npcEnabled`.
- Anadir parametro `npcVoiceVolume`.
- Aplicar ceiling de volumen: `Math.min(requestedVolume, family.getNpcVoiceVolume())`.
- Aplicar ceiling booleano existente: `applyFamilyCeiling(npcVoiceEnabled, family.isNpcVoiceEnabled())`.
- Si `npcVoiceEnabled` es false tras ceiling, `npcVoiceVolume` se almacena como 0.

**`updateChild()`:**
- Mismos renombrados y nuevo parametro.
- Aplicar ceiling de volumen: `Math.min(requestedVolume, family.getNpcVoiceVolume())`.
- Publicar eventos de sesion con nombres actualizados (ver 3.9).

**Regla de ceiling de volumen:**
```
childVolume = familyEnabled ? Math.min(requestedVolume, familyVolume) : 0
```

Esto garantiza que el volumen individual nunca supera el volumen global familiar, consistente con el patron de techo booleano ya existente.

### 3.7 Cambios en adaptador de persistencia

#### `ChildProfilePersistenceAdapter.java`

Actualizar metodos `toDomain()` y `toJpa()`:
- Renombrar `isTtsEnabled()`/`setTtsEnabled()` -> `isNpcVoiceEnabled()`/`setNpcVoiceEnabled()`.
- Renombrar `isAgentEnabled()`/`setAgentEnabled()` -> `isNpcEnabled()`/`setNpcEnabled()`.
- Anadir mapeo de `npcVoiceVolume` en ambas direcciones.

### 3.8 Cambios en controlador

#### `ChildProfileController.java`

**`createChild()`:**
- Actualizar acceso a campos del request: `request.npcVoiceEnabled()`, `request.npcEnabled()`, `request.npcVoiceVolume()`.

**`updateChild()`:**
- Actualizar acceso a campos del request con null-check: `request.npcVoiceEnabled()`, `request.npcEnabled()`, `request.npcVoiceVolume()`.
- Fallback a valor existente: `request.npcVoiceVolume() != null ? request.npcVoiceVolume() : existing.getNpcVoiceVolume()`.

**`toResponse()`:**
- Actualizar mapeo: `source.isNpcVoiceEnabled()`, `source.isNpcEnabled()`, `source.getNpcVoiceVolume()`.

**`changeStateChild()`:**
- Sin cambios funcionales. La semantica toggle ya esta implementada (`child.setActive(!child.isActive())`).

### 3.9 Cambios en eventos de sesion

#### `SessionEventType.java`

Renombrar eventos para coherencia con la nueva nomenclatura:

| Evento actual | Evento nuevo | Descripcion |
|---|---|---|
| `CHILD_TTS_ACTIVATED` | `CHILD_NPC_VOICE_ACTIVATED` | Voz del NPC activada para el perfil |
| `CHILD_TTS_DEACTIVATED` | `CHILD_NPC_VOICE_DEACTIVATED` | Voz del NPC desactivada para el perfil |
| `CHILD_AGENT_ACTIVATED` | `CHILD_NPC_ACTIVATED` | NPC activado para el perfil |
| `CHILD_AGENT_DEACTIVATED` | `CHILD_NPC_DEACTIVATED` | NPC desactivado para el perfil |

**Impacto:** Estos eventos se consumen via WebSocket por el frontend (game client). El frontend debe actualizarse para escuchar los nuevos nombres. Este cambio es parte del paquete bloqueante.

Adicionalmente, se deben anadir eventos para cambios de volumen:

| Evento nuevo | Descripcion |
|---|---|
| `CHILD_NPC_VOICE_VOLUME_CHANGED` | Volumen de voz del NPC cambiado para el perfil |

Este nuevo evento permite notificar al game client del cambio de volumen individual en tiempo real, consistente con los eventos de activacion/desactivacion.

### 3.10 Migracion de base de datos

Nueva migracion Liquibase `026__rename_child_profile_fields_and_add_volume.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    https://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.27.xsd">

    <changeSet id="026__rename_child_profile_fields_and_add_volume" author="vargontoc">
        <renameColumn tableName="child_profile"
            oldColumnName="tts_enabled"
            newColumnName="npc_voice_enabled"
            columnDataType="BOOLEAN"/>

        <renameColumn tableName="child_profile"
            oldColumnName="agent_enabled"
            newColumnName="npc_enabled"
            columnDataType="BOOLEAN"/>

        <addColumn tableName="child_profile">
            <column name="npc_voice_volume" type="INT" defaultValueNumeric="100">
                <constraints nullable="false"/>
            </column>
        </addColumn>
    </changeSet>

</databaseChangeLog>
```

**Consideraciones:**
- `renameColumn` preserva los datos existentes. Los valores de `tts_enabled` se trasladan a `npc_voice_enabled` sin perdida.
- `npc_voice_volume` se crea con default 100, coherente con el patron de la familia.
- No se modifica la constraint CHECK de `birthday`.
- No se modifica el indice `idx_child_profile_family_id`.
- Registrar en `db.changelog-master.xml`.

### 3.11 Validacion

#### `ChildProfileValidator.java`

Anadir validacion de `npcVoiceVolume`:
- Rango: 0-100 (inclusive).
- No requerido en update (puede ser null, se conserva el valor existente).

```java
if (input.npcVoiceVolume() != null) {
    requireMin(input.npcVoiceVolume(), 0, "npcVoiceVolume");
    requireMax(input.npcVoiceVolume(), 100, "npcVoiceVolume");
}
```

## 4. Contratos y dependencias externas

### 4.1 Inventario de contratos a modificar

| Fichero | Accion | Decision |
|---|---|---|
| `schemas/family/child-profile-response.yaml` | Renombrar `ttsEnabled` -> `npcVoiceEnabled`, `agentEnabled` -> `npcEnabled`. Anadir `npcVoiceVolume: integer (0-100)`. Ampliar enum `colorVisionMode` con `PROTANOMALY`, `TRITANOMALY`, `ACHROMATOMALY`. | P1, P5 |
| `schemas/family/update-child-profile-request.yaml` | Mismos cambios que `child-profile-response.yaml`. | P1, P5 |
| `schemas/family/create-child-profile-request.yaml` | Mismos cambios por coherencia. | P1, P5 |
| `paths/family/activate-children.yaml` | Anadir `description` con semantica toggle: «Intercambia el estado activo/inactivo del perfil infantil. No requiere body. Si el perfil tiene sesion activa, la termina.» | P2 |
| `paths/session/get-session-children.yaml` | Confirmar que la respuesta incluye `startedAt` (ya lo hace). Anadir `description` explicita. | P3 |

### 4.2 Detalle de cambios en esquemas

#### `child-profile-response.yaml` (propuesta)

```yaml
type: object
additionalProperties: false

properties:
  id:
    type: integer
    format: int64
  familyId:
    type: integer
    format: int64
  name:
    type: string
  active:
    type: boolean
  birthday:
    type: string
    format: date
  avatar:
    type: string
  npcVoiceEnabled:
    type: boolean
  npcEnabled:
    type: boolean
  npcVoiceVolume:
    type: integer
    minimum: 0
    maximum: 100
  colorVisionMode:
    type: string
    enum:
      - NONE
      - PROTANOPIA
      - PROTANOMALY
      - DEUTERANOMALY
      - DEUTERANOPIA
      - TRITANOPIA
      - TRITANOMALY
      - ACHROMATOMALY
      - ACHROMATOPSIA
  createdAt:
    type: string
    format: date-time
  updatedAt:
    type: string
    format: date-time
```

#### `update-child-profile-request.yaml` (propuesta)

```yaml
type: object
additionalProperties: false

properties:
  name:
    type: string
  birthday:
    type: string
    format: date
  avatar:
    type: string
  npcVoiceEnabled:
    type: boolean
  npcEnabled:
    type: boolean
  npcVoiceVolume:
    type: integer
    minimum: 0
    maximum: 100
  colorVisionMode:
    type: string
    enum:
      - NONE
      - PROTANOPIA
      - PROTANOMALY
      - DEUTERANOMALY
      - DEUTERANOPIA
      - TRITANOPIA
      - TRITANOMALY
      - ACHROMATOMALY
      - ACHROMATOPSIA
```

#### `create-child-profile-request.yaml` (propuesta)

```yaml
type: object
additionalProperties: false

properties:
  name:
    type: string
  birthday:
    type: string
    format: date
  avatar:
    type: string
  npcVoiceEnabled:
    type: boolean
  npcEnabled:
    type: boolean
  npcVoiceVolume:
    type: integer
    minimum: 0
    maximum: 100
  colorVisionMode:
    type: string
    enum:
      - NONE
      - PROTANOPIA
      - PROTANOMALY
      - DEUTERANOMALY
      - DEUTERANOPIA
      - TRITANOPIA
      - TRITANOMALY
      - ACHROMATOMALY
      - ACHROMATOPSIA
```

#### `activate-children.yaml` (propuesta)

```yaml
summary: Toggle child active state
description: >
  Intercambia el estado activo/inactivo del perfil infantil.
  No requiere body en la peticion.
  Si el perfil pasa a inactivo y tiene una sesion activa, la sesion se termina.
operationId: changeStateChild
tags:
  - child-profile-controller
parameters:
  - name: id
    in: path
    required: true
    schema:
      type: integer
      format: int64
responses:
  "204":
    description: No Content. Estado del perfil alternado correctamente.
  "404":
    description: Not Found. Perfil infantil no encontrado.
```

#### `get-session-children.yaml` (propuesta — solo documentacion)

```yaml
summary: Get active sessions
description: >
  Devuelve las sesiones activas de la familia identificada por familyId.
  Cada sesion incluye startedAt para calculo de duracion en tiempo real.
operationId: getActiveSessions
tags:
  - child-session-controller
parameters:
  - name: familyId
    in: query
    required: true
    schema:
      type: integer
      format: int64
responses:
  "200":
    description: OK
    content:
      "*/*":
        schema:
          $ref: "../../schemas/session/api-list-child-session-response.yaml"
```

### 4.3 Contratos sin cambios

| Fichero | Estado |
|---|---|
| `schemas/session/child-session-response.yaml` | Sin cambios. Ya incluye `startedAt`. |
| `schemas/session/api-list-child-session-response.yaml` | Sin cambios. |
| `schemas/family/api-child-profile-response.yaml` | Sin cambios (referencia a `child-profile-response.yaml`). |
| `schemas/family/api-list-child-profile-response.yaml` | Sin cambios (referencia a `child-profile-response.yaml`). |
| `paths/session/expel-session-children.yaml` | Sin cambios. |
| `paths/family/update-children.yaml` | Sin cambios (referencia a `update-child-profile-request.yaml`). |
| `paths/family/create-children.yaml` | Sin cambios (referencia a `create-child-profile-request.yaml`). |

### 4.4 Dependencias con otras capas

| Capa | Dependencia | Tipo |
|---|---|---|
| **Frontend** | Contratos OpenAPI actualizados | Bloqueante. Frontend no inicia hasta backend complete. |
| **Frontend** | Nuevos nombres de campos en respuestas y requests | Bloqueante. |
| **Frontend** | Nuevos eventos WebSocket (`CHILD_NPC_VOICE_ACTIVATED`, etc.) | Bloqueante si el frontend los consume. |
| **Agents** | `ColorVisionMode` ampliado | No bloqueante. Los agentes no usan directamente este enum. |
| **TTS** | Renombrado de eventos `CHILD_TTS_*` -> `CHILD_NPC_VOICE_*` | No bloqueante. TTS no consume estos eventos directamente. |

### 4.5 Ficheros backend afectados (inventario completo)

| Fichero | Accion |
|---|---|
| `family/model/ChildProfile.java` | Renombrar campos, anadir `npcVoiceVolume` |
| `family/model/ColorVisionMode.java` | Anadir 3 valores |
| `family/infrastructure/persistence/ChildProfileJpaEntity.java` | Renombrar columnas, anadir `npcVoiceVolume` |
| `family/infrastructure/persistence/ChildProfilePersistenceAdapter.java` | Actualizar `toDomain()` y `toJpa()` |
| `family/infrastructure/dto/ChildProfileResponse.java` | Renombrar campos, anadir `npcVoiceVolume` |
| `family/infrastructure/dto/CreateChildProfileRequest.java` | Renombrar campos, anadir `npcVoiceVolume` |
| `family/infrastructure/dto/UpdateChildProfileRequest.java` | Renombrar campos, anadir `npcVoiceVolume` |
| `family/infrastructure/web/ChildProfileController.java` | Actualizar mapeo DTOs y `toResponse()` |
| `family/ports/in/ChildProfileUseCase.java` | Actualizar firmas |
| `family/service/ChildProfileService.java` | Actualizar logica, ceiling de volumen, eventos |
| `family/validation/ChildProfileValidator.java` | Anadir validacion `npcVoiceVolume` |
| `session/infrastructure/websocket/SessionEventType.java` | Renombrar eventos, anadir `CHILD_NPC_VOICE_VOLUME_CHANGED` |
| `db/changelog/migrations/026__rename_child_profile_fields_and_add_volume.xml` | Nueva migracion |
| `db/changelog/db.changelog-master.xml` | Registrar migracion 026 |
| `test/.../ChildProfileControllerTest.java` | Actualizar nombres de campos en JSON |
| `test/.../ChildProfileServiceTest.java` | Actualizar firmas y aserciones |

## 5. Riesgos y mitigaciones

| Riesgo | Impacto | Probabilidad | Mitigacion |
|---|---|---|---|
| Renombrado de columnas DB rompe datos existentes | Alto: pérdida de configuracion de perfiles | Baja | `renameColumn` de Liquibase preserva datos. Se verifica en migracion con `SELECT` de control. |
| Renombrado de campos en contratos rompe clientes existentes | Alto: frontend deja de funcionar | Alta (intencionado) | Frontend esta BLOQUEADO hasta que backend complete. Coordinacion por sprints. |
| Renombrado de `SessionEventType` rompe game clients conectados | Medio: eventos no reconocidos | Media | Se coordina con frontend el cambio de nombres de eventos. Ambos se actualizan en el mismo release. |
| Ceiling de volumen individual mal calculado | Bajo: volumen incorrecto | Baja | Unit tests especificos para la regla de ceiling con multiples combinaciones. |
| Migracion 026 falla en produccion con datos existentes | Alto: tabla inconsistente | Baja | `renameColumn` es atomico. Default 100 para `npc_voice_volume` es coherente con el estado actual. Rollback manual documentado. |
| Tests existentes fallan tras renombrado | Bajo: CI bloqueado | Alta (intencionado) | Actualizacion de tests incluida en el sprint backend. No se mergea sin tests verdes. |
| Nuevo valor `ACHROMATOMALY` se confunde con `ACHROMATOPSIA` | Bajo: seleccion incorrecta por usuario | Baja | Nombres de enum claros. Frontend muestra etiquetas descriptivas. Sin impacto en backend. |

## 6. Preguntas de decision al usuario

No hay preguntas de decision pendientes. Todas las decisiones relevantes han sido confirmadas:

| # | Decision | Estado | Impacto backend |
|---|---|---|---|
| P1 | Ajustes individuales incluyen valor porcentual | Confirmada | Renombrar campos, anadir `npcVoiceVolume`, ceiling por volumen global |
| P2 | Endpoint `/activation/{id}` funciona como toggle sin body | Confirmada | Documentar semantica en OpenAPI. Sin cambios funcionales. |
| P3 | Polling cada 5s para duracion de sesion | Confirmada | Confirmar que `GET /sessions/children` devuelve `startedAt`. Ya lo hace. |
| P4 | Extraer stepper a componente reutilizable | Confirmada | Sin impacto backend. |
| P5 | Estado sin ajuste = `'NONE'` | Confirmada | Mantener enum con valor `NONE`. |

## 7. Sprints propuestos

### Sprint B1 — Modelo, persistencia y migracion

**Objetivo:** Renombrar campos del modelo de dominio, entidad JPA y DTOs; anadir `npcVoiceVolume`; ampliar `ColorVisionMode`; crear y ejecutar migracion DB.

**Tareas tecnicas backend:**

| # | Tarea | Descripcion |
|---|---|---|
| B1.1 | Crear migracion Liquibase 026 | Renombrar columnas `tts_enabled` -> `npc_voice_enabled`, `agent_enabled` -> `npc_enabled`. Anadir columna `npc_voice_volume INT DEFAULT 100 NOT NULL`. Registrar en `db.changelog-master.xml`. |
| B1.2 | Actualizar `ColorVisionMode.java` | Anadir `PROTANOMALY`, `TRITANOMALY`, `ACHROMATOMALY`. |
| B1.3 | Actualizar `ChildProfile.java` | Renombrar `ttsEnabled` -> `npcVoiceEnabled`, `agentEnabled` -> `npcEnabled`. Anadir campo `npcVoiceVolume: int`. Actualizar getters/setters. |
| B1.4 | Actualizar `ChildProfileJpaEntity.java` | Renombrar columnas y campos. Anadir columna `npc_voice_volume`. |
| B1.5 | Actualizar `ChildProfilePersistenceAdapter.java` | Actualizar `toDomain()` y `toJpa()` con nuevos nombres y `npcVoiceVolume`. |
| B1.6 | Actualizar DTOs | `ChildProfileResponse`, `CreateChildProfileRequest`, `UpdateChildProfileRequest`: renombrar campos y anadir `npcVoiceVolume`. |
| B1.7 | Actualizar `ChildProfileUseCase.java` | Actualizar firmas de `createChild()` y `updateChild()`. |
| B1.8 | Actualizar `ChildProfileService.java` | Renombrar parametros. Anadir logica de ceiling de volumen: `Math.min(requested, familyVolume)`. Si `!npcVoiceEnabled`, volumen = 0. |
| B1.9 | Actualizar `ChildProfileValidator.java` | Anadir validacion de rango 0-100 para `npcVoiceVolume`. |
| B1.10 | Actualizar `ChildProfileController.java` | Actualizar mapeo de DTOs a modelo y `toResponse()`. |
| B1.11 | Actualizar `SessionEventType.java` | Renombrar `CHILD_TTS_*` -> `CHILD_NPC_VOICE_*`, `CHILD_AGENT_*` -> `CHILD_NPC_*`. Anadir `CHILD_NPC_VOICE_VOLUME_CHANGED`. |
| B1.12 | Actualizar `ChildProfileService.java` (eventos) | Usar nuevos nombres de `SessionEventType` en `updateChild()`. Emitir `CHILD_NPC_VOICE_VOLUME_CHANGED` cuando cambia el volumen. |
| B1.13 | Actualizar tests unitarios | `ChildProfileServiceTest`: actualizar firmas, aserciones, test de ceiling de volumen. |
| B1.14 | Actualizar tests de integracion | `ChildProfileControllerTest`: actualizar JSON de peticiones y respuestas con nuevos nombres. |

**Criterios de aceptacion:**
- Migracion 026 se ejecuta sin errores sobre DB con datos existentes. Los valores de `tts_enabled` se preservan en `npc_voice_enabled`.
- `npc_voice_volume` se crea con default 100.
- `ChildProfile` usa `npcVoiceEnabled`, `npcEnabled`, `npcVoiceVolume`.
- `ChildProfileJpaEntity` mapea columnas `npc_voice_enabled`, `npc_enabled`, `npc_voice_volume`.
- DTOs reflejan los nuevos nombres y `npcVoiceVolume`.
- `ColorVisionMode` tiene 9 valores.
- Ceiling de volumen: `childVolume <= familyVolume`. Si `!npcVoiceEnabled`, volumen = 0.
- `SessionEventType` tiene los nuevos nombres.
- Tests unitarios y de integracion pasan.
- `mvn test` sin errores.

**Evidencias esperadas:**
- `mvn test` verde.
- Migracion 026 verificada en H2 (tests) y PostgreSQL (local).
- `ChildProfileServiceTest` incluye test de ceiling de volumen.
- `ChildProfileControllerTest` verifica campos renombrados en respuesta.

---

### Sprint B2 — Contratos OpenAPI y verificacion de bloqueo

**Objetivo:** Actualizar los contratos OpenAPI para reflejar los cambios del Sprint B1. Verificar que los endpoints funcionan correctamente con los nuevos contratos. Declarar backend como COMPLETADO para desbloquear frontend.

**Tareas tecnicas backend:**

| # | Tarea | Descripcion |
|---|---|---|
| B2.1 | Actualizar `child-profile-response.yaml` | Renombrar campos, anadir `npcVoiceVolume`, ampliar enum. |
| B2.2 | Actualizar `update-child-profile-request.yaml` | Mismos cambios. |
| B2.3 | Actualizar `create-child-profile-request.yaml` | Mismos cambios. |
| B2.4 | Actualizar `activate-children.yaml` | Anadir `description` con semantica toggle. Cambiar respuesta a 204. Anadir 404. |
| B2.5 | Actualizar `get-session-children.yaml` | Anadir `description` explicita sobre `startedAt`. |
| B2.6 | Verificacion end-to-end manual | Arrancar backend, crear perfil, verificar respuesta con nuevos campos. Verificar toggle. Verificar sesiones activas con `startedAt`. |
| B2.7 | Verificar expulsion de sesion | `DELETE /sessions/children/{id}/expel` funciona correctamente. |
| B2.8 | Documentar desbloqueo | Confirmar en la propuesta tecnica frontend que backend esta COMPLETADO. |

**Criterios de aceptacion:**
- Los 5 ficheros YAML actualizados son coherentes con la implementacion backend.
- `GET /family/children` devuelve `npcVoiceEnabled`, `npcEnabled`, `npcVoiceVolume` y enum ampliado.
- `POST /family/children` acepta los nuevos campos.
- `PATCH /family/children/{id}` acepta los nuevos campos y devuelve respuesta actualizada.
- `PUT /family/children/activation/{id}` funciona como toggle sin body. Documentado.
- `GET /sessions/children?familyId={id}` devuelve sesiones con `startedAt`.
- `mvn test` sin errores.

**Evidencias esperadas:**
- Contratos YAML actualizados y coherentes con implementacion.
- Verificacion manual end-to-end documentada.
- Confirmacion formal de backend COMPLETADO para desbloqueo de frontend.

---

### Resumen de dependencias entre sprints backend

```
Sprint B1 (Modelo, persistencia, migracion)
  |
  v
Sprint B2 (Contratos OpenAPI y verificacion)
  |
  v
[DESBLOQUEO de sprints frontend FEAT-006]
```

Sprint B2 depende de B1. Los sprints frontend no se inician hasta que B2 este completado y verificado.

### Matriz de trazabilidad decisiones -> tareas

| Decision | Tarea backend | Sprint |
|---|---|---|
| P1: Ajustes individuales con valor porcentual | B1.3, B1.4, B1.5, B1.6, B1.7, B1.8, B1.9, B1.10, B2.1, B2.2, B2.3 | B1, B2 |
| P2: Toggle sin body | B2.4 | B2 |
| P3: Polling 5s con `startedAt` | B2.5 | B2 |
| P5: Enum con `NONE` | B1.2, B2.1, B2.2, B2.3 | B1, B2 |
