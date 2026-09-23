<template>
  <div class="diary-period-filter" role="group" :aria-label="t('views.ninos.diary.periodFilter.ariaLabel')">
    <NubiButton
      v-for="option in options"
      :key="option.value"
      class="diary-period-filter__button"
      :variant="modelValue === option.value ? 'primary' : 'secondary'"
      size="sm"
      :aria-pressed="modelValue === option.value"
      @click="$emit('update:modelValue', option.value)"
    >
      {{ option.label }}
    </NubiButton>
  </div>
</template>

<script setup lang="ts">
/**
 * DiaryPeriodFilter - Botones de selección de periodo (Hoy, Semana, Mes, Total).
 */

import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import NubiButton from '../base/NubiButton.vue'
import type { DiaryPeriod } from '../../types/diary'

interface Props {
  modelValue: DiaryPeriod
}

defineProps<Props>()
defineEmits<{
  'update:modelValue': [value: DiaryPeriod]
}>()

const { t } = useI18n()

const options = computed(() => ([
  { value: 'TODAY' as DiaryPeriod, label: t('views.ninos.diary.periodFilter.today') },
  { value: 'WEEK' as DiaryPeriod, label: t('views.ninos.diary.periodFilter.week') },
  { value: 'MONTH' as DiaryPeriod, label: t('views.ninos.diary.periodFilter.month') },
  { value: 'ALL' as DiaryPeriod, label: t('views.ninos.diary.periodFilter.all') }
]))
</script>

<style scoped>
.diary-period-filter {
  display: flex;
  gap: var(--nubi-spacing-xs);
  flex-wrap: wrap;
}
</style>
