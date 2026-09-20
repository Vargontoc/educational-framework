# SPRINT-082: Color Recognition - Dynamic Assets & Splash Rendering

**Estado:** implemented

## Objetivo
Implementar la carga dinámica de assets de colores y el renderizado de splash (bloque de color) con icono superpuesto, donde target y opciones usan el mismo icono pero diferente color de fondo.

## Contexto
El minijuego de colores tiene una estructura de assets específica:
- Los assets están organizados en **bloques por color** (ej: "color_red", "color_blue")
- Cada bloque contiene **múltiples iconos** (ej: apple, sun, heart, car)
- El renderizado consiste en:
  1. **Splash**: Bloque de color de fondo (ej: splash rojo)
  2. **Icono**: Item aleatorio del bloque superpuesto sobre el splash
- **Target y opciones deben usar el MISMO icono** pero diferente color de fondo
- El campo `code` de `RecognitionElement` hace referencia al **bloque de assets** (ej: "color_red"), NO a la key directa del asset


### Estructura de Datos

**RecognitionElement para colores:**
```json
{
  "code": "color_red",
  "displayValue": "Red",
  "resourceRefs": "{\"nubi-audio\": \"¿Dónde está el color rojo?\", \"color\": \"#FF0000\"}",
  "accessibleColorId": 1
}
```

- `code`: Referencia al bloque de assets (ej: "color_red")
- `resourceRefs["color"]`: Código hexadecimal del color (para validación backend)
- `resourceRefs["nubi-audio"]`: Texto para audio de Nubi
- `accessibleColorId`: ID para paleta de colores accesibles

**Nota**: Los iconos se gestionan mediante la estructura existente en `assets-manifest.json`. Cada bloque `recognition-color-[color]` contiene:
- `splash`: imagen del splash (fondo de color)
- `item_1`, `item_2`, `item_3`, `item_4`, `item_5`: items del color

## Requisitos

### Carga Dinámica de Assets

1. **Estrategia de carga:**
   - Al inicio de ronda: cargar el bloque `recognition-color-[color]` del target (ej: `recognition-color-red`)
   - Cargar también los bloques de los distractores
   - Usar caché de texturas de Phaser para evitar recargas

2. **Key de textura para COLOR:**
   - El `code` de `RecognitionElement` hace referencia al bloque (ej: "color_red")
   - El bloque en `assets-manifest.json` es `recognition-color-[color]` (ej: `recognition-color-red`)
   - Dentro del bloque hay:
     - `splash`: imagen del splash (fondo de color)
     - `item_1`, `item_2`, `item_3`, `item_4`, `item_5`: items del color
   - Para cargar: usar `this.load.pack('recognition-color-red', 'assets-manifest.json', 'recognition-color-red')`

3. **Selección de item:**
   - Al inicio de ronda, seleccionar aleatoriamente uno de los items (`item_1`, `item_2`, etc.)
   - Ese mismo item debe usarse para TODAS las opciones (target + distractores)
   - Solo cambia el bloque de color entre opciones

4. **Mapeo de código a bloque:**
   - `code: "color_red"` → bloque `recognition-color-red`
   - `code: "color_blue"` → bloque `recognition-color-blue`
   - etc.

### Renderizado de Splash + Item

1. **Splash (bloque de color):**
   - Renderizar la imagen `splash` del bloque (ej: `recognition-color-red/splash`)
   - Tamaño: mínimo 80x80px (ajustable por resolución)
   - Posición: zona central para target, zona inferior para opciones

2. **Item superpuesto:**
   - Renderizar encima del splash el item seleccionado (ej: `recognition-color-red/item_2`)
   - **Tamaño: 50-60% del tamaño del splash** (para no ocultar el splash)
   - Centrado sobre el splash
   - Mismo item para target y todas las opciones
   - **El splash debe seguir siendo visible alrededor del item**

3. **Composición visual:**
   - Target: splash rojo + item_2 (centrado, dejando ver el splash)
   - Opción 1: splash azul + item_2 (mismo item, diferente color)
   - Opción 2: splash verde + item_2 (mismo item, diferente color)
   - **Importante**: El item no debe cubrir completamente el splash, debe quedar un margen visible del splash alrededor del item

### Item de Referencia (EASY/MEDIUM)

1. **Mostrar item de referencia:**
   - Si `showIcon = true` (EASY/MEDIUM): mostrar el mismo item seleccionado como pista
   - El item de referencia es el mismo que se usa en los splashes
   - Posición: zona superior, separado del target

2. **NO mostrar item (HARD):**
   - Si `showIcon = false` (HARD): no mostrar item de referencia
   - Solo mostrar el splash del target

### Integración con Flujo Actual

1. **Modificaciones en `loadResources()`:**
   - Para COLOR: cargar bloques completos (target + distractores)
   - Seleccionar icono aleatorio del bloque del target
   - Pasar icono seleccionado a `renderElements()`

2. **Modificaciones en `renderElements()`:**
   - Renderizar splash + icono para cada elemento
   - Usar el mismo icono para target y opciones
   - Solo cambiar el bloque de color (splash)

3. **Modificaciones en `renderTargetElement()`:**
   - Renderizar splash del target + icono seleccionado
   - Si `showIcon = true`, mostrar icono de referencia adicional

## Tareas

### Carga Dinámica
- [x] Modificar `loadResources()` en `RecognitionGameScene`:
  - Para COLOR: mapear `code` a bloque `recognition-color-[color]`
  - Cargar bloques necesarios usando `this.load.pack()`
  - Seleccionar item aleatorio (`item_1`, `item_2`, etc.) del bloque del target
  - Almacenar item seleccionado en variable de instancia
- [x] Implementar método auxiliar `selectRandomItem(blockCode: string)`:
  - Contar items disponibles en el bloque (`item_1`, `item_2`, etc.)
  - Seleccionar aleatoriamente uno
  - Retornar key del item seleccionado

### Renderizado de Splash + Item
- [x] Modificar `renderElements()` en `RecognitionGameScene`:
  - Para COLOR: renderizar splash + item seleccionado
  - Usar key `splash` del bloque para el splash
  - Usar key del item seleccionado para el item superpuesto
  - Centrar item sobre el splash
- [x] Modificar `renderTargetElement()` en `RecognitionGameScene`:
  - Para COLOR: renderizar splash + item seleccionado
  - Si `showIcon = true`: renderizar item de referencia adicional
  - Si `showIcon = false`: no renderizar item de referencia

### Selección de Item
- [x] Añadir variable de instancia `selectedColorItem: string` en `RecognitionGameScene`
- [x] En `loadResources()`: seleccionar item aleatorio y almacenarlo
- [x] Pasar item seleccionado a `renderElements()` y `renderTargetElement()`

### Item de Referencia (EASY/MEDIUM)
- [x] Modificar `renderTargetElement()` para:
  - Verificar `showIcon` del `RecognitionState`
  - Si `showIcon = true`: renderizar item de referencia adicional en zona superior
  - Si `showIcon = false`: no renderizar item de referencia

### Tests
- [x] Test: carga dinámica carga bloques de color correctos
- [x] Test: item seleccionado es el mismo para target y opciones
- [x] Test: splash se renderiza correctamente
- [x] Test: item se renderiza centrado sobre el splash
- [x] Test: EASY/MEDIUM muestran item de referencia
- [x] Test: HARD no muestra item de referencia

## Criterios de Aceptación

1. Los assets se cargan dinámicamente usando la estructura existente en `assets-manifest.json`
2. El mismo item se usa para target y todas las opciones
3. Solo cambia el bloque de color (splash) entre opciones
4. El splash se renderiza correctamente con la imagen `splash` del bloque
5. El item se renderiza centrado sobre el splash
6. EASY/MEDIUM muestran item de referencia, HARD no
7. La carga dinámica funciona correctamente con caché de Phaser
8. El mapeo de `code` a bloque `recognition-color-[color]` funciona correctamente

## Notas Técnicas

- **Estructura de assets**: Usar la estructura existente en `assets-manifest.json` con bloques `recognition-color-[color]`
- **Items**: Cada bloque tiene `splash`, `item_1`, `item_2`, `item_3`, `item_4`, `item_5`
- **Selección de item**: Seleccionar aleatoriamente al inicio de ronda, mantener durante toda la ronda
- **Performance**: Cachear bloques completos para evitar recargas
- **Accesibilidad**: El item de referencia ayuda en EASY/MEDIUM, pero no es necesario en HARD
- **Resoluciones**: El tamaño del splash y item debe adaptarse a la resolución (SPRINT-080)
- **Mapeo**: `code: "color_red"` → bloque `recognition-color-red`

## Dependencias

- SPRINT-078 completado (audio de Nubi y botón de salida)
- SPRINT-106 completado (backend: validación de similitud, showIcon)
- Assets de colores organizados en bloques por color

## Estimación

- **Tamaño:** M (Medium)
- **Complejidad:** Media (renderizado compuesto, pero usando estructura existente)
- **Riesgo:** Medio (lógica de selección de item, renderizado compuesto)

## Implementación completada (2026-09-20)

### Resumen
- **Carga (`DynamicAssetLoader`)**: `code: "color_red"` → bloque `recognition-color-red`. Las texturas se registran con prefijo de bloque (`recognition-color-red/splash`, `recognition-color-red/item_2`): todos los bloques repiten las claves `splash` e `item_N`, y con `this.load.pack()` (claves sin prefijo) el splash de un bloque pisaría al de otro. Se carga por el manifest en lugar de `load.pack`, con la misma caché de Phaser, precarga en CORRECT y liberación de las últimas rondas de SPRINT-079.
- **Selección de item**: `selectedColorItem` en la escena. El item se elige entre los `item_N` presentes en **todos** los bloques de la ronda (los bloques no tienen los mismos: `purple` solo tiene 3) y, si su fichero falla en algún bloque, se reintenta con otro; si ninguno carga, la ronda se pinta solo con splash. La precarga de la ronda siguiente conserva su elección.
- **Render (`SplashCompound`)**: cada opción COLOR es un `Container` (splash + item) para que toque, animaciones de acierto/error, `resize` y patrones traten ambos como uno. Splash ajustado al tamaño de la opción; item centrado al 55 % del splash, de modo que el splash sigue visible alrededor.
- **`showIcon`** (nuevo en `RecognitionState`; ausente = `true`): en EASY/MEDIUM el target lleva el item sobre su splash; en HARD el target es solo el splash. Las opciones conservan su item en todas las dificultades.
- **Accesibilidad (SPRINT-075) preservada**: con `nonChromaticKeyRequired` (perfil con visión cromática distinta de NONE) el fondo sigue siendo el color accesible generado (color + forma resuelto por el backend) y el item se superpone; los patrones no cromáticos se mantienen. Con perfil normal se usa el splash del bloque.

### Decisiones y desvíos del sprint
1. **Se cargan `splash` + el item elegido de cada bloque, no bloques completos.** Cada item pesa 150–500 KB y el splash 7 KB: cargar bloques completos serían ~5 MB por ronda con 3 opciones.
2. **El item de referencia NO se pinta aparte.** La primera versión pintaba el item sobre el splash del target y además un «item de referencia» separado: era la misma imagen dos veces en la cabecera (error detectado en revisión). Ahora el item aparece una sola vez, sobre el target, y solo con `showIcon`. Así el criterio «HARD: solo el splash del target» se cumple literalmente y el item del target hace de pista.
3. **«Mismo item» = mismo índice, no la misma imagen.** Los items son ilustraciones específicas de cada color (`item_1` es un camión de bomberos en `red`, una gota en `blue`, un cactus en `green`): no existe «la misma imagen sobre distintos fondos». La pista funciona porque la opción cuyo item coincide con el del target es la correcta.
4. Con perfil de visión cromática el item se superpone al color accesible; puede tapar parcialmente la forma (círculo/triángulo/estrella) del fondo. Requiere revisión visual con un perfil real.
5. El sprint de backend (SPRINT-106) define `resourceRefs.icon` (`apple`, `leaf`…) como icono relacionado con el color; este sprint no lo usa: los items salen del manifest.

### Corrección de datos
`public/assets-manifest.json`: `recognition-color-white/item_4` apuntaba a `white_4.png` y el fichero se llama `white_4_.png`. Se corrige la URL del manifest (el asset no se ha renombrado).

### Archivos
- Creados: `game/ui/SplashCompound.ts`, `cypress/e2e/fase7-gameview/recognition-color-splash.cy.ts`
- Modificados: `game/utils/DynamicAssetLoader.ts`, `game/RecognitionGameScene.ts`, `game/GameEvent.ts` (`showIcon`), `views/GameView.vue` (ganchos Cypress: hijos de cada composición, `selectedColorItem`, `showIcon`), `public/assets-manifest.json`

### Pruebas
- `npx tsc --noEmit`: sin errores.
- `recognition-color-splash.cy.ts` (10 tests: bloques correctos, splash, mismo item, item común a bloques, item centrado al 55 %, EASY/MEDIUM con item una sola vez, HARD sin item, cambio de `showIcon` entre rondas, perfil con visión cromática, toque): **no ejecutado** (requiere backend + BD).
- Verificado con un arnés temporal (Phaser real + escena, WebSocket falso; ya eliminado): 11/11 con los mismos escenarios más reintento con otro item cuando fallan los ficheros de un bloque, ronda sin ningún item cargable, payload antiguo sin `showIcon`, avance de 4 rondas con limpieza y liberación al salir, y `resize` (splash e item conservan proporción y el área táctil sigue al tamaño). Tras el aviso del duplicado se reverificó: en EASY hay exactamente un item en la cabecera y en HARD ninguno. Capturas revisadas.
- Parte pura del spec de layout (SPRINT-080): 17/17 en verde.

### Riesgos
- Los items son ilustraciones grandes (hasta ~500 KB): con conexión lenta la primera ronda tarda más; se ve un instante el placeholder si aún no han cargado.
- El manifest lista para `purple` solo 3 items: cualquier ronda que lo incluya se limita a `item_1..3`.
- Depende de SPRINT-106 en producción: sin `showIcon` en el payload se asume `true`.
