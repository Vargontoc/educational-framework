<template>
  <div class="section-placeholder">
    <NubiIcon name="construction" :size="48" color="var(--nubi-text-tertiary)" />
    <h1 class="section-placeholder__title">{{ t('views.placeholder.title') }}</h1>
    <p class="section-placeholder__description">{{ t('views.placeholder.description') }}</p>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import NubiIcon from '../components/base/NubiIcon.vue'

const { t } = useI18n()
</script>

<style scoped>
.section-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--nubi-spacing-md);
  padding: var(--nubi-spacing-3xl) var(--nubi-spacing-lg);
  text-align: center;
  min-height: 60vh;
}

.section-placeholder__title {
  font-size: var(--nubi-font-size-xl);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-primary);
  margin: 0;
}

.section-placeholder__description {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-secondary);
  margin: 0;
  max-width: 400px;
}
</style>
