<template>
  <section class="diary-summary" aria-live="polite">
    <DiaryEmptyState v-if="isEmpty" :message="t('views.ninos.diary.emptyToday')" />
    <div v-else class="diary-summary__stats">
      <div class="diary-summary__stat">
        <span class="diary-summary__stat-value">{{ summary?.playedTimeMinutes ?? 0 }}</span>
        <span class="diary-summary__stat-label">{{ t('views.ninos.diary.summary.playedMinutes') }}</span>
      </div>
      <div class="diary-summary__stat">
        <span class="diary-summary__stat-value">{{ summary?.uniqueActivitiesCompleted ?? 0 }}</span>
        <span class="diary-summary__stat-label">{{ t('views.ninos.diary.summary.uniqueActivities') }}</span>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
/**
 * DiarySummary - Tiempo jugado y actividades únicas completadas en el periodo.
 */

import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import DiaryEmptyState from './DiaryEmptyState.vue'
import type { DiarySummaryResponse } from '../../types/diary'

interface Props {
  summary: DiarySummaryResponse | null
}

const props = defineProps<Props>()
const { t } = useI18n()

const isEmpty = computed(() =>
  !props.summary || (props.summary.playedTimeMinutes === 0 && props.summary.uniqueActivitiesCompleted === 0)
)
</script>

<style scoped>
.diary-summary__stats {
  display: flex;
  gap: var(--nubi-spacing-lg);
  flex-wrap: wrap;
}

.diary-summary__stat {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-xs);
  padding: var(--nubi-spacing-md) var(--nubi-spacing-lg);
  background-color: var(--nubi-bg-surface);
  border: 1px solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-lg);
  min-width: 160px;
}

.diary-summary__stat-value {
  font-size: var(--nubi-font-size-2xl);
  font-weight: var(--nubi-font-weight-bold);
  color: var(--nubi-text-primary);
}

.diary-summary__stat-label {
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-secondary);
}
</style>
