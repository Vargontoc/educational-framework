# SPRINT-038 — Vista previa del cuento, sesión de lectura y precarga total

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-08-27
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-037 (catálogo), SPRINT-084-backend (API de cuentos), FEAT-005 (`narrativeVoiceEnabled`)
- **Impacto estimado:** Pantalla previa del cuento (portada+título+toggle condicional), estado de sesión de lectura compartido y disparo de la precarga completa del cuento antes de entrar a leer.

## Objetivo

Implementar la pantalla previa del cuento (portada, título y, si procede, interruptor de narración) y el estado compartido `useStoryReadingSession`, que arranca la precarga de **todo el cuento** en cuanto se conoce su detalle — antes incluso de que el adulto pulse "Empezar a leer" — para que la lectura no tenga esperas perceptibles.

## Contexto

**Decisión confirmada (2026-08-27):** los cuentos pueden tener entre 8 y 32 páginas con sus recursos. Dado ese rango, se precarga el **cuento completo** (no solo páginas adyacentes) en cuanto se conoce su detalle, es decir, ya desde esta pantalla previa — así, al llegar a la lectura, la mayoría (o la totalidad) de los recursos ya están en caché del navegador.

**Decisión confirmada (2026-08-27):** el interruptor de narración siempre arranca en su valor por defecto (activado si `narrativeVoiceEnabled` global lo permite); no se recuerda la elección de cuentos anteriores dentro de la sesión del panel.

## Diseño funcional-técnico

### 1. Extender `types/story.ts`

```typescript
export interface StoryPage {
  page: number
  text: string
  imageUrl: string
  audioUrl: string
}

export interface StoryDetail {
  id: string
  title: string
  coverUrl: string
  pages: StoryPage[]
}

export interface ApiStoryDetailResponse {
  success: boolean
  message: string | null
  errors: string[]
  data: StoryDetail
}
```

### 2. Extender `services/storyService.ts`

```typescript
export async function getStory(id: string): Promise<StoryDetail> {
  const response = await apiClient.get<ApiStoryDetailResponse>(`/api/v1/content/stories/${id}`)
  return response.data
}
```

### 3. `composables/useStoryPagePreloader.ts` (nuevo)

```typescript
export function useStoryPagePreloader() {
  const preloadedUrls = new Set<string>()
  const keepAlive: (HTMLImageElement | HTMLAudioElement)[] = []

  function preloadUrl(url: string, kind: 'image' | 'audio') {
    if (preloadedUrls.has(url)) return
    preloadedUrls.add(url)
    const el = kind === 'image' ? new Image() : new Audio()
    if (kind === 'audio') (el as HTMLAudioElement).preload = 'auto'
    el.src = url
    keepAlive.push(el) // evita que el GC libere el elemento antes de completar la carga
  }

  function preloadStory(pages: StoryPage[]) {
    pages.forEach(page => {
      preloadUrl(page.imageUrl, 'image')
      preloadUrl(page.audioUrl, 'audio')
    })
  }

  return { preloadStory }
}
```

### 4. `composables/useStoryReadingSession.ts` (nuevo, singleton de módulo)

Estado compartido entre la previa y la lectura sin pasar por la URL:

```typescript
import { ref, readonly } from 'vue'
import type { StoryDetail } from '../types/story'

const story = ref<StoryDetail | null>(null)
const narrateEnabled = ref(false)

export function useStoryReadingSession() {
  function start(detail: StoryDetail, narrate: boolean) {
    story.value = detail
    narrateEnabled.value = narrate
  }
  function reset() {
    story.value = null
    narrateEnabled.value = false
  }
  return { story: readonly(story), narrateEnabled: readonly(narrateEnabled), start, reset }
}
```

### 5. `PanelLecturaFamiliarPreviaView.vue` (nuevo)

- `onMounted`: `getStory(route.params.storyId)`.
  - 404 / no encontrado → mensaje sobrio "cuento no disponible" con botón volver al catálogo (nunca un error técnico).
  - Éxito → invoca `preloadStory(detail.pages)` inmediatamente (fire-and-forget, no bloquea la UI ni el resto de la carga de la pantalla).
- Muestra `coverUrl` + `title`.
- Si `family.narrativeVoiceEnabled` (leído vía `useFamilyStatus`) es `true` → interruptor local `localNarrateEnabled` (`ref(true)` por defecto, decisión confirmada), sin persistencia.
- Si es `false` → no se muestra ningún control ni mención de voz (FEAT-008 AC6).
- Botón "Empezar a leer": `useStoryReadingSession().start(detail, family.narrativeVoiceEnabled && localNarrateEnabled)` y navega a la lectura.

### 6. Ruta nueva

```typescript
{
  path: 'lectura-familiar/:storyId',
  name: 'PanelLecturaFamiliarPrevia',
  component: () => import('../views/PanelLecturaFamiliarPreviaView.vue')
}
```

## Contratos y dependencias externas

### Contratos

Consume `GET /api/v1/content/stories/{id}` (SPRINT-084-backend).

### Dependencias externas

| Capa | Dependencia | Estado |
|---|---|---|
| Backend | SPRINT-084-backend desplegado, endpoint de detalle disponible | ⏳ Pendiente |
| Frontend | `useFamilyStatus`/`FamilyData.narrativeVoiceEnabled` (ya existente, FEAT-005) | ✅ Sin bloqueo |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|---|---|---|
| R1 | Precargar hasta 32 páginas de audio+imagen al abrir la previa consume datos aunque el adulto no llegue a leer | MEDIA | Aceptable: solo se dispara cuando el adulto ya eligió un cuento concreto (no en el catálogo); es la única forma de tener el cuento listo antes de la lectura. |
| R2 | 404 al pedir el detalle de un id que desapareció del catálogo entre el listado y el tap | BAJA | Mensaje "cuento no disponible" + botón volver al catálogo, nunca pantalla de error técnico. |
| R3 | `useStoryReadingSession` como singleton de módulo podría arrastrar datos de un cuento anterior | BAJA | `start()` siempre sobrescribe el estado completo; no hay mezcla parcial posible. |

---

## Tareas del sprint

### Tarea 38.1: Extender `types/story.ts` y `storyService.getStory`

**Criterios de aceptación:** `getStory(id)` devuelve `StoryDetail` completo; 404 se propaga como `ApiError` reconocible.

### Tarea 38.2: Implementar `useStoryPagePreloader.ts`

**Criterios de aceptación:** no repite precarga de una misma URL; precarga imagen y audio de todas las páginas recibidas.

### Tarea 38.3: Implementar `useStoryReadingSession.ts`

**Criterios de aceptación:** `start()`/`reset()` mutan el estado compartido; el estado es legible (readonly) desde fuera del composable.

### Tarea 38.4: Implementar `PanelLecturaFamiliarPreviaView.vue`

**Criterios de aceptación:** portada+título visibles; interruptor condicional a `narrativeVoiceEnabled` global, inicializado activado; dispara la precarga en cuanto llega el detalle; "Empezar a leer" puebla la sesión y navega.

### Tarea 38.5: Añadir ruta `lectura-familiar/:storyId`

**Criterios de aceptación:** navegación funcional desde el catálogo (`StoryCard.vue`, SPRINT-037).

### Tarea 38.6: i18n de las cadenas nuevas

**Criterios de aceptación:** ninguna cadena visible como literal en template.

## Archivos afectados

| Archivo | Tipo de cambio |
|---|---|
| `framework/frontend/app/src/types/story.ts` | Modificación (añade `StoryPage`, `StoryDetail`) |
| `framework/frontend/app/src/services/storyService.ts` | Modificación (añade `getStory`) |
| `framework/frontend/app/src/composables/useStoryPagePreloader.ts` | Nuevo |
| `framework/frontend/app/src/composables/useStoryReadingSession.ts` | Nuevo |
| `framework/frontend/app/src/views/PanelLecturaFamiliarPreviaView.vue` | Nuevo |
| `framework/frontend/app/src/router/index.ts` | Modificación |
| `framework/frontend/app/src/i18n/locales/es.ts` | Modificación |

## Estimación

- **Duración:** 1.5 días
- **Complejidad:** Media
- **Riesgo:** Bajo

## Criterios de aceptación del sprint

1. Al elegir un cuento se muestra su portada y título antes de empezar a leer. *(FEAT-008 AC4)*
2. Con `narrativeVoiceEnabled` global activo, la previa muestra el interruptor de narración, inicializado en activado. *(FEAT-008 AC5, decisión confirmada)*
3. Con `narrativeVoiceEnabled` global inactivo, no aparece ningún control de voz en la previa. *(FEAT-008 AC6)*
4. Al recibir el detalle del cuento se dispara la precarga de imagen+audio de **todas** sus páginas, sin bloquear la interacción del usuario. *(Decisión de precarga confirmada)*
5. "Empezar a leer" deja `useStoryReadingSession` poblada con el cuento completo y el valor de narración elegido, y navega a la lectura.
6. Un id de cuento no disponible muestra un mensaje sobrio con vuelta al catálogo.

## Dependencias bloqueantes

- [ ] SPRINT-037 completado (rutas base, catálogo).
- [ ] SPRINT-084-backend desplegado.

## Handoffs a otras capas

- Ninguno adicional a los ya registrados en SPRINT-037.

## Notas adicionales

No se persiste ninguna elección de narración entre cuentos (decisión confirmada 2026-08-27): cada vista previa arranca limpia, activada por defecto si el ajuste global lo permite.
