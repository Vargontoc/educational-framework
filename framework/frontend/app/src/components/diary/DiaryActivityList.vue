<template>
  <div class="diary-activity-list">
    <DiaryEmptyState v-if="activities.length === 0" :message="t('views.ninos.diary.emptyActivities')" />

    <section v-for="group in groups" :key="group.category" class="diary-activity-list__group">
      <h3 class="diary-activity-list__group-title">{{ group.label }}</h3>
      <div class="diary-activity-list__cards">
        <DiaryActivityCard
          v-for="activity in group.items"
          :key="activity.activityId"
          :activity="activity"
          :abandonment-signal="abandonmentSignals[activity.activityId] ?? null"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
/**
 * DiaryActivityList - Actividades agrupadas por categoria, orden fijo:
 * Reconocimiento -> Comparacion -> Memoria (definido en frontend, aunque el backend ya las entrega asi).
 */

import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import DiaryActivityCard from './DiaryActivityCard.vue'
import DiaryEmptyState from './DiaryEmptyState.vue'
import type { AbandonmentSignalResponse, DiaryActivityResponse } from '../../types/diary'

const CATEGORY_ORDER = ['RECOGNITION', 'COMPARISON', 'MEMORY'] as const

interface Props {
  activities: DiaryActivityResponse[]
  abandonmentSignals: Record<number, AbandonmentSignalResponse | null>
}

const props = defineProps<Props>()
const { t } = useI18n()

const groups = computed(() =>
  CATEGORY_ORDER
    .map(category => ({
      category,
      label: t(`views.ninos.diary.category.${category.toLowerCase()}`),
      items: props.activities.filter(activity => activity.category === category)
    }))
    .filter(group => group.items.length > 0)
)
</script>

<style scoped>
.diary-activity-list {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-lg);
}

.diary-activity-list__group {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-sm);
}

.diary-activity-list__group-title {
  margin: 0;
  font-size: var(--nubi-font-size-lg);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-primary);
}

.diary-activity-list__cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: var(--nubi-spacing-md);
}
</style>
