<template>
  <NubiCard class="diary-activity-card">
    <template #header>
      <h4 class="diary-activity-card__name">{{ activity.name }}</h4>
    </template>

    <p class="diary-activity-card__category">{{ categoryLabel }}</p>

    <p class="diary-activity-card__difficulty-sentence">
      {{ t('views.ninos.diary.difficulty.sentence', { level: difficultyLabel }) }}
    </p>
    <DiaryDifficultyIndicator :level="activity.currentDifficulty" />

    <DiaryAbandonmentSignal v-if="abandonmentSignal" />
  </NubiCard>
</template>

<script setup lang="ts">
/**
 * DiaryActivityCard - Nombre, categoría/subcategoría, nivel actual y (si aplica) señal de abandono.
 */

import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import NubiCard from '../base/NubiCard.vue'
import DiaryDifficultyIndicator from './DiaryDifficultyIndicator.vue'
import DiaryAbandonmentSignal from './DiaryAbandonmentSignal.vue'
import type { AbandonmentSignalResponse, DiaryActivityResponse } from '../../types/diary'

interface Props {
  activity: DiaryActivityResponse
  abandonmentSignal: AbandonmentSignalResponse | null
}

const props = defineProps<Props>()
const { t } = useI18n()

const categoryLabel = computed(() => {
  const category = t(`views.ninos.diary.category.${props.activity.category.toLowerCase()}`)
  if (!props.activity.subcategory) return category
  const subcategory = t(`views.ninos.diary.subcategory.${props.activity.subcategory.toLowerCase()}`)
  return `${category} · ${subcategory}`
})

const difficultyLabel = computed(() => t(`views.ninos.diary.difficulty.${props.activity.currentDifficulty.toLowerCase()}`))
</script>

<style scoped>
.diary-activity-card__name {
  margin: 0;
}

.diary-activity-card__category {
  margin: 0 0 var(--nubi-spacing-sm) 0;
  color: var(--nubi-text-secondary);
  font-size: var(--nubi-font-size-sm);
}

.diary-activity-card__difficulty-sentence {
  margin: 0 0 var(--nubi-spacing-sm) 0;
  color: var(--nubi-text-primary);
  font-size: var(--nubi-font-size-sm);
  line-height: var(--nubi-line-height-normal);
}
</style>
