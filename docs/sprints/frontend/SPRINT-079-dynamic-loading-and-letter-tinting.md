# SPRINT-079 — Carga dinámica y tintado de letras

## Metadata
- **Estado:** implemented
- **Capa:** frontend
- **Dependencias:** SPRINT-078
- **Feature:** FEAT-014

## Objetivo

Optimizar la carga de opciones entre rondas mediante carga dinámica de assets, e implementar el tintado dinámico de letras con colores primarios para dar dinamismo visual al minijuego.

## Contexto

Actualmente:
- `RecognitionGameScene.loadResources()` carga todos los assets de una categoría al inicio
- Las letras se muestran con su color original (blanco en la mayoría de casos)
- No hay variación visual entre rondas más allá del cambio de elementos

Problemas:
- Cargar todos los assets al inicio puede ser lento si hay muchos elementos
- Las letras blancas sobre fondos claros tienen bajo contraste
- Falta dinamismo visual entre rondas

Solución:
1. **Carga dinámica:** Cargar solo los assets de la ronda actual + precarga de la siguiente
2. **Tintado dinámico:** Aplicar colores primarios a las letras en cada ronda

## Requisitos

### Carga dinámica de opciones

1. **Estrategia de carga:**
   - Al inicio de ronda: cargar assets de la ronda actual (si no están en caché)
   - Simultáneamente: precargar assets de la siguiente ronda (background)
   - Usar caché de texturas de Phaser para evitar recargas

2. **Key de textura para LETTER:**
   - Para categoría LETTER, el campo `code` del `RecognitionElement` es la key de la textura
   - Ejemplo: `code: "letter_a"` → textura key `"letter_a"` en pack `recognition-letters`
   - No usar `resourceRefs['image']` para LETTER, usar directamente `code`
   - Para otras categorías (NUMBER, SHAPE, COLOR, ANIMAL), mantener lógica actual con `resourceRefs`

3. **Optimización:**
   - Identificar qué elementos necesitan carga vs. están en caché
   - Cargar solo los faltantes
   - Mostrar placeholder mientras carga (si es necesario)

4. **Gestión de memoria:**
   - Liberar texturas de rondas anteriores (mantener solo últimas 2-3 rondas)
   - Evitar memory leak en sesiones largas

### Tintado dinámico de letras

1. **Paleta de colores:**
   - Colores primarios sin excesivo brillo:
     - Rojo: `0xE57373` (rojo suave)
     - Azul: `0x64B5F6` (azul suave)
     - Verde: `0x81C784` (verde suave)
     - Amarillo: `0xFFF176` (amarillo suave)
     - Naranja: `0xFFB74D` (naranja suave)
     - Morado: `0xBA68C8` (morado suave)

2. **Asignación de colores:**
   - En cada ronda, asignar colores a las opciones (target + distractores)
   - El target puede tener un color distintivo o seguir la misma lógica
   - Evitar colores muy similares en la misma ronda

3. **Aplicación del tint:**
   - Usar `setTint()` de Phaser en las imágenes de letras
   - Aplicar tint después de cargar la textura
   - Mantener el tint durante toda la ronda
   - Limpiar tint al destruir el elemento

4. **Consideraciones visuales:**
   - El tint debe ser visible pero no saturar
   - Funcionar con fondos de bioma (gradientes)
   - Respetar accesibilidad (contraste suficiente)

### Integración con flujo actual

1. **Modificaciones en `loadResources()`:**
   - Cambiar de cargar todos los assets a cargar solo los necesarios
   - Añadir lógica de precarga de siguiente ronda
   - Mantener compatibilidad con categorías no-LETTER

2. **Modificaciones en `renderElements()`:**
   - Aplicar tint a las imágenes después de crearlas
   - Generar asignación de colores para la ronda
   - Pasar colores a los métodos de renderizado

3. **Modificaciones en `advanceRound()`:**
   - Precargar assets de la siguiente ronda
   - Liberar texturas de rondas antiguas (opcional)

## Tareas

### Carga dinámica
- [x] Crear `DynamicAssetLoader` en `game/utils/DynamicAssetLoader.ts`:
  - Método `loadRoundAssets(items: RecognitionElement[], category: RECOGNITION_TYPE)` → carga solo los assets de la ronda
  - Para LETTER: usar `element.code` como key de textura en pack `recognition-letters`
  - Para otras categorías: usar `element.resourceRefs['image']` como key
  - Método `preloadNextRound(nextItems: RecognitionElement[], category: RECOGNITION_TYPE)` → precarga en background
  - Método `cleanupOldTextures(keepRecent: number)` → libera texturas antiguas
- [x] Integrar `DynamicAssetLoader` en `RecognitionGameScene`:
  - Reemplazar `loadResources()` con carga dinámica
  - Añadir precarga de siguiente ronda en `applyActionToResultType()` (caso CORRECT)
  - Añadir limpieza de texturas en `cleanup()`

### Tintado de letras
- [x] Crear `LetterColorizer` en `game/utils/LetterColorizer.ts`:
  - Paleta de colores primarios (constante)
  - Método `assignColors(optionCount: number)` → asigna colores a las opciones
  - Método `applyTint(image: Phaser.GameObjects.Image, color: number)` → aplica tint
  - Método `clearTint(image: Phaser.GameObjects.Image)` → limpia tint
- [x] Integrar `LetterColorizer` en `RecognitionGameScene`:
  - Generar asignación de colores en `renderElements()`
  - Aplicar tint a las imágenes de letras
  - Limpiar tint en `cleanup()`

### Optimización de renderizado
- [x] Modificar `renderElements()` para:
  - Aceptar parámetro de colores asignados
  - Aplicar tint después de crear la imagen
  - Solo aplicar tint si la categoría es LETTER
  - Para LETTER: usar `element.code` como key de textura
  - Para otras categorías: usar `element.resourceRefs['image']` como key
- [x] Modificar `loadResources()` para:
  - Cargar solo los assets necesarios usando la key correcta según categoría
  - Retornar promesa para esperar carga
- [x] Añadir precarga de siguiente ronda:
  - En `applyActionToResultType()`, si hay siguiente ronda, precargar sus assets
  - Usar `DynamicAssetLoader.preloadNextRound()`

### Tests
- [x] Test: carga dinámica solo carga assets de la ronda actual
- [x] Test: precarga de siguiente ronda no bloquea el renderizado
- [x] Test: tint se aplica correctamente a las letras
- [x] Test: colores asignados son diferentes en cada opción
- [x] Test: limpieza de texturas no afecta la ronda actual
- [x] Test: categorías no-LETTER no aplican tint

## Criterios de aceptación

1. Solo se cargan los assets de la ronda actual (no todos los de la categoría)
2. La siguiente ronda se precarga en background sin bloquear
3. Las letras se muestran con colores primarios dinámicos
4. Los colores son diferentes entre opciones de la misma ronda
5. El tint se limpia correctamente al salir del minijuego
6. No hay memory leak tras múltiples rondas
7. Categorías no-LETTER funcionan sin cambios

## Notas técnicas

- **Key de textura para LETTER:** El campo `code` del `RecognitionElement` (ej: "letter_a") es la key de la textura en el pack `recognition-letters` del asset manifest. No usar `resourceRefs['image']` para LETTER.
- Phaser permite cargar assets en background con `this.load.start()`
- El tint de Phaser se aplica sobre la textura original (multiplicación de colores)
- Si la textura es blanca, el tint se ve claramente; si tiene color, puede distorsionarse
- Considerar usar `setTintFill()` en lugar de `setTint()` si el resultado no es satisfactorio
- La precarga debe ser opcional (si falla, no bloquea el juego)

## Dependencias

- SPRINT-078 debe estar completado (audio de Nubi y botón de salir)
- Los assets de letras deben estar en blanco para que el tint funcione correctamente

## Implementación completada (2026-09-20)

### Resumen
- `DynamicAssetLoader` (`game/utils/`) lee `assets-manifest.json` (una vez) y carga con `load.image` solo las texturas de la ronda que faltan en la caché de Phaser. Key de textura: `code` para LETTER, `resourceRefs['image']` para el resto; COLOR sigue generándose en runtime. Las cargas en vuelo se deduplican por key.
- `preloadNextRound()` se llama en el caso CORRECT de `applyActionToResultType()` en cuanto llega el estado de la nueva ronda, para que las texturas se carguen durante la animación de feedback. El render de la nueva ronda espera a la carga (`loadResources` ahora es asíncrono) y mantiene `blockActions` hasta renderizar.
- Memoria: `cleanupOldTextures(3)` tras cada render libera las texturas propias no usadas en las últimas 3 rondas; `cleanup()` libera todas (`0`), limpia el tint y descarta cargas pendientes con un token de ronda.
- `LetterColorizer` (`game/utils/`): paleta de 6 colores del sprint, `assignColors()` (barajado, sin repetir dentro de la ronda mientras haya colores), `applyTint`/`clearTint`. El tint solo se aplica en LETTER y se restaura tras la animación de acierto (que usaba `clearTint`).

### Archivos
- Creados: `game/utils/DynamicAssetLoader.ts`, `game/utils/LetterColorizer.ts`, `cypress/e2e/fase7-gameview/recognition-dynamic-loading-tint.cy.ts`
- Modificados: `game/RecognitionGameScene.ts` (loadResources, renderElements con `colors`, CORRECT, cleanup; eliminados `renderPlaceholderElements` y `packKeyMap`, ya sin uso), `views/GameView.vue` (ganchos Cypress: `tint`/`isStimulus` en `getSceneData`, `textureExists`)

### Decisiones de detalle (no especificadas en el sprint)
1. **Sin lookahead real:** el backend solo envía los elementos de la ronda actual; no hay forma de conocer la ronda N+2. "Siguiente ronda" se implementa como precarga del estado recibido en CORRECT durante el feedback. Si se quiere precarga con antelación real hace falta un cambio de contrato backend (p. ej. `nextRoundElements`).
2. **Estímulo con color propio:** la tarjeta del estímulo recibe un color de la paleta que no usa ninguna opción de la ronda. Si compartiese el color de la opción correcta, el color serviría de pista para emparejar en vez de reconocer la letra.
3. **Límite de tamaño:** los PNG de letras miden ~578 px y `renderElements` solo escalaba hacia arriba; al cargar de verdad (antes caían a placeholder porque `resourceRefs.image` ya no llega para letras) se habrían pintado gigantes. Se añade reducción a 160 px (opciones) y 120 px (tarjeta de estímulo).
4. Se elimina `load.setBaseURL('/')` de la escena: con URLs del manifest que empiezan por `/` producía `//assets/...`.

### Pruebas
- `npx tsc --noEmit` (app): sin errores. `vite build`: OK.
- `recognition-dynamic-loading-tint.cy.ts`: 6 tests (solo la ronda actual con key=`code`, colores distintos y estímulo sin repetir, NUMBER sin tint, precarga y render de la siguiente ronda, limpieza tras 5 rondas, liberación al salir). **No ejecutados**: requieren backend + BD. Sí se verificó el mismo comportamiento con un arnés temporal (Phaser real + escena, WebSocket falso; ver SPRINT-080): carga solo de la ronda actual, tint distinto por opción y estímulo, render de rondas siguientes, limpieza de texturas antiguas tras 5 rondas, NUMBER sin tint y liberación al salir. Ese arnés destapó y se corrigió un cuelgue del loader ante imágenes inexistentes con respuesta 200 (fallback SPA).

### Riesgos
- Los PNG de letras no son blancos puros (media ≈ RGB 202,202,213): el tint multiplicativo saldrá más apagado de lo esperado, sobre todo en amarillo/naranja sobre biomas claros. Si no convence, `setTintFill()` (nota técnica del sprint) o reexportar los assets en blanco. Requiere revisión visual.
- `SHAPE` no tiene pack `recognition-shapes` en el manifest: cae a placeholder por elemento (como antes).
- Los tests Cypress asumen los PNG de `public/assets/images/letters` servidos por Vite.
