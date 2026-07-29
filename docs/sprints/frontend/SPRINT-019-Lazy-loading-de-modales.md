# SPRINT-002 — Lazy loading de modales en HomeView

## Estado

- **Estado:** planificado
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** Ninguna (puede ejecutarse en paralelo con SPRINT-001)
- **Impacto estimado:** Reducción de ~500 KB-1 MB y ~20-30 requests

## Objetivo

Convertir los imports estáticos de los modales de HomeView en imports dinámicos con `defineAsyncComponent`, de modo que solo se carguen cuando el usuario los active.

## Problema actual

### Archivo: `framework/frontend/app/src/views/HomeView.vue` (líneas 82-89)

```typescript
import FamilyRegistrationModal from '../components/home/FamilyRegistrationModal.vue'
import ChildSelectionModal from '../components/home/ChildSelectionModal.vue'
import ParentalAuthModal from '../components/home/ParentalAuthModal.vue'
```

Los 3 modales se cargan en el bundle inicial aunque solo se usen bajo interacción del usuario:
- `FamilyRegistrationModal`: solo se abre al pulsar "Registrar familia"
- `ChildSelectionModal`: solo se abre al pulsar la bienvenida de familia
- `ParentalAuthModal`: solo se abre al pulsar el acceso a configuración parental

Cada modal importa múltiples componentes base (NubiInfoModal, NubiTextInput, NubiPinInput, NubiButton, NubiIcon, NubiSpinner, NubiGrid, NubiErrorState, NubiStepper, etc.), lo que genera una cascada de imports que infla el bundle inicial.

## Tareas

### Tarea 2.1: Convertir imports de modales a defineAsyncComponent

**Descripción:** Reemplazar los imports estáticos por `defineAsyncComponent` para carga bajo demanda.

**Archivo:** `framework/frontend/app/src/views/HomeView.vue`

**Cambio esperado:**
```typescript
// ANTES (líneas 82-89)
import FamilyRegistrationModal from '../components/home/FamilyRegistrationModal.vue'
import ChildSelectionModal from '../components/home/ChildSelectionModal.vue'
import ParentalAuthModal from '../components/home/ParentalAuthModal.vue'

// DESPUÉS
import { defineAsyncComponent } from 'vue'

const FamilyRegistrationModal = defineAsyncComponent({
  loader: () => import('../components/home/FamilyRegistrationModal.vue'),
  loadingComponent: NubiSpinner, // ya importado estáticamente
  delay: 200
})

const ChildSelectionModal = defineAsyncComponent({
  loader: () => import('../components/home/ChildSelectionModal.vue'),
  loadingComponent: NubiSpinner,
  delay: 200
})

const ParentalAuthModal = defineAsyncComponent({
  loader: () => import('../components/home/ParentalAuthModal.vue'),
  loadingComponent: NubiSpinner,
  delay: 200
})
```

**Nota:** `NubiSpinner` debe permanecer como import estático ya que se usa en el estado de carga de HomeView.

**Criterios de aceptación:**
- Los modales se cargan solo cuando se abren (v-model = true)
- Se muestra NubiSpinner mientras carga el modal (si tarda >200ms)
- El template no requiere cambios (los componentes se usan igual)
- No hay errores de TypeScript por el cambio de tipo

---

### Tarea 2.2: Verificar que los componentes base internos se resuelven correctamente

**Descripción:** Los modales cargados dinámicamente importan sus propios componentes base. Verificar que Vite los incluye en el chunk del modal y no en el bundle inicial.

**Componentes base usados por los modales:**

| Modal | Componentes base que importa |
|-------|------------------------------|
| FamilyRegistrationModal | NubiInfoModal, NubiTextInput, NubiPinInput, NubiButton, NubiIcon |
| ChildSelectionModal | NubiInfoModal, NubiGrid, NubiButton, NubiSpinner, NubiErrorState, NubiPinInput, NubiTextInput, NubiStepper, ChildProfileCard, AvatarSelector |
| ParentalAuthModal | NubiInfoModal, NubiPinInput, NubiButton |

**Criterios de aceptación:**
- Los componentes base de cada modal se incluyen en el chunk asíncrono del modal
- El bundle inicial no contiene el código de NubiInfoModal, NubiTextInput, NubiPinInput, NubiStepper, NubiGrid, NubiErrorState
- Cada modal funciona correctamente al abrirse

---

### Tarea 2.3: Pruebas de regresión funcional de modales

**Descripción:** Verificar que los 3 modales funcionan correctamente tras el cambio a carga dinámica.

**Escenarios a probar:**

**FamilyRegistrationModal:**
1. Pulsar "Registrar familia" → modal se abre
2. Introducir nombre de familia → continuar a paso 2
3. Crear PIN de 4 dígitos → confirmar PIN → familia registrada
4. Modal se cierra y HomeView muestra bienvenida
5. Cancelar en cualquier paso → modal se cierra

**ChildSelectionModal:**
1. Pulsar bienvenida de familia → modal se abre
2. Seleccionar perfil existente → navega a GameView
3. Pulsar "Registrar niño" → pide PIN parental
4. Introducir PIN correcto → formulario de registro
5. Completar registro → perfil aparece en la lista
6. Cancelar → modal se cierra

**ParentalAuthModal:**
1. Pulsar acceso a configuración → modal se abre
2. Introducir PIN correcto → navega a PanelCover
3. Introducir PIN incorrecto → muestra error
4. 3 intentos fallidos → cooldown activado
5. Cancelar → modal se cierra

**Criterios de aceptación:**
- Todos los flujos funcionan igual que antes del cambio
- No hay flashes de contenido sin estilo (FOUC) al abrir modales
- El estado de carga (NubiSpinner) se muestra correctamente si la carga tarda
- Los modales se pueden abrir y cerrar múltiples veces sin errores

---

### Tarea 2.4: Medir reducción de bundle

**Descripción:** Comparar el tamaño del bundle antes y después del cambio.

**Métricas a capturar:**
- Tamaño del chunk inicial (main/vendor)
- Número de requests iniciales
- Tiempo de carga en simulación 3G

**Herramientas:**
- `npm run build` → analizar `dist/` con `npx vite-bundle-visualizer` o similar
- DevTools Network con throttling 3G

**Criterios de aceptación:**
- El chunk inicial es al menos 500 KB menor
- Los chunks de modales se cargan solo al abrirlos
- No hay regresión en tiempo de interacción

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `framework/frontend/app/src/views/HomeView.vue` | Cambiar 3 imports a defineAsyncComponent |

## Estimación

- **Duración:** 0.5 días
- **Complejidad:** Baja
- **Riesgo:** Muy bajo (cambio mínimo, sin afectar lógica de negocio)

## Métricas esperadas

| Métrica | Antes | Después (esperado) |
|---------|-------|-------------------|
| Tamaño chunk inicial | Incluye 3 modales | ~500 KB-1 MB menos |
| Requests iniciales | 152 | ~120-130 |
| Tiempo de carga inicial | 18.31s | ~14-16s (solo este sprint) |

## Plan de rollback

Si se detectan problemas, revertir el commit del sprint. El cambio es completamente reversible.
