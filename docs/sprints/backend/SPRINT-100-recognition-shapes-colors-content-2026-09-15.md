# Sprint 100 - backend
# -----------------------------------------------

## Goal
Completar el contenido de FORMAS y COLOR adaptado a la ladder de ADR-028, incluyendo seeds de topic, elementos de reconocimiento con `similarityGroup`, y verificación de paletas accesibles para COLOR.

## Status
status: pending
started_at:
closed_at:
blocked_by: Sprint 098, Sprint 099
waiting_for:

## Feature
FEAT-011 — RecognitionEngine: reglas de dificultad y registro parental.
ADR-028 — Reconocimiento visual sin fricción.

## Tasks

### COLOR Topic and Elements
- [ ] Crear seed de topic COLOR en `02-topics.json` con `recognitionType: "COLOR"`, `status: "ACTIVE"`, `minAge: 3`, `maxAge: 6`.
- [ ] Crear seed de `RecognitionElement` para COLOR en `16-recognition-elements.json` con al menos 4 elementos: rojo, azul, verde, amarillo.
- [ ] Asignar `similarityGroup` a elementos de COLOR para permitir `SIMILAR_OUTLINE` en HARD (agrupar por tonalidad similar: cálidos juntos, fríos juntos).
- [ ] Verificar que `AccessibleColorPalette` seed (`15-accessible-colors.json`) cubre los 4 colores básicos para todos los `colorVisionMode`.

### SHAPE Elements Enhancement
- [ ] Añadir `similarityGroup` a elementos de SHAPE en `16-recognition-elements.json` (agrupar por tipo de forma: círculos juntos, cuadrados juntos, triángulos juntos).
- [ ] Verificar que existan suficientes elementos de SHAPE para cubrir las 3 dificultades (mínimo 4 elementos activos).

### Seed Validation
- [ ] Verificar que `SeedService` carga correctamente los nuevos topics y elementos sin errores.
- [ ] Verificar que `RecognitionElementRepository.findByTopicIdAndStatus()` devuelve los elementos esperados para COLOR y SHAPE.
- [ ] Verificar que `TopicUseCase.listTopicsByRecognitionType()` incluye COLOR.

### Integration Tests
- [ ] Test de integración: `resolveCandidates()` para COLOR devuelve elementos de COLOR.
- [ ] Test de integración: `resolveCandidates()` para COLOR con `colorVisionMode=DEUTERANOPIA` funciona sin error.
- [ ] Test de integración: `resolveCandidates()` para SHAPE devuelve elementos de SHAPE con `similarityGroup`.
- [ ] Test de integración: `DistractorSelector.select()` con estrategia `SIMILAR_OUTLINE` para COLOR devuelve distractores del mismo grupo de tonalidad.
- [ ] Test de integración: `DistractorSelector.select()` con estrategia `SIMILAR_OUTLINE` para SHAPE devuelve distractores del mismo grupo de forma.

## Manual Tests
- Iniciar backend con seeds cargados.
- Verificar en logs que topic COLOR y elementos se cargan correctamente.
- Verificar que `resolveCandidates()` para COLOR devuelve al menos 4 elementos.
- Verificar que `resolveCandidates()` para SHAPE devuelve al menos 4 elementos.
- Verificar que las paletas accesibles de COLOR están disponibles para todos los `colorVisionMode`.

## Risks
- Contenido de COLOR insuficiente puede causar fallback frecuente a aleatorio: mitigar asegurando al menos 4 elementos.
- `similarityGroup` mal asignado puede causar distractores incorrectos: mitigar con validación manual.
- Seeds de COLOR pueden romper tests existentes si no se actualizan: mitigar verificando que tests de `resolveCandidates()` siguen pasando.

## Dependencies
- Sprint 098 completado (`similarityGroup` en `RecognitionElement`).
- Sprint 099 completado (integración de ladder en orquestador).
- `AccessibleColorPalette` ya implementado (Sprint anterior de contenido).

## Agent Instruction
- No modificar la lógica de `RecognitionEngine` ni `GameOrchestratorService` en este sprint.
- Solo añadir contenido (seeds) y tests de integración de contenido.
- Los elementos de COLOR deben tener `code`, `displayValue`, `resourceRefs` y `similarityGroup`.
- Los `resourceRefs` de COLOR deben incluir referencias a imágenes (no audio TTS para colores).

## Notes
- Este sprint completa el contenido necesario para que la ladder funcione con las 5 categorías.
- Las paletas accesibles de COLOR ya existen; este sprint solo verifica su completitud.
- Los `similarityGroup` de COLOR son orientativos; pueden ajustarse tras validación de contenido.
