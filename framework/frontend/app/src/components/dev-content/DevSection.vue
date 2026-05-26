<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

interface Props {
  sectionId: string
  loading?: boolean
  error?: boolean
}

defineProps<Props>()

defineEmits<{
  (e: 'retry'): void
}>()
</script>

<template>
  <div class="section">
    <header class="section__header">
      <h3 class="section__title">{{ t(`devContent.nav.${sectionId}`) }}</h3>
    </header>

    <div class="section__content">
      <!-- Loading state -->
      <div v-if="loading" class="section__state">
        <div class="spinner" aria-hidden="true"></div>
        <p>{{ t('devContent.loading') }}</p>
      </div>

      <!-- Error state -->
      <div v-else-if="error" class="section__state">
        <p class="section__error">{{ t('devContent.error') }}</p>
        <button class="section__retry-btn" @click="$emit('retry')">
          {{ t('devContent.retry') }}
        </button>
      </div>

      <!-- Empty state (default for shell) -->
      <div v-else class="section__state">
        <p class="section__empty">{{ t(`devContent.empty.${sectionId}`) }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.section {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.section__header {
  padding: var(--space-md) var(--space-lg);
  border-bottom: 1px solid #e2e8f0;
  background-color: #ffffff;
}

.section__title {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: #1f2937;
}

.section__content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-lg);
}

.section__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-md);
  text-align: center;
}

.section__empty {
  font-size: var(--font-size-md);
  color: #6b7280;
}

.section__error {
  font-size: var(--font-size-md);
  color: #ef4444;
}

.section__retry-btn {
  min-height: var(--touch-target-min);
  padding: var(--space-sm) var(--space-md);
  border: 2px solid var(--color-primary);
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  font-weight: 700;
  cursor: pointer;
  transition: background-color var(--transition-base), color var(--transition-base);
}

.section__retry-btn:hover {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--color-neutral);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
