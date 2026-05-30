<script setup lang="ts">
defineProps<{
  variant?: 'primary' | 'secondary' | 'ghost'
  disabled?: boolean
}>()
</script>

<template>
  <button
    class="btn"
    :class="[`btn--${variant ?? 'primary'}`, { 'btn--disabled': disabled }]"
    :disabled="disabled"
  >
    <slot />
  </button>
</template>

<style scoped>
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: var(--touch-target-min);
  min-height: var(--touch-target-min);
  padding: var(--space-sm) var(--space-md);
  border: none;
  border-radius: var(--radius-pill);
  font-size: var(--font-size-button);
  font-family: var(--font-family-base);
  font-weight: 600;
  letter-spacing: 0;
  transition: background-color var(--transition-base), box-shadow var(--transition-base), transform var(--transition-base);
  user-select: none;
}

.btn:active:not(.btn--disabled) {
  transform: scale(0.97);
}

/* Primary */
.btn--primary {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
  box-shadow: 0 4px 0 var(--color-primary-dark);
}

.btn--primary:hover:not(.btn--disabled) {
  background-color: var(--color-primary-dark);
}

/* Secondary */
.btn--secondary {
  background-color: var(--color-celebration);
  color: var(--color-text-on-secondary);
  box-shadow: 0 4px 0 var(--color-celebration-dark);
}

.btn--secondary:hover:not(.btn--disabled) {
  background-color: var(--color-celebration-dark);
}

.btn--ghost {
  background-color: var(--color-surface);
  color: var(--color-primary);
  box-shadow: inset 0 0 0 2px color-mix(in srgb, var(--color-primary) 18%, transparent);
}

/* Disabled */
.btn--disabled {
  background-color: var(--color-neutral);
  color: var(--color-disabled);
  box-shadow: none;
  cursor: not-allowed;
}
</style>
