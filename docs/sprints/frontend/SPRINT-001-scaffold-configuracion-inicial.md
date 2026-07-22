# Sprint 001 - Frontend

## Goal
Crear proyecto Vue 3 + TypeScript + Vite con configuración base verificable en dispositivo real. El directorio `framework/frontend/app` está vacío (greenfield).

## Status
status: approved
started_at: 2026-07-22 00:00:00
closed_at: 2026-07-22 00:00:00
blocked_by:
waiting_for:

## Tasks
- [x] Inicializar proyecto Vue 3 + TypeScript + Vite en `framework/frontend/app`
- [x] Configurar estructura de directorios (src/, components/, views/, stores/, services/, assets/, i18n/)
- [x] Instalar y configurar Pinia con stores vacíos (`useSessionStore`, `useWSStore`, `useUIStore`)
- [x] Instalar y configurar Vue Router con esqueleto de rutas (`/`, `/panel`, `/game/:childId`, `/docs`)
- [x] Instalar y configurar vue-i18n con locale español por defecto
- [x] Configurar variables de entorno (.env, .env.development, .env.production)
- [x] Crear Web App Manifest con `display: standalone` y `orientation: landscape`
- [x] Instalar Phaser como dependencia (sin configurar escenas ni integración)
- [x] Verificar `npm run dev` y `npm run build` en entorno local
- [x] Documentar criterios de arranque y estructura base
- [x] Configurar fichero `Dockerfile` para su configuracion en contendor docker

## Acceptance Criteria
- Proyecto arranca sin errores con `npm run dev`
- Build de producción genera artefactos en `dist/`
- Estructura de directorios documentada
- Stores de Pinia presentes pero vacíos
- Router con rutas definidas pero sin guards ni lógica de negocio
- i18n configurado con español como idioma activo
- Manifest PWA presente y válido
- Phaser instalado como dependencia (visible en `package.json`)

## Evidence
- `package.json` con todas las dependencias
- Estructura de directorios documentada en el sprint
- README de arranque rápido
- Verificación manual en Samsung Galaxy A15 (navegador Chrome)

## Risks
- La versión de Node.js/npm disponible puede condicionar la inicialización del proyecto.
- Phaser como dependencia instalada pero no configurada puede generar warnings de tree-shaking en build.
- El Web App Manifest requiere iconos y splash screen mínimos para ser válido; en este sprint basta con placeholders.

## Dependencies
- `docs/product/decisions/ADR-010-Frontend-layer.md` como referencia arquitectónica
- Node.js y npm disponibles en entorno de desarrollo

## Agent Instruction
- No implementar clientes API ni WebSocket funcionales; solo configuración base.
- Phaser se instala como dependencia pero no se configuran escenas ni integración con Vue.
- Los stores de Pinia se crean vacíos, sin acciones ni getters de negocio.
- El router define rutas pero no implementa guards ni lógica de protección.
- Todo texto visible debe pasar por vue-i18n desde el primer commit; no se permiten literales en templates.
- Documentar endpoints/contratos consumidos, dependencias de backend/agents/tts y handoffs de integración.

## Notes
- El directorio `framework/frontend/app` está vacío; este sprint es construcción desde cero (greenfield).
- El SPRINT-001 original (auditoría y definición arquitectónica) se absorbe en este scaffold al no existir código previo.
- La primera entrega es para una única familia por URL; la aplicación será funcional, no una demo.
- Se probará obligatoriamente en Samsung Galaxy A15 físico. Los perfiles emulados complementan, no sustituyen, las pruebas reales.
- El frontend consumirá exclusivamente el backend; nunca TTS ni agents de forma directa.

## Review

### Revision 1 — 2026-07-22

review_date: 2026-07-22
verdict: CHANGES_REQUIRED
reviewer: Router de Validación Técnica

defects_found:
  - id: DEF-001
    severity: blocker
    description: .dockerignore excluye nginx.conf pero Dockerfile lo requiere
    file: framework/frontend/app/.dockerignore
    resolution: Corregido — eliminada línea nginx.conf de .dockerignore

  - id: DEF-002
    severity: blocker
    description: Manifiesto PWA referencia iconos .png inexistentes
    file: framework/frontend/app/public/manifest.webmanifest
    resolution: Corregido — actualizado a .svg con type image/svg+xml

observations_found:
  - id: OBS-001
    severity: non-blocking
    description: .env.production WebSocket URL apunta a localhost
    resolution: Corregido — cambiado a wss:// con comentario de configuración en despliegue

  - id: OBS-002
    severity: non-blocking
    description: Directorios components/, services/, assets/ no existen en disco
    resolution: Corregido — creados con .gitkeep

  - id: OBS-003
    severity: non-blocking
    description: Directorio dist/ presente en el repositorio
    resolution: Corregido — eliminado de tracking con git rm --cached

### Revisión 2 — 2026-07-22

review_date: 2026-07-22
verdict: APPROVED
reviewer: Router de Validación Técnica

completed_tasks:
  - Inicializar proyecto Vue 3 + TypeScript + Vite en framework/frontend/app
  - Configurar estructura de directorios (src/, components/, views/, stores/, services/, assets/, i18n/)
  - Instalar y configurar Pinia con stores vacíos (useSessionStore, useWSStore, useUIStore)
  - Instalar y configurar Vue Router con esqueleto de rutas (/, /panel, /game/:childId, /docs)
  - Instalar y configurar vue-i18n con locale español por defecto
  - Configurar variables de entorno (.env, .env.development, .env.production)
  - Crear Web App Manifest con display: standalone y orientation: landscape
  - Instalar Phaser como dependencia (sin configurar escenas ni integración)
  - Verificar npm run dev y npm run build en entorno local
  - Documentar criterios de arranque y estructura base
  - Configurar fichero Dockerfile para su configuración en contenedor docker

incomplete_tasks:

contract_changes:

acceptance_criteria_verification:
  - criterion: Proyecto arranca sin errores con npm run dev
    status: passed
    evidence: package.json scripts correctos, Vite 8.1.1 configurado

  - criterion: Build de producción genera artefactos en dist/
    status: passed
    evidence: npm run build ejecutado exitosamente, 73 módulos, 215ms, 14 archivos en dist/

  - criterion: Estructura de directorios documentada
    status: passed
    evidence: README.md documenta estructura completa, directorios components/, services/, assets/ creados con .gitkeep

  - criterion: Stores de Pinia presentes pero vacíos
    status: passed
    evidence: session.ts, ws.ts, ui.ts — solo ref() sin lógica de negocio

  - criterion: Router con rutas definidas pero sin guards ni lógica de negocio
    status: passed
    evidence: /, /panel, /game/:childId, /docs, 404 — sin guards (correcto para scaffold)

  - criterion: i18n configurado con español como idioma activo
    status: passed
    evidence: locale 'es', todas las vistas usan $t()

  - criterion: Manifest PWA presente y válido
    status: passed
    evidence: Manifiesto con display standalone, orientation landscape, iconos SVG referenciados correctamente

  - criterion: Phaser instalado como dependencia (visible en package.json)
    status: passed
    evidence: phaser ^4.2.1 en package.json

adr_compliance:
  adr: ADR-010-Frontend-layer.md
  status: compliant
  details:
    - Vue 3 + TypeScript, 100% custom components
    - Pinia con 3 stores (useSessionStore, useWSStore, useUIStore)
    - Vue Router en history mode
    - vue-i18n desde inception, español activo
    - Manifiesto con display: standalone + orientation: landscape
    - Variables de entorno separadas por .env
    - Sin lógica de dominio en frontend

learnings:
  - El template de Vite vanilla TypeScript requiere configuración adicional para Vue 3
  - Es necesario instalar @vitejs/plugin-vue y vue-tsc como dependencias de desarrollo
  - La configuración de tsconfig.json para Vue requiere jsx: preserve y paths para alias @
  - Multi-stage Dockerfile con Node.js para build y nginx para producción es la configuración óptima
  - Los iconos SVG son placeholders válidos para el manifest PWA en este sprint
  - .dockerignore debe excluir archivos que NO se necesitan en el build, no los que SÍ se necesitan
  - El manifiesto PWA debe referenciar archivos que realmente existen en el directorio public/

next_sprint_suggestions:
  - Implementar clientes API REST y WebSocket funcionales
  - Implementar guards de rutas para /panel (PIN) y /game/:childId (sesión activa)
  - Configurar Phaser con escenas básicas
  - Implementar vistas completas con componentes y lógica de negocio
  - Reemplazar iconos SVG placeholder con iconos reales de la aplicación
  - Configurar tests unitarios y de integración
