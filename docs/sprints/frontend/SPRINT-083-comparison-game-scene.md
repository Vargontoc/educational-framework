# SPRINT-083: Extender RecognitionGameScene para Modo Comparación

**Estado:** implemented (falta el contenido gráfico de los 10 objetos)

## Objetivo
Extender RecognitionGameScene para soportar modo comparación (grande/pequeño), reutilizando toda la infraestructura existente de feedback visual, audio de Nubi, botón de salida, etc.

## Contexto
En lugar de crear una nueva escena ComparisonGameScene, extendemos RecognitionGameScene para soportar dos modos:
- **Modo Reconocimiento** (existente): el niño identifica el elemento correcto entre opciones diferentes
- **Modo Comparación** (nuevo): el niño identifica el elemento más grande entre opciones del mismo elemento con diferentes escalas

**Escalera de dificultad para modo comparación:**
- **EASY**: 2 opciones, proporciones 100% y 40%
- **MEDIUM**: 2 opciones, proporciones 100% y 65%
- **HARD**: 3 opciones, proporciones 100%, 75% y 50%

## Requisitos

### Extensión del Modelo de Datos

**Añadir a GameEvent.ts:**
```typescript
export class ComparisonOption {
    elementId: string = ''
    scalePercent: number = 100
}

export class RecognitionState {
    // ... campos existentes
    
    // Nuevos campos para modo comparación
    comparisonMode: boolean = false
    comparisonOptions: ComparisonOption[] = []
}
```

### Detección de Modo Comparación

**En RecognitionGameScene.readEvent():**
```typescript
case 'GAME_READY':
    if (event.payload.engine === "RECOGNITION" && event.payload.recognitionState?.recognitionCategory) {
        const rs = event.payload.recognitionState
        
        // Detectar modo comparación
        if (rs.comparisonMode) {
            this.renderComparisonElements(rs.elements, rs.comparisonOptions, rs.targetElementId)
        } else {
            // Lógica existente de reconocimiento
            this.loadResources(rs.recognitionCategory ?? null, rs.elements, rs.targetElementId ?? '', () => {
                // ...
            })
        }
    }
    break
```

### Renderizado de Opciones con Escalas

**Nuevo método `renderComparisonElements()`:**
```typescript
renderComparisonElements(items: RecognitionElement[], comparisonOptions: ComparisonOption[], targetElementId: string) {
    this.images = []
    this.cleanupStimulusCard()
    
    // Renderizar consigna visual "¿Cuál es el más grande?"
    this.renderComparisonConsigna()
    
    // Renderizar opciones con escalas
    const count = comparisonOptions.length
    const spacing = count > 0 ? Math.max(this.minElementHitSize, VIEWPORT_WIDTH / (count + 1)) : 0
    
    comparisonOptions.forEach((option, i) => {
        const element = items.find(e => e.id === option.elementId)
        if (!element) return
        
        const imageKey = element.resourceRefs?.['image']
        if (!imageKey || !this.textures.exists(imageKey)) return
        
        const x = spacing * (i + 1)
        const y = VIEWPORT_HEIGHT * OPTIONS_ZONE_Y
        
        // Crear imagen con escala
        const img = this.add.image(x, y, imageKey)
        const scale = option.scalePercent / 100
        img.setScale(scale)
        
        // Asegurar tamaño táctil mínimo
        if (img.displayWidth < this.minElementHitSize || img.displayHeight < this.minElementHitSize) {
            const hitScale = this.minElementHitSize / Math.max(img.displayWidth, img.displayHeight)
            img.setScale(Math.max(scale, hitScale))
        }
        
        img.setData('elementId', element.id)
        img.setInteractive({ useHandCursor: false })
        
        img.on('pointerdown', () => {
            if (this.startingGame) return
            if (this.blockActions) return
            this.blockActions = true
            this.selectedOptionId = element.id
            this.removeVisualHint()
            if (this.websocket) {
                const ev = new GameRecognitionActionEvent()
                const diff = Date.now() - this.dateClick
                ev.setAction(element.id, diff)
                this.websocket.send(JSON.stringify(ev))
                this.dateClick = Date.now()
            }
        })
        
        this.images.push(img)
    })
}
```

### Consigna Visual

**Nuevo método `renderComparisonConsigna()`:**
```typescript
private renderComparisonConsigna(): void {
    // Icono de comparación: dos objetos con flechas de tamaño
    // Posición: zona superior central
    const x = VIEWPORT_WIDTH * 0.5
    const y = VIEWPORT_HEIGHT * 0.15
    
    // Crear icono visual (dos círculos de diferentes tamaños con flechas)
    const graphics = this.add.graphics()
    graphics.fillStyle(0xFFFFFF, 0.8)
    
    // Círculo grande
    graphics.fillCircle(x - 30, y, 20)
    // Círculo pequeño
    graphics.fillCircle(x + 30, y, 10)
    
    // Flechas
    graphics.lineStyle(3, 0xFFFFFF, 0.8)
    graphics.strokeCircle(x - 30, y, 22)
    graphics.strokeCircle(x + 30, y, 12)
    
    // Texto "¿Cuál es el más grande?" (solo para debug, el niño no lo ve)
    // En producción, solo el icono visual
}
```

### Integración con Infraestructura Existente

**Reutilizar de SPRINT-078:**
- Audio de Nubi (evento `GAME_AVATAR_EVENT` con `eventType = "ROUND_PROMPT"`)
- Botón de salida (doble-tap para abandonar)
- Nubi como botón de audio (single-tap para repetir audio)

**Reutilizar de SPRINT-079/081:**
- Carga dinámica de assets

**Reutilizar de SPRINT-080:**
- Renderizado adaptable a resoluciones

**Reutilizar de SPRINT-074:**
- `applyTouchEnableDelay()` para EASY/MEDIUM
- `renderGuideChrom()` para EASY

**Reutilizar de SPRINT-072:**
- Feedback visual (acierto/fallo)
- Hint visual (tras 2 fallos)
- Celebración al completar

### Assets de Comparación

Reutilizar assets existentes:
- `apple` de `recognition-animals` o crear en `recognition-comparison`
- `car` de `recognition-animals` o crear en `recognition-comparison`
- `house`, `tree`, `flower`, `ball`, `book`, `cup`, `hat`, `shoe`

Crear bloque `recognition-comparison` en `assets-manifest.json`:
```json
"recognition-comparison": {
    "files": [
        { "type": "image", "key": "apple", "url": "assets/images/comparison/apple.png" },
        { "type": "image", "key": "car", "url": "assets/images/comparison/car.png" },
        { "type": "image", "key": "house", "url": "assets/images/comparison/house.png" },
        { "type": "image", "key": "tree", "url": "assets/images/comparison/tree.png" },
        { "type": "image", "key": "flower", "url": "assets/images/comparison/flower.png" },
        { "type": "image", "key": "ball", "url": "assets/images/comparison/ball.png" },
        { "type": "image", "key": "book", "url": "assets/images/comparison/book.png" },
        { "type": "image", "key": "cup", "url": "assets/images/comparison/cup.png" },
        { "type": "image", "key": "hat", "url": "assets/images/comparison/hat.png" },
        { "type": "image", "key": "shoe", "url": "assets/images/comparison/shoe.png" }
    ]
}
```

## Tareas

### Extensión del Modelo
- [x] Añadir clase `ComparisonOption` en `game/GameEvent.ts`
- [x] Añadir campos `comparisonMode` y `comparisonOptions` a `RecognitionState`

### Detección de Modo
- [x] Modificar `readEvent()` en `RecognitionGameScene` para detectar `comparisonMode`
- [x] Si `comparisonMode=true`, llamar a `renderComparisonElements()` en lugar de `loadResources()`

### Renderizado de Comparación
- [x] Crear método `renderComparisonElements()` en `RecognitionGameScene`
- [x] Implementar renderizado de opciones con escalas (usar `setScale()`)
- [x] Asegurar tamaño táctil mínimo (44px) incluso para opciones pequeñas
- [x] Implementar lógica de selección (igual que reconocimiento)

### Consigna Visual
- [x] Crear método `renderComparisonConsigna()` en `RecognitionGameScene`
- [x] Diseñar icono visual claro para "¿Cuál es el más grande?"
- [x] Renderizar consigna en zona superior

### Assets
- [x] Crear bloque `recognition-comparison` en `assets-manifest.json`
- [ ] Crear assets de comparación (apple, car, house, tree, flower, ball, book, cup, hat, shoe) — **pendiente: son contenido gráfico; ver "Assets" abajo**
- [x] Implementar carga dinámica en `loadResources()` para categoría COMPARISON

### Integración con Infraestructura
- [x] Reutilizar `applyTouchEnableDelay()` para EASY/MEDIUM
- [x] Reutilizar `renderGuideChrom()` para EASY
- [x] Reutilizar feedback visual (acierto/fallo)
- [x] Reutilizar hint visual (tras 2 fallos)
- [x] Reutilizar celebración al completar
- [x] Reutilizar audio de Nubi y botón de salida

### Tests
- [x] Test: modo comparación se detecta correctamente
- [x] Test: opciones se renderizan con escalas correctas
- [x] Test: selección correcta avanza ronda
- [x] Test: selección incorrecta mantiene ronda
- [x] Test: hint se muestra tras 2 fallos
- [x] Test: audio de Nubi se reproduce al inicio de ronda
- [x] Test: botón de salida funciona correctamente
- [x] Test: celebración se muestra al completar

## Criterios de Aceptación

1. RecognitionGameScene detecta `comparisonMode` correctamente
2. Las opciones se renderizan con las escalas correctas (100%, 75%, 65%, 50%, 40%)
3. Todas las opciones usan el mismo sprite, solo cambia el tamaño
4. La consigna visual es clara y comprensible sin audio
5. El audio de Nubi se reproduce al inicio de cada ronda
6. El botón de salida funciona correctamente (doble-tap)
7. Nubi permite repetir el audio con single-tap
8. El feedback visual es correcto (acierto/fallo)
9. El hint se muestra tras 2 fallos consecutivos
10. La celebración se muestra al completar todas las rondas
11. La escena funciona sin audio
12. Los tests pasan correctamente

## Notas Técnicas

- **Reutilización máxima**: no se crea una nueva escena, solo se extiende RecognitionGameScene
- **Escalas**: se aplican como `setScale()` de Phaser sobre el sprite
- **Mismo sprite**: todas las opciones usan el mismo asset, solo cambia la escala
- **Tamaño táctil**: incluso las opciones pequeñas (40%) deben tener un hit area de al menos 44px
- **Consigna**: usar icono visual claro, no depender solo de audio
- **Accesibilidad**: la experiencia debe funcionar sin audio, sin lectura, sin color como única señal

## Dependencias

- SPRINT-078 completado (audio de Nubi y botón de salida)
- SPRINT-079/081 completado (carga dinámica)
- SPRINT-080 completado (renderizado adaptable)
- SPRINT-108 completado (backend: extensión de RecognitionEngine)

## Estimación

- **Tamaño:** M (Medium)
- **Complejidad:** Media (extensión de escena existente, no creación de nueva escena)
- **Riesgo:** Bajo (lógica similar a reconocimiento, solo cambia el renderizado de opciones)

## Implementación completada (2026-09-21)

### Resumen técnico
- **Modelo** (`GameEvent.ts`): `ComparisonOption`, `RecognitionState.comparisonMode/comparisonOptions`, `'COMPARISON'` en `RECOGNITION_TYPE` y `GameRecognitionActionEvent.setAction(id, time, scalePercent?)`, que añade `selectedScalePercent` a la acción (contrato de SPRINT-108: sin él el backend no da la respuesta por acertada).
- **Detección:** `GAME_READY` y `GAME_ACTION_RESULT` guardan `comparisonMode`/`comparisonOptions`; `loadResources` (carga dinámica, feedback, hint y celebración ya existentes) pinta con `renderComparisonElements` cuando el modo está activo en lugar de `renderElements`. No hay escena nueva.
- **Render** (`renderComparisonElements`): una imagen por opción, todas con la misma textura (`resourceRefs.image`, o el `code`) escalada para que su lado mayor sea el del 100 % por su porcentaje. Sin textura se dibuja un marcador con el nombre del objeto, igual que en reconocimiento.
- **Layout** (`ResponsiveLayout.getComparisonSlots`): el sprint usaba `VIEWPORT_WIDTH`/`OPTIONS_ZONE_Y` sueltos; se ha integrado con el layout adaptable de SPRINT-080. Las opciones se reparten en la franja libre de Nubi (a su izquierda) y bajo la consigna, así la mayor no la tapa; el lado del 100 % se ajusta al hueco (máx. 240 px CSS) y el resto sale de su porcentaje. Se recolocan al redimensionar.
- **Área táctil:** cada opción tiene un área tactil cuadrada de al menos `hitSize` (≥ 44 px CSS) aunque se vea más pequeña (la de 40 % se toca con la misma comodidad), sin invadir a las vecinas. Se conserva al reactivar el toque tras `touchEnableDelayMs` (`makeTouchable`).
- **Consigna visual:** icono en la tarjeta de la cabecera (círculo grande verde con flecha hacia él, junto a uno pequeño gris); sin texto ni audio. No se dibuja el objeto en la cabecera (todas las opciones son el mismo objeto; mostrarlo a un tamaño induciría a error).
- **Selección:** como todas las opciones comparten `elementId`, la opción tocada se identifica por su índice (`selectedOptionIndex`) para el feedback correcto/incorrecto, y se envía su `scalePercent`. El hint (tras 2 fallos) señala la opción de mayor tamaño.
- **Assets:** `assets-manifest.json` tiene el bloque `recognition-comparison` (10 objetos, `assets/images/comparison/<code>.png`) y `DynamicAssetLoader` lo carga por ronda (solo lo que falta) como el resto de categorías.
- **Hook de test** (`GameView.vue`, solo Cypress): `scalePercent`, `optionIndex`, `hitDisplayWidth` por imagen, `comparisonMode`, `spyWsSend()` y `tapOption(i)`.

### Decisiones y desvíos
1. **Assets no creados.** El sprint dice reutilizar `apple`, `car`… de `recognition-animals`/`recognition-colors`, pero **no existen** (solo hay animales y colores) y no es contenido que se pueda inventar. El manifest ya los referencia; hasta que se añadan los PNG en `public/assets/images/comparison/`, cada opción se ve como un marcador con el nombre. Con cualquier textura existente el juego funciona (verificado con la vaca de animales).
2. **La cabecera no muestra el objeto**, solo el icono de consigna (ver arriba).
3. **Detección por `comparisonMode`, no por la categoría**, como pide el sprint; `renderComparisonElements` no recibe `targetElementId` porque el objetivo es el tamaño mayor, no un elemento.
4. **Consigna con color:** el tamaño y la flecha son el canal principal; el verde/gris solo refuerza (no es la única señal).

### Archivos
- **Nuevos:** `cypress/e2e/fase7-gameview/recognition-comparison.cy.ts`.
- **Modificados:** `GameEvent.ts`, `RecognitionGameScene.ts`, `utils/ResponsiveLayout.ts`, `utils/DynamicAssetLoader.ts`, `views/GameView.vue`, `public/assets-manifest.json`.

### Pruebas
- `npx tsc --noEmit`: sin errores.
- **Parte pura del spec nuevo (13 tests, sin backend):** para las 4 resoluciones objetivo de SPRINT-080 × EASY/MEDIUM/HARD: proporciones exactas (100/40, 100/65, 100/75/50), la mayor ≤ 240 px CSS, área táctil ≥ 44 px CSS incluso en la menor, y nada se solapa con Nubi, el botón de salir, la consigna ni entre sí (ni dibujado ni tactil). 13/13.
- **En juego, con Phaser real y un arnés temporal (ya borrado) + Cypress, 6/6:** las tres escaleras se detectan y se pintan a su escala con la misma textura; ninguna tarjeta de objeto en cabecera; tocar envía `selectedScalePercent`; incorrecta mantiene la ronda y activa el hint sobre la de 100 %; correcta pasa a la siguiente ronda con otra escalera; el resize recoloca conservando la proporción y el área tactil ≥ 44 px CSS; sin textura hay marcador tocable. Comprobado visualmente con capturas.
- **No ejecutado:** el bloque `describe` "en juego" del spec permanente (necesita backend/BD, como el resto de specs de `fase7-gameview`); su equivalente sí se verificó con el arnés.

### Riesgos, deuda y handoffs
- **Contenido:** faltan los 10 PNG de `assets/images/comparison/` (y que mantengan identidad visual clara al escalarse; ver riesgos de FEAT-012).
- **Consigna:** el icono es una propuesta funcional; conviene validarlo con niñas y niños de 3-4 años.
- Sin audio la ronda es resoluble (la consigna es visual); el audio de Nubi (`ROUND_PROMPT`) y el botón de salir son los genéricos de SPRINT-078.
- Las opciones de la ronda pequeña (40 %) pueden verse muy pequeñas en móvil pequeño; se toca bien, pero validarlo en dispositivo real.
