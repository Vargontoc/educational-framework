# SPRINT-032 — Retorno contextual ("Volver") y detección de origen

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-08-03
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-031 (verificado y cerrado)
- **Impacto estimado:** Lógica de detección del origen de navegación para mostrar/ocultar "Volver" con retorno contextual exacto a la sección parental de origen. Validación defensiva contra open redirect.

## Objetivo

Implementar la lógica de "Volver" condicional en `DocumentationLayout.vue` que:
- Detecte si el usuario llegó desde el panel parental mediante `query.from`.
- Muestre el botón "Volver" solo cuando corresponda.
- Retorne a la sección parental exacta de origen.
- Valide defensivamente `query.from` para prevenir open redirect.
- Persista el contexto de retorno tras recargar la página.

## Contexto

**FEAT-007** establece tres vías de entrada a la documentación:
1. **Home** → no muestra "Volver".
2. **URL pública directa** → no muestra "Volver".
3. **Panel parental** → muestra "Volver" que retorna a la sección exacta de origen.

**SPRINT-031** ya implementó:
- `DocumentationLayout.vue` con estructura base.
- `ParentSidebar.vue` que navega a `/docs?from=/panel/...`.
- Routing `/docs/:section` con `DocumentationLayout`.

**Mecanismo seleccionado:** `query.from` en la URL (no `history.state`) porque:
- Persiste al recargar página (compatible con URL pública directa).
- Es deep-linkable.
- No expone datos sensibles (solo paths internos del panel).

## Diseño funcional-técnico

### 1. Lógica de detección de origen en `DocumentationLayout.vue`

**Archivo:** `framework/frontend/app/src/layouts/DocumentationLayout.vue` (modificación)

**Estado y computed:**
```typescript
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const sidebarOpen = ref(false)

// Rutas parentales válidas para validación defensiva
const VALID_PANEL_PATHS = [
  '/panel',
  '/panel/configuracion',
  '/panel/ninos',
  '/panel/chatbot',
  '/panel/lectura-familiar',
  '/panel/relajacion-familiar'
]

// Detectar si query.from es válido
const rawFrom = computed(() => {
  const from = route.query.from
  return typeof from === 'string' ? from : null
})

const isValidPanelPath = computed(() => {
  if (!rawFrom.value) return false
  // Debe comenzar con /panel y coincidir con una ruta válida
  // o ser una ruta de edición de perfil (/panel/ninos/:id)
  return rawFrom.value.startsWith('/panel') && (
    VALID_PANEL_PATHS.includes(rawFrom.value) ||
    /^\/panel\/ninos\/\d+$/.test(rawFrom.value) ||
    /^\/panel\/ninos\/\d+\/dashboard$/.test(rawFrom.value)
  )
})

const showBackButton = computed(() => isValidPanelPath.value)

const currentSection = computed(() => route.params.section as string)

function goBack(): void {
  if (isValidPanelPath.value && rawFrom.value) {
    router.replace(rawFrom.value)
  }
}
```

### 2. Renderizado condicional del botón "Volver"

**Template (modificación sobre SPRINT-031):**
```vue
<header class="documentation-layout__header">
  <div class="documentation-layout__header-actions">
    <button
      v-if="showBackButton"
      class="documentation-layout__back-button"
      @click="goBack"
      :aria-label="t('views.docs.backToPanel')"
    >
      <NubiIcon name="arrow-left" />
      <span>{{ t('views.docs.backToPanel') }}</span>
    </button>
    <button
      class="documentation-layout__menu-toggle"
      @click="sidebarOpen = !sidebarOpen"
      :aria-label="t('views.docs.menuToggle')"
    >
      <NubiIcon name="menu" />
    </button>
  </div>
</header>
```

### 3. Validación defensiva contra open redirect

**Reglas:**
- `query.from` debe comenzar con `/panel`.
- `query.from` debe coincidir con una ruta parental conocida o patrón válido (`/panel/ninos/:id`, `/panel/ninos/:id/dashboard`).
- Si no cumple las validaciones, `showBackButton = false` y el valor se ignora.
- No se aceptan URLs externas, protocolos, ni paths que no comiencen con `/panel`.

**Casos de prueba:**

| `query.from` | ¿Válido? | "Volver" visible |
|---|---|---|
| (ausente) | No | No |
| `/panel` | Sí | Sí |
| `/panel/configuracion` | Sí | Sí |
| `/panel/ninos/3` | Sí | Sí |
| `/panel/ninos/3/dashboard` | Sí | Sí |
| `https://evil.com` | No | No |
| `/panel/../home` | No (no coincide con patrón) | No |
| `/docs` | No (no empieza con /panel) | No |
| `` (vacío) | No | No |

### 4. i18n — Clave nueva

**Archivo:** `framework/frontend/app/src/i18n/locales/es.ts` (modificación)

```typescript
views: {
  docs: {
    // ... claves existentes de SPRINT-031
    backToPanel: 'Volver al panel parental'
  }
}
```

## Contratos y dependencias externas

### Contratos

- **Sin cambios.** Este sprint no consume endpoints backend.

### Dependencias externas

| Capa | Dependencia | Estado |
|------|-------------|--------|
| Backend | Ninguna. | ✅ Sin bloqueo |
| Agents | Ninguna. | ✅ Sin dependencia |
| TTS | Ninguna. | ✅ Sin dependencia |
| Frontend | SPRINT-031 verificado (layout base). | ⏳ Pendiente |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Open redirect via `query.from` manipulado. | ALTA | Validación estricta: solo paths que comienzan con `/panel` y coinciden con patrones conocidos. |
| R2 | Recarga de página pierde el contexto de retorno. | BAJA | `query.from` persiste en la URL, no se pierde al recargar. |
| R3 | Nuevas rutas parentales futuras no estén en la lista válida. | BAJA | Mantener lista `VALID_PANEL_PATHS` actualizada cuando se añadan rutas. Usar patrón regex como fallback para rutas dinámicas. |

---

## Tareas del sprint

### Tarea 32.1: Implementar lógica de detección de origen en `DocumentationLayout.vue`

**Descripción:** Añadir computed properties para detectar `query.from`, validar que es una ruta parental válida y determinar si mostrar "Volver".

**Archivo:** `framework/frontend/app/src/layouts/DocumentationLayout.vue` (modificación)

**Especificación completa:** Ver sección 1 del diseño funcional-técnico.

**Criterios de aceptación:**
- `rawFrom` lee `route.query.from` y devuelve string o null.
- `isValidPanelPath` valida que comienza con `/panel` y coincide con patrón conocido.
- `showBackButton` es `true` solo cuando `isValidPanelPath` es `true`.
- `goBack()` ejecuta `router.replace(rawFrom.value)` solo si es válido.
- TypeScript compila sin errores.

---

### Tarea 32.2: Renderizado condicional del botón "Volver"

**Descripción:** Añadir el botón "Volver" al header de `DocumentationLayout.vue`, visible solo cuando `showBackButton` es `true`.

**Archivo:** `framework/frontend/app/src/layouts/DocumentationLayout.vue` (modificación)

**Especificación completa:** Ver sección 2 del diseño funcional-técnico.

**Criterios de aceptación:**
- Botón "Volver" visible solo cuando `showBackButton === true`.
- Botón muestra icono `arrow-left` + texto "Volver al panel parental".
- `@click` ejecuta `goBack()`.
- `aria-label` presente para accesibilidad.
- Botón posicionado antes del menú hamburger.
- TypeScript compila sin errores.

---

### Tarea 32.3: Validación defensiva contra open redirect

**Descripción:** Implementar y verificar la validación estricta de `query.from` para prevenir redirecciones externas.

**Archivo:** `framework/frontend/app/src/layouts/DocumentationLayout.vue` (modificación)

**Especificación completa:** Ver sección 3 del diseño funcional-técnico.

**Criterios de aceptación:**
- `https://evil.com` → "Volver" no visible.
- `/panel/../home` → "Volver" no visible.
- `/docs` → "Volver" no visible.
- `/panel` → "Volver" visible.
- `/panel/configuracion` → "Volver" visible.
- `/panel/ninos/3` → "Volver" visible.
- `/panel/ninos/3/dashboard` → "Volver" visible.
- `query.from` vacío → "Volver" no visible.
- TypeScript compila sin errores.

---

### Tarea 32.4: Actualizar i18n

**Descripción:** Añadir clave `backToPanel` para el botón "Volver".

**Archivo:** `framework/frontend/app/src/i18n/locales/es.ts` (modificación)

**Criterios de aceptación:**
- Clave `views.docs.backToPanel` disponible con valor "Volver al panel parental".
- TypeScript compila sin errores.

---

### Tarea 32.5: Verificación de escenarios de entrada

**Descripción:** Verificar los tres escenarios de entrada a la documentación y el comportamiento de "Volver".

**Escenarios:**

1. **Entrada desde Home:**
   - Navegar desde Home a `/docs`.
   - Verificar que "Volver" NO se muestra.

2. **Entrada desde URL directa:**
   - Navegar directamente a `/docs/quien-soy`.
   - Verificar que "Volver" NO se muestra.

3. **Entrada desde panel parental:**
   - Desde `/panel/configuracion`, pulsar "Documentación".
   - Verificar que URL es `/docs?from=/panel/configuracion`.
   - Verificar que "Volver" SÍ se muestra.
   - Pulsar "Volver".
   - Verificar que retorna a `/panel/configuracion`.

4. **Recarga de página:**
   - Con `query.from` presente, recargar la página.
   - Verificar que "Volver" sigue visible y funciona.

5. **Manipulación de `query.from`:**
   - Navegar a `/docs?from=https://evil.com`.
   - Verificar que "Volver" NO se muestra.

**Criterios de aceptación:**
- Los 5 escenarios funcionan correctamente.
- `vue-tsc --noEmit` sin errores.

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `framework/frontend/app/src/layouts/DocumentationLayout.vue` | Modificación (lógica "Volver") |
| `framework/frontend/app/src/i18n/locales/es.ts` | Modificación (añadir `backToPanel`) |

## Estimación

- **Duración:** 1 día
- **Complejidad:** Baja-Media
- **Riesgo:** Bajo (la lógica es contenida, el riesgo principal es open redirect ya mitigado)

## Criterios de aceptación del sprint

1. Al acceder desde Home o URL directa, "Volver" no se muestra. *(FEAT-007 CA #6)*
2. Al acceder desde sección X del panel parental, "Volver" se muestra. *(CA #7)*
3. Al pulsar "Volver", se retorna a la sección parental exacta de origen. *(CA #7)*
4. Un valor manipulado de `query.from` (ej: `https://evil.com`) no provoca redirección externa. *(Seguridad)*
5. Tras recargar la página con `query.from` presente, "Volver" sigue funcionando. *(Persistencia)*
6. `vue-tsc --noEmit` compila sin errores. *(Calidad)*

## Evidencias esperadas

- Test manual: desde Home → "Volver" no visible.
- Test manual: desde URL directa → "Volver" no visible.
- Test manual: desde `/panel/configuracion` → "Documentación" → "Volver" visible → retorna a `/panel/configuracion`.
- Test manual: desde `/panel/ninos/3` → "Documentación" → "Volver" visible → retorna a `/panel/ninos/3`.
- Test manual: recargar con `?from=/panel/configuracion` → "Volver" sigue visible.
- Test manual: `?from=https://evil.com` → "Volver" no visible.
- Test manual: `?from=/panel/../home` → "Volver" no visible.
- `vue-tsc --noEmit` sin errores.

## Dependencias bloqueantes

- [ ] SPRINT-031 completado y verificado.

## Handoffs a otras capas

### Backend:
- Sin cambios requeridos.

### Agents/TTS:
- Sin dependencia.

## Notas adicionales

### Relación con otros sprints

- **Depende de:** SPRINT-031 (layout base).
- **SPRINT-033** es independiente de este sprint (el formulario funciona igual con o sin "Volver").

### Privacidad infantil

- `query.from` solo contiene paths internos del panel parental, no datos personales.
- La validación defensiva previene open redirect, protegiendo al usuario de manipulaciones externas.
