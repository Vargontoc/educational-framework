<template>
  <div class="diary-difficulty-indicator" role="group" :aria-label="ariaLabel">
    <div
      v-for="step in steps"
      :key="step.value"
      :class="['diary-difficulty-indicator__stop', { 'diary-difficulty-indicator__stop--current': step.value === level }]"
    >
      <span class="diary-difficulty-indicator__dot" aria-hidden="true">
        <NubiIcon v-if="step.value === level" name="check" :size="12" />
      </span>
      <span class="diary-difficulty-indicator__label">{{ step.label }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * DiaryDifficultyIndicator - Tres paradas discretas (Fácil/Normal/Difícil).
 *
 * La parada actual se marca con un icono (no solo con color) para que el nivel
 * se entienda sin depender de la percepción del color.
 */

import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import NubiIcon from '../base/NubiIcon.vue'
import type { DifficultyLevel } from '../../types/diary'

interface Props {
  level: DifficultyLevel
}

const props = defineProps<Props>()
const { t } = useI18n()

const steps = computed(() => ([
  { value: 'EASY' as DifficultyLevel, label: t('views.ninos.diary.difficulty.easy') },
  { value: 'MEDIUM' as DifficultyLevel, label: t('views.ninos.diary.difficulty.medium') },
  { value: 'HARD' as DifficultyLevel, label: t('views.ninos.diary.difficulty.hard') }
]))

const ariaLabel = computed(() => {
  const current = steps.value.find(step => step.value === props.level)
  return t('views.ninos.diary.difficulty.ariaLabel', { level: current?.label ?? props.level })
})
</script>

<style scoped>
.diary-difficulty-indicator {
  display: flex;
  gap: var(--nubi-spacing-md);
}

.diary-difficulty-indicator__stop {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--nubi-spacing-xs);
}

.diary-difficulty-indicator__dot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: var(--nubi-radius-full);
  border: 2px solid var(--nubi-border-default);
  color: var(--nubi-text-inverse);
}

.diary-difficulty-indicator__stop--current .diary-difficulty-indicator__dot {
  background-color: var(--nubi-color-primary);
  border-color: var(--nubi-color-primary);
}

.diary-difficulty-indicator__label {
  font-size: var(--nubi-font-size-xs);
  color: var(--nubi-text-secondary);
}

.diary-difficulty-indicator__stop--current .diary-difficulty-indicator__label {
  color: var(--nubi-text-primary);
  font-weight: var(--nubi-font-weight-semibold);
}
</style>
