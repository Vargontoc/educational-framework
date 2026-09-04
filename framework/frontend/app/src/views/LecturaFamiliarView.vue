<template>
  <div class="lecture-view">
    <div class="grid-catalog" v-if="catalog && catalog.length > 0">
      <nubi-card v-for="item in catalog" :key="item.id" :title="item.title" :clickable="true" :image-alt="item.title">
        <template #image>
          <div class="story-cover" @click="goToStory(item.id)">
            <img v-if="imageCovers[item.id]" :src="imageCovers[item.id]" :alt="item.title" />
            <button
              type="button"
              class="story-cover__narrate-toggle"
              :class="{ 'story-cover__narrate-toggle--active': narrateEnabled[item.id] }"
              :disabled="!globalNarrativeVoiceEnabled"
              :aria-label="narrateEnabled[item.id] ? 'Desactivar narración' : 'Activar narración'"
              @click.stop="toggleNarrate(item.id)"
            >
              <nubi-icon :name="narrateEnabled[item.id] ? 'volume-2' : 'volume-x'" :size="18" />
            </button>
          </div>
        </template>
      </nubi-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useChildSessions } from '@/composables/useChildSessions';
import { useFamilyStatus } from '@/composables/useFamilyStatus';
import { getCatalog, getCover, StoryEntry } from '@/services/storyService';
import { useParentalAuthStore } from '@/stores/parentalAuth';
import { computed, onMounted, onUnmounted, ref } from 'vue';
import NubiCard from '@/components/base/NubiCard.vue';
import NubiIcon from '@/components/base/NubiIcon.vue';
import { useRouter } from 'vue-router';
const { startPolling, stopPolling } = useChildSessions()
const { family, fetchFamilyStatus } = useFamilyStatus()

const router = useRouter()
const parentalStore = useParentalAuthStore();
const catalog = ref<StoryEntry[]>([])
const imageCovers = ref<Record<string, string>>({})
const narrateEnabled = ref<Record<string, boolean>>({})

/** Ajuste global «Voz narrativa» (FEAT-005): si está desactivado, el toggle de cada card queda deshabilitado. */
const globalNarrativeVoiceEnabled = computed(() => family.value?.narrativeVoiceEnabled ?? true)

function loadCovers(items: StoryEntry[]) {
  items.forEach((item) => {
    getCover(item.id).then((blob) => {
      if (blob) {
        imageCovers.value[item.id] = URL.createObjectURL(blob)
      }
    })
  })
}

function initNarratePreferences(items: StoryEntry[]) {
  items.forEach((item) => {
    if (!(item.id in narrateEnabled.value)) {
      narrateEnabled.value[item.id] = true
    }
  })
}

function toggleNarrate(id: string) {
  if (!globalNarrativeVoiceEnabled.value) return
  narrateEnabled.value[id] = !narrateEnabled.value[id]
}

function goToStory(id: string){
  router.replace({ name: 'reader', params: { id: id } })
}

onMounted(() => {
  getCatalog().then((c) => {
    catalog.value = c
    loadCovers(c)
    initNarratePreferences(c)
  })
  fetchFamilyStatus()
  const fid = parentalStore.familyId;
  if(fid !== null) {
    startPolling(fid, 5000)
  }
})

onUnmounted(() => {
  stopPolling()
  Object.values(imageCovers.value).forEach((url) => URL.revokeObjectURL(url))
})

</script>

<style lang="css" scoped>
.lecture-view {
  position: relative;
  isolation: isolate;
  overflow: hidden;
  width: 100%;
  min-height: 100dvh;
}

/* Dos capas de la misma imagen: una versión ampliada y desenfocada rellena
   todo el ancho (evita los márgenes en blanco en horizontal), y encima la
   imagen nítida sin recortar ni deformar (FEAT-008 AC1).
   position: fixed (en vez de absolute) ajusta ambas capas al tamaño del
   dispositivo/viewport en vez de al alto total del contenido, y hace que el
   fondo permanezca siempre en pantalla al hacer scroll por el catálogo. */
.lecture-view::before,
.lecture-view::after {
  content: '';
  position: fixed;
  inset: 0;
  background-image: url("../assets/images/experiencia_familiar.jpg");
  background-repeat: no-repeat;
  background-position: center;
}

.lecture-view::before {
  z-index: -2;
  background-size: cover;
  filter: blur(24px) brightness(0.85) saturate(1.1);
  transform: scale(1.15);
}

.lecture-view::after {
  z-index: -1;
  background-size: contain;
}

.grid-catalog {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: var(--nubi-spacing-lg);
  padding: var(--nubi-spacing-lg);
}

/* La card flota sobre el fondo de la sección: sin relleno propio pero con
   un borde sutil para que se distinga como objetivo táctil sobre la imagen.
   La portada ocupa solo una porción del contenedor, no la tarjeta entera. */
.grid-catalog :deep(.nubi-card) {
  background-color: transparent;
  border: 1px solid var(--nubi-border-default);
}

.grid-catalog :deep(.nubi-card--clickable:hover) {
  box-shadow: none;
}

.grid-catalog :deep(.nubi-card__image) {
  width: 45%;
  aspect-ratio: 3 / 4;
  margin: 0 auto;
  background-color: transparent;
  border-radius: var(--nubi-radius-md);
  box-shadow: var(--nubi-shadow-md);
}

.grid-catalog :deep(.nubi-card__content) {
  text-align: center;
}

.grid-catalog :deep(.nubi-card__title) {
  color: var(--nubi-text-inverse, #fff);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.6);
}

.story-cover {
  position: relative;
  width: 100%;
  height: 100%;
}

.story-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Toggle pequeño de narración por cuento; deshabilitado por completo si la
   configuración familiar global de voz narrativa (FEAT-005) está apagada. */
.story-cover__narrate-toggle {
  position: absolute;
  bottom: var(--nubi-spacing-xs);
  right: var(--nubi-spacing-xs);
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.55);
  color: #fff;
  cursor: pointer;
  transition: background-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              opacity var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.story-cover__narrate-toggle--active {
  background-color: var(--nubi-color-primary);
}

.story-cover__narrate-toggle:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.story-cover__narrate-toggle:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}
</style>
