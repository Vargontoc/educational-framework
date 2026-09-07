# SPRINT-058 — Validación cross-viewport y documentación de contenido

## Estado

- **Estado:** implemented
- **Fecha de creación:** 2026-09-07
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-057 (OrientationRequiredScene y preservación de estado)
- **Impacto estimado:** Valida el comportamiento en los viewports objetivo (móvil estándar y tableta estándar), documenta la convención de zona esencial vs decoración, y deja instrucciones claras para que el equipo de contenido proporcione assets. Cierra FEAT-011.

## Objetivo

Validar el comportamiento de la experiencia jugable en los viewports horizontales objetivo (móvil estándar 667x375 y tableta estándar 1024x768), documentar la convención de zona esencial vs decoración, y proporcionar instrucciones claras para el equipo de contenido sobre cómo definir contenido esencial y proporcionar assets con proporciones coherentes.

## Contexto

FEAT-011 requiere que la experiencia jugable se vea completa y estable en horizontal, sin recortes ni cambios confusos. Los sprints anteriores (056 y 057) implementaron:
- Canvas de referencia 1280x720 (16:9).
- Detección de orientación con `useGameOrientation`.
- `OrientationRequiredScene` con preservación de estado.

Este sprint se centra en:
- Validación manual en viewports objetivo.
- Documentación de la convención de zona esencial (1080px centrales de 1280px).
- Handoff a contenido con especificaciones de assets.
- Ajustes finales de CSS para márgenes decorativos.

## Diseño funcional-técnico

### 1. Viewports a validar

| Dispositivo | Resolución landscape | Aspect ratio | Comportamiento esperado con FIT sobre 1280x720 |
|-------------|---------------------|--------------|------------------------------------------------|
| Móvil estándar | 667×375 | ~16:9 | Escala completa, sin márgenes |
| Tableta estándar | 1024×768 | ~4:3 | Márgenes superiores/inferiores decorativos (~96px cada uno) |
| Tableta 16:10 | 1280×800 | 16:10 | Márgenes mínimos superiores/inferiores |

**Nota sobre World Map (futura feature, fuera de FEAT-011):** Para la escena de World Map se usará una imagen de fondo scrollable con sistema de biomas, lo que permitirá aprovechar viewports 4:3 sin márgenes. Esto NO forma parte de FEAT-011.

### 2. Convención de zona esencial

**Canvas de referencia:** 1280x720px (16:9) con `Phaser.Scale.FIT`

**Comportamiento con FIT:**
- Todo el canvas 1280x720 es visible en todos los viewports (sin recortes)
- En viewports 16:9 (móviles): el canvas ocupa toda la pantalla, sin márgenes
- En viewports 4:3 (tabletas): el canvas se escala dejando márgenes arriba/abajo (~96px)

**Zonas (guía de diseño, no mecanismo técnico):**

```
┌──────────────────────────────────────────────────────────┐
│  Margen decorativo superior (~96px en tabletas 4:3)      │
│  ┌──────────────────────────────────────────────────┐    │
│  │  Zona segura superior (Y: 60-160)                 │    │
│  ├──────────────────────────────────────────────────┤    │
│  │                                                  │    │
│  │         ZONA ESENCIAL (1080px centrales)          │    │
│  │   Nubi, indicadores, elementos interactivos,     │    │
│  │   controles de juego, objetivos táctiles          │    │
│  │                                                  │    │
│  │   X: [100, 1180]  |  Y: [160, 560]               │    │
│  ├──────────────────────────────────────────────────┤    │
│  │  Zona segura inferior (Y: 560-660)                │    │
│  └──────────────────────────────────────────────────┘    │
│  Margen decorativo inferior (~96px en tabletas 4:3)      │
└──────────────────────────────────────────────────────────┘
```

**Zona segura para elementos críticos:**
- **X:** [160, 1120] — 960px centrales (75% del ancho)
- **Y:** [60, 660] — 600px centrales (83% del alto)

**Regla de diseño:** Todo contenido esencial debe caber en los 1080px centrales del canvas (84% del ancho). Esta es una guía de diseño para garantizar legibilidad y usabilidad en todos los viewports, no un mecanismo técnico de recorte.

**Nota:** Con FIT, todo el canvas es visible. La zona esencial es una convención de diseño para posicionar contenido crítico en un área que funciona bien en todos los aspect ratios.

### 3. Especificaciones de assets para contenido

| Asset | Dimensiones recomendadas | Zona | Formato |
|-------|--------------------------|------|---------|
| Fondo de escena | 1280x720 | Canvas completo | PNG/WebP |
| Nubi (sprite/placeholder) | Máx 200x200 | Zona esencial | PNG con transparencia |
| Icono de giro (OrientationRequiredScene) | 120x120 | Centro del canvas (640, 360) | PNG con transparencia o sprite sheet |
| Elementos interactivos | Mín 48x48 (objetivo táctil) | Zona esencial | PNG |
| Textos infantiles | Variable, mín legible a 375px ancho | Zona esencial | Renderizado por Phaser Text |

### 4. Especificaciones para OrientationRequiredScene

- **Fondo:** color plano o textura suave que no distraiga. Coherente con paleta existente.
- **Icono central:** representación visual de un dispositivo girando. Debe comprenderse sin lectura.
- **Animación:** rotación suave (360° en 2s, loop). Si `prefers-reduced-motion`, sin animación.
- **Texto de apoyo:** breve (2-4 palabras), amable, no evaluativo. Ej: "Gira tu tablet". Posición: debajo del icono.
- **No incluir:** temporizadores, barras de progreso, mensajes de error, nombres del niño, datos de sesión.

### 5. Ajustes de CSS para márgenes

**Archivo:** `GameView.vue`

El fondo de márgenes debe ser coherente con el design system. Actualmente, el `backgroundColor` de Phaser es `#028af8`. Si los márgenes deben tener un color diferente, se ajusta en el CSS del contenedor `.game-view`:

```css
.game-view {
  width: 100%;
  height: 100vh;
  background-color: #028af8; /* Coherente con Phaser backgroundColor */
}
```

## Contratos y dependencias externas

No se requieren contratos nuevos con backend. Este sprint es puramente frontend.

**Dependencias internas:**

| Dependencia | Estado | Impacto |
|-------------|--------|---------|
| SPRINT-056 | Pendiente | Proporciona canvas 1280x720. |
| SPRINT-057 | Pendiente | Proporciona OrientationRequiredScene y preservación de estado. |

**Dependencias de contenido:**

| Elemento | Responsable | Estado |
|----------|-------------|--------|
| Asset visual de Nubi girando dispositivo | Contenido/diseño | Pendiente — usar placeholder |
| Texto i18n de indicación de giro | Contenido + i18n | Pendiente — usar placeholder |
| Definición de zona esencial por escena | Contenido + frontend | Pendiente — documentar convención |
| Assets decorativos de relleno para márgenes | Contenido/diseño | Pendiente — usar color plano |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Contenido esencial posicionado fuera de zona segura en tableta 4:3 | MEDIA | Validación manual en 1024x768. Ajustar coordenadas si es necesario. Con FIT no hay recortes, pero los márgenes arriba/abajo pueden afectar percepción visual. |
| R2 | Los márgenes decorativos no son coherentes con el design system | BAJA | Ajustar CSS del contenedor `.game-view` para que el fondo sea coherente. |
| R3 | La documentación para contenido no es suficientemente clara | BAJA | Incluir ejemplos visuales y especificaciones de dimensiones. |

---

## Tareas del sprint

### Tarea 58.1: Validar comportamiento en viewport 667x375 (móvil estándar landscape)

**Método:** Manual / DevTools

**Criterios de aceptación:**
- Todo contenido esencial visible, sin recortes.
- Nubi, indicadores y elementos interactivos son completamente visibles.
- Los recursos visuales no aparecen deformados.

### Tarea 58.2: Validar comportamiento en viewport 1024x768 (tableta estándar landscape)

**Método:** Manual / DevTools

**Criterios de aceptación:**
- Márgenes superiores/inferiores decorativos (~96px cada uno), contenido esencial íntegro.
- Los márgenes superiores/inferiores son decorativos y no ocultan contenido esencial.
- Los recursos visuales no aparecen deformados.

### Tarea 58.3: Validar comportamiento en viewport 1280x800 (tableta 16:10)

**Método:** Manual / DevTools

**Criterios de aceptación:**
- Márgenes mínimos superiores/inferiores.
- Contenido esencial íntegro.
- Los recursos visuales no aparecen deformados.

### Tarea 58.4: Validar que cambios de tamaño durante sesión no reinician escena

**Método:** Manual / DevTools

**Criterios de aceptación:**
- Un cambio de tamaño durante una escena (resize de ventana) no reinicia la carga, la escena ni la sesión.
- El estado de la escena se conserva.

### Tarea 58.5: Documentar convención de zona esencial

**Archivo:** Documento en `docs/` o comentario en código

**Criterios de aceptación:**
- Existe documentación de la convención de zona esencial (1080px centrales de 1280px).
- La documentación explica que es una guía de diseño, no un mecanismo técnico (con FIT todo el canvas es visible).
- La documentación incluye zona segura para elementos críticos: X [160, 1120], Y [60, 660].
- La documentación explica cómo debe aplicarse en futuras escenas.
- Se incluyen ejemplos visuales.

### Tarea 58.6: Documentar instrucciones para contenido

**Archivo:** Documento para handoff a contenido

**Criterios de aceptación:**
- Existe handoff claro para el equipo de contenido con especificaciones de assets.
- Se especifican dimensiones recomendadas, zonas y formatos.
- Se especifican las particularidades de OrientationRequiredScene.

### Tarea 58.7: Ajustar CSS del contenedor .game-view

**Archivo:** `GameView.vue`

**Criterios de aceptación:**
- El fondo de márgenes es coherente con el design system.
- El contenedor `.game-view` ocupa 100% del viewport.

### Tarea 58.8: Verificación estática

**Criterios de aceptación:**
- `vue-tsc --noEmit` sin errores nuevos en archivos del sprint.

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|----------------|
| `framework/frontend/app/src/views/GameView.vue` | Modificación (CSS de márgenes) |
| `docs/frontend/convencion-zona-esencial.md` | Nuevo (documentación de convención) |
| `docs/handoff/contenido-feat-011-assets.md` | Nuevo (handoff a contenido) |

## Estimación

- **Duración:** 1.5 días
- **Complejidad:** Baja
- **Riesgo:** Bajo

## Criterios de aceptación del sprint

1. En 667x375 landscape, Nubi, indicadores y elementos interactivos son completamente visibles. *(FEAT-011 AC7)*
2. En 1024x768 landscape, los márgenes superiores/inferiores son decorativos y no ocultan contenido esencial. *(FEAT-011 AC8)*
3. En 1280x800 landscape, el contenido esencial es íntegro y los márgenes son mínimos.
4. Un cambio de tamaño durante una escena no reinicia la carga, la escena ni la sesión. *(FEAT-011 req6)*
5. Los recursos visuales no aparecen deformados en ningún viewport validado. *(FEAT-011 AC9)*
6. Existe documentación de la convención de zona esencial para futuras escenas.
7. Existe handoff claro para el equipo de contenido con especificaciones de assets.
8. `vue-tsc --noEmit` sin errores nuevos.

## Dependencias bloqueantes

- [ ] SPRINT-056 completado (reconfiguración de viewport y detección de orientación).
- [ ] SPRINT-057 completado (OrientationRequiredScene y preservación de estado).

## Handoffs a otras capas

| Capa | Handoff | Prioridad |
|------|---------|-----------|
| Contenido | Proporcionar asset visual de Nubi girando dispositivo (reemplazar placeholder) | Media |
| Contenido | Validar texto i18n de indicación de giro | Media |
| Contenido | Proporcionar assets decorativos de relleno para márgenes (opcional, actualmente color plano) | Baja |
| Producto | Validar experiencia infantil en viewports objetivo | Media |

## Notas adicionales

Este sprint cierra FEAT-011. Con la validación cross-viewport y la documentación de contenido, la feature queda completa.

Los placeholders visuales (icono de giro, texto, fondos) deben ser reemplazados por contenido cuando esté disponible. La documentación proporcionada en este sprint guía al equipo de contenido sobre cómo proporcionar los assets.

Las futuras escenas (WorldMapScene, minijuegos) deben seguir la convención de zona esencial documentada. El desarrollador frontend posiciona elementos esenciales dentro de la zona segura (X: [160, 1120], Y: [60, 660]).

**Nota sobre World Map (futura feature, fuera de FEAT-011):** Para la escena de World Map se usará una imagen de fondo scrollable con sistema de biomas, lo que permitirá aprovechar viewports 4:3 sin márgenes laterales. Esto NO forma parte de FEAT-011 ni de este sprint.

**Importante:** La zona esencial (1080px centrales) es una guía de diseño, no un mecanismo técnico. Con `Phaser.Scale.FIT`, todo el canvas 1280x720 es visible en todos los viewports. La zona esencial garantiza que el contenido crítico esté bien posicionado en todos los aspect ratios.

## Resumen de archivos afectados (FEAT-011 completo)

| Archivo | Tipo | Sprint |
|---------|------|--------|
| `GameView.vue` | Modificación (config Phaser, integración composable, lógica pausa/restauración, CSS) | 056, 057, 058 |
| `LoadingScene.ts` | Modificación (coordenadas) | 056 |
| `BaseStateScene.ts` | Modificación (coordenadas) | 056 |
| `FarewellScene.ts` | Modificación (coordenadas) | 056 |
| `composables/useGameOrientation.ts` | Nuevo | 056 |
| `OrientationRequiredScene.ts` | Nuevo | 057 |
| `WorldMapScene.ts` | Modificación (listeners pause/resume) | 057 |
| `RecognitionGameScene.ts` | Modificación (listeners pause/resume) | 057 |
| `i18n/locales/es.ts` | Modificación (texto de orientación requerida) | 057 |
| `docs/frontend/convencion-zona-esencial.md` | Nuevo | 058 |
| `docs/handoff/contenido-feat-011-assets.md` | Nuevo | 058 |

**Archivos NO modificados:** `ConnectionMonitor.ts`, `ErrorClassifier.ts`, `websocket.ts`, `GameEvent.ts`, `OrientationManager.vue`, sprints 041-047.
