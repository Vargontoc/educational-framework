# SPRINT-036 — Interacción de chat: envío, streaming, atajo @ y transparencia de herramientas

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-08-11
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-034, SPRINT-035, SPRINT-080-backend (streaming + tool calls)
- **Impacto estimado:** completa la sección Chatbot con interacción real: caja de mensajes, input con atajo `@`, bloqueo durante streaming, renderizado de tokens y tool calls.

## Objetivo

- Caja de chat que renderiza `useWSStore.chatbotMessages` + el buffer de streaming en curso.
- Input (`NubiTextarea`, `maxLength` 4000) con atajo `@` para mencionar perfiles infantiles.
- Envío del mensaje a `/app/chatbot/send`; bloqueo del input desde el envío hasta `COMPLETE`/`ERROR`.
- Renderizado diferenciado de `TOOL_CALL_START`/`TOOL_CALL_RESULT` (transparencia del agente), sin mezclarlo con el texto conversacional.

## Contexto

FEAT-003 exige que el chatbot distinga hechos, síntesis y consejo, y que las respuestas no mezclen datos entre perfiles — la selección de perfil ante ambigüedad la resuelve el propio agente por texto (FEAT-003, escenario "Adulto que consulta un perfil concreto"), así que esta UI no necesita un selector de perfil dedicado, solo el atajo `@` para facilitar que el adulto nombre al niño correcto. SPRINT-080 (backend) especifica que las tool calls actuales son `getAllChilds` y `getChild`.

## Diseño funcional-técnico

### 1. Composable `useChatbotComposer.ts`

**Archivo:** `framework/frontend/app/src/composables/useChatbotComposer.ts` (nuevo)

```typescript
export function useChatbotComposer(profileNames: Ref<string[]>) {
  const draft = ref('')
  const mentionQuery = ref<string | null>(null)

  function onInput(value: string, cursorPos: number) {
    draft.value = value
    const match = /@(\w*)$/.exec(value.slice(0, cursorPos))
    mentionQuery.value = match ? match[1] : null
  }

  const mentionSuggestions = computed(() =>
    mentionQuery.value === null
      ? []
      : profileNames.value.filter(n => n.toLowerCase().startsWith(mentionQuery.value!.toLowerCase()))
  )

  function applyMention(name: string) {
    draft.value = draft.value.replace(/@(\w*)$/, `@${name} `)
    mentionQuery.value = null
  }

  return { draft, mentionSuggestions, onInput, applyMention }
}
```

### 2. Composable `useChatbotStream.ts` (envío + bloqueo)

**Archivo:** `framework/frontend/app/src/composables/useChatbotStream.ts` (nuevo)

```typescript
const STREAM_CLIENT_TIMEOUT_MS = 65000 // > 60s timeout de backend (SPRINT-080)

export function useChatbotStream() {
  const wsStore = useWSStore()
  const authStore = useParentalAuthStore()
  const { setPending, clearPending, isWaitingForChatbot } = useChatbotPendingResponse()
  let timeoutHandle: ReturnType<typeof setTimeout> | null = null

  function send(message: string) {
    if (isWaitingForChatbot.value) return
    setPending()
    wsStore.publishParentMessage('/app/chatbot/send', { message })
    timeoutHandle = setTimeout(() => {
      wsStore.chatbotError = 'No se ha recibido respuesta. Inténtalo de nuevo.'
      clearPending()
    }, STREAM_CLIENT_TIMEOUT_MS)
  }

  function clearTimeoutGuard() {
    if (timeoutHandle) clearTimeout(timeoutHandle)
    timeoutHandle = null
  }

  watch(() => wsStore.chatbotError, (err) => {
    if (err) { clearPending(); clearTimeoutGuard() }
  })

  watch(() => wsStore.chatbotMessages.length, () => {
    clearPending() // se dispara tras COMPLETE, que empuja un mensaje nuevo
    clearTimeoutGuard()
  })

  return { send, isWaitingForChatbot }
}
```

`useChatbotPendingResponse` ya existe y ya está integrado con `useInactivityTimer` en `ParentPanelLayout.vue` (pausa el temporizador de inactividad mientras se espera respuesta) — se reutiliza tal cual, sin tocar ese layout.

### 3. `ChatbotView.vue` — ensamblado final

```
┌───────────────────────────────────┐
│ [Lista de mensajes - scroll]       │
│  Usuario: ...                      │
│  Nubi: ... (+ chips de tool calls) │
│  Nubi: <buffer de streaming...>    │
├───────────────────────────────────┤
│ [Sugerencias @ si aplica]          │
│ [NubiTextarea] [Botón enviar]      │
└───────────────────────────────────┘
```

- Mensaje en construcción: se muestra `wsStore.chatbotStreamBuffer` como burbuja "en vivo" mientras `isWaitingForChatbot` es `true`.
- Tool calls: se agrupan por `conversationId` + orden de llegada y se muestran como chip `NubiBadge` con `toolCall.toolName` y, si `status === 'SUCCESS'`, `toolCall.summary`; nunca se mezclan con el texto del mensaje ni se muestra `parameters` (ver Riesgo R3).
- `NubiTextarea` deshabilitado (`disabled`) mientras `isWaitingForChatbot`.
- Botón enviar deshabilitado si `draft` vacío, `isWaitingForChatbot`, o `wsStore.parentChannelStatus !== 'connected'`.
- Tras `ERROR`: mostrar `wsStore.chatbotError` como `NubiAlert` bajo la caja de chat, sin bloquear reintento inmediato.

### 4. i18n — claves nuevas

`framework/frontend/app/src/i18n/locales/es.ts`:

```typescript
views: {
  chatbot: {
    title: 'Chatbot',
    inputPlaceholder: 'Escribe tu mensaje... usa @ para mencionar a un perfil',
    send: 'Enviar',
    emptyState: 'Aún no has hablado con Nubi. Escribe tu primer mensaje.',
    errorGeneric: 'Ha ocurrido un error. Puedes intentarlo de nuevo.',
    errorTimeout: 'No se ha recibido respuesta. Inténtalo de nuevo.',
    toolCall: {
      getAllChilds: 'Consultando los perfiles registrados...',
      getChild: 'Consultando el perfil de {name}...'
    }
  }
}
```

## Contratos y dependencias externas

| Capa | Dependencia | Estado |
|---|---|---|
| Backend | SPRINT-080-backend desplegado con `TOOL_CALL_START`/`TOOL_CALL_RESULT` emitidos con los nombres de campo ya corregidos | ⏳ Pendiente |
| Frontend | SPRINT-034 y SPRINT-035 verificados | ⏳ Pendiente |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|---|---|---|
| R1 | Input bloqueado indefinidamente si el backend nunca emite `COMPLETE`/`ERROR` (p. ej. caída del proceso del modelo) | ALTA | Timeout cliente de 65s (por encima del timeout de 60s del backend) que fuerza `clearPending()` y muestra error genérico si no ha llegado respuesta. |
| R2 | Doble envío si el usuario pulsa Enter y el botón simultáneamente | BAJA | `send()` es no-op si `isWaitingForChatbot` ya es `true`. |
| R3 | Tool calls con `parameters` conteniendo datos sensibles se muestran tal cual | MEDIA | Frontend no renderiza `parameters` en la UI (solo `toolName`/`summary`/`status`), coherente con SPRINT-080 ("frontend debe renderizar tool calls de forma clara pero no técnica"). |
| R4 | Atajo `@` con nombres duplicados entre perfiles | BAJA | Mostrar un identificador adicional (p. ej. edad) junto al nombre en las sugerencias si hay colisión (mejora opcional, no bloqueante). |

## Tareas del sprint

### Tarea 36.1: Implementar `useChatbotComposer.ts`

**Criterios:** detecta `@` seguido de texto en la posición del cursor; sugiere perfiles cuyo nombre empieza por la query; `applyMention` sustituye correctamente el fragmento `@query` por `@NombreCompleto `.

### Tarea 36.2: Implementar `useChatbotStream.ts`

**Criterios:** `send()` no hace nada si ya está pendiente; bloquea/desbloquea vía `useChatbotPendingResponse` existente; timeout cliente de 65s con mensaje de error si no llega respuesta (R1).

### Tarea 36.3: Ensamblar `ChatbotView.vue` final

**Criterios:** lista de mensajes con scroll; burbuja de streaming en vivo; chips de tool call diferenciados visualmente del texto; input con `maxLength` 4000 y contador; sugerencias `@` navegables por teclado (flechas + Enter); botón enviar deshabilitado según reglas (incluye estado de conexión).

### Tarea 36.4: Actualizar i18n

**Criterios:** todas las cadenas visibles pasan por `vue-i18n`, sin literales en template.

### Tarea 36.5: Verificación de accesibilidad y responsive

**Criterios:** navegación por teclado completa (incluida la lista de sugerencias `@`); objetivo táctil ≥48dp en botón enviar y sugerencias; layout correcto en 320px y 1024px; `aria-live="polite"` en la lista de mensajes para que los nuevos mensajes se anuncien sin interrumpir la lectura en curso.

## Archivos afectados

| Archivo | Tipo de cambio |
|---|---|
| `framework/frontend/app/src/composables/useChatbotComposer.ts` | Nuevo |
| `framework/frontend/app/src/composables/useChatbotStream.ts` | Nuevo |
| `framework/frontend/app/src/views/ChatbotView.vue` | Modificación |
| `framework/frontend/app/src/i18n/locales/es.ts` | Modificación |

## Estimación

- **Duración:** 3 días
- **Complejidad:** Alta
- **Riesgo:** Medio-Alto (depende de tool calls reales del backend para verificación completa)

## Criterios de aceptación del sprint

1. Escribir `@` muestra sugerencias de perfiles y seleccionarlas inserta el nombre en el texto.
2. Al enviar, el input queda bloqueado hasta `COMPLETE` o `ERROR` (o timeout cliente de 65s).
3. Los tokens (`TOKEN`) se muestran progresivamente en una burbuja "en vivo" que se sustituye por el contenido final de `COMPLETE`.
4. Las tool calls se muestran como elemento visual distinto del texto conversacional, sin exponer `parameters`.
5. Un `ERROR` desbloquea el input y muestra un mensaje claro, permitiendo reintentar.
6. Todo el texto visible pasa por i18n.
7. `vue-tsc --noEmit` sin errores.

## Evidencias esperadas

- Prueba manual end-to-end contra backend real: enviar mensaje, ver tokens en vivo, ver `COMPLETE`.
- Prueba manual: forzar un `ERROR` (p. ej. desconectando el modelo) y verificar desbloqueo + mensaje.
- Prueba manual: escribir `@` y verificar sugerencias + inserción.
- Prueba manual: pregunta que dispare `getChild` y verificar que se muestra el chip de tool call sin datos técnicos.
- Captura de navegación por teclado completa.

## Dependencias bloqueantes

- [ ] SPRINT-034 y SPRINT-035 verificados.
- [ ] SPRINT-080-backend desplegado en desarrollo con tool calls funcionando.

## Handoffs a otras capas

### Backend

- Confirmar el timeout real de streaming (60s, SPRINT-080) para mantener el timeout cliente (65s) siempre por encima.

### Contenido

- Validar los textos de `toolCall.getAllChilds`/`getChild` y el mensaje de estado vacío.

## Notas adicionales

### Privacidad infantil

- La UI nunca muestra `parameters` de tool calls (pueden incluir nombres de perfiles en bruto) ni datos crudos devueltos por las herramientas, solo `summary` ya filtrado por backend.
- Coherente con FEAT-003: no se muestran comparativas, rankings ni diagnósticos — esto lo garantiza el contenido de la respuesta del agente, no la UI, pero la UI tampoco debe añadir visualizaciones que sugieran evaluación (p. ej. barras de progreso) sobre las respuestas del chatbot.
