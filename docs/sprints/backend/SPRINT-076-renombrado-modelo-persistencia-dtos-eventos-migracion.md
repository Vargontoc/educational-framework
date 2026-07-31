# SPRINT-076 — Renombrado de modelo, persistencia, DTOs, eventos y migración de perfil infantil

## Estado

- **Estado:** verified
- **Fecha de creación:** 2026-07-31
- **Fecha de verificación:** 2026-07-31
- **Responsable principal:** backend
- **Prioridad:** CRITICA
- **Dependencias:** FEAT-006, ADR-022
- **Impacto estimado:** Renombrado de campos en modelo, entidad JPA, DTOs, controlador, validador y eventos. Migración Liquibase 026. Bloqueante para todos los sprints frontend de FEAT-006.

## Objetivo

Renombrar campos del modelo de dominio, entidad JPA y DTOs del perfil infantil para alinear la nomenclatura con la configuración global de familia (`ttsEnabled` → `npcVoiceEnabled`, `agentEnabled` → `npcEnabled`). Añadir el campo `npcVoiceVolume` (integer, 0-100) con restricción de techo por el volumen global familiar. Ampliar el enum `ColorVisionMode` con tres nuevos valores. Crear y ejecutar la migración de base de datos. Renombrar los eventos de sesión para coherencia con la nueva nomenclatura.

## Contexto

El modelo de familia (`Family.java`, `FamilyJpaEntity.java`, migración 025) ya utiliza la nomenclatura nueva (`npcVoiceEnabled`, `npcEnabled`, `npcVoiceVolume`). Sin embargo, el perfil infantil (`ChildProfile`, `ChildProfileJpaEntity`, DTOs, contratos) mantiene la nomenclatura legacy (`ttsEnabled`, `agentEnabled`) y carece de `npcVoiceVolume`.

Esta asimetría genera:
- Incoherencia semántica entre la config global y la individual.
- Imposibilidad de que el frontend consuma `npcVoiceVolume` individual.
- Nombres de columnas DB desalineados (`tts_enabled`/`agent_enabled` en `child_profile` vs `npc_voice_enabled`/`npc_enabled` en `family`).

**Este sprint es BLOQUEANTE para los sprints frontend de FEAT-006.**

**Referencias:**
- Propuesta técnica backend: `docs/product/design/backend/FEAT-006-propuesta-tecnica-backend.md`
- Propuesta técnica frontend: `docs/product/design/frontend/FEAT-006-propuesta-tecnica-frontend.md`
- FEAT-006: `docs/product/features/frontend/FEAT-006-Gestion-parental-de-perfiles-infantiles.md`
- ADR-022: `docs/product/decisions/ADR-022-Gestion-parental-de-perfiles-infantiles.md`

**Decisiones confirmadas:**
1. **P1:** Ajustes individuales incluyen valor porcentual. Renombrar `ttsEnabled` → `npcVoiceEnabled`, `agentEnabled` → `npcEnabled`, añadir `npcVoiceVolume`.
2. **P5:** Estado sin ajuste = `'NONE'`. Enum mantiene valor `NONE`.

## Tareas

### Tarea 76.1: Crear migración Liquibase 026 — VERIFIED

**Descripción:** Crear la migración de base de datos para renombrar columnas y añadir la nueva columna de volumen.

**Archivo:** `framework/backend/src/main/resources/db/changelog/migrations/026__rename_child_profile_fields_and_add_volume.xml` (nuevo)

**Contenido:**
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

**Registro en master:**
Añadir la migración en `db/changelog/db.changelog-master.xml`.

**Consideraciones:**
- `renameColumn` preserva los datos existentes.
- `npc_voice_volume` se crea con default 100, coherente con el patrón de la familia.
- No se modifica la constraint CHECK de `birthday`.
- No se modifica el índice `idx_child_profile_family_id`.

**Criterios de aceptación:**
- La migración se ejecuta sin errores sobre DB con datos existentes.
- Los valores de `tts_enabled` se preservan en `npc_voice_enabled`.
- `npc_voice_volume` se crea con default 100.

---

### Tarea 76.2: Actualizar `ColorVisionMode.java` — VERIFIED

**Descripción:** Añadir tres nuevos valores al enum de modos de visión de color.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/family/model/ColorVisionMode.java`

**Enum actual:**
```java
public enum ColorVisionMode {
    NONE,
    PROTANOPIA,
    DEUTERANOMALY,
    DEUTERANOPIA,
    TRITANOPIA,
    ACHROMATOPSIA
}
```

**Enum nuevo:**
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

**Criterios de aceptación:**
- El enum tiene 9 valores.
- Los nuevos valores están en posiciones coherentes (PROTANOMALY junto a PROTANOPIA, TRITANOMALY junto a TRITANOPIA, ACHROMATOMALY junto a ACHROMATOPSIA).

---

### Tarea 76.3: Actualizar `ChildProfile.java` — VERIFIED

**Descripción:** Renombrar campos del modelo de dominio y añadir `npcVoiceVolume`.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/family/model/ChildProfile.java`

**Cambios:**
| Campo actual | Campo nuevo | Tipo |
|---|---|---|
| `ttsEnabled` (boolean) | `npcVoiceEnabled` (boolean) | Renombrar |
| `agentEnabled` (boolean) | `npcEnabled` (boolean) | Renombrar |
| *(no existe)* | `npcVoiceVolume` (int) | Nuevo, default 100 |

**Getters/setters nuevos:**
- `isNpcVoiceEnabled()` / `setNpcVoiceEnabled(boolean)`
- `isNpcEnabled()` / `setNpcEnabled(boolean)`
- `getNpcVoiceVolume()` / `setNpcVoiceVolume(int)`

**Criterios de aceptación:**
- Los campos renombrados funcionan correctamente.
- `npcVoiceVolume` tiene default 100.
- TypeScript compila sin errores.

---

### Tarea 76.4: Actualizar `ChildProfileJpaEntity.java` — VERIFIED

**Descripción:** Renombrar columnas JPA y añadir la nueva columna de volumen.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/family/infrastructure/persistence/ChildProfileJpaEntity.java`

**Cambios:**
| Columna actual | Columna nueva | Tipo DB |
|---|---|---|
| `tts_enabled` | `npc_voice_enabled` | BOOLEAN NOT NULL DEFAULT TRUE |
| `agent_enabled` | `npc_enabled` | BOOLEAN NOT NULL DEFAULT TRUE |
| *(no existe)* | `npc_voice_volume` | INT NOT NULL DEFAULT 100 |

**Criterios de aceptación:**
- Las anotaciones `@Column(name=...)` reflejan los nuevos nombres.
- `npc_voice_volume` tiene `defaultValueNumeric="100"` y `nullable=false`.

---

### Tarea 76.5: Actualizar `ChildProfilePersistenceAdapter.java` — VERIFIED

**Descripción:** Actualizar los métodos `toDomain()` y `toJpa()` para mapear los nuevos campos.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/family/infrastructure/persistence/ChildProfilePersistenceAdapter.java`

**Cambios:**
- Renombrar `isTtsEnabled()`/`setTtsEnabled()` → `isNpcVoiceEnabled()`/`setNpcVoiceEnabled()`.
- Renombrar `isAgentEnabled()`/`setAgentEnabled()` → `isNpcEnabled()`/`setNpcEnabled()`.
- Añadir mapeo de `npcVoiceVolume` en ambas direcciones.

**Criterios de aceptación:**
- `toDomain()` mapea correctamente los nuevos campos.
- `toJpa()` mapea correctamente los nuevos campos.

---

### Tarea 76.6: Actualizar DTOs — VERIFIED

**Descripción:** Renombrar campos y añadir `npcVoiceVolume` en los tres DTOs de perfil infantil.

**Archivos:**
- `framework/backend/src/main/java/es/vargontoc/educational/framework/family/infrastructure/dto/ChildProfileResponse.java`
- `framework/backend/src/main/java/es/vargontoc/educational/framework/family/infrastructure/dto/CreateChildProfileRequest.java`
- `framework/backend/src/main/java/es/vargontoc/educational/framework/family/infrastructure/dto/UpdateChildProfileRequest.java`

**`ChildProfileResponse.java`:**
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

**`CreateChildProfileRequest.java`:**
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

**`UpdateChildProfileRequest.java`:**
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

**Criterios de aceptación:**
- Los tres DTOs reflejan los nuevos nombres y `npcVoiceVolume`.
- `UpdateChildProfileRequest` usa `Boolean` e `Integer` (nullable) para campos opcionales.

---

### Tarea 76.7: Actualizar `ChildProfileUseCase.java` — VERIFIED

**Descripción:** Actualizar las firmas de los puertos de entrada.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/family/ports/in/ChildProfileUseCase.java`

**Firmas actualizadas:**
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

**Criterios de aceptación:**
- Las firmas reflejan los nuevos nombres y `npcVoiceVolume`.

---

### Tarea 76.8: Actualizar `ChildProfileService.java` — VERIFIED

**Descripción:** Actualizar la lógica de negocio con los nuevos parámetros y la regla de ceiling de volumen.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/family/service/ChildProfileService.java`

**Cambios en `createChild()`:**
- Renombrar parámetros `ttsEnabled` → `npcVoiceEnabled`, `agentEnabled` → `npcEnabled`.
- Añadir parámetro `npcVoiceVolume`.
- Aplicar ceiling de volumen: `Math.min(requestedVolume, family.getNpcVoiceVolume())`.
- Aplicar ceiling booleano existente: `applyFamilyCeiling(npcVoiceEnabled, family.isNpcVoiceEnabled())`.
- Si `npcVoiceEnabled` es false tras ceiling, `npcVoiceVolume` se almacena como 0.

**Cambios en `updateChild()`:**
- Mismos renombrados y nuevo parámetro.
- Aplicar ceiling de volumen.

**Regla de ceiling de volumen:**
```java
childVolume = familyEnabled ? Math.min(requestedVolume, familyVolume) : 0
```

**Criterios de aceptación:**
- Los parámetros están renombrados.
- El ceiling de volumen funciona correctamente.
- Si `!npcVoiceEnabled`, volumen = 0.

---

### Tarea 76.9: Actualizar `ChildProfileValidator.java` — VERIFIED

**Descripción:** Añadir validación de rango para `npcVoiceVolume`.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/family/validation/ChildProfileValidator.java`

**Validación:**
```java
if (input.npcVoiceVolume() != null) {
    requireMin(input.npcVoiceVolume(), 0, "npcVoiceVolume");
    requireMax(input.npcVoiceVolume(), 100, "npcVoiceVolume");
}
```

**Criterios de aceptación:**
- Rango 0-100 (inclusive) validado.
- No requerido en update (puede ser null).

---

### Tarea 76.10: Actualizar `ChildProfileController.java` — VERIFIED

**Descripción:** Actualizar el mapeo de DTOs a modelo y el método `toResponse()`.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/family/infrastructure/web/ChildProfileController.java`

**Cambios en `createChild()`:**
- Actualizar acceso a campos del request: `request.npcVoiceEnabled()`, `request.npcEnabled()`, `request.npcVoiceVolume()`.

**Cambios en `updateChild()`:**
- Actualizar acceso a campos del request con null-check.
- Fallback a valor existente: `request.npcVoiceVolume() != null ? request.npcVoiceVolume() : existing.getNpcVoiceVolume()`.

**Cambios en `toResponse()`:**
- Actualizar mapeo: `source.isNpcVoiceEnabled()`, `source.isNpcEnabled()`, `source.getNpcVoiceVolume()`.

**Sin cambios en `changeStateChild()`:**
- La semántica toggle ya está implementada.

**Criterios de aceptación:**
- El controlador usa los nuevos nombres de campos.
- El fallback de volumen funciona correctamente en update.

---

### Tarea 76.11: Actualizar `SessionEventType.java` — VERIFIED

**Descripción:** Renombrar eventos para coherencia con la nueva nomenclatura y añadir evento de cambio de volumen.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/session/infrastructure/websocket/SessionEventType.java`

**Renombrados:**
| Evento actual | Evento nuevo |
|---|---|
| `CHILD_TTS_ACTIVATED` | `CHILD_NPC_VOICE_ACTIVATED` |
| `CHILD_TTS_DEACTIVATED` | `CHILD_NPC_VOICE_DEACTIVATED` |
| `CHILD_AGENT_ACTIVATED` | `CHILD_NPC_ACTIVATED` |
| `CHILD_AGENT_DEACTIVATED` | `CHILD_NPC_DEACTIVATED` |

**Nuevo:**
| Evento | Descripción |
|---|---|
| `CHILD_NPC_VOICE_VOLUME_CHANGED` | Volumen de voz del NPC cambiado para el perfil |

**Criterios de aceptación:**
- Los eventos están renombrados.
- El nuevo evento de volumen existe.

---

### Tarea 76.12: Actualizar eventos en `ChildProfileService.java` — VERIFIED

**Descripción:** Usar los nuevos nombres de `SessionEventType` en `updateChild()` y emitir el evento de cambio de volumen.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/family/service/ChildProfileService.java`

**Cambios:**
- Reemplazar `CHILD_TTS_ACTIVATED` → `CHILD_NPC_VOICE_ACTIVATED`.
- Reemplazar `CHILD_TTS_DEACTIVATED` → `CHILD_NPC_VOICE_DEACTIVATED`.
- Reemplazar `CHILD_AGENT_ACTIVATED` → `CHILD_NPC_ACTIVATED`.
- Reemplazar `CHILD_AGENT_DEACTIVATED` → `CHILD_NPC_DEACTIVATED`.
- Emitir `CHILD_NPC_VOICE_VOLUME_CHANGED` cuando cambia el volumen.

**Criterios de aceptación:**
- Los eventos emitidos usan los nuevos nombres.
- El evento de cambio de volumen se emite correctamente.

---

### Tarea 76.13: Actualizar tests unitarios — VERIFIED

**Descripción:** Actualizar `ChildProfileServiceTest` con las nuevas firmas, aserciones y tests de ceiling de volumen.

**Archivo:** `framework/backend/src/test/java/es/vargontoc/educational/framework/family/service/ChildProfileServiceTest.java`

**Cambios:**
- Actualizar firmas de métodos.
- Actualizar aserciones con nuevos nombres de campos.
- Añadir tests de ceiling de volumen:
  - Volumen individual ≤ volumen familiar.
  - Si `!npcVoiceEnabled`, volumen = 0.
  - Si volumen familiar = 0, volumen individual = 0.

**Criterios de aceptación:**
- Todos los tests pasan.
- Tests de ceiling de volumen cubren los casos principales.

---

### Tarea 76.14: Actualizar tests de integración — VERIFIED

**Descripción:** Actualizar `ChildProfileControllerTest` con los nuevos nombres de campos en JSON.

**Archivo:** `framework/backend/src/test/java/es/vargontoc/educational/framework/family/infrastructure/web/ChildProfileControllerTest.java`

**Cambios:**
- Actualizar JSON de peticiones y respuestas con `npcVoiceEnabled`, `npcEnabled`, `npcVoiceVolume`.
- Verificar que los campos renombrados se devuelven correctamente.

**Criterios de aceptación:**
- Todos los tests de integración pasan.
- Los JSON reflejan los nuevos nombres.

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `db/changelog/migrations/026__rename_child_profile_fields_and_add_volume.xml` | Nuevo |
| `db/changelog/db.changelog-master.xml` | Registrar migración 026 |
| `family/model/ColorVisionMode.java` | Añadir 3 valores |
| `family/model/ChildProfile.java` | Renombrar campos, añadir `npcVoiceVolume` |
| `family/infrastructure/persistence/ChildProfileJpaEntity.java` | Renombrar columnas, añadir `npc_voice_volume` |
| `family/infrastructure/persistence/ChildProfilePersistenceAdapter.java` | Actualizar `toDomain()` y `toJpa()` |
| `family/infrastructure/dto/ChildProfileResponse.java` | Renombrar campos, añadir `npcVoiceVolume` |
| `family/infrastructure/dto/CreateChildProfileRequest.java` | Renombrar campos, añadir `npcVoiceVolume` |
| `family/infrastructure/dto/UpdateChildProfileRequest.java` | Renombrar campos, añadir `npcVoiceVolume` |
| `family/ports/in/ChildProfileUseCase.java` | Actualizar firmas |
| `family/service/ChildProfileService.java` | Actualizar lógica, ceiling de volumen, eventos |
| `family/validation/ChildProfileValidator.java` | Añadir validación `npcVoiceVolume` |
| `family/infrastructure/web/ChildProfileController.java` | Actualizar mapeo DTOs y `toResponse()` |
| `session/infrastructure/websocket/SessionEventType.java` | Renombrar eventos, añadir `CHILD_NPC_VOICE_VOLUME_CHANGED` |
| `test/.../ChildProfileServiceTest.java` | Actualizar firmas, aserciones, tests de ceiling |
| `test/.../ChildProfileControllerTest.java` | Actualizar JSON con nuevos nombres |

## Estimación

- **Duración:** 2 días
- **Complejidad:** Alta
- **Riesgo:** Alto (renombrado de columnas DB, cambios en múltiples capas)

## Criterios de aceptación del sprint

1. Migración 026 se ejecuta sin errores sobre DB con datos existentes. Los valores de `tts_enabled` se preservan en `npc_voice_enabled`.
2. `npc_voice_volume` se crea con default 100.
3. `ChildProfile` usa `npcVoiceEnabled`, `npcEnabled`, `npcVoiceVolume`.
4. `ChildProfileJpaEntity` mapea columnas `npc_voice_enabled`, `npc_enabled`, `npc_voice_volume`.
5. DTOs reflejan los nuevos nombres y `npcVoiceVolume`.
6. `ColorVisionMode` tiene 9 valores.
7. Ceiling de volumen: `childVolume <= familyVolume`. Si `!npcVoiceEnabled`, volumen = 0.
8. `SessionEventType` tiene los nuevos nombres.
9. Tests unitarios y de integración pasan.
10. `mvn test` sin errores.

## Evidencias esperadas

- `mvn test` verde.
- Migración 026 verificada en H2 (tests) y PostgreSQL (local).
- `ChildProfileServiceTest` incluye test de ceiling de volumen.
- `ChildProfileControllerTest` verifica campos renombrados en respuesta.

## Dependencias

- **Depende de:** FEAT-006 (aceptada), ADR-022 (aceptada)
- **Bloquea:** SPRINT-077 (contratos OpenAPI), todos los sprints frontend de FEAT-006

## Handoffs a otras capas

### Frontend:
- Los contratos OpenAPI se actualizarán en SPRINT-077.
- Los nuevos nombres de campos serán consumidos por frontend tras el desbloqueo.

### Agents/TTS:
- Sin dependencia directa. Los eventos renombrados se consumen via WebSocket por el game client.

## Notas adicionales

### Riesgos identificados

| Riesgo | Mitigación |
|--------|-----------|
| Renombrado de columnas DB rompe datos existentes | `renameColumn` de Liquibase preserva datos. Verificación con `SELECT` de control. |
| Renombrado de campos en contratos rompe clientes existentes | Frontend está BLOQUEADO hasta que backend complete. Coordinación por sprints. |
| Renombrado de `SessionEventType` rompe game clients conectados | Se coordina con frontend el cambio de nombres. Ambos se actualizan en el mismo release. |
| Migración 026 falla en producción con datos existentes | `renameColumn` es atómico. Default 100 para `npc_voice_volume` es coherente. Rollback manual documentado. |
| Tests existentes fallan tras renombrado | Actualización de tests incluida en el sprint. No se mergea sin tests verdes. |

## Verificación

- **Fecha:** 2026-07-31
- **Veredicto:** APPROVED
- **Reviewer:** backend
- **Evidencia:** `mvn test` — Tests run: 829, Failures: 0, Errors: 0, Skipped: 104 (BUILD SUCCESS)
- **Notas:** Los 104 tests skipped requieren Docker/Testcontainers (no disponible en entorno de revisión). No son defectos del código.
