# SPRINT-037 — Catálogo de cuentos y corrección de fondo de la intro

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-08-27
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** FEAT-008, SPRINT-084-backend (API de cuentos)
- **Impacto estimado:** Primera pieza del flujo de Lectura Familiar: rutas base, catálogo (portada+título) y corrección del fondo/navegación de la pantalla de intro ya existente.

## Objetivo

Añadir las rutas del flujo de Lectura Familiar, implementar la pantalla de catálogo consumiendo `GET /api/v1/content/stories`, y corregir el ajuste de fondo y la navegación de la pantalla de intro (`LecturaFamiliarView.vue`), hoy un placeholder mínimo.

## Contexto

FEAT-008 exige un fondo correctamente ajustado en vertical/horizontal (§3.1, AC1) y un catálogo mostrado como portadas con título, sin edad/duración ni otros datos (§3.3, AC3). `LecturaFamiliarView.vue` ya existe pero con un bug: `width: 100vh; height: 100vw` (ejes invertidos) y un botón "Ver Cuentos" sin navegación real.

## Diseño funcional-técnico

### 1. Tipos — `types/story.ts` (nuevo)

```typescript
export interface StoryCatalogItem {
  id: string
  title: string
  coverUrl: string
}

export interface ApiListStoryResponse {
  success: boolean
  message: string | null
  errors: string[]
  data: StoryCatalogItem[]
}
```

### 2. Servicio — `services/storyService.ts` (nuevo)

```typescript
import { apiClient } from './api'
import type { ApiListStoryResponse, StoryCatalogItem } from '../types/story'

export async function listStories(): Promise<StoryCatalogItem[]> {
  const response = await apiClient.get<ApiListStoryResponse>('/api/v1/content/stories')
  return response.data
}
```

### 3. `StoryCard.vue` (nuevo)

Portada + título como único contenido (sin edad/duración, FEAT-008 §3.3). Tarjeta completa como `router-link`, siguiendo el mismo patrón táctil que `panel-cover-view__card` (objetivo ≥48px, `aria-label` con el título). Imagen con `loading="lazy"` y `alt` = título.

### 4. `PanelLecturaFamiliarCatalogoView.vue` (nuevo)

- `onMounted`: `listStories()`; estados `loading`/`error`/`items`.
- Grid responsive (`repeat(auto-fit, minmax(...))`, mismo patrón que `token-grid` en `CatalogView.vue`).
- Estado vacío (ningún cuento válido en el catálogo, p. ej. mientras `story_0` no tenga sus recursos completos según SPRINT-083-backend): mensaje sobrio i18n, **no** tratado como error.
- Estado de error de red: mensaje con reintento, coherente con el tono del resto del panel.

### 5. Corrección de `LecturaFamiliarView.vue`

- Sustituir `width: 100vh; height: 100vw` por `width: 100%; min-height: 100dvh` con `background-size: cover; background-position: center`, verificado en portrait y landscape (móvil y tablet).
- Quitar el hack de posicionamiento del botón (`margin-top: 50%; margin-left: 50%`); centrar con flexbox.
- El botón "Ver Cuentos" navega con `router.push({ name: 'PanelLecturaFamiliarCatalogo' })`.

### 6. Ruta nueva

```typescript
{
  path: 'lectura-familiar/catalogo',
  name: 'PanelLecturaFamiliarCatalogo',
  component: () => import('../views/PanelLecturaFamiliarCatalogoView.vue')
}
```

## Contratos y dependencias externas

### Contratos

Consume `GET /api/v1/content/stories` (SPRINT-084-backend).

### Dependencias externas

| Capa | Dependencia | Estado |
|---|---|---|
| Backend | SPRINT-084-backend desplegado, devolviendo `coverUrl` resoluble desde el frontend | ⏳ Pendiente |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|---|---|---|
| R1 | Catálogo vacío en desarrollo mientras `story_0` no tenga sus recursos completos (SPRINT-083-backend) | BAJA | Estado vacío explícito y sobrio, no tratado como error; documentado para QA. |
| R2 | Fondo distorsionado en pantallas muy anchas o muy altas | MEDIA | `background-size: cover` + `background-position: center`; verificar en 320px portrait y 1024px landscape (breakpoints ya usados en el proyecto). |
| R3 | `coverUrl` del backend es una ruta relativa que necesita prefijo de `API_BASE_URL` | MEDIA | Confirmar con backend el formato exacto (ver Handoffs); si es relativa, `StoryCard.vue` la resuelve contra `API_BASE_URL` igual que `services/api.ts`. |

---

## Tareas del sprint

### Tarea 37.1: Definir `types/story.ts` y `services/storyService.ts`

**Criterios de aceptación:** `listStories()` devuelve `StoryCatalogItem[]`; manejo de error vía el mismo patrón `ApiError` que el resto de servicios.

### Tarea 37.2: Implementar `StoryCard.vue`

**Criterios de aceptación:** portada+título únicamente; objetivo táctil ≥48px; `alt`/`aria-label` con el título; sin datos de edad/duración.

### Tarea 37.3: Implementar `PanelLecturaFamiliarCatalogoView.vue`

**Criterios de aceptación:** grid responsive; estados loading/error/vacío diferenciados; ningún dato infantil ni comparativo mostrado (FEAT-008 §6).

### Tarea 37.4: Añadir ruta `lectura-familiar/catalogo`

**Criterios de aceptación:** navegación funcional desde la intro; ruta protegida por el guard `requiresParentalAuth` heredado de `/panel`.

### Tarea 37.5: Corregir fondo y navegación de `LecturaFamiliarView.vue`

**Criterios de aceptación:** fondo correctamente ajustado en portrait y landscape, sin recortes ni deformaciones (FEAT-008 AC1); botón navega a la ruta real del catálogo.

### Tarea 37.6: i18n de las cadenas nuevas

**Criterios de aceptación:** ninguna cadena visible como literal en template; añadidas a `i18n/locales/es.ts`.

## Archivos afectados

| Archivo | Tipo de cambio |
|---|---|
| `framework/frontend/app/src/types/story.ts` | Nuevo |
| `framework/frontend/app/src/services/storyService.ts` | Nuevo |
| `framework/frontend/app/src/components/lectura-familiar/StoryCard.vue` | Nuevo |
| `framework/frontend/app/src/views/PanelLecturaFamiliarCatalogoView.vue` | Nuevo |
| `framework/frontend/app/src/views/LecturaFamiliarView.vue` | Modificación |
| `framework/frontend/app/src/router/index.ts` | Modificación |
| `framework/frontend/app/src/i18n/locales/es.ts` | Modificación |

## Estimación

- **Duración:** 1.5 días
- **Complejidad:** Baja-Media
- **Riesgo:** Bajo (maquetable con datos mock si SPRINT-084-backend no está aún desplegado)

## Criterios de aceptación del sprint

1. El fondo de la intro se muestra correctamente ajustado en vertical y horizontal, sin recortes ni deformaciones. *(FEAT-008 AC1)*
2. Existe una acción visible para abrir el catálogo desde la intro. *(FEAT-008 AC2)*
3. El catálogo muestra cada cuento como portada con título únicamente. *(FEAT-008 AC3)*
4. El catálogo vacío o en error muestra un mensaje sobrio, nunca una pantalla rota. *(Robustez)*
5. Toda cadena visible pasa por i18n. *(Calidad)*
6. `vue-tsc --noEmit` sin errores. *(Calidad)*

## Dependencias bloqueantes

- [ ] SPRINT-084-backend desplegado en desarrollo (o mock local documentado para maquetar mientras tanto).

## Handoffs a otras capas

### Backend

- Confirmar si `coverUrl`/`imageUrl`/`audioUrl` son rutas relativas (requieren prefijo `API_BASE_URL` en frontend) o absolutas.

## Notas adicionales

### Privacidad infantil

- La sección solo es alcanzable tras el guard `requiresParentalAuth` ya existente en `/panel`; no se registra qué cuentos se listan ni se abren (FEAT-008 §6).
