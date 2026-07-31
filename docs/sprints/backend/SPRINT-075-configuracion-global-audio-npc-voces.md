# SPRINT-074 — Configuración global de audio, NPC y voces

## Estado

- **Estado:** implemented
- **Fecha de creación:** 2026-07-30
- **Responsable principal:** backend
- **Prioridad:** ALTA
- **Dependencias:** FEAT-005 (Configuración global de audio, NPC y PIN)
- **Impacto estimado:** Soporte de persistencia y validación para configuración global familiar

## Objetivo

Ampliar el modelo de familia para soportar los 8 nuevos campos de configuración global (audio general, NPC, voz NPC, voz narrativa con estados y porcentajes), implementar la persistencia, validación y valores por defecto para familias existentes.

## Contexto

El FEAT-005 requiere que el adulto autenticado pueda configurar globalmente audio general, NPC, voz del NPC y voz narrativa. El frontend ya dispone de los contratos ampliados (SPRINT-023) y la vista completa (SPRINT-025).

**Situación actual:**
- El modelo de familia solo incluye `name`, `pin`, `ttsEnabled`, `agentEnabled`
- No hay persistencia de los nuevos campos de configuración global
- No hay validación de los nuevos campos en el PATCH
- No hay valores por defecto para familias existentes

**Decisiones confirmadas:**
1. **Coexistencia de campos legacy:** `ttsEnabled` y `agentEnabled` coexisten con los nuevos campos sin mapeos
2. **Valores por defecto:** Todo activo al 100% para familias existentes sin configuración previa
3. **PATCH parcial:** Solo se envían los campos modificados

## Tareas

### Tarea 74.1: Ampliar modelo de dominio `Family`

**Descripción:** Añadir los 8 nuevos campos al modelo de dominio de familia.

**Archivo:** `framework/backend/src/main/java/com/myfriendnubi/backend/family/domain/Family.java`

**Campos a añadir:**
```java
private boolean audioGeneralEnabled = true;
private int audioGeneralVolume = 100;
private boolean npcEnabled = true;
private boolean npcVoiceEnabled = true;
private int npcVoiceVolume = 100;
private boolean narrativeVoiceEnabled = true;
private int narrativeVoiceVolume = 100;
```

**Nota:** Los valores por defecto se aplican en la definición del campo para familias nuevas.

**Criterios de aceptación:**
- El modelo incluye los 8 nuevos campos
- Los valores por defecto son correctos (todo activo al 100%)
- Los getters y setters se generan correctamente
- TypeScript compila sin errores

---

### Tarea 74.2: Ampliar esquema de base de datos

**Descripción:** Añadir las columnas correspondientes a la tabla `family`.

**Archivo:** `framework/backend/src/main/resources/db/migration/V{version}__add_global_config_fields.sql` (nuevo)

**SQL:**
```sql
ALTER TABLE family
ADD COLUMN audio_general_enabled BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN audio_general_volume INT NOT NULL DEFAULT 100,
ADD COLUMN npc_enabled BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN npc_voice_enabled BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN npc_voice_volume INT NOT NULL DEFAULT 100,
ADD COLUMN narrative_voice_enabled BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN narrative_voice_volume INT NOT NULL DEFAULT 100;
```

**Criterios de aceptación:**
- La migración SQL se ejecuta correctamente
- Las columnas tienen los tipos correctos (BOOLEAN, INT)
- Los valores por defecto son correctos (TRUE, 100)
- Las columnas son NOT NULL
- Familias existentes reciben los valores por defecto

---

### Tarea 74.3: Ampliar DTOs de familia

**Descripción:** Actualizar los DTOs de solicitud y respuesta para incluir los nuevos campos.

**Archivos:**
- `framework/backend/src/main/java/com/myfriendnubi/backend/family/web/dto/UpdateFamilyRequest.java`
- `framework/backend/src/main/java/com/myfriendnubi/backend/family/web/dto/FamilyResponse.java`

**Campos a añadir:**
```java
// UpdateFamilyRequest.java
private Boolean audioGeneralEnabled;
private Integer audioGeneralVolume;
private Boolean npcEnabled;
private Boolean npcVoiceEnabled;
private Integer npcVoiceVolume;
private Boolean narrativeVoiceEnabled;
private Integer narrativeVoiceVolume;

// FamilyResponse.java
private boolean audioGeneralEnabled;
private int audioGeneralVolume;
private boolean npcEnabled;
private boolean npcVoiceEnabled;
private int npcVoiceVolume;
private boolean narrativeVoiceEnabled;
private int narrativeVoiceVolume;
```

**Nota:** En `UpdateFamilyRequest`, los campos son opcionales (wrapper types: Boolean, Integer) para permitir PATCH parcial.

**Criterios de aceptación:**
- Los DTOs incluyen los 8 nuevos campos
- `UpdateFamilyRequest` usa wrapper types para campos opcionales
- `FamilyResponse` usa tipos primitivos con valores por defecto
- Jackson serializa/deserializa correctamente

---

### Tarea 74.4: Implementar validación en PATCH

**Descripción:** Validar los nuevos campos en el endpoint PATCH `/api/v1/family`.

**Archivo:** `framework/backend/src/main/java/com/myfriendnubi/backend/family/web/FamilyController.java`

**Validaciones:**
```java
// Nuevo metedo para setear valores del volumen
int setDefaultAudio(value) {
    if(value <= 0)
        return 0;
    if(value >= 100)
        return 100;
    return value;
}
// Validar volúmenes (0-100)
if (request.getAudioGeneralVolume() != null) {
    request.setAudioGeneralVolume(setDefaultAudio(request.getAudioGeneralVolume()))
}
if (request.getNpcVoiceVolume() != null) {
    request.setNpcVoiceVolume(setDefaultAudio(request.getNpcVoiceVolume()))
}
if (request.getNarrativeVoiceVolume() != null) {
    request.setNarrativeVoiceVolume(setDefaultAudio(request.getNarrativeVoiceVolume()))
}

// Validar PIN (4 dígitos numéricos)
if (request.getPin() != null) {
    if (!request.getPin().matches("^\\d{4}$")) {
        throw new ValidationException("pin must be exactly 4 digits");
    }
}


```

**Criterios de aceptación:**
- Valida volúmenes (0-100) si se proporcionan
- Valida PIN (4 dígitos numéricos) si se proporciona
- Lanza `ValidationException` con mensaje claro si la validación falla
- Retorna HTTP 400 con detalle de error

---

### Tarea 74.5: Implementar lógica de actualización

**Descripción:** Actualizar el servicio de familia para aplicar los cambios de configuración global.

**Archivo:** `framework/backend/src/main/java/com/myfriendnubi/backend/family/application/FamilyService.java`

**Lógica:**
```java
public Family updateFamily(Long familyId, UpdateFamilyRequest request) {
    Family family = familyRepository.findById(familyId)
        .orElseThrow(() -> new FamilyNotFoundException(familyId));
    
    // Actualizar campos opcionales solo si se proporcionan
    if (request.getName() != null) {
        family.setName(request.getName());
    }
    if (request.getPin() != null) {
        family.setPin(passwordEncoder.encode(request.getPin()));
    }
    if (request.getTtsEnabled() != null) {
        family.setTtsEnabled(request.getTtsEnabled());
    }
    if (request.getAgentEnabled() != null) {
        family.setAgentEnabled(request.getAgentEnabled());
    }
    
    // Nuevos campos de configuración global
    if (request.getAudioGeneralEnabled() != null) {
        family.setAudioGeneralEnabled(request.getAudioGeneralEnabled());
    }
    if (request.getAudioGeneralVolume() != null) {
        family.setAudioGeneralVolume(request.getAudioGeneralVolume());
    }
    if (request.getNpcEnabled() != null) {
        family.setNpcEnabled(request.getNpcEnabled());
    }
    if (request.getNpcVoiceEnabled() != null) {
        family.setNpcVoiceEnabled(request.getNpcVoiceEnabled());
    }
    if (request.getNpcVoiceVolume() != null) {
        family.setNpcVoiceVolume(request.getNpcVoiceVolume());
    }
    if (request.getNarrativeVoiceEnabled() != null) {
        family.setNarrativeVoiceEnabled(request.getNarrativeVoiceEnabled());
    }
    if (request.getNarrativeVoiceVolume() != null) {
        family.setNarrativeVoiceVolume(request.getNarrativeVoiceVolume());
    }
    
    return familyRepository.save(family);
}
```

**Criterios de aceptación:**
- Actualiza solo los campos proporcionados (PATCH parcial)
- Mantiene los valores actuales si no se proporcionan
- Guarda los cambios en la base de datos
- Retorna la familia actualizada
---

### Tarea 74.6: Actualizar respuesta GET

**Descripción:** Asegurar que el endpoint GET `/api/v1/family` devuelve todos los campos de configuración global.

**Archivo:** `framework/backend/src/main/java/com/myfriendnubi/backend/family/web/FamilyController.java`

**Lógica:**
```java
@GetMapping
public FamilyResponse getFamily(@AuthenticationPrincipal CustomUserDetails userDetails) {
    Family family = familyService.getFamilyByAdultId(userDetails.getAdultId());
    return FamilyResponse.from(family);
}
```

**Mapeo en `FamilyResponse.from()`:**
```java
public static FamilyResponse from(Family family) {
    FamilyResponse response = new FamilyResponse();
    response.setId(family.getId());
    response.setName(family.getName());
    response.setTtsEnabled(family.isTtsEnabled());
    response.setAgentEnabled(family.isAgentEnabled());
    response.setAudioGeneralEnabled(family.isAudioGeneralEnabled());
    response.setAudioGeneralVolume(family.getAudioGeneralVolume());
    response.setNpcEnabled(family.isNpcEnabled());
    response.setNpcVoiceEnabled(family.isNpcVoiceEnabled());
    response.setNpcVoiceVolume(family.getNpcVoiceVolume());
    response.setNarrativeVoiceEnabled(family.isNarrativeVoiceEnabled());
    response.setNarrativeVoiceVolume(family.getNarrativeVoiceVolume());
    response.setCreatedAt(family.getCreatedAt());
    response.setUpdatedAt(family.getUpdatedAt());
    return response;
}
```

**Criterios de aceptación:**
- GET devuelve todos los campos de configuración global
- Los valores por defecto se aplican correctamente para familias existentes
- La respuesta es coherente con el schema `family-response.yaml`

---

### Tarea 74.7: Pruebas de integración

**Descripción:** Implementar pruebas de integración para los nuevos campos.

**Archivo:** `framework/backend/src/test/java/com/myfriendnubi/backend/family/web/FamilyControllerIntegrationTest.java`

**Escenarios a probar:**
1. **GET devuelve valores por defecto:** Familia sin configuración previa recibe todo activo al 100%
2. **PATCH parcial actualiza solo campos proporcionados:** Enviar solo `audioGeneralVolume: 50` no afecta otros campos
3. **PATCH completo actualiza todos los campos:** Enviar todos los campos los actualiza correctamente
4. **Validación de volumen fuera de rango:** Enviar `audioGeneralVolume: 150` retorna `audioGeneralVolume: 100`
5. **Validación de PIN inválido:** Enviar `pin: "123"` retorna HTTP 400
6. **Validación de PIN válido:** Enviar `pin: "1234"` se actualiza correctamente
7. **Coexistencia de campos legacy:** `ttsEnabled` y `audioGeneralEnabled` son independientes

**Criterios de aceptación:**
- Todas las pruebas pasan
- Los valores por defecto se aplican correctamente
- PATCH parcial funciona
- Validaciones funcionan
- Coexistencia de campos legacy funciona

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `framework/backend/src/main/java/com/myfriendnubi/backend/family/domain/Family.java` | Añadir 8 campos |
| `framework/backend/src/main/resources/db/migration/V{version}__add_global_config_fields.sql` | Nuevo archivo (migración) |
| `framework/backend/src/main/java/com/myfriendnubi/backend/family/web/dto/UpdateFamilyRequest.java` | Añadir 8 campos opcionales |
| `framework/backend/src/main/java/com/myfriendnubi/backend/family/web/dto/FamilyResponse.java` | Añadir 8 campos |
| `framework/backend/src/main/java/com/myfriendnubi/backend/family/web/FamilyController.java` | Añadir validaciones |
| `framework/backend/src/main/java/com/myfriendnubi/backend/family/application/FamilyService.java` | Añadir lógica de actualización |
| `framework/backend/src/test/java/com/myfriendnubi/backend/family/web/FamilyControllerIntegrationTest.java` | Añadir pruebas |

## Estimación

- **Duración:** 1 día
- **Complejidad:** Media
- **Riesgo:** Bajo (cambios de modelo y validación, sin lógica compleja)

## Criterios de aceptación del sprint

1. El modelo de familia incluye los 8 nuevos campos con valores por defecto
2. La migración SQL se ejecuta correctamente y aplica valores por defecto a familias existentes
3. Los DTOs incluyen los nuevos campos (opcionales en request, obligatorios en response)
4. PATCH valida volúmenes (0-100) y PIN (4 dígitos)
5. PATCH actualiza solo los campos proporcionados (parcial)
6. GET devuelve todos los campos de configuración global
7. Las pruebas de integración pasan
8. Coexistencia de campos legacy sin mapeos

## Evidencias esperadas

- Test manual: GET `/api/v1/family` devuelve todos los campos con valores por defecto
- Test manual: PATCH con `audioGeneralVolume: 50` actualiza solo ese campo
- Test manual: PATCH con `audioGeneralVolume: 150` actualiza solo ese campo a valor 100
- Test manual: PATCH con `pin: "1234"` actualiza el PIN
- Test manual: PATCH con `pin: "123"` retorna HTTP 400
- Pruebas de integración pasan (`mvn test`)
- Migración SQL se ejecuta correctamente en base de datos limpia
- Familias existentes reciben valores por defecto tras migración

## Handoffs a otras capas

### Frontend debe:
1. **Consumir GET** para cargar configuración actual
2. **Enviar PATCH** con los campos modificados
3. **Manejar errores** de validación (HTTP 400)
4. **Manejar errores** de autenticación (HTTP 401)


## Dependencias de producto

- FEAT-005 (Configuración global de audio, NPC y PIN)
- FEAT-004 (Panel parental y acción «Salir»)

## Notas adicionales

### Coexistencia de campos legacy

Los campos `ttsEnabled` y `agentEnabled` se mantienen por compatibilidad hacia atrás. El backend:
1. Los acepta en PATCH (sin validar ni mapear)
2. Los devuelve en GET
3. No los sincroniza con los nuevos campos

El frontend puede enviar ambos durante la transición, pero el backend los trata como campos independientes.

### Valores por defecto

Para familias existentes que no tengan configuración previa:
- La migración SQL aplica los valores por defecto (todo activo al 100%)
- El modelo de dominio tiene los valores por defecto en la definición de campos
- El GET devuelve estos valores correctamente

### Seguridad del PIN

- El PIN se almacena hasheado (usando `passwordEncoder`)
- El PIN no se devuelve en claro en el GET (solo se devuelve si se solicita explícitamente)
- El cambio de PIN no cierra la sesión automáticamente (el frontend maneja el logout)

### Rendimiento

- Los nuevos campos se cargan en el mismo GET de familia (sin consultas adicionales)
- El PATCH actualiza solo los campos proporcionados (eficiente)
- No se requieren índices adicionales (los campos se consultan por `familyId`)
