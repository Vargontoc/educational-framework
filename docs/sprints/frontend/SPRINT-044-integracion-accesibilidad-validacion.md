# SPRINT-044 — Integración, accesibilidad y validación infantil

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-09-05
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-041, SPRINT-042, SPRINT-043
- **Impacto estimado:** Valida accesibilidad, responsividad y comprensión infantil del flujo completo de FEAT-010. Cierra la feature con validación de producto y contenido.

## Objetivo

Validar que el flujo completo de FEAT-010 (HomeView → GameView → BaseStateScene, y HomeView → aviso de bloqueo) es accesible, responsivo y comprensible para niños de 3-4 años. Obtener validación formal de producto y contenido.

## Contexto

Los sprints anteriores implementaron:
- SPRINT-041: Validación de perfil y aviso de bloqueo.
- SPRINT-042: Rediseño de LoadingScene y placeholder visual.
- SPRINT-043: BaseStateScene y exclusión de WorldMap/RecognitionGame.

Este sprint se centra en la validación final:
- Accesibilidad (sin dependencia exclusiva de color, texto o sonido).
- Responsividad (móvil y tableta).
- Comprensión infantil (validación con contenido y producto).
- Pruebas E2E completas.

## Diseño funcional-técnico

### 1. Validación de accesibilidad

**Criterios a verificar:**

| Elemento | Criterio |
|----------|----------|
| Placeholder de carga | Comprensible sin lectura. Iconografía clara. |
| Aviso de bloqueo | Icono + texto, no solo color. Comprensible sin lectura. |
| BaseStateScene | Visualmente comprensible. Sin dependencia de color, texto o sonido. |
| Selector de perfiles | Objetivos táctiles amplios. Identificación por avatar + nombre. |

**Técnicas de validación:**
- Verificar contraste WCAG 2.1 AA (mínimo 4.5:1 para texto).
- Verificar que los iconos tienen etiquetas `aria-label`.
- Verificar que los estados no dependen solo de color (ej. rojo/verde).
- Verificar que los objetivos táctiles son al menos 44x44px.

### 2. Validación de responsividad

**Dispositivos a probar:**

| Dispositivo | Resolución | Orientación |
|-------------|------------|-------------|
| Móvil pequeño | 320x568 | Portrait |
| Móvil estándar | 375x667 | Portrait |
| Tableta pequeña | 768x1024 | Portrait/Landscape |
| Tableta estándar | 1024x768 | Landscape |

**Criterios a verificar:**
- Phaser escala correctamente (modo FIT + CENTER_BOTH).
- Los elementos no se cortan ni se superponen.
- Los objetivos táctiles son accesibles en todas las resoluciones.
- El texto es legible en todas las resoluciones.

### 3. Validación con contenido y producto

**Elementos a validar:**

| Elemento | Validador | Criterio |
|----------|-----------|----------|
| Placeholder de carga | Contenido | Adecuado para niños de 3-4 años. Iconografía clara. |
| Aviso de bloqueo | Producto | Texto neutral. No revela causa ni controles parentales. |
| BaseStateScene | Producto | No sugiere interactividad. Visualmente comprensible. |
| Flujo completo | Producto + Contenido | Predecible y amable para el niño. |

**Proceso de validación:**
1. Frontend presenta capturas de pantalla y/o demo en dispositivo real.
2. Contenido valida adecuación por edad del placeholder visual.
3. Producto valida texto e iconografía del aviso neutral.
4. Producto valida que BaseStateScene no sugiere interactividad.
5. Se documenta la validación firmada por contenido y producto.

### 4. Pruebas E2E completas

**Flujos a probar:**

| Flujo | Pasos | Resultado esperado |
|-------|-------|-------------------|
| Perfil habilitado | HomeView → seleccionar perfil → GameView → LoadingScene → BaseStateScene | Llega a BaseStateScene sin porcentajes ni interactividad. |
| Perfil bloqueado | HomeView → seleccionar perfil bloqueado → aviso neutral | Muestra aviso neutral, permanece en HomeView. |
| Recarga de página | GameView → recargar página | Recupera sesión y continúa en GameView (si sesión activa). |
| Sin audio/NPC | Perfil habilitado → GameView → BaseStateScene | Llega a BaseStateScene sin requerir audio ni NPC. |

## Contratos y dependencias externas

### Contratos requeridos

Ninguno nuevo. Este sprint consume los contratos definidos en sprints anteriores.

### Dependencias de otras capas

| Capa | Dependencia | Impacto |
|------|-------------|---------|
| Producto | Validación de texto e iconografía del aviso neutral | Crítico |
| Contenido | Validación de placeholder visual y BaseStateScene | Crítico |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Contenido no valida placeholder visual | MEDIA | Escalar a contenido como dependencia crítica. Frontend puede usar placeholders técnicos si contenido no proporciona assets. |
| R2 | Producto no valida aviso neutral | MEDIA | Escalar a producto como dependencia crítica. Frontend puede usar texto provisional si producto no valida. |
| R3 | Problemas de accesibilidad no detectados en pruebas | BAJA | Probar en dispositivo real con niños de 3-4 años (si es posible). Revisar WCAG 2.1 AA. |
| R4 | Problemas de responsividad en dispositivos específicos | BAJA | Probar en múltiples dispositivos y resoluciones. Ajustar escalado de Phaser si es necesario. |

---

## Tareas del sprint

### Tarea 44.1: Validación de accesibilidad

**Criterios de aceptación:**
- Placeholder de carga es comprensible sin depender solo de color, texto o sonido.
- Aviso de bloqueo es comprensible sin depender solo de color, texto o sonido.
- BaseStateScene es comprensible sin depender solo de color, texto o sonido.
- Selector de perfiles tiene objetivos táctiles amplios (mínimo 44x44px).
- Contraste WCAG 2.1 AA (mínimo 4.5:1 para texto).
- Iconos tienen etiquetas `aria-label`.

### Tarea 44.2: Validación de responsividad

**Criterios de aceptación:**
- Phaser escala correctamente en móvil (320px-480px).
- Phaser escala correctamente en tableta (768px-1024px).
- Los elementos no se cortan ni se superponen en ninguna resolución.
- Los objetivos táctiles son accesibles en todas las resoluciones.

### Tarea 44.3: Validación con contenido

**Criterios de aceptación:**
- Contenido valida que el placeholder visual es adecuado para niños de 3-4 años.
- Contenido valida que BaseStateScene es visualmente comprensible.
- Se documenta la validación firmada por contenido.

### Tarea 44.4: Validación con producto

**Criterios de aceptación:**
- Producto valida que el aviso neutral es comprensible y no revela información parental.
- Producto valida que BaseStateScene no sugiere interactividad.
- Se documenta la validación firmada por producto.

### Tarea 44.5: Pruebas E2E completas

**Criterios de aceptación:**
- Prueba E2E: Flujo completo de perfil habilitado (HomeView → GameView → BaseStateScene).
- Prueba E2E: Flujo completo de perfil bloqueado (HomeView → aviso neutral).
- Prueba E2E: Recarga de página en GameView.
- Prueba E2E: Flujo sin audio/NPC.

### Tarea 44.6: Documentación final de FEAT-010

**Criterios de aceptación:**
- Se documentan todas las decisiones técnicas tomadas durante la implementación.
- Se documentan los handoffs a otras capas.
- Se documentan las validaciones de producto y contenido.
- Se actualiza FEAT-010 con el estado final (completado).

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|----------------|
| `framework/frontend/app/src/views/HomeView.vue` | Validación de accesibilidad |
| `framework/frontend/app/src/components/game/LoadingScene.ts` | Validación de accesibilidad |
| `framework/frontend/app/src/components/game/BaseStateScene.ts` | Validación de accesibilidad |
| `framework/frontend/app/src/components/game/BlockedProfileNotice.vue` | Validación de accesibilidad |
| `docs/product/features/frontend/FEAT-010-Entrada-a-GameView-y-carga-inicial.md` | Actualización de estado |

## Estimación

- **Duración:** 2 días
- **Complejidad:** Media
- **Riesgo:** Medio (dependencia de validación de producto y contenido)

## Criterios de aceptación del sprint

1. Los estados de carga y bloqueo se pueden diferenciar sin depender únicamente de color, texto o sonido y son utilizables en móvil y tableta. *(FEAT-010 AC9)*
2. Los estados visibles deben ser comprensibles en móvil y tableta, con apoyos visuales y sin depender exclusivamente de texto, color o sonido. *(FEAT-010 req 10)*
3. El placeholder visual es adecuado para niños de 3-4 años (validado con contenido). *(FEAT-010 §6)*
4. El aviso neutral es comprensible y no revela información parental (validado con producto). *(FEAT-010 §6)*
5. BaseStateScene es visualmente comprensible y no sugiere interactividad (validado con producto). *(FEAT-010 §6)*
6. Todas las pruebas E2E pasan sin errores.
7. `vue-tsc --noEmit` sin errores.

## Dependencias bloqueantes

- [ ] SPRINT-041 completado (validación de perfil y aviso de bloqueo).
- [ ] SPRINT-042 completado (rediseño de LoadingScene y placeholder visual).
- [ ] SPRINT-043 completado (BaseStateScene y exclusión de WorldMap/RecognitionGame).
- [ ] Contenido valida placeholder visual y BaseStateScene.
- [ ] Producto valida aviso neutral y BaseStateScene.

## Handoffs a otras capas

| Capa | Handoff | Prioridad |
|------|---------|-----------|
| Producto | Validación final de texto e iconografía del aviso neutral | Crítico |
| Contenido | Validación final de placeholder visual y BaseStateScene | Crítico |

## Notas adicionales

Este sprint cierra FEAT-010. La validación de producto y contenido es crítica para garantizar que la experiencia es adecuada para niños de 3-4 años.

Si contenido o producto no pueden validar en el plazo del sprint, se puede marcar como `BLOCKED` hasta obtener la validación.

Con el cierre de este sprint, FEAT-010 queda completa y se puede marcar como `completed` en el documento de la feature.

## Resumen de handoffs transversales

| Capa | Handoff | Estado |
|------|---------|--------|
| Backend | Confirmar que `POST /api/v1/sessions/children` rechaza perfiles bloqueados | Pendiente (SPRINT-041) |
| Contenido | Proporcionar assets para placeholder y estado base | Pendiente (SPRINT-042, SPRINT-043) |
| Producto | Validar texto e iconografía del aviso neutral | Pendiente (SPRINT-041, SPRINT-044) |
| Contenido | Validar placeholder visual y BaseStateScene | Pendiente (SPRINT-044) |
| Producto | Validar BaseStateScene no sugiere interactividad | Pendiente (SPRINT-044) |
