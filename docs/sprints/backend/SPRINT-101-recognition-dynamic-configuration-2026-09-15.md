# Sprint 101 - backend
# -----------------------------------------------

## Goal
Externalizar la configuración de la ladder de dificultad a `application.yml` mediante `@ConfigurationProperties`, incluyendo el tamaño táctil de referencia (88-96 px) y los parámetros de espera por dificultad.

## Status
status: pending
started_at:
closed_at:
blocked_by: Sprint 099
waiting_for:

## Feature
FEAT-011 — RecognitionEngine: reglas de dificultad y registro parental.
ADR-028 — Reconocimiento visual sin fricción.

## Tasks

### Configuration Properties
- [ ] Crear clase `RecognitionProperties` con `@ConfigurationProperties(prefix = "app.recognition")` en `game/application/`.
- [ ] Añadir propiedad `touchTargetSizePx` (int, default 92, rango orientativo 88-96).
- [ ] Añadir propiedad `difficulty.easy.optionCount` (int, default 2).
- [ ] Añadir propiedad `difficulty.easy.touchEnableDelayMs` (int, default 500).
- [ ] Añadir propiedad `difficulty.easy.guideChromEnabled` (boolean, default true).
- [ ] Añadir propiedad `difficulty.medium.optionCount` (int, default 3).
- [ ] Añadir propiedad `difficulty.medium.touchEnableDelayMs` (int, default 800).
- [ ] Añadir propiedad `difficulty.medium.guideChromEnabled` (boolean, default false).
- [ ] Añadir propiedad `difficulty.hard.optionCount` (int, default 4).
- [ ] Añadir propiedad `difficulty.hard.touchEnableDelayMs` (int, default 0).
- [ ] Añadir propiedad `difficulty.hard.guideChromEnabled` (boolean, default false).

### RecognitionDifficultyConfig Refactoring
- [ ] Modificar `RecognitionDifficultyConfig` (Sprint 097) para que lea valores desde `RecognitionProperties` en lugar de constantes hardcodeadas.
- [ ] Inyectar `RecognitionProperties` en `RecognitionDifficultyService`.
- [ ] Verificar que `RecognitionDifficultyService.resolveRoundParameters()` usa los valores de `RecognitionProperties`.

### Application Configuration
- [ ] Añadir sección `app.recognition` en `application.yml` con valores por defecto.
- [ ] Añadir sección `app.recognition` en `application-test.yml` con valores de test.
- [ ] Habilitar `@EnableConfigurationProperties(RecognitionProperties.class)` en `GameModuleConfiguration`.

### Tests
- [ ] Test unitario: `RecognitionProperties` carga valores por defecto si no se especifican en YAML.
- [ ] Test unitario: `RecognitionProperties` carga valores personalizados desde YAML.
- [ ] Test unitario: `RecognitionDifficultyService` usa valores de `RecognitionProperties` para resolver `RoundParameters`.
- [ ] Test unitario: `touchTargetSizePx` es accesible desde `RecognitionProperties`.
- [ ] Test de integración: modificar `application.yml` y verificar que `resolveRoundParameters()` devuelve los valores configurados.

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
