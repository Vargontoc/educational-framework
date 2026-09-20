# SPRINT-081: Carga dinámica y tintado de números

**Estado:** implemented

## Objetivo

Optimizar la carga de opciones entre rondas mediante carga dinámica de assets para la categoría NUMBER, e implementar el tintado dinámico de números con colores primarios para dar dinamismo visual al minijuego.

## Contexto

Actualmente:
- `RecognitionGameScene.loadResources()` carga todos los assets de una categoría al inicio
- Los números se muestran con su color original (blanco en la mayoría de casos)
- No hay variación visual entre rondas más allá del cambio de elementos

Problemas:
- Cargar todos los assets al inicio puede ser lento si hay muchos elementos
- Los números blancos sobre fondos claros tienen bajo contraste
- Falta dinamismo visual entre rondas

Solución:
1. **Carga dinámica:** Cargar solo los assets de la ronda actual + precarga de la siguiente
2. **Tintado dinámico:** Aplicar colores primarios a los números en cada ronda

## Requisitos

### Carga dinámica de opciones

1. **Estrategia de carga:**
   - Al inicio de ronda: cargar assets de la ronda actual (si no están en caché)
   - Simultáneamente: precargar assets de la siguiente ronda (background)
   - Usar caché de texturas de Phaser para evitar recargas

2. **Key de textura para NUMBER:**
   - Para categoría NUMBER, el campo `code` del `RecognitionElement` es la key de la textura
   - Ejemplo: `code: "number_5"` → textura key `"number_5"` en pack `recognition-numbers`
   - No usar `resourceRefs['image']` para NUMBER, usar directamente `code`
   - Para otras categorías (LETTER, SHAPE, COLOR, ANIMAL), mantener lógica actual

3. **Optimización:**
   - Identificar qué elementos necesitan carga vs. están en caché
   - Cargar solo los faltantes
   - Mostrar placeholder mientras carga (si es necesario)

4. **Gestión de memoria:**
   - Liberar texturas de rondas anteriores (mantener solo últimas 2-3 rondas)
   - Evitar memory leak en sesiones largas

### Tintado dinámico de números

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
   - Usar `setTint()` de Phaser en las imágenes de números
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
   - Mantener compatibilidad con categorías no-NUMBER

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
  - Para NUMBER: usar `element.code` como key de textura en pack `recognition-numbers`
  - Para otras categorías: usar `element.resourceRefs['image']` como key
  - Método `preloadNextRound(nextItems: RecognitionElement[], category: RECOGNITION_TYPE)` → precarga en background
  - Método `cleanupOldTextures(keepRecent: number)` → libera texturas antiguas
- [x] Integrar `DynamicAssetLoader` en `RecognitionGameScene`:
  - Reemplazar `loadResources()` con carga dinámica
  - Añadir precarga de siguiente ronda en `applyActionToResultType()` (caso CORRECT)
  - Añadir limpieza de texturas en `cleanup()`

### Tintado de números
- [x] Crear `NumberColorizer` en `game/utils/NumberColorizer.ts` (**hecho como `RecognitionColorizer` unificado**, ver "Refactorización recomendada"; `LetterColorizer` de SPRINT-079 pasa a ser `RecognitionColorizer`):
  - Paleta de colores primarios (constante)
  - Método `assignColors(optionCount: number)` → asigna colores a las opciones
  - Método `applyTint(image: Phaser.GameObjects.Image, color: number)` → aplica tint
  - Método `clearTint(image: Phaser.GameObjects.Image)` → limpia tint
- [x] Integrar `NumberColorizer` en `RecognitionGameScene`:
  - Generar asignación de colores en `renderElements()`
  - Aplicar tint a las imágenes de números
  - Limpiar tint en `cleanup()`

### Optimización de renderizado
- [x] Modificar `renderElements()` para:
  - Aceptar parámetro de colores asignados
  - Aplicar tint después de crear la imagen
  - Solo aplicar tint si la categoría es NUMBER
  - Para NUMBER: usar `element.code` como key de textura
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
- [x] Test: tint se aplica correctamente a los números
- [x] Test: colores asignados son diferentes en cada opción
- [x] Test: limpieza de texturas no afecta la ronda actual
- [x] Test: categorías no-NUMBER funcionan sin cambios

## Criterios de aceptación

1. Solo se cargan los assets de la ronda actual (no todos los de la categoría)
2. La siguiente ronda se precarga en background sin bloquear
3. Los números se muestran con colores primarios dinámicos
4. Los colores son diferentes entre opciones de la misma ronda
5. El tint se limpia correctamente al salir del minijuego
6. No hay memory leak tras múltiples rondas
7. Categorías no-NUMBER funcionan sin cambios

## Notas técnicas

- **Key de textura para NUMBER:** El campo `code` del `RecognitionElement` (ej: "number_5") es la key de la textura en el pack `recognition-numbers` del asset manifest. No usar `resourceRefs['image']` para NUMBER.
- Phaser permite cargar assets en background con `this.load.start()`
- El tint de Phaser se aplica sobre la textura original (multiplicación de colores)
- Si la textura es blanca, el tint se ve claramente; si tiene color, puede distorsionarse
- Considerar usar `setTintFill()` en lugar de `setTint()` si el resultado no es satisfactorio
- La precarga debe ser opcional (si falla, no bloquea el juego)
- **Refactorización recomendada:** Unificar `LetterColorizer` (SPRINT-079) y `NumberColorizer` en una sola clase `RecognitionColorizer` que maneje ambas categorías.

## Dependencias

- SPRINT-078 completado (audio de Nubi y botón de salir)
- SPRINT-079 completado (infraestructura de carga dinámica y tintado para letras)
- Los assets de números deben estar en blanco para que el tint funcione correctamente

## Estimación

- **Tamaño:** M (Medium)
- **Complejidad:** Media
- **Riesgo:** Medio (optimización de memoria y rendimiento)

## Implementación completada (2026-09-20)

### Resumen
La infraestructura de SPRINT-079 (`DynamicAssetLoader`: carga por ronda, precarga en CORRECT, limpieza de las últimas 3 rondas, liberación al salir) ya era genérica por categoría; este sprint la extiende a NUMBER:
- `DynamicAssetLoader.textureKey`: NUMBER usa `element.code` como key (igual que LETTER); LETTER/NUMBER cargan del pack `recognition-letters`/`recognition-numbers` del manifest. SHAPE y ANIMAL siguen con `resourceRefs['image']`; COLOR sigue generándose en runtime.
- **`RecognitionColorizer`** (`game/utils/`): unifica el antiguo `LetterColorizer` como recomienda el sprint en vez de crear un `NumberColorizer` duplicado. `RecognitionColorizer.appliesTo(category)` decide qué categorías se tintan (LETTER y NUMBER); el resto no. La escena usa esa comprobación en lugar de `type === 'LETTER'`. La clave de datos del tint pasa de `letterTint` a `recognitionTint` (interna).
- **Corrección de datos:** `public/assets-manifest.json` apuntaba `number_1..9` a `assets/images/letters/number_N.png` (no existe; los ficheros están en `assets/images/numbers/`). Se corrigen las 9 URLs; sin esto solo `number_0` cargaba y el resto de números caía a placeholder.
- El estímulo mantiene la decisión de SPRINT-079: color propio distinto de todas las opciones para que el color no delate la respuesta.

### Archivos
- Creado: `cypress/e2e/fase7-gameview/recognition-number-loading-tint.cy.ts`
- Renombrado: `game/utils/LetterColorizer.ts` → `game/utils/RecognitionColorizer.ts`
- Modificados: `game/RecognitionGameScene.ts`, `game/utils/DynamicAssetLoader.ts`, `public/assets-manifest.json`, `cypress/e2e/fase7-gameview/recognition-dynamic-loading-tint.cy.ts` (el caso "sin tint" usaba NUMBER, que ahora sí se tinta; pasa a ANIMAL)

### Pruebas
- `npx tsc --noEmit`: sin errores.
- `recognition-number-loading-tint.cy.ts` (6 tests: solo la ronda actual con key=`code`, colores distintos y estímulo sin repetir, precarga y render de la siguiente ronda, limpieza tras 5 rondas, liberación al salir, LETTER sin cambios): **no ejecutado** (requiere backend + BD).
- Verificado con un arnés temporal (Phaser real + escena, WebSocket falso; ya eliminado): 4/4 en verde con los mismos escenarios (carga solo de `number_1..3` y no `number_9`, key=`code` ignorando `resourceRefs`, 3 tints distintos y estímulo con otro color, precarga y render de la ronda siguiente, tras 5 rondas se liberan `number_0..3` y se conservan `number_4..9`, salir libera todo, ANIMAL sin tint y LETTER igual que antes). Captura revisada: números tintados y legibles sobre el bioma.

### Riesgos
- Los PNG de números no son blancos puros (media ≈ RGB 195–200, 200–211), igual que las letras: el tint multiplicativo sale algo más apagado, sobre todo amarillo/naranja sobre biomas claros. Requiere revisión visual; alternativas: `setTintFill()` o reexportar los assets en blanco.
- SPRINT-079 y SPRINT-080 mencionan `LetterColorizer` en su documentación; ahora es `RecognitionColorizer`.
