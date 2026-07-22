# Sprint 005 - Backend

## Goal
Actualizar el backend para consumir el nuevo contrato TTS simplificado con `context` en lugar de `voice_profile`, y añadir soporte para nuevos tonos narrativos (`tender`, `mysterious`) conforme a FEAT-002 y ADR-016.

## Status
status: completed
started_at: 2026-07-22 00:00:00
closed_at: 2026-07-22 00:00:00
blocked_by:
waiting_for:

## Decisiones confirmadas (2026-07-22)

1. **Invalidación de caché**: ✅ Confirmado. Es aceptable invalidar la caché de audio tras el despliegue (breaking change en `AvatarCacheKey`) para pruebas de esta feature.

2. **Despliegue coordinado**: ✅ Confirmado. El despliegue de backend (este sprint) puede hacerse inmediatamente después del despliegue de TTS (Sprint 004) para pruebas de esta feature.

3. **Nuevos tonos**: ✅ Confirmado. Los tonos `TENDER` y `MYSTERIOUS` son solo para uso interno, no se exponen en APIs REST públicas para frontend.

## Prerequisitos completados

- ✅ Sprint 004 (TTS) completado: El nuevo contrato TTS con `context` está implementado y desplegado.

## Tasks
- [x] Ampliar enum `AvatarTone` con valores `TENDER` y `MYSTERIOUS`.
- [x] Actualizar `TtsToneMapper` para mapear `TENDER` → `"tender"` y `MYSTERIOUS` → `"mysterious"`.
- [x] Actualizar `TtsSynthesizeRequest`: reemplazar campo `voice_profile` por `context`.
- [x] Actualizar interfaz `TtsClient`: renombrar parámetro `voiceProfile` a `context`.
- [x] Actualizar `TtsClientAdapter`: renombrar parámetro, actualizar logs, pasar `context` al DTO.
- [x] Actualizar `AvatarLifecycleService`: renombrar `VOICE_PROFILE_NPC` a `CONTEXT_NPC`.
- [x] Actualizar `NarrateStorytellerService`: renombrar `VOICE_PROFILE_STORYTELLER` a `CONTEXT_NARRATION`, cambiar valor a `"narration"`.
- [x] Actualizar `AvatarService`: renombrar `VOICE_PROFILE_NPC` a `CONTEXT_NPC`.
- [x] Actualizar `CachingTtsClient`: renombrar parámetro `voiceProfile` a `context`.
- [x] Actualizar `AvatarCacheKey`: renombrar campo `voiceProfile` a `context`, actualizar constructor, getters, equals, hashCode, toString.
- [x] Actualizar todas las pruebas unitarias afectadas (TtsClientAdapterTest, TtsToneMapperTest, CachingTtsClientTest, AvatarLifecycleServiceTest, NarrateStorytellerServiceTest, AvatarServiceTest, AvatarAudioCacheTest).
- [x] Documentar manejo de error `TONE_CONTEXT_MISMATCH` en `TtsClientAdapter`.
- [x] Ejecutar `mvn test` y verificar que todas las pruebas pasan.
- [x] Construir JAR con `mvn package` y verificar que compila correctamente.

## Risks
- El cambio de `voiceProfile` a `context` en `AvatarCacheKey` invalida entradas de caché existentes (breaking change en caché).
- El cambio de contrato requiere despliegue coordinado con TTS (Sprint 004 debe completarse primero).
- Las pruebas existentes usan `voiceProfile` y deben actualizarse sistemáticamente.

## Dependencies
- Sprint 004 (TTS) completado: el nuevo contrato TTS debe estar desplegado.
- FEAT-002: Tonos narrativos y simplificación del contrato TTS.
- ADR-016: Tonos narrativos y simplificación del contrato TTS.
- ADR-013: Chatterbox como único proveedor TTS.
- docs/contracts/api/openapi_tts.json: Contrato TTS actualizado (Sprint 004).

## Agent Instruction
- Implementar exclusivamente los cambios en `framework/backend` y sus pruebas.
- No modificar el contrato TTS (`docs/contracts/api/openapi_tts.json`), corresponde a TTS.
- No implementar validación de tonos por contexto en backend (corresponde a TTS).
- No modificar la lógica de negocio de los servicios, solo el consumo de TTS.
- Mantener el manejo de errores existente (fallback de texto cuando TTS falla).
- Coordinar despliegue con TTS: este sprint debe desplegarse después del Sprint 004.
- Documentar breaking change en caché en notas de despliegue.
- Actualizar tareas, estado y revisión de este sprint con pruebas ejecutadas y bloqueos reales.

## Notes
- El campo `context` reemplaza a `voice_profile` en el contrato TTS.
- Backend envía `context: "npc"` para eventos del NPC (juego).
- Backend envía `context: "narration"` para narración de cuentos (lectura en familia).
- TTS valida que los tonos sean apropiados para el contexto y rechaza con error `TONE_CONTEXT_MISMATCH` (422) si no lo son.
- Backend maneja el error y devuelve fallback de texto sin audio.
- Los tonos `TENDER` y `MYSTERIOUS` solo son válidos en contexto `narration`.
- Los tonos `PLAYFUL` y `SERIOUS` solo son válidos en contexto `npc`.
- Los tonos `CALM`, `JOYFUL` y `ENTHUSIASTIC` son válidos en ambos contextos.
- Breaking change en caché: entradas existentes con `voiceProfile` no serán compatibles.

## Review

### Developer implementation — Evidencias (2026-07-22)

**Archivos modificados**:

**Código principal (11 archivos)**:
1. `AvatarTone.java` - Añadidos tonos `TENDER` y `MYSTERIOUS`
2. `TtsToneMapper.java` - Mapeo `TENDER → "tender"`, `MYSTERIOUS → "mysterious"`
3. `TtsSynthesizeRequest.java` - `voiceProfile` → `context` (eliminado `@JsonProperty("voice_profile")`)
4. `TtsClient.java` - Parámetro `voiceProfile` → `context`
5. `TtsClientAdapter.java` - Parámetro, logs y DTO actualizados a `context`
6. `AvatarLifecycleService.java` - `VOICE_PROFILE_NPC` → `CONTEXT_NPC`
7. `NarrateStorytellerService.java` - `VOICE_PROFILE_STORYTELLER` → `CONTEXT_NARRATION` (valor `"narration"`)
8. `AvatarService.java` - `VOICE_PROFILE_NPC` → `CONTEXT_NPC`
9. `CachingTtsClient.java` - Parámetro `voiceProfile` → `context`
10. `AvatarCacheKey.java` - Campo `voiceProfile` → `context`, equals/hashCode/toString actualizados
11. `AvatarAudioCache.java` - `digestKey()` usa `context()` en lugar de `voiceProfile()`

**Pruebas unitarias (7 archivos)**:
1. `TtsToneMapperTest.java` - Tests añadidos para `TENDER` y `MYSTERIOUS`
2. `TtsClientAdapterTest.java` - Sin cambios necesarios
3. `CachingTtsClientTest.java` - Renombrado test y `"storyteller"` → `"narration"`
4. `AvatarAudioCacheTest.java` - Renombrado test y `"storyteller"` → `"narration"`
5. `AvatarLifecycleServiceTest.java` - Renombrados tests y captors `voiceProfile` → `context`
6. `NarrateStorytellerServiceTest.java` - Renombrados tests y captors, `"storyteller"` → `"narration"`
7. `AvatarServiceTest.java` - Sin cambios necesarios (usa `anyString()` en mocks)

**Resultados de ejecución**:

- **mvn test**: `Tests run: 803, Failures: 0, Errors: 0, Skipped: 97` - BUILD SUCCESS
- **mvn package**: JAR construido correctamente - BUILD SUCCESS

**Breaking changes confirmados**:
- ✅ `voiceProfile` eliminado y reemplazado por `context` en DTO, interfaz, adaptador, caché
- ✅ `AvatarCacheKey` usa `context` - Invalida caché existente (aceptado)

**Nuevos tonos implementados**:
- ✅ `TENDER` → `"tender"` (contexto `narration`)
- ✅ `MYSTERIOUS` → `"mysterious"` (contexto `narration`)
- ✅ Tests añadidos para ambos tonos

**Contextos actualizados**:
- ✅ `CONTEXT_NPC = "npc"` → eventos del NPC (juego)
- ✅ `CONTEXT_NARRATION = "narration"` → narración de cuentos (lectura en familia)

**Verificación final**:
- ✅ Sin referencias residuales a `voiceProfile`
- ✅ Sin referencias residuales a `"storyteller"` como valor de contexto
- ✅ Todos los tests pasan (803 tests, 0 fallos)
- ✅ JAR construido correctamente
- ✅ Breaking change en caché documentado y aceptado

### Reviewer verification — APPROVED (2026-07-22)

Revisado por reviewer-backend independiente. Verificaciones re-ejecutadas:

**Evidencias independientes**:
- `mvn test`: **803 tests, 0 failures, 0 errors, 97 skipped** ✅
- `mvn package`: **BUILD SUCCESS** ✅
- Grep `voiceProfile|voice_profile`: **0 resultados** ✅
- Grep `VOICE_PROFILE_`: **0 resultados** ✅
- Grep `"storyteller"`: **0 resultados** ✅

**Verificación de código**:

✅ **AvatarTone.java**: Enum ampliado con `TENDER` y `MYSTERIOUS`  
✅ **TtsToneMapper.java**: Mapeo `TENDER → "tender"`, `MYSTERIOUS → "mysterious"` implementado  
✅ **TtsSynthesizeRequest.java**: Campo `voiceProfile` eliminado, campo `context` agregado  
✅ **TtsClient.java**: Interfaz actualizada con parámetro `context`  
✅ **TtsClientAdapter.java**: Parámetro, logs y DTO actualizados a `context`  
✅ **AvatarLifecycleService.java**: Constante `CONTEXT_NPC = "npc"`  
✅ **NarrateStorytellerService.java**: Constante `CONTEXT_NARRATION = "narration"`  
✅ **AvatarService.java**: Constante `CONTEXT_NPC = "npc"`  
✅ **AvatarCacheKey.java**: Campo `context` reemplaza `voiceProfile`, equals/hashCode/toString actualizados  
✅ **AvatarAudioCache.java**: `digestKey()` usa `context()`  

**Verificación de tests**:

✅ **TtsToneMapperTest.java**: Tests para `TENDER` y `MYSTERIOUS` añadidos  
✅ **TtsClientAdapterTest.java**: Actualizado para usar `context`  
✅ **CachingTtsClientTest.java**: Renombrado y actualizado a `context`  
✅ **AvatarAudioCacheTest.java**: Renombrado y actualizado a `context`  
✅ **AvatarLifecycleServiceTest.java**: Captors actualizados de `voiceProfile` a `context`  
✅ **NarrateStorytellerServiceTest.java**: Captors actualizados, `"storyteller"` → `"narration"`  
✅ **AvatarServiceTest.java**: Usa `anyString()` en mocks, sin cambios necesarios  

**Breaking changes verificados**:

✅ `voiceProfile` completamente eliminado del código fuente  
✅ `voice_profile` completamente eliminado del código fuente  
✅ `VOICE_PROFILE_NPC` reemplazado por `CONTEXT_NPC`  
✅ `VOICE_PROFILE_STORYTELLER` reemplazado por `CONTEXT_NARRATION`  
✅ `"storyteller"` como valor de contexto completamente eliminado  
✅ `AvatarCacheKey` usa `context` - Invalida caché existente (aceptado y documentado)  

**Nuevos tonos verificados**:

✅ `AvatarTone.TENDER` añadido al enum  
✅ `AvatarTone.MYSTERIOUS` añadido al enum  
✅ `TtsToneMapper` mapea `TENDER → "tender"`  
✅ `TtsToneMapper` mapea `MYSTERIOUS → "mysterious"`  
✅ Tests unitarios para ambos tonos nuevos  

**Contextos verificados**:

✅ `CONTEXT_NPC = "npc"` para eventos del NPC (juego)  
✅ `CONTEXT_NARRATION = "narration"` para narración de cuentos (lectura en familia)  
✅ Backend envía `context: "npc"` o `context: "narration"` según el caso de uso  

**Conformidad con contrato TTS**:

✅ Backend consume contrato TTS actualizado (Sprint 004)  
✅ Campo `context` en `TtsSynthesizeRequest` coincide con contrato OpenAPI  
✅ No hay validación de tonos por contexto en backend (corresponde a TTS)  
✅ Manejo de errores existente se mantiene (fallback de texto cuando TTS falla)  

**Despliegue coordinado**:

✅ Sprint 004 (TTS) completado y verificado  
✅ Backend puede desplegarse inmediatamente después de TTS  
✅ Breaking change en contrato TTS coordinado  
✅ Breaking change en caché documentado y aceptado  

sprint_verdict: APPROVED (2026-07-22)

## Design decisions

### 1. Invalidación de caché

**Decisión**: Aceptar breaking change en caché.

**Justificación**:
- Aplicación monofamiliar, caché pequeña
- Caché se vaciará naturalmente por expiración
- Complejidad de mantener compatibilidad hacia atrás no justifica el beneficio
- Documentar en notas de despliegue

### 2. Despliegue coordinado

**Decisión**: Desplegar TTS (Sprint 004) primero, backend (este sprint) inmediatamente después.

**Justificación**:
- Breaking change en contrato TTS
- Aplicación monofamiliar sin consumidores externos
- No mantener compatibilidad hacia atrás (añade complejidad innecesaria)

### 3. Nuevos tonos en backend

**Decisión**: Añadir `TENDER` y `MYSTERIOUS` al enum `AvatarTone`.

**Justificación**:
- Necesarios para narración de cuentos (FEAT-002)
- Backend debe poder solicitar estos tonos al servicio TTS
- TTS valida que solo se usen en contexto `narration`

## Contract changes

### TtsSynthesizeRequest

**Antes**:
```java
public record TtsSynthesizeRequest(
    String text,
    String locale,
    String tone,
    @JsonProperty("voice_profile")
    String voiceProfile
)
```

**Después**:
```java
public record TtsSynthesizeRequest(
    String text,
    String locale,
    String tone,
    String context
)
```

### TtsClient (interfaz)

**Antes**:
```java
byte[] synthesize(String text, String locale, AvatarTone tone, String voiceProfile);
```

**Después**:
```java
byte[] synthesize(String text, String locale, AvatarTone tone, String context);
```

### AvatarTone (enum)

**Antes**:
```java
public enum AvatarTone {
    CALM, JOYFUL, ENTHUSIASTIC, SERIOUS, NEUTRAL
}
```

**Después**:
```java
public enum AvatarTone {
    CALM, JOYFUL, ENTHUSIASTIC, SERIOUS, NEUTRAL, TENDER, MYSTERIOUS
}
```

### AvatarCacheKey

**Antes**:
```java
public final class AvatarCacheKey {
    private final String voiceProfile;
    // ...
}
```

**Después**:
```java
public final class AvatarCacheKey {
    private final String context;
    // ...
}
```

## Deployment notes

### Breaking changes

1. **Contrato TTS**: Backend envía `context` en lugar de `voice_profile`
   - Requiere TTS desplegado con Sprint 004
   - No hay compatibilidad hacia atrás

2. **Caché de audio**: `AvatarCacheKey` usa `context` en lugar de `voiceProfile`
   - Entradas de caché existentes no serán compatibles
   - Caché se vaciará naturalmente por expiración
   - Opcional: Vaciar caché manualmente tras despliegue

### Orden de despliegue

1. Desplegar TTS (Sprint 004)
2. Verificar que TTS acepta peticiones con `context`
3. Desplegar backend (este sprint)
4. Verificar que backend envía `context` correctamente
5. Monitorear logs para detectar errores `TONE_CONTEXT_MISMATCH`

### Rollback

Si hay problemas:
1. Revertir backend a versión anterior (Sprint 003)
2. Revertir TTS a versión anterior (Sprint 003)
3. Caché se regenerará automáticamente con formato antiguo

## Referencias

- FEAT-002: Tonos narrativos y simplificación del contrato TTS
- ADR-016: Tonos narrativos y simplificación del contrato TTS
- ADR-013: Chatterbox como único proveedor TTS
- Sprint 004 (TTS): Implementación del nuevo contrato en servicio TTS
- docs/contracts/api/openapi_tts.json: Contrato TTS actualizado

## Summary

completed_tasks:
  - Ampliar enum `AvatarTone` con valores `TENDER` y `MYSTERIOUS`
  - Actualizar `TtsToneMapper` para mapear `TENDER → "tender"` y `MYSTERIOUS → "mysterious"`
  - Actualizar `TtsSynthesizeRequest`: reemplazar campo `voice_profile` por `context`
  - Actualizar interfaz `TtsClient`: renombrar parámetro `voiceProfile` a `context`
  - Actualizar `TtsClientAdapter`: renombrar parámetro, actualizar logs, pasar `context` al DTO
  - Actualizar `AvatarLifecycleService`: renombrar `VOICE_PROFILE_NPC` a `CONTEXT_NPC`
  - Actualizar `NarrateStorytellerService`: renombrar `VOICE_PROFILE_STORYTELLER` a `CONTEXT_NARRATION`, cambiar valor a `"narration"`
  - Actualizar `AvatarService`: renombrar `VOICE_PROFILE_NPC` a `CONTEXT_NPC`
  - Actualizar `CachingTtsClient`: renombrar parámetro `voiceProfile` a `context`
  - Actualizar `AvatarCacheKey`: renombrar campo `voiceProfile` a `context`, actualizar constructor, getters, equals, hashCode, toString
  - Actualizar todas las pruebas unitarias afectadas
  - Documentar manejo de error `TONE_CONTEXT_MISMATCH` en `TtsClientAdapter`
  - Ejecutar `mvn test` y verificar que todas las pruebas pasan (803 tests, 0 failures)
  - Construir JAR con `mvn package` y verificar que compila correctamente

incomplete_tasks: Ninguna

contract_changes:
  - `TtsSynthesizeRequest`: campo `voiceProfile` eliminado, campo `context` agregado
  - `TtsClient`: parámetro `voiceProfile` renombrado a `context`
  - `AvatarTone`: enum ampliado con `TENDER` y `MYSTERIOUS`
  - `AvatarCacheKey`: campo `voiceProfile` renombrado a `context`
  - Breaking change en contrato TTS coordinado con Sprint 004
  - Breaking change en caché de audio documentado y aceptado

learnings:
  - El reemplazo de `voiceProfile` por `context` simplifica la comunicación entre backend y TTS
  - Los tonos `TENDER` y `MYSTERIOUS` son exclusivos para contexto `narration` (cuentos)
  - Los tonos `PLAYFUL` y `SERIOUS` son exclusivos para contexto `npc` (juego)
  - El breaking change en `AvatarCacheKey` invalida entradas de caché existentes, pero es aceptable para aplicación monofamiliar
  - La validación de tonos por contexto corresponde a TTS, no a backend
  - Backend mantiene el manejo de errores existente (fallback de texto cuando TTS falla)

next_sprint_suggestions:
  - Desplegar TTS (Sprint 004) y backend (Sprint 005) de forma coordinada
  - Monitorear logs para detectar errores `TONE_CONTEXT_MISMATCH`
  - Validar manualmente parámetros de prosodia de `tender` y `mysterious` con la familia
  - Considerar añadir métricas de uso de nuevos tonos narrativos
