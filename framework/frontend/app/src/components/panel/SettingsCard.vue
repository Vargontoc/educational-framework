<script setup lang="ts">
withDefaults(defineProps<{
  title: string
  description?: string
}>(), {
  description: ''
})
</script>

<template>
  <div class="settings-card">
    <div class="settings-card__content">
      <h3 class="settings-card__title">{{ title }}</h3>
      <p v-if="description" class="settings-card__description">{{ description }}</p>
    </div>
    <div class="settings-card__action">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.settings-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
  padding: var(--space-lg);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  box-shadow: 0 4px 16px rgba(26, 35, 64, 0.06);
}

.settings-card__content {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
  flex: 1;
  min-width: 0;
}

.settings-card__title {
  margin: 0;
  font-size: var(--font-size-body);
  font-weight: 700;
  color: var(--color-text-primary);
}

.settings-card__description {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: 1.4;
}

.settings-card__action {
  flex-shrink: 0;
}

@media (max-width: 480px) {
  .settings-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .settings-card__action {
    width: 100%;
    margin-top: var(--space-sm);
  }
}
</style>