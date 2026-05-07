<script setup lang="ts">
defineProps<{
  variant?: 'primary' | 'secondary'
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
  border-radius: var(--radius-md);
  font-size: var(--font-size-md);
  font-family: var(--font-family-base);
  font-weight: 700;
  letter-spacing: 0.02em;
  transition: background-color var(--transition-base), transform var(--transition-base);
  user-select: none;
}

.btn:active:not(.btn--disabled) {
  transform: scale(0.97);
}

/* Primary */
.btn--primary {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.btn--primary:hover:not(.btn--disabled) {
  background-color: var(--color-primary-dark);
}

/* Secondary */
.btn--secondary {
  background-color: var(--color-secondary);
  color: var(--color-text-on-secondary);
}

.btn--secondary:hover:not(.btn--disabled) {
  background-color: var(--color-secondary-dark);
}

/* Disabled */
.btn--disabled {
  background-color: var(--color-neutral);
  color: var(--color-neutral-dark);
  cursor: not-allowed;
}
</style>
