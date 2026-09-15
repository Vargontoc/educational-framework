# Sprint 077 - frontend
# -----------------------------------------------

## Goal
Verificar que la experiencia completa del minijuego de reconocimiento cumple los criterios de accesibilidad de FEAT-014 y añadir pruebas E2E para la ladder de dificultad, accesibilidad cromática y configuración dinámica. Cierra FEAT-014 en frontend.

## Contexto

Verificado por análisis técnico (`analyser-frontend`, 2026-09-15):

- **FEAT-014 §5**: Criterios de aceptación verificables (9 criterios).
- **FEAT-014 §6**: Ámbitos que deben validar los responsables (frontend, contenido, backend, agentes, privacidad).
- **SPRINT-073**: Layout de dos zonas + categorías extendidas.
- **SPRINT-074**: Ladder visual (cromo guía, espera antes del toque).
- **SPRINT-075**: Accesibilidad cromática para COLOR.
- **SPRINT-076**: Configuración dinámica de tamaño táctil.
- **Estado actual**:
  - Todos los sprints de FEAT-014 están implementados.
  - Falta verificación integral de accesibilidad.
  - Las pruebas Cypress existentes (SPRINT-070) no cubren la ladder de dificultad ni la accesibilidad cromática.
- **Gap crítico**:
  - No hay pruebas E2E que validen la ladder visual (EASY/MEDIUM/HARD).
  - No hay pruebas E2E que validen la accesibilidad cromática.
  - No hay verificación integral de los 9 criterios de aceptación de FEAT-014.

## Status
status: pending
started_at:
closed_at:
blocked_by: SPRINT-073, SPRINT-074, SPRINT-075, SPRINT-076
waiting_for:

## Decisiones confirmadas (2026-09-15)

1. **Verificación manual de los 9 criterios de FEAT-014.** Confirmado — se verificará cada criterio manualmente y se documentará el resultado.
2. **Pruebas E2E para ladder y accesibilidad.** Confirmado — se añadirán tests Cypress para los escenarios clave de ladder, accesibilidad cromática y configuración dinámica.
3. **`prefers-reduced-motion: reduce` en todas las animaciones nuevas.** Confirmado — se verificará que todas las animaciones de SPRINT-074 y SPRINT-075 respetan la preferencia de movimiento reducido.

## Diseño propuesto

### 1. Verificación de criterios de aceptación

**Criterio 1**: "Con audio y Nubi desactivados, el niño puede identificar el estímulo, elegir una opción, reintentar, recibir ayuda visual prevista por su actividad y completar la experiencia."
- Verificación: desactivar audio y NPC en configuración. Iniciar minijuego. Verificar que el estímulo es visible, las opciones son táctiles, el feedback visual funciona, y la experiencia se completa.

**Criterio 2**: "En EASY se ven 2 opciones, cromo guía y una espera corta antes del toque; en MEDIUM, 3 opciones y espera media sin cromo; en HARD, 3–4 opciones sin cromo y sin espera apreciable o con una mínima."
- Verificación: forzar cada nivel de dificultad desde backend. Verificar número de opciones, cromo guía y espera.

**Criterio 3**: "El estímulo se mantiene visible en la tarjeta propia antes, durante y después de una selección no acertada."
- Verificación: realizar selección incorrecta. Verificar que el estímulo permanece visible en zona superior.

**Criterio 4**: "Una selección no acertada no cambia la ronda, no muestra texto de error, rojo, sonido negativo, fallo acumulado ni pantalla de pérdida."
- Verificación: realizar múltiples selecciones incorrectas. Verificar que no hay texto, rojo, sonido negativo, contador ni pantalla de pérdida. Solo vaivén visual.

**Criterio 5**: "El niño puede seleccionar la opción correcta tras cualquier número de selecciones no acertadas y la ronda se resuelve inmediatamente al tocarla."
- Verificación: realizar 3-5 selecciones incorrectas, luego la correcta. Verificar que la ronda avanza.

**Criterio 6**: "Nubi no contiene el estímulo visual; cuando está dormido no emite voz ni animación y la ronda conserva todas sus señales necesarias."
- Verificación: desactivar NPC. Verificar que Nubi aparece dormido, sin voz ni animación, y el minijuego funciona.

**Criterio 7**: "Una ronda de letras o números puede presentar el modelo y las opciones con diferente tamaño o estilo sin convertir la actividad en fonética, lectura, cantidad o conteo."
- Verificación: iniciar minijuego de LETTER/NUMBER. Verificar que el estímulo puede tener diferente tamaño/estilo que las opciones.

**Criterio 8**: "Con una preferencia de visión de color configurada, una ronda de color presenta al menos una clave visual adicional al matiz para cada distractor relevante."
- Verificación: configurar `colorVisionMode=DEUTERANOPIA`. Iniciar minijuego de COLOR. Verificar patrones no cromáticos.

**Criterio 9**: "El niño no puede consultar datos de acierto, selección no acertada ni dificultad; la información queda reservada al adulto autorizado tras completar el minijuego."
- Verificación: durante el minijuego, verificar que no hay contadores, métricas ni indicadores de dificultad visibles para el niño.

### 2. Pruebas E2E (Cypress)

**Suite `recognition-ladder.cy.ts`**:
- Test: EASY muestra 2 opciones, cromo guía y espera.
- Test: MEDIUM muestra 3 opciones, sin cromo y espera media.
- Test: HARD muestra 3-4 opciones, sin cromo y sin espera.
- Test: la espera bloquea el toque durante `touchEnableDelayMs`.
- Test: el cromo guía se muestra solo en EASY.

**Suite `recognition-accessibility.cy.ts`**:
- Test: con `colorVisionMode=DEUTERANOPIA` y categoría COLOR, se muestran patrones.
- Test: sin `colorVisionMode`, no se muestran patrones.
- Test: con categoría no-COLOR, no se muestran patrones.
- Test: `prefers-reduced-motion: reduce` simplifica animaciones de espera y cromo.

**Suite `recognition-configuration.cy.ts`**:
- Test: `touchTargetSizePx=88` se aplica correctamente.
- Test: `touchTargetSizePx=96` se aplica correctamente.
- Test: sin configuración, se usa default 92.

### 3. Documentación de resultados

**Archivo de verificación**:
- Crear documento en `docs/` con los resultados de la verificación manual de los 9 criterios.
- Incluir capturas de pantalla o evidencia visual de cada criterio.

## Contratos y dependencias externas

| Contrato | Estado | Notas |
|----------|--------|-------|
| SPRINT-073 (layout dos zonas) | Requerida | Verificar estímulo arriba, opciones abajo |
| SPRINT-074 (ladder visual) | Requerida | Verificar cromo guía y espera |
| SPRINT-075 (accesibilidad cromática) | Requerida | Verificar patrones no cromáticos |
| SPRINT-076 (configuración dinámica) | Requerida | Verificar tamaño táctil configurable |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Algún criterio de FEAT-014 no se cumple | MEDIA | Documentar el fallo y crear ticket de corrección. |
| R2 | Las pruebas E2E son frágiles o lentas | BAJA | Usar selectores robustos y timeouts adecuados. |

## Tareas del sprint

### Verificación manual
- [ ] Verificar criterio 1: minijuego funciona sin audio ni Nubi.
- [ ] Verificar criterio 2: ladder EASY/MEDIUM/HARD con opciones, cromo y espera correctas.
- [ ] Verificar criterio 3: estímulo visible antes, durante y después de selección incorrecta.
- [ ] Verificar criterio 4: selección incorrecta no muestra texto, rojo, sonido negativo ni contador.
- [ ] Verificar criterio 5: selección correcta tras múltiples fallos resuelve la ronda.
- [ ] Verificar criterio 6: Nubi dormido no emite voz ni animación.
- [ ] Verificar criterio 7: letras/números con diferente tamaño/estilo.
- [ ] Verificar criterio 8: patrones no cromáticos en COLOR con preferencia visual.
- [ ] Verificar criterio 9: niño no ve métricas ni datos de acierto/fallo.

### Pruebas E2E
- [ ] Crear suite `recognition-ladder.cy.ts` con tests para EASY/MEDIUM/HARD.
- [ ] Crear suite `recognition-accessibility.cy.ts` con tests para accesibilidad cromática.
- [ ] Crear suite `recognition-configuration.cy.ts` con tests para configuración dinámica.
- [ ] Verificar que todas las pruebas pasan en CI.

### Documentación
- [ ] Crear documento de verificación de FEAT-014 con resultados de los 9 criterios.
- [ ] Incluir evidencia visual (capturas o descripciones).

## Manual Tests
- Ejecutar los 9 criterios de verificación manualmente en dispositivo real (tablet y móvil).
- Ejecutar las pruebas E2E con `npx cypress run`.
- Verificar que `prefers-reduced-motion: reduce` funciona en todas las animaciones.

## Dependencies
- SPRINT-073 (layout dos zonas) — verificado.
- SPRINT-074 (ladder visual) — verificado.
- SPRINT-075 (accesibilidad cromática) — verificado.
- SPRINT-076 (configuración dinámica) — verificado.

## Agent Instruction
- No modificar código de producción en este sprint (salvo correcciones de bugs descubiertos).
- Solo añadir pruebas E2E y documentación de verificación.
- Código, comentarios y nombres en inglés.
- Si se descubren bugs, documentarlos como defects y decidir si se corrigen en este sprint o en uno posterior.

## Notes
- Este sprint cierra FEAT-014 en frontend.
- Tras la verificación, FEAT-014 puede marcarse como completada.
- Los sprints 073-077 implementan la experiencia visual y de dificultad del minijuego de reconocimiento conforme a ADR-028.
