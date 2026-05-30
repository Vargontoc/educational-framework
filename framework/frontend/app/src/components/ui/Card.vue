<script setup lang="ts">
defineProps<{
  title: string
  description?: string
  icon?: string
  variant?: 'primary' | 'secondary'
  disabled?: boolean
}>()
</script>

<template>
  <div
    class="card"
    :class="[`card--${variant ?? 'primary'}`, { 'card--disabled': disabled }]"
  >
    <div class="card__header">
      <span v-if="icon" class="card__icon" aria-hidden="true">{{ icon }}</span>
      <span class="card__title">{{ title }}</span>
    </div>
    <p v-if="description" class="card__description">{{ description }}</p>
    <slot />
  </div>
</template>

<style scoped>
.card {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
  padding: var(--space-md);
  border-radius: var(--radius-lg);
  background-color: var(--color-surface);
  box-shadow: 0 18px 40px rgba(26, 35, 64, 0.08);
  border-top: 6px solid transparent;
  transition: box-shadow var(--transition-base), transform var(--transition-base);
  min-width: 180px;
}

.card:not(.card--disabled):hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.14);
  transform: translateY(-2px);
}

/* Primary accent */
.card--primary {
  border-top-color: var(--color-primary);
}

/* Secondary accent */
.card--secondary {
  border-top-color: var(--color-celebration);
}

/* Disabled */
.card--disabled {
  border-top-color: var(--color-neutral-dark);
  opacity: 0.55;
  pointer-events: none;
}

.card__header {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.card__icon {
  font-size: var(--font-size-lg);
  line-height: 1;
}

.card__title {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: var(--color-text-primary);
}

.card__description {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: 1.5;
}
</style>
