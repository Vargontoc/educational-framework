<template>
  <p class="diary-abandonment-signal">
    <NubiIcon name="info" :size="16" class="diary-abandonment-signal__icon" aria-hidden="true" />
    <span>{{ t('views.ninos.diary.abandonment.message') }}</span>
  </p>
</template>

<script setup lang="ts">
/**
 * DiaryAbandonmentSignal - Aviso neutro, contextual, no evaluativo.
 * El padre solo lo renderiza cuando el backend ha confirmado la señal (4+ abandonos).
 */

import { useI18n } from 'vue-i18n'
import NubiIcon from '../base/NubiIcon.vue'

const { t } = useI18n()
</script>

<style scoped>
.diary-abandonment-signal {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-xs);
  margin: 0;
  padding: var(--nubi-spacing-xs) var(--nubi-spacing-sm);
  background-color: var(--nubi-bg-surface-secondary);
  border-radius: var(--nubi-radius-md);
  color: var(--nubi-text-secondary);
  font-size: var(--nubi-font-size-sm);
}

.diary-abandonment-signal__icon {
  flex-shrink: 0;
  color: var(--nubi-text-tertiary);
}
</style>
