# SPRINT-040 — Audio de lectura: altavoz, repetición y reproducción automática

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-08-27
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-039 (navegación de páginas), SPRINT-038 (sesión de lectura + precarga)
- **Impacto estimado:** Cierra el flujo de FEAT-008: reproducción automática del audio por página, altavoz independiente del toggle de portada, y control de repetición.

## Objetivo

Añadir la reproducción de audio por página sobre la navegación ya construida en SPRINT-039, con un altavoz que actúa como silencio temporal independiente del toggle de la vista previa, y un control de repetición, aprovechando que SPRINT-038 ya precargó todos los audios del cuento.

## Contexto

El valor inicial de narración (activado/desactivado) viene de `useStoryReadingSession().narrateEnabled`, decidido en la vista previa (SPRINT-038). El altavoz de esta pantalla es un **silencio temporal** que nunca modifica esa decisión original ni la de otros cuentos (FEAT-008 req 8, AC9). Al haberse precargado el cuento completo en SPRINT-038, la reproducción normalmente arranca desde caché del navegador, sin espera perceptible.

## Diseño funcional-técnico

### 1. `composables/useStoryPageAudio.ts` (nuevo)

```typescript
export function useStoryPageAudio(narrateEnabled: Ref<boolean>) {
  const audioEl = new Audio()
  const speakerMuted = ref(false)
  const isPlaying = ref(false)

  function playPage(page: StoryPage) {
    if (!narrateEnabled.value || speakerMuted.value) return
    audioEl.src = page.audioUrl
    audioEl.currentTime = 0
    audioEl.play()
    isPlaying.value = true
  }

  function replay(page: StoryPage) {
    // Disponible incluso si speakerMuted está activo (FEAT-008 AC10)
    audioEl.src = page.audioUrl
    audioEl.currentTime = 0
    audioEl.play()
    isPlaying.value = true
  }

  function toggleSpeaker() {
    speakerMuted.value = !speakerMuted.value
    if (speakerMuted.value) {
      audioEl.pause()
      isPlaying.value = false
    }
  }

  audioEl.addEventListener('ended', () => { isPlaying.value = false })
  onUnmounted(() => { audioEl.pause() })

  return { speakerMuted, isPlaying, playPage, replay, toggleSpeaker }
}
```

### 2. Integración en `PanelLecturaFamiliarLecturaView.vue`

- Si `useStoryReadingSession().narrateEnabled` es `false` → no se instancia `useStoryPageAudio` ni se muestra ningún control de voz en esta pantalla (FEAT-008 AC6, aplicado también aquí, no solo en la previa).
- `watch(currentPageIndex)` → `playPage(currentPage)` automáticamente cuando `narrateEnabled` es `true`.
- Botón de altavoz (icono distinto según `speakerMuted`, no solo color) y botón de "reproducir de nuevo" en `StoryReaderControls.vue` (SPRINT-039), visibles bajo las mismas reglas de `useTapControlsVisibility`.

### 3. Reutilización de la precarga

`audioEl.src = page.audioUrl` reproduce normalmente desde la caché ya poblada por `useStoryPagePreloader` (SPRINT-038) — no se vuelve a pedir ninguna precarga en esta pantalla, solo se consume lo ya cacheado.

## Contratos y dependencias externas

Ninguno nuevo.

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|---|---|---|
| R1 | La política de autoplay del navegador bloquea `audio.play()` sin interacción de usuario reciente | MEDIA | El cambio de página siempre ocurre por gesto/tap del adulto (swipe o flecha, SPRINT-039), que cuenta como interacción; validar explícitamente en iOS Safari (el más estricto) en dispositivo real. |
| R2 | Cambios de página muy rápidos (varios swipes seguidos) podrían solapar reproducciones | BAJA | Cada `playPage` reasigna `audioEl.src` y reinicia; el elemento `Audio` nativo corta la reproducción anterior al cambiar `src`. |
| R3 | El adulto espera que el altavoz o el toggle persistan entre cuentos | BAJA | Por diseño no persisten (decisión confirmada en SPRINT-038); documentado como comportamiento esperado, no defecto. |

---

## Tareas del sprint

### Tarea 40.1: Implementar `useStoryPageAudio.ts`

**Criterios de aceptación:** `playPage` respeta `narrateEnabled` y `speakerMuted`; `replay` funciona incluso con `speakerMuted` activo; `toggleSpeaker` pausa el audio en curso al silenciar.

### Tarea 40.2: Integrar reproducción automática al cambiar de página

**Criterios de aceptación:** con narración activada, cada página nueva reproduce su audio al mostrarse, sin intervención adicional del adulto.

### Tarea 40.3: Añadir botón de altavoz y de repetición en `StoryReaderControls.vue`

**Criterios de aceptación:** iconos reconocibles sin depender solo del color; estado de silenciado visualmente distinguible; visibles bajo las mismas reglas de aparición por tap que el resto de controles.

### Tarea 40.4: Ocultar todo control de voz si `narrativeVoiceEnabled` global es falso

**Criterios de aceptación:** ni altavoz ni botón de repetición aparecen en la lectura cuando el ajuste global está desactivado, coherente con la previa.

### Tarea 40.5: i18n de las cadenas nuevas

**Criterios de aceptación:** ninguna cadena visible como literal en template.

### Tarea 40.6: Prueba manual de autoplay en dispositivo real

**Criterios de aceptación:** verificado en iOS y Android que el audio arranca tras el gesto de cambio de página, sin bloqueo silencioso del navegador.

## Archivos afectados

| Archivo | Tipo de cambio |
|---|---|
| `framework/frontend/app/src/composables/useStoryPageAudio.ts` | Nuevo |
| `framework/frontend/app/src/views/PanelLecturaFamiliarLecturaView.vue` | Modificación |
| `framework/frontend/app/src/components/lectura-familiar/StoryReaderControls.vue` | Modificación |
| `framework/frontend/app/src/i18n/locales/es.ts` | Modificación |

## Estimación

- **Duración:** 1.5 días
- **Complejidad:** Media
- **Riesgo:** Medio (autoplay en iOS)

## Criterios de aceptación del sprint

1. Con narración activada, cada página nueva reproduce automáticamente su audio al mostrarse. *(FEAT-008 AC8)*
2. El altavoz silencia/reactiva sin alterar el toggle original de la portada ni el de otros cuentos. *(FEAT-008 AC9)*
3. "Reproducir de nuevo" repite el audio de la página actual, incluso si ya se escuchó antes o está silenciado. *(FEAT-008 AC10)*
4. Con `narrativeVoiceEnabled` global desactivado, no aparece ningún control de voz en la pantalla de lectura. *(FEAT-008 AC6)*
5. Los iconos de altavoz/reproducir son reconocibles sin depender solo del color. *(Accesibilidad, FEAT-008 §6)*
6. `vue-tsc --noEmit` sin errores.

## Dependencias bloqueantes

- [ ] SPRINT-039 completado (navegación de páginas y controles).
- [ ] SPRINT-038 completado (sesión de lectura, `narrateEnabled` inicial, precarga).

## Handoffs a otras capas

- Ninguno — ADR-024 confirma que este catálogo no invoca TTS ni integra con Agents en tiempo real.

## Notas adicionales

Con el cierre de este sprint queda completo el flujo de FEAT-008: intro → catálogo → previa → lectura con audio, incluyendo la precarga total del cuento decidida el 2026-08-27.
