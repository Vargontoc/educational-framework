# Sprint 008 - Frontend — Pantalla principal y accesos iniciales

## Goal
Implementar vista Home con lógica condicional basada en estado de familia, incluyendo modales básicos de registro familiar y selección/alta de niños.

## Status
status: pending
started_at:
closed_at:
blocked_by: SPRINT-007
waiting_for: —

## Tasks
- [ ] Implementar vista HomeView.vue con layout específico
- [ ] Implementar componente HomeAction.vue (acción principal superpuesta al avatar)
- [ ] Implementar componente HomeHeader.vue (accesos superiores documentación/configuración)
- [ ] Implementar composable useFamilyStatus() para consultar GET /api/v1/family
- [ ] Implementar lógica condicional: sin familia → "Registrar familia", con familia → "Bienvenida familia <nombre>"
- [ ] Implementar truncamiento de nombre de familia a 50 caracteres con puntos suspensivos
- [ ] Implementar modal FamilyRegistrationModal.vue (estructura básica: abrir/cerrar)
- [ ] Implementar modal ChildSelectionModal.vue (estructura básica: abrir/cerrar)
- [ ] Implementar navegación a /docs desde acceso de documentación
- [ ] Implementar navegación a /panel desde acceso de configuración (solo si hay familia)
- [ ] Usar avatar de Nubi como asset central (proporcionado por producto)
- [ ] Usar placeholder para fondo infantil (pendiente de proporcionar)
- [ ] Validar responsive en móvil y tablet
- [ ] Validar accesibilidad táctil (objetivos 48x48dp mínimo)
- [ ] Implementar i18n completo (no literales en templates)
- [ ] Manejar estados de loading, error, sin familia, con familia

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
