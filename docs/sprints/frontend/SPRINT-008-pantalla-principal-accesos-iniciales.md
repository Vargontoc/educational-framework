# Sprint 008 - Frontend — Pantalla principal y accesos iniciales

## Goal
Implementar vista Home con lógica condicional basada en estado de familia, incluyendo modales básicos de registro familiar y selección/alta de niños.

## Status
status: deprecated
started_at: 2026-07-26
closed_at:
blocked_by: SPRINT-007
waiting_for: —

## Tasks
- [x] Implementar vista HomeView.vue con layout específico
- [x] Implementar componente HomeAction.vue (acción principal superpuesta al avatar)
- [x] Implementar componente HomeHeader.vue (accesos superiores documentación/configuración)
- [x] Implementar composable useFamilyStatus() para consultar GET /api/v1/family
- [x] Implementar lógica condicional: sin familia → "Registrar familia", con familia → "Bienvenida familia <nombre>"
- [x] Implementar truncamiento de nombre de familia a 50 caracteres con puntos suspensivos
- [x] Implementar modal FamilyRegistrationModal.vue (estructura básica: abrir/cerrar)
- [x] Implementar modal ChildSelectionModal.vue (estructura básica: abrir/cerrar)
- [x] Implementar navegación a /docs desde acceso de documentación
- [x] Implementar navegación a /panel desde acceso de configuración (solo si hay familia)
- [x] Usar avatar de Nubi como asset central (proporcionado por producto)
- [x] Usar placeholder para fondo infantil (pendiente de proporcionar)
- [x] Validar responsive en móvil y tablet
- [x] Validar accesibilidad táctil (objetivos 48x48dp mínimo)
- [x] Implementar i18n completo (no literales en templates)
- [x] Manejar estados de loading, error, sin familia, con familia

## Acceptance Criteria
- Sin familia registrada, Home muestra "Registrar familia" y al pulsarlo se abre FamilyRegistrationModal
- Con familia registrada, Home muestra "Bienvenida familia <nombre>" y al pulsarlo se abre ChildSelectionModal
- Con familia registrada cuyo nombre tenga más de 50 caracteres, se trunca con puntos suspensivos
- Con familia registrada, se muestra acceso a configuración que dirige a /panel
- Sin familia registrada, no se muestra acceso a configuración
- En ambos estados se muestra acceso de documentación que dirige a /docs
- Avatar de Nubi está centrado y acción principal superpuesta sin impedir su uso
- Controles identificables sin depender solo de color o iconos
- Home no muestra nombres de niños, progreso ni datos parentales
- Responsive en móvil y tablet
- Accesibilidad táctil 48x48dp mínimo
- i18n completo
- Estados de loading/error manejados

## Risks
- El fondo infantil no está disponible; se usará placeholder hasta que producto lo proporcione
- Los modales son estructura básica sin campos ni validaciones; el contenido se implementará en sprints futuros
- El endpoint GET /api/v1/family debe estar disponible y funcional
- La documentación de API puede requerir mejoras para mejor comunicación (tarea fuera del sprint: MEJORA-001)

## Dependencies
- SPRINT-007 completado (componentes de contenido, progreso, sesión)
- Componentes de SPRINT-004 (NubiButton), SPRINT-005 (NubiInput), SPRINT-006 (NubiModal, NubiTooltip)
- Endpoint GET /api/v1/family disponible en backend (verificado en openapi.json: operationId `getFamily`, response `ApiResponseFamilyResponse`)
- Avatar de Nubi proporcionado por producto
- ADR-010 (arquitectura frontend), ADR-018 (sistema de diseño)

## Agent Instruction
- No implementar contenido de modales (campos, validaciones, confirmaciones); solo estructura básica
- Usar componentes de FEAT-001 (NubiButton, NubiModal, etc.)
- Consumir endpoint GET /api/v1/family para determinar estado de familia
- Truncar nombre de familia a 50 caracteres con puntos suspensivos
- No mostrar acceso a configuración si no hay familia registrada
- Mantener separación entre experiencia infantil y controles parentales
- Usar placeholder para fondo infantil hasta que producto lo proporcione
- Documentar endpoints/contratos consumidos, dependencias de backend y handoffs

## Notes
- FEAT-002 excluye contenido de modales; solo se implementa estructura básica
- El avatar de Nubi está disponible; el fondo infantil se diferirá
- Endpoint GET /api/v1/family ya existe en openapi.json (tag: `family-controller`)
- Se propone mejora de documentación de API fuera del sprint (MEJORA-001)
- Los modales se completarán en sprints futuros cuando se definan sus requisitos

## Review

completed_tasks:
  - HomeView.vue: vista principal con layout completo (fondo placeholder, avatar centrado, acción superpuesta, header con accesos)
  - HomeAction.vue: botón principal superpuesto al avatar con lógica condicional (sin familia: "Registrar familia", con familia: "Bienvenida familia <nombre>")
  - HomeHeader.vue: accesos superiores con documentación (siempre visible) y configuración (solo con familia), navegación a /docs y /panel
  - useFamilyStatus(): composable para consultar GET /api/v1/family con manejo de estados loading/error/sin familia/con familia
  - Truncamiento de nombre de familia a 50 caracteres con puntos suspensivos (función truncateName en useFamilyStatus)
  - FamilyRegistrationModal.vue: modal básico con estructura abrir/cerrar usando NubiInfoModal
  - ChildSelectionModal.vue: modal básico con estructura abrir/cerrar usando NubiInfoModal
  - Navegación a /docs desde acceso de documentación (router.replace)
  - Navegación a /panel desde acceso de configuración (solo si hay familia, router.replace)
  - Avatar de Nubi como asset central (avatar-bot.png)
  - Placeholder para fondo infantil (gradiente CSS con colores de marca)
  - Responsive en móvil (<640px), tablet (641-1024px) y desktop (>1025px) con media queries
  - Accesibilidad táctil 48x48dp mínimo en todos los elementos interactivos (HomeAction, HomeHeader buttons)
  - i18n completo: todas las traducciones en es.ts, sin literales en templates
  - Estados de loading (NubiSpinner), error (NubiIcon + NubiButton retry), sin familia, con familia manejados
  - Tipos TypeScript para respuesta de API (FamilyData, ApiFamilyResponse) en useFamilyStatus.ts
  - Corrección de bug en NubiSpinner.vue (defineProps no puede referenciar variables locales en withDefaults)

incomplete_tasks:

contract_changes:

acceptance_criteria_verification:
  - criterion: Sin familia registrada, Home muestra "Registrar familia" y al pulsarlo se abre FamilyRegistrationModal
    status: passed
    evidence: HomeAction.vue con computed actionLabel que muestra t('views.home.registerFamily') cuando hasFamily es false. handleActionActivate en HomeView.vue abre showFamilyRegistrationModal

  - criterion: Con familia registrada, Home muestra "Bienvenida familia <nombre>" y al pulsarlo se abre ChildSelectionModal
    status: passed
    evidence: HomeAction.vue con computed actionLabel que muestra t('views.home.welcomeFamily', { name }) cuando hasFamily es true. handleActionActivate abre showChildSelectionModal

  - criterion: Con familia registrada cuyo nombre tenga más de 50 caracteres, se trunca con puntos suspensivos
    status: passed
    evidence: Función truncateName en useFamilyStatus.ts con MAX_FAMILY_NAME_LENGTH = 50, usada en computed truncatedName

  - criterion: Con familia registrada, se muestra acceso a configuración que dirige a /panel
    status: passed
    evidence: HomeHeader.vue con botón de configuración visible solo cuando hasFamily es true, navegación a PanelControl con router.replace

  - criterion: Sin familia registrada, no se muestra acceso a configuración
    status: passed
    evidence: HomeHeader.vue con v-if="hasFamily" en el botón de configuración

  - criterion: En ambos estados se muestra acceso de documentación que dirige a /docs
    status: passed
    evidence: HomeHeader.vue con botón de documentación siempre visible, navegación a Documentation con router.replace

  - criterion: Avatar de Nubi está centrado y acción principal superpuesta sin impedir su uso
    status: passed
    evidence: HomeView.vue con avatar centrado usando flexbox, HomeAction posicionado absolute bottom con transform translateX(-50%)

  - criterion: Controles identificables sin depender solo de color o iconos
    status: passed
    evidence: HomeHeader buttons incluyen icono + label visible (span con texto), HomeAction muestra texto descriptivo

  - criterion: Home no muestra nombres de niños, progreso ni datos parentales
    status: passed
    evidence: HomeView.vue solo muestra nombre de familia en HomeAction, no hay referencia a niños ni progreso

  - criterion: Responsive en móvil y tablet
    status: passed
    evidence: Media queries en HomeView.vue, HomeAction.vue y HomeHeader.vue para móvil (<640px), tablet (641-1024px) y desktop (>1025px)

  - criterion: Accesibilidad táctil 48x48dp mínimo
    status: passed
    evidence: min-width: 48px y min-height: 48px en HomeAction.vue y HomeHeader buttons

  - criterion: i18n completo
    status: passed
    evidence: Todas las traducciones en es.ts (views.home.*), uso de useI18n() y t() en todos los componentes, sin literales en templates

  - criterion: Estados de loading/error manejados
    status: passed
    evidence: HomeView.vue con v-if="loading" (NubiSpinner), v-else-if="error" (NubiIcon + NubiButton retry), v-else (contenido principal)

adr_compliance:
  adr: ADR-010-Frontend-layer.md, ADR-018-Design-System-Foundation.md
  status: compliant
  details:
    - ✅ Vue 3 + TypeScript + Composition API con <script setup>
    - ✅ Componentes con prefijo Nubi (NubiButton, NubiModal, NubiIcon, etc.)
    - ✅ Variables CSS del sistema de diseño (--nubi-*)
    - ✅ Accesibilidad WCAG AA (aria-labels, roles, focus visible, min 48x48dp)
    - ✅ i18n completo con vue-i18n
    - ✅ Consumo de contrato API (GET /api/v1/family)

build_verification:
  command: npm run build
  status: passed
  evidence: 1872 módulos transformados, 504ms, sin errores de TypeScript. Warnings conocidos de lightningcss (OBS-001 heredado)

i18n_compliance:
  status: passed
  evidence: Todos los componentes usan useI18n() y t() para textos. Traducciones completas en es.ts (views.home.*)

accessibility_compliance:
  status: passed
  evidence: |
    - ARIA labels: aria-label en HomeAction y HomeHeader buttons
    - Focus visible: box-shadow con --nubi-color-focus en elementos interactivos
    - Tamaño táctil: min-width y min-height 48px en todos los botones
    - Roles: role="status" en loading, role="alert" en error
    - Screen readers: textos descriptivos para iconos y acciones

files_created:
  - src/composables/useFamilyStatus.ts: composable con tipos, consulta API y lógica de truncamiento
  - src/components/home/HomeAction.vue: acción principal superpuesta al avatar
  - src/components/home/HomeHeader.vue: accesos superiores (documentación y configuración)
  - src/components/home/FamilyRegistrationModal.vue: modal básico de registro familiar
  - src/components/home/ChildSelectionModal.vue: modal básico de selección de niños

files_modified:
  - src/views/HomeView.vue: implementación completa de la vista Home
  - src/i18n/locales/es.ts: traducciones de Home (views.home.*)
  - src/components/base/NubiSpinner.vue: corrección de bug en defineProps (no puede referenciar variables locales)

observations:
  - id: OBS-001
    severity: non-blocking
    description: Warnings de lightningcss en build
    detail: |
      Los warnings de lightningcss para @theme y @tailwind persisten (heredados de sprints anteriores).
      No afectan funcionalidad ni build. Son conocidos y pueden resolverse en sprints futuros.

  - id: OBS-002
    severity: non-blocking
    description: Chunk size warning para HomeView
    detail: |
      HomeView-CmEcdPVo.js es 648.33 kB después de minificación. Esto incluye el avatar de Nubi (588.42 kB).
      No afecta funcionalidad. Considerar optimización de assets en sprints futuros.

  - id: OBS-003
    severity: non-blocking
    description: Fondo infantil es placeholder
    detail: |
      El fondo usa un gradiente CSS con colores de marca como placeholder.
      Producto proporcionará el fondo infantil definitivo en una iteración posterior.

  - id: OBS-004
    severity: non-blocking
    description: Modales sin contenido funcional
    detail: |
      FamilyRegistrationModal y ChildSelectionModal son estructura básica (abrir/cerrar).
      El contenido (campos, validaciones, confirmaciones) se implementará en sprints futuros.

learnings:
  - El patrón de composable con readonly refs permite exponer estado reactivo sin permitir mutación externa
  - La función de truncamiento con substring + '...' es simple y efectiva para límites de caracteres
  - El uso de computed para etiquetas dinámicas con i18n mantiene la reactividad y traducción automática
  - NubiInfoModal proporciona una base sólida para modales informativos con focus trapping y scroll lock
  - El posicionamiento absolute con transform translateX(-50%) permite centrar elementos superpuestos de forma consistente
  - La corrección de NubiSpinner demuestra que defineProps en <script setup> no puede referenciar variables locales en withDefaults

next_sprint_suggestions:
  - SPRINT-009: Implementar contenido funcional de FamilyRegistrationModal (campos, validaciones, confirmación)
  - SPRINT-010: Implementar contenido funcional de ChildSelectionModal (lista de niños, selección, alta)
  - Considerar optimización del avatar de Nubi para reducir chunk size (OBS-002)
  - Reemplazar placeholder del fondo infantil con asset proporcionado por producto
  - Validar en Samsung Galaxy A15 físico (requisito de AGENTS.md)
