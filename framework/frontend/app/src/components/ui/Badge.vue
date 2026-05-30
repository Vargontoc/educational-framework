<script setup lang="ts">
defineProps<{
  label: string
  variant?: 'primary' | 'secondary'
  disabled?: boolean
}>()
</script>

<template>
  <span
    class="badge"
    :class="[`badge--${variant ?? 'primary'}`, { 'badge--disabled': disabled }]"
  >
    {{ label }}
  </span>
</template>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 28px;
  padding: 6px var(--space-md);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  font-weight: 500;
  letter-spacing: 0;
  white-space: nowrap;
}

/* Primary */
.badge--primary {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

/* Secondary */
.badge--secondary {
  background-color: var(--color-celebration);
  color: var(--color-text-on-secondary);
}

/* Disabled */
.badge--disabled {
  background-color: var(--color-neutral);
  color: var(--color-neutral-dark);
}
</style>
