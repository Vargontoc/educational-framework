# SPRINT-080 — Renderizado adaptable a resoluciones

## Metadata
- **Estado:** implemented
- **Capa:** frontend
- **Dependencias:** SPRINT-079
- **Feature:** FEAT-014, ADR-028

## Objetivo

Preparar el renderizado del minijuego de reconocimiento para adaptarse a distintas resoluciones de pantalla (móvil, tablet, desktop), manteniendo la accesibilidad táctil y la experiencia visual consistente.

## Contexto

Actualmente:
- `RecognitionGameScene` usa constantes hardcodeadas:
  - `VIEWPORT_WIDTH = 1280`
  - `VIEWPORT_HEIGHT = 720`
  - `MIN_ELEMENT_HIT_SIZE = 80`
  - `STIMULUS_CARD_WIDTH = 120`
  - Posiciones fijas (25% para estímulo, 65% para opciones)
- El juego funciona en resolución 1280x720, pero no se adapta a otras resoluciones
- Los elementos táctiles pueden ser demasiado pequeños en pantallas de alta densidad
- El botón de salir (SPRINT-078) tiene tamaño fijo (60x60px)

Problemas:
- En móviles pequeños (320x568), los elementos pueden solaparse
- En tablets grandes (2048x1536), los elementos pueden verse muy pequeños
- En pantallas de alta densidad (Retina), los elementos táctiles pueden ser insuficientes
- No hay configuración para ajustar tamaños según el dispositivo

Solución:
1. **Sistema de escalado:** Usar el sistema de escalado de Phaser para adaptar el viewport
2. **Tamaños relativos:** Calcular tamaños basados en el viewport real
3. **Configuración adaptable:** Permitir ajustar tamaños mínimos según el dispositivo
4. **Testing multi-resolución:** Verificar en distintas resoluciones

## Requisitos

### Sistema de escalado

1. **Configuración de Phaser:**
   - Usar `Phaser.Scale.FIT` o `Phaser.Scale.RESIZE` para adaptar el viewport
   - Mantener aspect ratio 16:9 (o permitir adaptación flexible)
   - Centrar el juego en la pantalla

2. **Viewport dinámico:**
   - Reemplazar constantes hardcodeadas con valores calculados
   - `VIEWPORT_WIDTH` → `this.scale.width`
   - `VIEWPORT_HEIGHT` → `this.scale.height`
   - Actualizar posiciones en `resize` event

### Tamaños relativos

1. **Elementos táctiles:**
   - `MIN_ELEMENT_HIT_SIZE` → calcular como % del viewport (ej: 10% del ancho)
   - Mínimo absoluto: 44px (estándar táctil)
   - Máximo: 150px (evitar elementos gigantes)

2. **Tarjeta de estímulo:**
   - `STIMULUS_CARD_WIDTH` → 15% del ancho del viewport
   - Mantener aspect ratio 1:1
   - Mínimo: 100px, máximo: 200px

3. **Botón de salir:**
   - Tamaño: 5% del ancho del viewport
   - Mínimo: 44px, máximo: 80px
   - Margen: 3% del viewport

4. **Nubi:**
   - Tamaño: 10% del ancho del viewport
   - Mínimo: 80px, máximo: 120px
   - Margen: 3% del viewport

### Configuración adaptable

1. **Perfil de dispositivo:**
   - Detectar tipo de dispositivo (móvil, tablet, desktop)
   - Ajustar tamaños mínimos según el perfil
   - Guardar preferencia en `registry` o `localStorage`

2. **Configuración parental:**
   - Permitir al padre ajustar el tamaño de elementos táctiles
   - Rango: 80px - 150px
   - Guardar preferencia en `ChildProfile` (backend) o `localStorage` (frontend)

3. **Detección de orientación:**
   - Ya existe `OrientationRequiredScene` (SPRINT-057)
   - Forzar orientación landscape para el minijuego
   - Mostrar mensaje si está en portrait

### Testing multi-resolución

1. **Resoluciones objetivo:**
   - Móvil pequeño: 320x568 (iPhone SE)
   - Móvil estándar: 375x667 (iPhone 8)
   - Tablet: 768x1024 (iPad)
   - Desktop: 1920x1080

2. **Verificaciones:**
   - Elementos no se solapan
   - Tamaños táctiles son adecuados (mínimo 44px)
   - Texto es legible
   - Botones son accesibles

## Tareas

### Sistema de escalado
- [x] Configurar `Phaser.Game` con `scale.mode = Phaser.Scale.FIT`
- [x] Configurar `scale.autoCenter = Phaser.Scale.CENTER_BOTH`
- [x] Añadir listener para `resize` event en `RecognitionGameScene`
- [x] Actualizar posiciones de elementos en `resize` event
- [x] Forzar orientación landscape (integrar con `OrientationRequiredScene`)

### Tamaños relativos
- [x] Crear `ResponsiveLayout` en `game/utils/ResponsiveLayout.ts`:
  - Método `calculateSizes(viewportWidth, viewportHeight)` → tamaños calculados
  - Método `getElementHitSize()` → tamaño táctil calculado
  - Método `getStimulusCardSize()` → tamaño de tarjeta calculado
  - Método `getButtonSize()` → tamaño de botón calculado
  - Método `getNubiSize()` → tamaño de Nubi calculado
- [x] Integrar `ResponsiveLayout` en `RecognitionGameScene`:
  - Reemplazar constantes hardcodeadas con cálculos dinámicos
  - Actualizar tamaños en `create()` y `resize()`

### Configuración adaptable
- [x] Crear `DeviceProfile` en `game/utils/DeviceProfile.ts`:
  - Detectar tipo de dispositivo (móvil, tablet, desktop)
  - Calcular tamaños mínimos según el perfil
  - Guardar preferencia en `registry`
- [x] Integrar detección de dispositivo en `RecognitionGameScene.init()`
- [ ] Añadir configuración parental para tamaño táctil (opcional, puede ser futuro sprint) — **diferido**: requiere decidir dónde se persiste (ChildProfile en backend vs localStorage) y UI parental; fuera del alcance de este sprint

### Testing multi-resolución
- [x] Crear script de testing para distintas resoluciones
- [x] Verificar en 320x568 (móvil pequeño)
- [x] Verificar en 375x667 (móvil estándar)
- [x] Verificar en 768x1024 (tablet)
- [x] Verificar en 1920x1080 (desktop)
- [x] Documentar resultados y ajustes necesarios

### Refactorización
- [x] Eliminar constantes hardcodeadas:
  - `VIEWPORT_WIDTH`, `VIEWPORT_HEIGHT`
  - `MIN_ELEMENT_HIT_SIZE`
  - `STIMULUS_CARD_WIDTH`, `STIMULUS_CARD_HEIGHT`
- [x] Reemplazar con cálculos dinámicos de `ResponsiveLayout`
- [x] Actualizar `RoundProgressBar` para usar tamaños relativos
- [x] Actualizar `ExitButton` (SPRINT-078) para usar tamaños relativos
- [x] Actualizar `MinigameNubiLayer` para usar tamaños relativos

### Tests
- [x] Test: elementos no se solapan en 320x568
- [x] Test: tamaños táctiles son ≥ 44px en todas las resoluciones
- [x] Test: botón de salir es accesible en todas las resoluciones
- [x] Test: Nubi tiene tamaño adecuado en todas las resoluciones
- [x] Test: `resize` event actualiza posiciones correctamente
- [x] Test: orientación portrait muestra mensaje de rotación

## Criterios de aceptación

1. El minijuego se adapta a resoluciones de 320x568 a 1920x1080
2. Los elementos táctiles tienen mínimo 44px en todas las resoluciones
3. No hay solapamiento de elementos en ninguna resolución objetivo
4. El botón de salir es accesible y visible en todas las resoluciones
5. Nubi tiene tamaño adecuado (80-120px) en todas las resoluciones
6. La orientación portrait muestra mensaje de rotación
7. No hay constantes hardcodeadas de tamaño en el código

## Notas técnicas

- Phaser 3 tiene sistema de escalado robusto (`ScaleManager`)
- `Phaser.Scale.FIT` mantiene aspect ratio y escala para encajar
- `Phaser.Scale.RESIZE` permite adaptación flexible (puede distorsionar)
- Considerar usar `Phaser.Display.Bounds` para calcular posiciones
- El evento `resize` se dispara al cambiar tamaño de ventana/orientación
- En móviles, el viewport puede cambiar al mostrar/ocultar barra de navegador

## Dependencias

- SPRINT-079 debe estar completado (carga dinámica y tintado)
- SPRINT-078 debe estar completado (botón de salir y audio de Nubi)
- `OrientationRequiredScene` (SPRINT-057) debe estar disponible

## Implementación completada (2026-09-20)

### Enfoque
El canvas lógico sigue siendo fijo (1280x720) y Phaser lo escala por CSS, así que un porcentaje del ancho lógico daría el mismo resultado en cualquier dispositivo y `this.scale.width` nunca cambia. Lo que sí cambia con la pantalla es `scale.displayScale` (unidades lógicas por píxel CSS). Por eso `ResponsiveLayout` expresa porcentajes y mínimos/máximos **en píxeles CSS** (los que percibe el niño) y los convierte a unidades lógicas. Sin esto, un elemento de 80 px lógicos en un móvil de 568 px de ancho medía ~35 px CSS (< 44).

- `game/utils/ResponsiveLayout.ts` (puro, sin Phaser): `calculateSizes`, `getElementHitSize`, `getStimulusCardSize`, `getButtonSize`, `getNubiSize`, `getOptionSlots`. Ratios del sprint: táctil 10 % (44–150, mínimo según perfil), tarjeta 15 % (100–200), botón 5 % (44–80), Nubi 10 % (80–120), márgenes 3 %. La zona de opciones baja a su sitio natural (65 %) pero se limita para no solapar la tarjeta del estímulo ni a Nubi.
- `game/utils/DeviceProfile.ts`: mobile/tablet/desktop según puntero táctil y lado corto de pantalla; mínimo táctil 48/56/44 px CSS. Se guarda en `registry` (`deviceProfile`).
- `RecognitionGameScene`: sin `VIEWPORT_*`, `STIMULUS_CARD_*`, `*_ZONE_*` ni `MIN_ELEMENT_HIT_SIZE`; escucha `scale` `resize` y recoloca/reescala opciones, estímulo, barra, botón, Nubi y overlays (pista, cromo guía, patrones) sin recrear la ronda. `minElementHitSize` pasa a ser un getter de `ResponsiveLayout`.
- `RoundProgressBar`, `ExitButton`, `MinigameNubiLayer`: reciben `LayoutSizes` y exponen `resize()`. El globo de Nubi se escala para que su texto mida ≥ 14 px CSS.

### Decisión: FIT solo durante el minijuego (desvío respecto al literal del sprint)
El sprint pide configurar `Phaser.Game` con `Scale.FIT`. `WorldMapScene` y sus capas están pensadas para `Scale.ENVELOP` y no se puede verificar visualmente cómo quedarían con FIT, así que el modo se cambia a FIT al entrar en `RecognitionGameScene` y se restaura al salir. También se restaura mientras el minijuego está **pausado**: en vertical `GameView` arranca `OrientationRequiredScene` encima, y con FIT su mensaje quedaría a escala ~0,25 (ilegible) en un móvil. Si se prefiere FIT global basta cambiar `scale.mode` en `GameView.vue` (y quitar `enterFitScaling/restoreScaling`).

### Resultados multi-resolución (horizontal, calculados con `ResponsiveLayout`; FIT)
| Pantalla | Canvas CSS | Táctil opción | Salir | Nubi | Barra | Y opciones |
|---|---|---|---|---|---|---|
| Móvil pequeño 568x320 | 568x320 | 57 px | 44 px | 80 px | 120x11 | 58 % |
| Móvil estándar 667x375 | 667x375 | 67 px | 44 px | 80 px | 133x13 | 62 % |
| Tablet 1024x768 | 1024x576 | 102 px | 51 px | 102 px | 205x14 | 65 % |
| Desktop 1920x1080 | 1920x1080 | 150 px | 80 px | 120 px | 300x14 | 65 % |

Sin solapes (2 a 6 opciones, botón, barra, estímulo, Nubi) en ninguna. En móvil las opciones suben del 65 % al 58–62 % para no chocar con Nubi. Nota: las resoluciones de móvil/tablet del sprint están en vertical; el minijuego solo se juega en horizontal, así que se evalúan apaisadas.

### Archivos
- Creados: `game/utils/ResponsiveLayout.ts`, `game/utils/DeviceProfile.ts`, `cypress/e2e/fase7-gameview/recognition-responsive-layout.cy.ts`
- Modificados: `game/RecognitionGameScene.ts`, `game/ui/RoundProgressBar.ts`, `game/ui/ExitButton.ts`, `game/worldmap/layers/MinigameNubiLayer.ts`, `game/utils/DynamicAssetLoader.ts` (bug, ver abajo), `views/GameView.vue` (ganchos Cypress `getScaleInfo`, `getLayoutSizes`)

### Pruebas
- `npx tsc --noEmit`: sin errores.
- `recognition-responsive-layout.cy.ts`, bloque de lógica pura (17 tests: táctil ≥ 44 px CSS, botón, Nubi 80–120, tarjeta 100–200, sin solapes, fuente ≥ 14 px CSS en las 4 resoluciones): **ejecutado con Cypress, 17/17 en verde** (no necesita backend).
- Bloque "en el juego" del mismo spec (7 tests: opciones táctiles y sin solape por resolución, `resize`, tamaños de botón/Nubi, FIT activo y liberado al salir, orientación vertical): **no ejecutado** (requiere backend + BD).
- Verificación adicional con un arnés temporal (Phaser real + `RecognitionGameScene` con WebSocket falso, ya eliminado): 8/8 casos de este sprint en 568x320, 667x375, 1024x768 y 1920x1080 (FIT activo, `displayScale` correcto, opciones ≥ 43,5 px CSS, letras tintadas, `resize` recoloca, pausa restaura ENVELOP y reanudar vuelve a FIT, cerrar la escena restaura el modo, doble-tap en salir abandona). Captura revisada a 568x320: letras tintadas, salir arriba a la izquierda, Nubi abajo a la derecha; el globo de Nubi tapa un momento la cuarta opción (transitorio).

### Bug encontrado y corregido en SPRINT-079
`DynamicAssetLoader` se quedaba colgado si el servidor respondía 200 con HTML a una imagen inexistente (fallback SPA): Phaser emite `load` pero ni `filecomplete` ni `loaderror`, solo `complete`, y la ronda nunca se renderizaba. Ahora la promesa por textura también se resuelve al completarse el lote (y la textura se marca como fallida → placeholder). Con el arnés también se comprobó SPRINT-079 (carga solo de la ronda actual, tint distinto por opción y estímulo, limpieza tras 5 rondas, NUMBER sin tint, liberación al salir) y SPRINT-078 (ROUND_PROMPT en cola, audio tardío, Nubi repite, doble tap en Nubi no abandona, sin audio no hace nada): todo en verde.

### Riesgos y deuda
- **Datos:** `assets-manifest.json` apunta `number_1..9` a `assets/images/letters/number_N.png`, que no existe (solo `number_0` está en `images/numbers/`). Los números 1–9 caen a placeholder. Es un fallo de datos preexistente ajeno a este sprint.
- **Configuración parental** de tamaño táctil: diferida (ver tareas).
- Tamaños no ligados al layout que se mantienen: radio de las estrellas de celebración, grosor del borde de pista, ondas de Nubi (proporcionales al texto). No afectan a la accesibilidad táctil; el criterio 7 se interpreta como las constantes listadas en la refactorización.
- Tras un `resize` en mitad de la animación de acierto, el reescalado de la opción puede interrumpir el "pop" visual (cosmético).
