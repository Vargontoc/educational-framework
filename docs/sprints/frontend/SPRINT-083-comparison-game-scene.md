# SPRINT-083: Extender RecognitionGameScene para Modo Comparación

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
- [ ] Añadir clase `ComparisonOption` en `game/GameEvent.ts`
- [ ] Añadir campos `comparisonMode` y `comparisonOptions` a `RecognitionState`

### Detección de Modo
- [ ] Modificar `readEvent()` en `RecognitionGameScene` para detectar `comparisonMode`
- [ ] Si `comparisonMode=true`, llamar a `renderComparisonElements()` en lugar de `loadResources()`

### Renderizado de Comparación
- [ ] Crear método `renderComparisonElements()` en `RecognitionGameScene`
- [ ] Implementar renderizado de opciones con escalas (usar `setScale()`)
- [ ] Asegurar tamaño táctil mínimo (44px) incluso para opciones pequeñas
- [ ] Implementar lógica de selección (igual que reconocimiento)

### Consigna Visual
- [ ] Crear método `renderComparisonConsigna()` en `RecognitionGameScene`
- [ ] Diseñar icono visual claro para "¿Cuál es el más grande?"
- [ ] Renderizar consigna en zona superior

### Assets
- [ ] Crear bloque `recognition-comparison` en `assets-manifest.json`
- [ ] Crear assets de comparación (apple, car, house, tree, flower, ball, book, cup, hat, shoe)
- [ ] Implementar carga dinámica en `loadResources()` para categoría COMPARISON

### Integración con Infraestructura
- [ ] Reutilizar `applyTouchEnableDelay()` para EASY/MEDIUM
- [ ] Reutilizar `renderGuideChrom()` para EASY
- [ ] Reutilizar feedback visual (acierto/fallo)
- [ ] Reutilizar hint visual (tras 2 fallos)
- [ ] Reutilizar celebración al completar
- [ ] Reutilizar audio de Nubi y botón de salida

### Tests
- [ ] Test: modo comparación se detecta correctamente
- [ ] Test: opciones se renderizan con escalas correctas
- [ ] Test: selección correcta avanza ronda
- [ ] Test: selección incorrecta mantiene ronda
- [ ] Test: hint se muestra tras 2 fallos
- [ ] Test: audio de Nubi se reproduce al inicio de ronda
- [ ] Test: botón de salida funciona correctamente
- [ ] Test: celebración se muestra al completar

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
