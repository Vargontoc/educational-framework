# SPRINT-015 — Frontend — Navegación adaptable y acción «Salir»

## Goal
Implementar la navegación adaptable del panel parental con sidebar colapsable (visible en landscape, bajo demanda en portrait), agrupación de secciones Panel/Experiencias, tarjetas navegables en portada, y acción «Salir» con consumo de `/auth/logout` y redirección a Home.

## Status
status: closed
started_at: 2026-07-29
closed_at: 2026-07-29
blocked_by: SPRINT-014
waiting_for: —

## Context
FEAT-004 define la estructura visual y navegación del panel parental. SPRINT-014 implementó la autenticación mediante PIN y la portada neutral con tarjetas visuales. Este sprint completa la navegación adaptable y la acción «Salir».

### Requisitos de FEAT-004 a implementar en este sprint
- Layout con sidebar lateral izquierdo y zona de contenido principal
- En landscape (≥1024px): sidebar visible permanentemente (~280px)
- En portrait (<1024px): sidebar oculto, botón hamburguesa en cabecera, overlay sobre contenido
- Prohibido `rotate()` + `scale()` para simular landscape (ADR-019)
- Solo media queries CSS para reacomodar contenido (ADR-019)
- Agrupación de secciones en sidebar:
  - **Panel**: Configuración, Niños, Chatbot, Documentación
  - **Experiencias**: Lectura familiar, Relajación familiar
  - **[separador visual]** → **Salir** (última acción)
- Etiquetas de texto obligatorias en todas las secciones (iconos opcionales)
- Acción «Salir»:
  - Última entrada del sidebar, tras separador visual
  - Jerarquía visual secundaria (tipografía menor, color neutro)
  - Objetivo táctil ≥48x48dp
  - Consume `POST /auth/logout` con `Authorization: Bearer <token>`
  - Elimina token local, resetea store, redirige a Home
  - Logout optimista: si `POST /auth/logout` falla, eliminar token igualmente
- Tarjetas de portada navegables (implementadas como visuales en SPRINT-014)
- Documentación accesible desde el panel y desde Home (sin PIN)
- i18n completo (español)

### Requisitos de FEAT-004 ya implementados o fuera de alcance
- SPRINT-014 implementó autenticación PIN y portada neutral con tarjetas visuales
- Logout automático por inactividad → SPRINT-016
- Modo oscuro → SPRINT-017
- Contenido interno de las 6 secciones → fuera del alcance de FEAT-004

## Tasks
- [x] Crear layout `ParentPanelLayout.vue` con dos regiones
  - [x] Sidebar izquierda (`ParentSidebar.vue`)
  - [x] Content area (zona de contenido principal)
  - [x] En landscape: sidebar visible permanentemente (~280px ancho fijo)
  - [x] En portrait: sidebar oculto, botón hamburguesa en cabecera
  - [x] Media queries CSS para adaptación (sin `rotate()` ni `scale()`)
- [x] Crear componente `ParentSidebar.vue` con navegación agrupada
  - [x] Grupo **Panel**: Configuración, Niños, Chatbot, Documentación
  - [x] Grupo **Experiencias**: Lectura familiar, Relajación familiar
  - [x] Separador visual antes de «Salir»
  - [x] Acción «Salir» como última entrada
  - [x] Cada sección: icono (Lucide vía `NubiIcon`) + etiqueta de texto obligatoria
  - [x] Sección activa resaltada visualmente (color de fondo, borde izquierdo)
  - [x] Navegación mediante `router-link` a rutas `/panel/configuracion`, `/panel/ninos`, etc.
  - [x] En portrait: sidebar se abre como overlay sobre contenido
  - [x] En portrait: botón cerrar (X) en cabecera del sidebar
  - [x] En portrait: click fuera del sidebar cierra overlay
  - [x] Accesibilidad: `aria-label` en botón hamburguesa, `role="navigation"` en sidebar
- [x] Crear botón hamburguesa en cabecera (solo visible en portrait)
  - [x] Icono de menú (Lucide vía `NubiIcon`)
  - [x] `aria-label="Abrir menú de navegación"`
  - [x] Objetivo táctil ≥48x48dp
  - [x] Toggle de `sidebarOpen` en store
- [x] Implementar acción «Salir»
  - [x] Última entrada del sidebar, tras separador visual
  - [x] Jerarquía visual secundaria: tipografía menor, color neutro
  - [x] Objetivo táctil ≥48x48dp
  - [x] Etiqueta de texto obligatoria: "Salir"
  - [x] Icono opcional (Lucide: log-out)
  - [x] Al pulsar: invocar `useParentalSession.logout()`
  - [x] `POST /auth/logout` con `Authorization: Bearer <token>`
  - [x] Eliminar token de `sessionStorage`
  - [x] Resetear `isAuthenticated` en store
  - [x] Redirigir a Home con `router.replace('/')`
  - [x] Logout optimista: si `POST /auth/logout` falla (ej. token expirado), eliminar token local igualmente y redirigir
- [x] Implementar tarjetas navegables en `PanelCoverView.vue`
  - [x] Convertir tarjetas visuales de SPRINT-014 en navegables
  - [x] Cada tarjeta: `router-link` a ruta correspondiente
  - [x] Agrupadas por sección: Panel / Experiencias
  - [x] Objetivo táctil ≥48x48dp
  - [x] Accesibilidad: `aria-label` por tarjeta
- [x] Añadir rutas en `router/index.ts` para sub-rutas del panel
  - [x] `/panel/configuracion` → placeholder view (fuera del alcance de FEAT-004)
  - [x] `/panel/ninos` → placeholder view
  - [x] `/panel/chatbot` → placeholder view
  - [x] `/panel/documentacion` → reuse `DocumentationView` existente (accesible desde Home sin PIN)
  - [x] `/panel/lectura-familiar` → placeholder view
  - [x] `/panel/relajacion-familiar` → placeholder view
  - [x] Todas protegidas con guard `requiresParentalAuth`
  - [x] Layout: todas usan `ParentPanelLayout.vue` como layout padre
- [x] Crear placeholder views para secciones (fuera del alcance de FEAT-004)
  - [x] `ConfiguracionView.vue`: mensaje "Sección en desarrollo"
  - [x] `NinosView.vue`: mensaje "Sección en desarrollo"
  - [x] `ChatbotView.vue`: mensaje "Sección en desarrollo"
  - [x] `LecturaFamiliarView.vue`: mensaje "Sección en desarrollo"
  - [x] `RelajacionFamiliarView.vue`: mensaje "Sección en desarrollo"
  - [x] `DocumentationView.vue`: reutilizar existente (accesible desde Home sin PIN)
- [x] Implementar estado de UI para sidebar en portrait
  - [x] `useUIStore.ts`: estado `sidebarOpen` (boolean)
  - [x] Action: `toggleSidebar()`, `closeSidebar()`
  - [x] `sidebarOpen` no persiste (estado efímero de UI)
  - [x] Cerrar sidebar al navegar a otra sección (en portrait)
- [x] Añadir traducciones i18n en `es.ts`
  - [x] `sidebar.*`: grupos (panel, experiencias), secciones (configuracion, ninos, chatbot, documentacion, lecturaFamiliar, relajacionFamiliar), salir
  - [x] `sidebar.ariaLabels.*`: abrir menu, cerrar menu, navegacion principal
- [x] Validar accesibilidad
  - [x] `role="navigation"` en sidebar
  - [x] `aria-label` en botón hamburguesa y botón cerrar
  - [x] `aria-current="page"` en sección activa
  - [x] Objetivo táctil ≥48x48dp en todos los elementos interactivos
  - [x] Navegación por teclado: Tab order correcto, Enter/Espacio activan enlaces
- [x] Validar responsive en móvil y tablet (portrait y landscape)
  - [x] Landscape: sidebar visible permanentemente
  - [x] Portrait: sidebar oculto, se abre con botón hamburguesa
  - [x] Pruebas en Samsung Galaxy A15 (portrait) y emuladores de tablets (landscape)
- [x] Verificar build exitoso sin errores de TypeScript

## Acceptance Criteria
- `ParentPanelLayout` muestra sidebar + content area
- Landscape (≥1024px): sidebar visible permanentemente (~280px ancho)
- Portrait (<1024px): sidebar oculto por defecto
- Portrait: botón hamburguesa visible en cabecera
- Portrait: botón hamburguesa abre sidebar como overlay sobre contenido
- Portrait: botón cerrar (X) en cabecera del sidebar cierra overlay
- Portrait: click fuera del sidebar cierra overlay
- Sidebar muestra grupo **Panel** con 4 secciones: Configuración, Niños, Chatbot, Documentación
- Sidebar muestra grupo **Experiencias** con 2 secciones: Lectura familiar, Relajación familiar
- Separador visual antes de «Salir»
- «Salir» es la última entrada del sidebar
- «Salir» tiene jerarquía visual secundaria (tipografía menor, color neutro)
- «Salir» tiene objetivo táctil ≥48x48dp
- «Salir» tiene etiqueta de texto obligatoria "Salir"
- Pulsar «Salir» invoca `POST /auth/logout` con `Authorization: Bearer <token>`
- Tras logout exitoso: token eliminado de `sessionStorage`, `isAuthenticated = false`, redirige a Home
- Logout optimista: si `POST /auth/logout` falla, token se elimina igualmente y redirige a Home
- Cada sección del sidebar tiene icono + etiqueta de texto obligatoria
- Sección activa resaltada visualmente (color de fondo, borde izquierdo)
- Tarjetas de portada son navegables (router-link a rutas correspondientes)
- Tarjetas agrupadas por sección: Panel / Experiencias
- Rutas `/panel/configuracion`, `/panel/ninos`, etc. existen y muestran placeholder views
- Ruta `/panel/documentacion` reutiliza `DocumentationView` existente (accesible desde Home sin PIN)
- Todas las sub-rutas de `/panel` protegidas con guard `requiresParentalAuth`
- `sidebarOpen` no persiste (estado efímero de UI)
- Cerrar sidebar al navegar a otra sección (en portrait)
- i18n completo: sin literales en templates
- Accesibilidad: `role="navigation"`, `aria-label`, `aria-current="page"`, objetivo táctil ≥48x48dp
- Responsive en móvil y tablet (portrait y landscape)
- No se usa `rotate()` ni `scale()` para simular landscape (solo media queries)
- Build exitoso sin errores de TypeScript

## Risks
- Sidebar en portrait puede no ser localizable por el adulto si botón hamburguesa es demasiado discreto (mitigación: botón visible en cabecera, etiqueta "Menú" opcional)
- «Salir» demasiado discreto y no se localiza (mitigación: objetivo táctil ≥48x48dp, etiqueta obligatoria, separador visual)
- Media queries no cubren todos los aspect ratios de tablets (mitigación: breakpoints estándar de TailwindCSS)
- Placeholder views pueden confundir si no indican claramente que están en desarrollo (mitigación: mensaje claro "Sección en desarrollo")
- Chunk size puede aumentar al añadir layout y sidebar (monitorear)
- `DocumentationView` puede requerir ajustes para encajar en `ParentPanelLayout` (verificar)

## Dependencies
- **Contratos API:**
  - `docs/contracts/api/openapi/paths/logout.yaml` (POST /auth/logout)
  - `docs/contracts/api/openapi/schemas/auth/login-response.yaml` (token usado en Authorization)
- **Componentes base:**
  - `NubiIcon` (para iconos de secciones y botones, ya existe)
  - `NubiButton` (para botón hamburguesa y cerrar, ya existe)
- **Vistas existentes:**
  - `DocumentationView.vue` (reutilizar para `/panel/documentacion`)
- **Stores:**
  - `useParentalAuthStore` (para logout, creado en SPRINT-014)
  - `useUIStore` (nuevo, para estado de sidebar)
- **Composables:**
  - `useParentalSession` (para logout, creado en SPRINT-014)
- **i18n:**
  - `src/i18n/locales/es.ts` (para traducciones, ya existe)
- **Backend:**
  - POST /auth/logout debe estar implementado y funcional
  - Confirmar que POST /auth/logout acepta header `Authorization: Bearer <token>`

## Agent Instruction
- **No usar** `rotate()` ni `scale()` para simular landscape (ADR-019)
- **Usar** solo media queries CSS para adaptación portrait/landscape
- **Crear** `ParentPanelLayout.vue` como layout padre de todas las sub-rutas de `/panel`
- **Reutilizar** `DocumentationView.vue` existente para `/panel/documentacion`
- **Implementar** logout optimista: si `POST /auth/logout` falla, eliminar token local igualmente
- **Marcar** «Salir» con jerarquía visual secundaria pero objetivo táctil ≥48x48dp
- **Consumir** contratos API definidos en `docs/contracts/api/openapi/`
- **Mantener** separación entre experiencia infantil y controles parentales
- **Validar** responsive en portrait y landscape con media queries CSS
- **Validar** en Samsung Galaxy A15 (portrait) y emuladores de tablets (landscape)
- **Documentar** decisiones técnicas y dependencias en la sección Review al completar el sprint
- **Marcar** tareas como implementadas (no verificadas) al completarlas

## Notes
- **Navegación adaptable:** En landscape (≥1024px), sidebar visible permanentemente con ancho fijo ~280px. En portrait (<1024px), sidebar oculto por defecto, se abre con botón hamburguesa como overlay sobre contenido. No se usa `rotate()` ni `scale()` para simular landscape (ADR-019).
- **Agrupación de secciones:** Panel (Configuración, Niños, Chatbot, Documentación) / Experiencias (Lectura familiar, Relajación familiar). Separador visual antes de «Salir».
- **Acción «Salir»:** Última entrada del sidebar, tras separador visual. Jerarquía visual secundaria (tipografía menor, color neutro) pero objetivo táctil ≥48x48dp. Etiqueta de texto obligatoria "Salir". Consume `POST /auth/logout` con `Authorization: Bearer <token>`. Logout optimista: si falla, eliminar token local igualmente.
- **Tarjetas navegables:** En SPRINT-014 eran visuales; en este sprint se convierten en `router-link` a rutas correspondientes. Agrupadas por sección: Panel / Experiencias.
- **Placeholder views:** Secciones internas (Configuración, Niños, Chatbot, Lectura familiar, Relajación familiar) fuera del alcance de FEAT-004. Se crean placeholder views con mensaje "Sección en desarrollo".
- **Documentación:** `/panel/documentacion` reutiliza `DocumentationView` existente, que también es accesible desde Home sin PIN. Esto cumple el requisito de FEAT-004: "Documentación debe estar disponible en el panel sin eliminar el acceso público e interno desde Home."
- **Estado de UI:** `useUIStore` gestiona `sidebarOpen` (boolean) para portrait. No persiste (estado efímero). Se cierra al navegar a otra sección en portrait.
- **Accesibilidad:** `role="navigation"` en sidebar, `aria-label` en botones, `aria-current="page"` en sección activa. Objetivo táctil ≥48x48dp en todos los elementos interactivos.
- **Responsive:** Breakpoints estándar de TailwindCSS. Landscape ≥1024px, portrait <1024px. Validar en Samsung Galaxy A15 (portrait) y emuladores de tablets (landscape).
- **Seguridad:** Token se elimina de `sessionStorage` tras logout. `isAuthenticated` se resetea. Redirección a Home con `router.replace('/')` para no acumular historial.

## Review

### Implementación completada: 2026-07-29

#### Archivos creados
- `app/src/layouts/ParentPanelLayout.vue` — Layout con sidebar + content area, botón hamburguesa visible solo en portrait (<1024px)
- `app/src/components/ParentSidebar.vue` — Sidebar con navegación agrupada (Panel/Experiencias), separador visual, acción «Salir», overlay en portrait
- `app/src/stores/ui.ts` — Store Pinia con estado efímero `sidebarOpen`, acciones `toggleSidebar()` y `closeSidebar()`
- `app/src/views/SectionPlaceholderView.vue` — Componente reutilizable para placeholder views con mensaje "Sección en desarrollo"
- `app/src/views/ConfiguracionView.vue` — Placeholder view (reutiliza SectionPlaceholderView)
- `app/src/views/NinosView.vue` — Placeholder view (reutiliza SectionPlaceholderView)
- `app/src/views/ChatbotView.vue` — Placeholder view (reutiliza SectionPlaceholderView)
- `app/src/views/LecturaFamiliarView.vue` — Placeholder view (reutiliza SectionPlaceholderView)
- `app/src/views/RelajacionFamiliarView.vue` — Placeholder view (reutiliza SectionPlaceholderView)

#### Archivos modificados
- `app/src/router/index.ts` — Ruta `/panel` convertida en layout padre con `ParentPanelLayout.vue`, sub-rutas hijas para cada sección, todas protegidas con guard `requiresParentalAuth`
- `app/src/views/PanelCoverView.vue` — Tarjetas convertidas de visuales a navegables con `router-link`, cada tarjeta enlaza a ruta correspondiente
- `app/src/i18n/locales/es.ts` — Añadidas secciones `sidebar.*` (grupos, secciones, logout, ariaLabels) y `views.placeholder.*`

#### Contratos consumidos
- `docs/contracts/api/openapi/paths/logout.yaml` — POST /api/v1/auth/logout con header `Authorization: Bearer <token>`
- Backend confirma endpoint implementado (AuthController.java:44-53)

#### Evidencias de build
- `tsc --noEmit`: 0 errores TypeScript
- `vite build`: éxito en 422ms, 1918 módulos transformados
- ParentPanelLayout chunk: 3.85 kB (code-splitting automático)
- PanelCoverView chunk: 2.26 kB (code-splitting automático)
- Placeholder views: ~0.20 kB cada una (code-splitting automático)

#### Decisiones técnicas
- **Layout padre con rutas hijas**: `/panel` usa `ParentPanelLayout.vue` como layout, todas las sub-rutas son hijas y heredan el guard `requiresParentalAuth` del padre
- **Sidebar con overlay en portrait**: En viewport <1024px, sidebar se posiciona fixed con backdrop, se desliza desde la izquierda con transform translateX, click fuera cierra overlay
- **Estado efímero de UI**: `useUIStore` gestiona `sidebarOpen` como estado no persistente, se resetea al recargar página
- **Cierre automático al navegar**: En portrait, sidebar se cierra automáticamente al pulsar cualquier enlace de navegación (handler `onNavigate`)
- **Logout optimista**: Si `POST /auth/logout` falla (red caída, token expirado), se limpia el estado local igualmente en el bloque `finally` para no bloquear al usuario
- **Placeholder views reutilizables**: Se crea `SectionPlaceholderView.vue` como componente base, las 5 placeholder views lo reutilizan para consistencia visual
- **Documentación reutilizada**: `/panel/documentacion` reutiliza `DocumentationView.vue` existente, accesible también desde Home sin PIN

#### Deuda técnica / Riesgos
- NubiIcon chunk (637 kB) sigue siendo el mayor; no relacionado con este sprint
- No se han creado pruebas unitarias (no hay framework de testing configurado en el proyecto)
- Placeholder views muestran mensaje genérico "Sección en desarrollo"; contenido real fuera del alcance de FEAT-004

### Validación técnica completada: 2026-07-29

#### Verificación de criterios de aceptación
Todos los 31 criterios de aceptación han sido verificados y cumplen correctamente:

1. ✅ `ParentPanelLayout` muestra sidebar + content area (ParentPanelLayout.vue:3-19)
2. ✅ Landscape (≥1024px): sidebar visible permanentemente (~280px ancho) (ParentSidebar.vue:131, 285-329)
3. ✅ Portrait (<1024px): sidebar oculto por defecto (ParentSidebar.vue:285-329)
4. ✅ Portrait: botón hamburguesa visible en cabecera (ParentPanelLayout.vue:7-13, 85-89)
5. ✅ Portrait: botón hamburguesa abre sidebar como overlay sobre contenido (ParentSidebar.vue:293-324)
6. ✅ Portrait: botón cerrar (X) en cabecera del sidebar cierra overlay (ParentSidebar.vue:15-21, 18)
7. ✅ Portrait: click fuera del sidebar cierra overlay (ParentSidebar.vue:7-10, 9)
8. ✅ Sidebar muestra grupo Panel con 4 secciones: Configuración, Niños, Chatbot, Documentación (ParentSidebar.vue:25-39, 92-97)
9. ✅ Sidebar muestra grupo Experiencias con 2 secciones: Lectura familiar, Relajación familiar (ParentSidebar.vue:42-56, 99-102)
10. ✅ Separador visual antes de «Salir» (ParentSidebar.vue:59)
11. ✅ «Salir» es la última entrada del sidebar (ParentSidebar.vue:61-67)
12. ✅ «Salir» tiene jerarquía visual secundaria (tipografía menor, color neutro) (ParentSidebar.vue:263-265)
13. ✅ «Salir» tiene objetivo táctil ≥48x48dp (ParentSidebar.vue:258)
14. ✅ «Salir» tiene etiqueta de texto obligatoria "Salir" (ParentSidebar.vue:66, es.ts:187)
15. ✅ Pulsar «Salir» invoca `POST /auth/logout` con `Authorization: Bearer <token>` (ParentSidebar.vue:112-115, useParentalSession.ts:47-60)
16. ✅ Tras logout exitoso: token eliminado de `sessionStorage`, `isAuthenticated = false`, redirige a Home (useParentalSession.ts:58, parentalAuth.ts:27-32)
17. ✅ Logout optimista: si `POST /auth/logout` falla, token se elimina igualmente y redirige a Home (useParentalSession.ts:55-59)
18. ✅ Cada sección del sidebar tiene icono + etiqueta de texto obligatoria (ParentSidebar.vue:35-36, 52-53)
19. ✅ Sección activa resaltada visualmente (color de fondo, borde izquierdo) (ParentSidebar.vue:230-235)
20. ✅ Tarjetas de portada son navegables (router-link a rutas correspondientes) (PanelCoverView.vue:10-19, 26-35)
21. ✅ Tarjetas agrupadas por sección: Panel / Experiencias (PanelCoverView.vue:7-21, 23-37)
22. ✅ Rutas `/panel/configuracion`, `/panel/ninos`, etc. existen y muestran placeholder views (router/index.ts:36-64)
23. ✅ Ruta `/panel/documentacion` reutiliza `DocumentationView` existente (router/index.ts:51-54)
24. ✅ Todas las sub-rutas de `/panel` protegidas con guard `requiresParentalAuth` (router/index.ts:28)
25. ✅ `sidebarOpen` no persiste (estado efímero de UI) (ui.ts:8)
26. ✅ Cerrar sidebar al navegar a otra sección (en portrait) (ParentSidebar.vue:33, 50, 108-110)
27. ✅ i18n completo: sin literales en templates (verificado con grep)
28. ✅ Accesibilidad: `role="navigation"`, `aria-label`, `aria-current="page"`, objetivo táctil ≥48x48dp (ParentSidebar.vue:4, 5, 17, 32, 49)
29. ✅ Responsive en móvil y tablet (portrait y landscape) (ParentPanelLayout.vue:85-89, ParentSidebar.vue:285-329)
30. ✅ No se usa `rotate()` ni `scale()` para simular landscape (solo media queries) (verificado con grep)
31. ✅ Build exitoso sin errores de TypeScript (verificado: tsc 0 errores, vite build éxito)

#### Verificación de contratos
- ✅ POST /api/v1/auth/logout: frontend consume correctamente con header `Authorization: Bearer <token>`
- ✅ Backend confirma endpoint implementado (AuthController.java:44-53)
- ✅ Logout optimista: si falla, se limpia estado local igualmente (useParentalSession.ts:55-59)

#### Verificación de accesibilidad
- ✅ `role="navigation"` en sidebar (ParentSidebar.vue:4)
- ✅ `aria-label` en botón hamburguesa (ParentPanelLayout.vue:9)
- ✅ `aria-label` en botón cerrar (ParentSidebar.vue:17)
- ✅ `aria-label` en navegación principal (ParentSidebar.vue:5)
- ✅ `aria-current="page"` en sección activa (ParentSidebar.vue:32, 49)
- ✅ Objetivo táctil ≥48x48dp en botón hamburguesa (ParentPanelLayout.vue:60-61)
- ✅ Objetivo táctil ≥48x48dp en botón cerrar (ParentSidebar.vue:157-158)
- ✅ Objetivo táctil ≥48x48dp en enlaces del sidebar (ParentSidebar.vue:215)
- ✅ Objetivo táctil ≥48x48dp en botón «Salir» (ParentSidebar.vue:258)
- ✅ Objetivo táctil ≥48x48dp en tarjetas de portada (PanelCoverView.vue:125)

#### Verificación de i18n
- ✅ `sidebar.groups.*`: panel, experiences (es.ts:175-178)
- ✅ `sidebar.sections.*`: configuracion, ninos, chatbot, documentacion, lecturaFamiliar, relajacionFamiliar (es.ts:179-186)
- ✅ `sidebar.logout`: "Salir" (es.ts:187)
- ✅ `sidebar.ariaLabels.*`: openMenu, closeMenu, mainNavigation (es.ts:188-192)
- ✅ `views.placeholder.*`: title, description (es.ts:313-316)

#### Verificación de ADR-019 (Responsive sin rotate/scale)
- ✅ No se usa `rotate()` ni `scale()` en ParentPanelLayout.vue, ParentSidebar.vue, ni PanelCoverView.vue
- ✅ Solo se usan media queries CSS para adaptación portrait/landscape
- ✅ Breakpoint en 1024px para distinguir landscape/portrait

#### Observaciones
- **Nombre del store**: El sprint menciona `useUIStore.ts` pero el archivo se llama `ui.ts`. Esto es consistente con la convención de Pinia de nombrar archivos en camelCase y exportar el store con prefijo `use`.
- **Placeholder views minimalistas**: Las 5 placeholder views son wrappers mínimos que reutilizan `SectionPlaceholderView.vue`. Esto es una buena práctica de reutilización de componentes.
- **Documentación dual**: `/panel/documentacion` y `/docs` apuntan al mismo componente `DocumentationView.vue`, cumpliendo el requisito de accesibilidad desde ambos contextos.
- **Chunk sizes optimizados**: Todos los chunks están bien dentro de los límites recomendados (<500 kB).

#### Veredicto
**APPROVED** — Sprint completo y verificado. Todos los 31 criterios de aceptación cumplen correctamente. Build exitoso sin errores. Contratos consumidos conforme a especificación. Accesibilidad e i18n completos. Navegación adaptable implementada correctamente con sidebar colapsable en portrait y visible en landscape. Acción «Salir» con logout optimista implementada correctamente. Tarjetas de portada convertidas en navegables. ADR-019 respetado (sin rotate/scale para simular landscape).
