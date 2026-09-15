# Sprint 101 - backend
# -----------------------------------------------

## Goal
Externalizar la configuración de la ladder de dificultad a `application.yml` mediante `@ConfigurationProperties`, incluyendo el tamaño táctil de referencia (88-96 px) y los parámetros de espera por dificultad.

## Status
status: verified
started_at: 2026-09-15
closed_at: 2026-09-15
verified_at: 2026-09-15
blocked_by: Sprint 099
waiting_for:

## Feature
FEAT-011 — RecognitionEngine: reglas de dificultad y registro parental.
ADR-028 — Reconocimiento visual sin fricción.

## Tasks

### Configuration Properties
- [x] Crear clase `RecognitionProperties` con `@ConfigurationProperties(prefix = "app.recognition")` en `game/application/`.
- [x] Añadir propiedad `touchTargetSizePx` (int, default 92, rango orientativo 88-96).
- [x] Añadir propiedad `difficulty.easy.optionCount` (int, default 2).
- [x] Añadir propiedad `difficulty.easy.touchEnableDelayMs` (int, default 500).
- [x] Añadir propiedad `difficulty.easy.guideChromEnabled` (boolean, default true).
- [x] Añadir propiedad `difficulty.medium.optionCount` (int, default 3).
- [x] Añadir propiedad `difficulty.medium.touchEnableDelayMs` (int, default 800).
- [x] Añadir propiedad `difficulty.medium.guideChromEnabled` (boolean, default false).
- [x] Añadir propiedad `difficulty.hard.optionCount` (int, default 4).
- [x] Añadir propiedad `difficulty.hard.touchEnableDelayMs` (int, default 0).
- [x] Añadir propiedad `difficulty.hard.guideChromEnabled` (boolean, default false).

### RecognitionDifficultyConfig Refactoring
- [x] Modificar `RecognitionDifficultyConfig` (Sprint 097) para que lea valores desde `RecognitionProperties` en lugar de constantes hardcodeadas. `distractorStrategy` por tier se mantiene hardcodeado (no forma parte de ninguna property pedida en este sprint) — ver Decisiones.
- [x] Inyectar `RecognitionProperties` en `RecognitionDifficultyService` — **desviación documentada**: no se inyecta directamente porque sería un campo no usado; llega transitivamente vía `RecognitionDifficultyConfig`. Ver Decisiones.
- [x] Verificar que `RecognitionDifficultyService.resolveRoundParameters()` usa los valores de `RecognitionProperties` (`RecognitionPropertiesTest.resolveRoundParameters_reflectsYamlOverriddenProperties`, `RecognitionDifficultyServiceTest.resolveRoundParameters_usesCustomRecognitionProperties`).

### Application Configuration
- [x] Añadir sección `app.recognition` en `application.yml` con valores por defecto.
- [x] Añadir sección `app.recognition` en `application-test.yml` con valores de test (archivo nuevo, no existía antes de este sprint).
- [x] Habilitar `@EnableConfigurationProperties(RecognitionProperties.class)` en `GameModuleConfiguration`.

### Tests
- [x] Test unitario: `RecognitionProperties` carga valores por defecto si no se especifican en YAML (`RecognitionPropertiesTest.defaults_appliedWhenNoYamlOverride`).
- [x] Test unitario: `RecognitionProperties` carga valores personalizados desde YAML (`RecognitionPropertiesTest.customValues_bindFromProperties`).
- [x] Test unitario: `RecognitionDifficultyService` usa valores de `RecognitionProperties` para resolver `RoundParameters` (2 tests, ver arriba).
- [x] Test unitario: `touchTargetSizePx` es accesible desde `RecognitionProperties` (cubierto en `defaults_appliedWhenNoYamlOverride`/`customValues_bindFromProperties`).
- [x] Test de integración: modificar `application.yml` y verificar que `resolveRoundParameters()` devuelve los valores configurados — implementado con `ApplicationContextRunner.withPropertyValues(...)` en vez de editar `application.yml` real, ver Decisiones.

## Manual Tests
- Iniciar backend con configuración por defecto y verificar que los valores son los esperados.
- Modificar `application.yml` con valores personalizados y reiniciar backend.
- Verificar que `resolveRoundParameters()` devuelve los valores personalizados.
- Verificar que `touchTargetSizePx` es accesible (si se expone vía endpoint o log).

## Risks
- Valores de configuración incorrectos pueden romper la ladder: mitigar con validación de rangos en `RecognitionProperties`.
- Cambios en `application.yml` pueden afectar tests existentes: mitigar usando `application-test.yml` separado.

## Dependencies
- Sprint 099 completado (integración de ladder en orquestador).
- `GameModuleConfiguration` existente.

## Agent Instruction
- No modificar la lógica de `RecognitionEngine` ni `GameOrchestratorService` en este sprint.
- No crear endpoint REST para exponer la configuración (decisión confirmada: solo WebSocket).
- `touchTargetSizePx` es informativo para frontend; no se usa en lógica de negocio backend.
- Los valores por defecto deben coincidir con la ladder de ADR-028.

## Notes
- Este sprint externaliza la configuración para permitir ajustes sin recompilar.
- `touchTargetSizePx` se incluye como referencia para frontend, pero su consumo principal es frontend.
- Los valores de `optionCount` para HARD (4) son el máximo; el motor puede usar 3 si no hay suficientes candidatos.

## Decisiones confirmadas

1. **`distractorStrategy` no se externaliza a `RecognitionProperties`.** El doc solo pide `optionCount`/`touchEnableDelayMs`/`guideChromEnabled` por tier; `distractorStrategy` (SEMANTICALLY_FAR/SAME_CATEGORY/SIMILAR_OUTLINE) no aparece en ninguna property pedida y se mantiene hardcodeado dentro de `RecognitionDifficultyConfig`, igual que antes de este sprint.
2. **`RecognitionProperties` no se inyecta directamente en `RecognitionDifficultyService`.** El checklist lo pedía como ítem, pero `RecognitionDifficultyConfig` ya se construye a partir de `RecognitionProperties`, y es lo único que `RecognitionDifficultyService` necesita. Añadir `RecognitionProperties` como constructor param no usado en el Service habría sido un campo muerto. El propio doc pide verificar `touchTargetSizePx` "accesible desde `RecognitionProperties`" (no desde el Service), y así se testeó.
3. **El test de integración no usa `@SpringBootTest` con contexto completo.** El entorno de desarrollo no tiene Docker/Testcontainers disponible (limitación preexistente, ya documentada en sprints anteriores — afecta a todos los tests con datasource real). Se usó `org.springframework.boot.test.context.runner.ApplicationContextRunner` (ya disponible vía `spring-boot-starter-test`) para cargar `RecognitionProperties` con `@EnableConfigurationProperties` y `withPropertyValues(...)` simulando overrides de YAML, envolviendo el resultado en `RecognitionDifficultyConfig`+`RecognitionDifficultyService` reales y verificando `resolveRoundParameters()` end-to-end. Prueba el mismo camino de binding sin necesitar Docker.
4. **`application-test.yml` es un archivo nuevo.** No existía antes de este sprint (solo `application.yml`/`application-dev.yml`/`application-prod.yml`), pese a que `EducationalFrameworkApplicationTests` ya usa `@ActiveProfiles("test")`. Se creó con valores de `app.recognition` distinguibles de producción (`touch-target-size-px: 96`, delays 400/700/50). Su carga real por Spring queda pendiente de que el entorno tenga Testcontainers disponible (ese test concreto ya está bloqueado por la limitación de Docker documentada en sprints anteriores, no por este sprint).

## Verificación
- `mvn -o compile` / `mvn -o test-compile`: BUILD SUCCESS.
- `mvn -o test -Dtest=RecognitionPropertiesTest,RecognitionDifficultyServiceTest`: 11 tests ejecutados (3 + 8), 0 fallos.
- `mvn -o test -Dtest="es.vargontoc.educational.framework.game.**"`: 182 tests ejecutados, 0 fallos, 0 errores.
- Confirmado (`git diff --stat`) que `RecognitionEngine.java` y `GameOrchestratorService.java` no tienen ningún cambio.

## Review

### Developer implementation — Evidencias
1. **`RecognitionProperties`** (`game/application/RecognitionProperties.java`, archivo nuevo): `@ConfigurationProperties(prefix = "app.recognition")` con `touchTargetSizePx=92` y `Difficulty` (clases anidadas `Difficulty`/`Tier`, getters/setters JavaBean estándar requeridos por el binder de Spring Boot). Defaults verificados exactamente contra la ladder de ADR-028: EASY(2,500,true) / MEDIUM(3,800,false) / HARD(4,0,false) — idénticos a los valores hardcodeados que existían en `RecognitionDifficultyConfig` antes de este sprint (Sprint 097), por lo que no hay regresión de comportamiento por defecto.
2. **`RecognitionDifficultyConfig` refactorizado**: ahora recibe `RecognitionProperties` por constructor y construye el `EnumMap<DifficultyCode, LadderTier>` leyendo cada campo desde `properties.getDifficulty().getX()`. `distractorStrategy` por tier sigue hardcodeado (SEMANTICALLY_FAR/SAME_CATEGORY/SIMILAR_OUTLINE) tal como documenta la Decisión 1 — correcto, el checklist del sprint nunca pidió externalizar esa propiedad.
3. **`GameModuleConfiguration`**: añade `@EnableConfigurationProperties(RecognitionProperties.class)` a nivel de clase y cablea `recognitionDifficultyConfig(RecognitionProperties)` → `recognitionDifficultyService(RecognitionDifficultyConfig)`. Confirmado que `GameOrchestratorService` no cambió su lista de dependencias en este sprint (coincide con el `git diff --stat` vacío reportado).
4. **`application.yml`**: sección `app.recognition` añadida con los mismos defaults, cada valor parametrizado vía variable de entorno (`APP_RECOGNITION_*`) siguiendo el mismo patrón que el resto de `app.*` en el archivo (tracking, world, session, audio). Consistente con el resto del archivo.
5. **`application-test.yml`** (archivo nuevo): valores deliberadamente distintos de producción (`touch-target-size-px: 96`, delays 400/700/50) para poder detectar si el perfil de test no se está aplicando. La Decisión 4 documenta honestamente que la carga real de este perfil por Spring no puede probarse en este entorno (sin Testcontainers/Docker) — limitación preexistente ya aceptada en sprints anteriores, no introducida por este sprint.
6. **Desviaciones documentadas de forma transparente y técnicamente correctas**: (a) Decisión 1 — `distractorStrategy` no forma parte del checklist de properties pedidas, se mantiene hardcodeado; (b) Decisión 2 — `RecognitionProperties` no se inyecta directamente en `RecognitionDifficultyService` porque sería un campo muerto (ya llega vía `RecognitionDifficultyConfig`, que es la única dependencia que el Service necesita); verifiqué que el checklist pide "verificar que `touchTargetSizePx` es accesible desde `RecognitionProperties`" (no desde el Service), y así está testeado; (c) Decisión 3 — el test de integración usa `ApplicationContextRunner` en vez de `@SpringBootTest` real, evitando la limitación de Docker mientras sigue probando el mismo camino de binding YAML→properties→config→service end-to-end.

### Tests — Verificación independiente
- Leí `RecognitionPropertiesTest.java` completo (3 tests): `defaults_appliedWhenNoYamlOverride` verifica los 9 valores por defecto uno a uno; `customValues_bindFromProperties` verifica binding de overrides vía `withPropertyValues(...)`; `resolveRoundParameters_reflectsYamlOverriddenProperties` construye `RecognitionDifficultyConfig`+`RecognitionDifficultyService` reales a partir del `RecognitionProperties` cargado por Spring y confirma que `resolveRoundParameters()` refleja los valores de YAML overrideados (incluyendo que `distractorStrategy` se mantiene fijo aunque `optionCount`/`touchEnableDelayMs`/`guideChromEnabled` cambien).
- Leí `RecognitionDifficultyServiceTest.java` (8 tests, incluyendo los 6 pre-existentes de Sprint 097 sin alterar su intención + 1 nuevo `resolveRoundParameters_usesCustomRecognitionProperties` que confirma que un `RecognitionProperties` con valores custom se propaga correctamente hasta `RoundParameters`).
- Comandos re-ejecutados de forma independiente:
  - `mvn -o test -Dtest=RecognitionPropertiesTest,RecognitionDifficultyServiceTest` → **11 tests, 0 fallos** (3+8, coincide exacto).
  - `mvn -o test -Dtest="es.vargontoc.educational.framework.game.**"` → **182 tests, 0 fallos, 0 errores** (coincide exacto; a diferencia de sprints anteriores, este módulo no toca ningún `@SpringBootTest` con datasource real, por lo que no hay errores Docker que filtrar).
- `git diff --stat` confirma que `RecognitionEngine.java` no tiene cambios; `GameOrchestratorService.java` tampoco aparece en el diff (su único uso de `RecognitionDifficultyService` ya existía desde Sprint 099 y no requería cambios).

### Criterios vs Tests

| Criterio | Evidencia |
|---|---|
| `RecognitionProperties` con prefix `app.recognition` y defaults ADR-028 | `RecognitionProperties.java` + `defaults_appliedWhenNoYamlOverride` |
| Binding YAML personalizado funciona | `customValues_bindFromProperties` |
| `RecognitionDifficultyConfig` lee de `RecognitionProperties` (no constantes) | Lectura de código + `resolveRoundParameters_usesCustomRecognitionProperties` |
| `RecognitionDifficultyService` refleja overrides end-to-end | `resolveRoundParameters_reflectsYamlOverriddenProperties` |
| `RecognitionEngine`/`GameOrchestratorService` no modificados | `git diff --stat` vacío para ambos |

### Reviewer verification

**Veredicto: APPROVED_WITH_OBSERVATIONS**

El sprint cumple todo lo solicitado: la ladder queda externalizada a `application.yml` sin alterar el comportamiento por defecto (verificado valor a valor contra ADR-028 y contra los hardcodes previos de Sprint 097), con tests que prueban tanto los defaults como el binding de overrides y su propagación completa hasta `RoundParameters`. Las tres desviaciones del checklist están documentadas de forma honesta y son técnicamente justificadas, no atajos para evitar trabajo.

**Observación no bloqueante:** la sección "Risks" del propio sprint dice textualmente que valores de configuración incorrectos "se mitigan con validación de rangos en `RecognitionProperties`", pero no existe tal validación en el código (`RecognitionProperties.java` no tiene `@Min`/`@Max` ni ningún chequeo manual). En la práctica el impacto está acotado: `touchTargetSizePx` es puramente informativo para frontend (no se usa en lógica backend, per Agent Instruction), y un `optionCount` mal configurado no provoca una excepción — `RecognitionEngine.buildOptions()` solo lo acota con `Math.min(optionCount, candidates.size())`, así que un valor ≤0 degradaría silenciosamente una ronda a mostrar solo la respuesta correcta sin distractores, en vez de fallar. Sugerencia (no bloqueante): añadir `@Min(1)` en `Tier.optionCount`/`@Min(1)` en `touchTargetSizePx` con `@Validated` en la clase, o eliminar la frase del Risks si no se va a implementar en este sprint.

### Developer follow-up (post-review)

Se implementó la sugerencia del revisor en vez de eliminar la frase de Risks, ya que el fix es pequeño y cierra el gap real:
- `RecognitionProperties` anotada con `@Validated`; `touchTargetSizePx` y `Tier.optionCount` con `@Min(1)`, `Tier.touchEnableDelayMs` con `@Min(0)` (bonus: un delay negativo tampoco tiene sentido, mismo tipo de riesgo).
- `@Valid` en cascada en `difficulty` (`RecognitionProperties`) y en `easy`/`medium`/`hard` (`Difficulty`) para que Spring Boot valide los objetos anidados, no solo el nivel superior.
- 3 tests nuevos en `RecognitionPropertiesTest` (`invalidOptionCount_failsContextStartup`, `invalidTouchTargetSizePx_failsContextStartup`, `negativeTouchEnableDelayMs_failsContextStartup`) que confirman, vía `ApplicationContextRunner`, que valores inválidos hacen fallar el arranque del contexto (`ConfigurationPropertiesBindException`) en vez de degradar silenciosamente.
- Confirmado que los valores reales de `application.yml`/`application-test.yml` siguen siendo válidos (la suite completa de `game` sigue en verde tras el cambio).
- `git diff --stat` re-confirmado vacío para `RecognitionEngine.java`/`GameOrchestratorService.java`.

**Verificación post-fix:**
- `mvn -o test -Dtest=RecognitionPropertiesTest,RecognitionDifficultyServiceTest`: 14 tests (6 + 8), 0 fallos.
- `mvn -o test -Dtest="es.vargontoc.educational.framework.game.**"`: 185 tests, 0 fallos, 0 errores.

### Reviewer follow-up verification

Re-leí `RecognitionProperties.java` completo: confirmado `@Validated` a nivel de clase, `@Min(1)` en `touchTargetSizePx`, `@Min(1)` en `Tier.optionCount`, `@Min(0)` en `Tier.touchEnableDelayMs`, y `@Valid` en cascada en `difficulty` y en cada `easy`/`medium`/`hard` — la validación anidada está correctamente propagada, no solo a nivel superior.

Antes de aceptar el resultado verifiqué que `hibernate-validator`/`jakarta.validation-api` están realmente en el classpath (`mvn -o dependency:tree`, llegan transitivamente vía `spring-boot-starter-validation` → confirmado presente), porque `@Validated`/`@Min` sin un proveedor de Bean Validation en el classpath se ignoran silenciosamente en vez de fallar — habría invalidado el fix sin que ningún test lo detectara.

Re-ejecuté los tests de forma independiente:
- `mvn -o test -Dtest=RecognitionPropertiesTest,RecognitionDifficultyServiceTest` → **14 tests, 0 fallos** (6+8, coincide exacto). El log muestra explícitamente 3 `ConfigurationPropertiesBindException: Error creating bean... prefix=app.recognition` — uno por cada test negativo (`invalidOptionCount_failsContextStartup`, `invalidTouchTargetSizePx_failsContextStartup`, `negativeTouchEnableDelayMs_failsContextStartup`), confirmando que la validación realmente se dispara y aborta el arranque del contexto, no que el assert simplemente pase por otra razón.
- `mvn -o test -Dtest="es.vargontoc.educational.framework.game.**"` → **185 tests, 0 fallos, 0 errores** (coincide exacto; +3 respecto a la ronda anterior, los 3 tests nuevos).
- `git diff --stat` reconfirmado vacío para `RecognitionEngine.java` y `GameOrchestratorService.java`: el fix no tocó lógica de negocio, solo la clase de properties.

**Veredicto final: APPROVED.** El desarrollador implementó exactamente la sugerencia (en vez de descartarla), cerrando el único gap real detectado, con evidencia verificable de que la validación funciona de extremo a extremo (incluye el caso sutil de que Bean Validation requiere un proveedor en el classpath, que aquí sí está presente). Sin observaciones pendientes.
