# SPRINT-033 — Formulario de contacto público

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-08-03
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-031 (verificado y cerrado), SPRINT-078-backend (endpoint POST /contact disponible), SPRINT-001-contenido (avisos y textos de contacto)
- **Impacto estimado:** Nueva sección Contacto con textarea, aviso de privacidad, confirmación de persona adulta responsable, información de finalidad, envío a backend y estados de feedback (loading, success, error). Nuevo componente base `NubiTextarea`. Contratos OpenAPI para `POST /contact`.

## Objetivo

Implementar la sección Contacto dentro del layout de documentación con:
- Componente base `NubiTextarea.vue` para el campo de texto.
- Vista `ContactView.vue` con formulario completo.
- Aviso de privacidad bloqueante (no incluir datos de menores, nombres, PIN).
- Confirmación de persona adulta responsable (checkbox obligatorio).
- Información de finalidad del mensaje.
- Servicio `contactService.ts` que consume `POST /api/v1/contact`.
- Composable `useContactForm.ts` con estado y validaciones.
- Estados de feedback: loading, success, error (genérico, sin exponer datos).
- Contratos OpenAPI para el endpoint de contacto.

## Contexto

**FEAT-007** requisitos para Contacto:
- Textarea para comentarios, sugerencias o errores.
- Sin adjuntos de imágenes, audio, vídeo ni archivos.
- Sin datos personales como campos obligatorios.
- Aviso claro que prohíba incluir datos de menores, nombres, PIN o información privada.
- Confirmación de persona adulta responsable.
- Aceptación informada de la finalidad del mensaje.
- Estados de éxito/error comprensibles sin exponer datos de terceros.

**SPRINT-031** ya implementó el layout de documentación con sidebar. La sección Contacto será una ruta más dentro de ese layout.

**Decisión confirmada:**
- Longitud máxima del textarea: 2000 caracteres.
- Rate limiting: mensaje genérico "Inténtalo más tarde" (no expone infraestructura).

## Diseño funcional-técnico

### 1. Componente base `NubiTextarea.vue`

**Archivo:** `framework/frontend/app/src/components/base/NubiTextarea.vue` (nuevo)

**Responsabilidad:** Componente base textarea siguiendo las convenciones de `NubiTextInput.vue` pero renderizando `<textarea>` en lugar de `<input>`.

**Props:**
```typescript
interface Props {
  modelValue: string
  label: string
  placeholder?: string
  maxLength?: number
  error?: string
  disabled?: boolean
  rows?: number
  required?: boolean
  descriptionId?: string
}
```

**Emits:**
```typescript
interface Emits {
  (e: 'update:modelValue', value: string): void
}
```

**Estructura template:**
```vue
<template>
  <div class="nubi-textarea" :class="{ 'nubi-textarea--error': error, 'nubi-textarea--disabled': disabled }">
    <label :for="inputId" class="nubi-textarea__label">
      {{ label }}
      <span v-if="required" class="nubi-textarea__required" aria-hidden="true">*</span>
    </label>
    <textarea
      :id="inputId"
      class="nubi-textarea__input"
      :value="modelValue"
      :placeholder="placeholder"
      :maxlength="maxLength"
      :disabled="disabled"
      :rows="rows"
      :required="required"
      :aria-invalid="!!error"
      :aria-describedby="descriptionId"
      @input="$emit('update:modelValue', ($event.target as HTMLTextAreaElement).value)"
    />
    <div v-if="maxLength" class="nubi-textarea__counter">
      {{ modelValue.length }} / {{ maxLength }}
    </div>
    <p v-if="error" class="nubi-textarea__error" role="alert">{{ error }}</p>
  </div>
</template>
```

**Accesibilidad:**
- `<label>` visible asociado al textarea.
- `aria-invalid` cuando hay error.
- `aria-describedby` para descripción o aviso.
- `role="alert"` en el mensaje de error.
- Objetivo táctil ≥ 48×48dp.

### 2. Vista `ContactView.vue`

**Archivo:** `framework/frontend/app/src/views/documentation/ContactView.vue` (nuevo)

**Responsabilidad:** Formulario de contacto completo con aviso de privacidad, confirmación adulta, información de finalidad, textarea y estados de feedback.

**Estructura visual:**
```
┌─────────────────────────────────────────────────┐
│  Contacto                                        │
│                                                  │
│  [Aviso de privacidad - NubiAlert]               │
│  "No incluyas datos de menores, nombres, PIN    │
│   ni otra información privada."                  │
│                                                  │
│  [Textarea - NubiTextarea]                       │
│  "Escribe tu comentario, sugerencia o error"     │
│                                                  │
│  [Información de finalidad]                      │
│  "Tu mensaje será recibido por el equipo de      │
│   My Friend Nubi para atender tu consulta.       │
│   No se utiliza para publicidad, perfilado       │
│   ni entrenamiento de IA."                       │
│                                                  │
│  [Checkbox - NubiCheckbox]                       │
│  "Soy persona adulta responsable y acepto        │
│   la información sobre el uso de mi mensaje."    │
│                                                  │
│  [Botón enviar - NubiButton]                     │
│                                                  │
│  [Estado: loading / success / error]             │
└─────────────────────────────────────────────────┘
```

**Flujo de interacción:**
1. Usuario ve el aviso de privacidad (siempre visible).
2. Usuario escribe en el textarea.
3. Usuario lee la información de finalidad.
4. Usuario marca el checkbox de confirmación adulta.
5. Botón "Enviar" se habilita (textarea no vacío + checkbox marcado).
6. Usuario pulsa "Enviar" → estado loading.
7. Backend responde:
   - 202 → estado success con mensaje de confirmación.
   - 400 → estado error con mensaje de validación.
   - 429 → estado error con mensaje genérico "Inténtalo más tarde".
   - Otro → estado error con mensaje genérico.

### 3. Composable `useContactForm.ts`

**Archivo:** `framework/frontend/app/src/composables/useContactForm.ts` (nuevo)

**Responsabilidad:** Estado del formulario, validaciones y lógica de envío.

**Interfaz:**
```typescript
export function useContactForm() {
  const message = ref('')
  const isAdultConfirmed = ref(false)
  const isSubmitting = ref(false)
  const submitError = ref<string | null>(null)
  const submitSuccess = ref(false)

  const isValid = computed(() =>
    message.value.trim().length > 0 &&
    message.value.length <= 2000 &&
    isAdultConfirmed.value
  )

  const messageError = computed(() => {
    if (message.value.length > 2000) return 'El mensaje es demasiado largo (máximo 2000 caracteres)'
    return null
  })

  async function submit(): Promise<void> {
    if (!isValid.value) return
    isSubmitting.value = true
    submitError.value = null
    submitSuccess.value = false

    try {
      await contactService.sendMessage(message.value.trim())
      submitSuccess.value = true
      message.value = ''
      isAdultConfirmed.value = false
    } catch (error) {
      if (error instanceof HttpError && error.status === 429) {
        submitError.value = 'Has realizado demasiados intentos. Inténtalo más tarde.'
      } else if (error instanceof HttpError && error.status === 400) {
        submitError.value = 'El mensaje no es válido. Revisa el contenido e inténtalo de nuevo.'
      } else {
        submitError.value = 'No se ha podido enviar el mensaje. Inténtalo más tarde.'
      }
    } finally {
      isSubmitting.value = false
    }
  }

  return {
    message,
    isAdultConfirmed,
    isSubmitting,
    submitError,
    submitSuccess,
    isValid,
    messageError,
    submit
  }
}
```

### 4. Servicio `contactService.ts`

**Archivo:** `framework/frontend/app/src/services/contactService.ts` (nuevo)

**Responsabilidad:** Consumir `POST /api/v1/contact` via `apiClient`.

**Interfaz:**
```typescript
import { apiClient } from './apiClient'

export const contactService = {
  async sendMessage(message: string): Promise<ContactResponse> {
    return apiClient.post<ContactResponse>('/api/v1/contact', { message })
  }
}

interface ContactResponse {
  status: 'received'
  timestamp: string
}
```

### 5. Contratos OpenAPI

**Archivos:**
- `docs/contracts/api/openapi/paths/contact/post-contact.yaml` (nuevo)
- `docs/contracts/api/openapi/schemas/contact/contact-request.yaml` (nuevo)
- `docs/contracts/api/openapi/schemas/contact/contact-response.yaml` (nuevo)

Ya creados en este mismo análisis técnico.

### 6. i18n — Claves nuevas

**Archivo:** `framework/frontend/app/src/i18n/locales/es.ts` (modificación)

```typescript
views: {
  docs: {
    // ... claves existentes
    contact: {
      title: 'Contacto',
      textareaLabel: 'Escribe tu comentario, sugerencia o error',
      textareaPlaceholder: 'Tu mensaje...',
      maxLengthError: 'El mensaje es demasiado largo (máximo 2000 caracteres)',
      privacyNotice: 'No incluyas datos de menores, nombres, PIN ni otra información privada.',
      purposeInfo: 'Tu mensaje será recibido por el equipo de My Friend Nubi para atender tu consulta. No se utiliza para publicidad, perfilado ni entrenamiento de IA.',
      adultConfirmation: 'Soy persona adulta responsable y acepto la información sobre el uso de mi mensaje.',
      sendButton: 'Enviar',
      sending: 'Enviando...',
      successMessage: 'Tu mensaje ha sido recibido. Gracias por contactar con nosotros.',
      errorGeneric: 'No se ha podido enviar el mensaje. Inténtalo más tarde.',
      errorValidation: 'El mensaje no es válido. Revisa el contenido e inténtalo de nuevo.',
      errorRateLimit: 'Has realizado demasiados intentos. Inténtalo más tarde.'
    }
  }
}
```

## Contratos y dependencias externas

### Contratos

- **POST /api/v1/contact**: ya definido en `docs/contracts/api/openapi/paths/contact/post-contact.yaml`.
- **contact-request.yaml**: schema del request body.
- **contact-response.yaml**: schema del response body.

### Dependencias externas

| Capa | Dependencia | Estado |
|------|-------------|--------|
| Backend | SPRINT-078-backend: endpoint `POST /api/v1/contact` disponible. | ⏳ Pendiente |
| Agents | Ninguna. | ✅ Sin dependencia |
| TTS | Ninguna. | ✅ Sin dependencia |
| Frontend | SPRINT-031 verificado (layout base). | ⏳ Pendiente |
| Contenido | SPRINT-001-contenido: textos de aviso, finalidad y confirmación. | ⏳ Pendiente |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Usuario ignora aviso e incluye datos sensibles. | ALTA | Aviso claro bloqueante + confirmación adulta obligatoria. Backend debe aplicar sanitización adicional. |
| R2 | Endpoint público sin rate limiting → abuso. | MEDIA | Backend implementa rate limiting. Frontend muestra estado 429 con mensaje genérico. |
| R3 | `NubiTextarea` no existe y hay que crearlo desde cero. | BAJA | Se crea siguiendo convenciones de `NubiTextInput`. |
| R4 | El formulario puede no ser accesible en móvil. | BAJA | Objetivo táctil ≥ 48dp, label visible, aria-describedby para avisos. |

---

## Tareas del sprint

### Tarea 33.1: Crear contratos OpenAPI para POST /contact

**Descripción:** Verificar que los contratos OpenAPI ya creados son correctos y registrar el endpoint en `openapi.yaml`.

**Archivos:**
- `docs/contracts/api/openapi/paths/contact/post-contact.yaml` (verificar)
- `docs/contracts/api/openapi/schemas/contact/contact-request.yaml` (verificar)
- `docs/contracts/api/openapi/schemas/contact/contact-response.yaml` (verificar)
- `docs/contracts/api/openapi/openapi.yaml` (modificar: añadir referencia a `/contact`)

**Criterios de aceptación:**
- Los 3 archivos YAML existen y son válidos.
- `openapi.yaml` incluye la referencia `paths: /contact: post: $ref: "./paths/contact/post-contact.yaml"`.
- Schema request: `message` string, minLength 1, maxLength 2000, required.
- Schema response: `status` enum [received], `timestamp` date-time.

---

### Tarea 33.2: Implementar `NubiTextarea.vue`

**Descripción:** Componente base textarea siguiendo convenciones de `NubiTextInput.vue`.

**Archivo:** `framework/frontend/app/src/components/base/NubiTextarea.vue` (nuevo)

**Especificación completa:** Ver sección 1 del diseño funcional-técnico.

**Criterios de aceptación:**
- Renderiza `<textarea>` con `v-model`.
- Label visible asociado al textarea.
- Contador de caracteres si `maxLength` está definido.
- Mensaje de error con `role="alert"`.
- `aria-invalid` cuando hay error.
- `aria-describedby` para descripción.
- Estado disabled visual y funcional.
- Objetivo táctil ≥ 48×48dp.
- TypeScript compila sin errores.

---

### Tarea 33.3: Crear historia `NubiTextarea.story.vue`

**Descripción:** Historia para el catálogo de componentes (Histoire) con los estados de `NubiTextarea`.

**Archivo:** `framework/frontend/app/src/components/base/NubiTextarea.story.vue` (nuevo)

**Variantes:**
- Default (vacío)
- Con texto
- Con placeholder
- Con error
- Disabled
- Con contador de caracteres
- Con texto largo (cerca del límite)

**Criterios de aceptación:**
- Historia visible en Histoire (`/dev/components`).
- Todas las variantes renderizan correctamente.
- TypeScript compila sin errores.

---

### Tarea 33.4: Implementar `contactService.ts`

**Descripción:** Servicio que consume `POST /api/v1/contact` via `apiClient`.

**Archivo:** `framework/frontend/app/src/services/contactService.ts` (nuevo)

**Especificación completa:** Ver sección 4 del diseño funcional-técnico.

**Criterios de aceptación:**
- Método `sendMessage(message: string)` que hace POST a `/api/v1/contact`.
- Request body: `{ message: string }`.
- Response tipada como `ContactResponse`.
- Manejo de errores HTTP (400, 429, genérico).
- TypeScript compila sin errores.

---

### Tarea 33.5: Implementar `useContactForm.ts`

**Descripción:** Composable con estado del formulario, validaciones y lógica de envío.

**Archivo:** `framework/frontend/app/src/composables/useContactForm.ts` (nuevo)

**Especificación completa:** Ver sección 3 del diseño funcional-técnico.

**Criterios de aceptación:**
- `message` reactivo con v-model.
- `isAdultConfirmed` reactivo con checkbox.
- `isValid` computed: message no vacío + ≤ 2000 chars + adultConfirmed.
- `messageError` computed: mensaje si > 2000 chars.
- `submit()` ejecuta envío con estados loading/success/error.
- Errores diferenciados: 429 (rate limit), 400 (validación), genérico.
- Tras éxito, resetea formulario.
- TypeScript compila sin errores.

---

### Tarea 33.6: Implementar `ContactView.vue`

**Descripción:** Vista con formulario completo: aviso privacidad, textarea, finalidad, confirmación adulta, botón envío y estados de feedback.

**Archivo:** `framework/frontend/app/src/views/documentation/ContactView.vue` (nuevo)

**Especificación completa:** Ver sección 2 del diseño funcional-técnico.

**Criterios de aceptación:**
- Muestra título "Contacto".
- Aviso de privacidad con `NubiAlert` (siempre visible).
- Textarea con `NubiTextarea` (label, placeholder, maxLength 2000, contador).
- Información de finalidad visible.
- Checkbox de confirmación adulta con `NubiCheckbox`.
- Botón "Enviar" con `NubiButton`, disabled hasta validación completa.
- Estado loading: spinner en botón, texto "Enviando...".
- Estado success: mensaje de confirmación, formulario reseteado.
- Estado error: mensaje de error sin exponer datos.
- No ofrece controles para adjuntar archivos.
- No solicita datos personales como campos obligatorios.
- TypeScript compila sin errores.

---

### Tarea 33.7: Registrar ruta `/docs/contacto` en el router

**Descripción:** Añadir la ruta de Contacto dentro del layout de documentación.

**Archivo:** `framework/frontend/app/src/router/index.ts` (modificación)

**Cambio:** La ruta `/docs/:section` ya existe desde SPRINT-031. Verificar que `contacto` funciona como valor de `:section`. Si `DocSectionView.vue` no puede renderizar Contacto, añadir una ruta específica:

```typescript
{
  path: '/docs',
  component: () => import('../layouts/DocumentationLayout.vue'),
  children: [
    { path: '', redirect: '/docs/quien-soy' },
    {
      path: 'contacto',
      name: 'DocumentationContact',
      component: () => import('../views/documentation/ContactView.vue')
    },
    {
      path: ':section',
      name: 'DocumentationSection',
      component: () => import('../views/documentation/DocSectionView.vue')
    }
  ]
}
```

**Nota:** La ruta `contacto` debe ir antes de `:section` para que tenga prioridad.

**Criterios de aceptación:**
- `/docs/contacto` renderiza `ContactView.vue` dentro de `DocumentationLayout`.
- El sidebar de documentación marca "Contacto" como sección activa.
- TypeScript compila sin errores.

---

### Tarea 33.8: Actualizar i18n

**Descripción:** Añadir claves de i18n para Contacto.

**Archivo:** `framework/frontend/app/src/i18n/locales/es.ts` (modificación)

**Criterios de aceptación:**
- Claves `views.docs.contact.*` disponibles.
- Textos coinciden con los aprobados por Contenido (SPRINT-001-contenido).
- TypeScript compila sin errores.

---

### Tarea 33.9: Verificación de accesibilidad y responsive

**Descripción:** Verificar que el formulario es accesible y usable en móvil y tableta.

**Requisitos:**
1. Textarea es alcanzable por teclado (Tab).
2. Checkbox es alcanzable por teclado (Tab, Space).
3. Botón "Enviar" es alcanzable por teclado (Tab, Enter/Space).
4. Label del textarea es visible y está asociado.
5. Aviso de privacidad tiene `role="alert"` o es suficientemente visible.
6. Objetivo táctil ≥ 48×48dp en todos los controles.
7. Layout correcto en móvil (320px) y tableta (1024px).
8. Contador de caracteres es visible y actualiza en tiempo real.

**Criterios de aceptación:**
- Navegación por teclado completa (Tab recorre todos los controles).
- Label visible y asociado al textarea.
- Aviso de privacidad visible.
- Objetivo táctil ≥ 48dp.
- Layout correcto en 320px, 375px, 768px y 1024px.
- `vue-tsc --noEmit` sin errores.

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `docs/contracts/api/openapi/openapi.yaml` | Modificación (añadir /contact) |
| `framework/frontend/app/src/components/base/NubiTextarea.vue` | Nuevo archivo |
| `framework/frontend/app/src/components/base/NubiTextarea.story.vue` | Nuevo archivo |
| `framework/frontend/app/src/services/contactService.ts` | Nuevo archivo |
| `framework/frontend/app/src/composables/useContactForm.ts` | Nuevo archivo |
| `framework/frontend/app/src/views/documentation/ContactView.vue` | Nuevo archivo |
| `framework/frontend/app/src/router/index.ts` | Modificación (añadir ruta contacto) |
| `framework/frontend/app/src/i18n/locales/es.ts` | Modificación (añadir claves contact) |

## Estimación

- **Duración:** 3 días
- **Complejidad:** Media
- **Riesgo:** Medio (formulario con validaciones, estados, consumo de endpoint público)

## Criterios de aceptación del sprint

1. Contacto permite introducir texto en textarea y no ofrece controles para adjuntar archivos. *(FEAT-007 CA #8)*
2. Antes de enviar, se muestran: aviso de privacidad, confirmación de persona adulta e información de finalidad. *(CA #9)*
3. La pantalla de Contacto no solicita datos personales o infantiles como campos obligatorios. *(CA #10)*
4. El botón de envío está deshabilitado hasta que textarea tenga contenido Y checkbox adulta esté marcado. *(Validación)*
5. Ante éxito de envío, se muestra confirmación comprensible sin exponer datos de terceros. *(CA #11)*
6. Ante fallo de envío, se muestra aviso comprensible sin exponer contenido protegido. *(CA #11)*
7. La documentación no ofrece buscador, herramientas de edición ni actualización de contenido. *(CA #12)*
8. `NubiTextarea` es usable en móvil y tableta con objetivo táctil ≥ 48dp. *(Accesibilidad)*
9. El endpoint `POST /api/v1/contact` tiene contratos OpenAPI definidos. *(Contratos)*
10. `vue-tsc --noEmit` compila sin errores. *(Calidad)*

## Evidencias esperadas

- Test manual: abrir `/docs/contacto` → formulario visible con aviso, textarea, finalidad, checkbox, botón.
- Test manual: textarea vacío + checkbox sin marcar → botón "Enviar" disabled.
- Test manual: escribir texto + marcar checkbox → botón "Enviar" enabled.
- Test manual: escribir > 2000 caracteres → mensaje de error "demasiado largo".
- Test manual: pulsar "Enviar" → estado loading (spinner).
- Test manual: éxito → mensaje de confirmación, formulario reseteado.
- Test manual: error 429 → mensaje "Inténtalo más tarde".
- Test manual: error 400 → mensaje de validación.
- Test manual: error genérico → mensaje genérico.
- Test manual: verificar que no hay controles de adjuntar archivos.
- Test manual: verificar que no hay campos de datos personales.
- Test manual: Tab recorre todos los controles.
- Test manual: layout correcto en 320px y 1024px.
- `vue-tsc --noEmit` sin errores.

## Dependencias bloqueantes

- [ ] SPRINT-031 completado y verificado.
- [ ] SPRINT-078-backend: endpoint `POST /api/v1/contact` disponible.
- [ ] SPRINT-001-contenido: textos de aviso, finalidad y confirmación aprobados.

## Handoffs a otras capas

### Backend:
- SPRINT-078-backend debe implementar `POST /api/v1/contact` con rate limiting, sanitización y minimización de datos.

### Contenido:
- SPRINT-001-contenido debe proporcionar los textos definitivos del aviso de privacidad, finalidad y confirmación adulta.

### Agents/TTS:
- Sin dependencia.

## Notas adicionales

### Relación con otros sprints

- **Depende de:** SPRINT-031 (layout base), SPRINT-078-backend (endpoint), SPRINT-001-contenido (textos).
- **Independiente de:** SPRINT-032 (retorno contextual).

### Privacidad infantil

- El formulario no solicita datos personales ni de menores.
- El aviso de privacidad es bloqueante (visible antes de poder enviar).
- La confirmación adulta es obligatoria.
- Los mensajes no se comparten entre familias ni se usan para publicidad, perfilado o entrenamiento de IA.
- Backend debe aplicar sanitización y minimización de datos.
