<template>
  <div class="nubi-progress-bar">
    <div v-if="showLabel" class="nubi-progress-bar__header">
      <span v-if="label" class="nubi-progress-bar__label">{{ label }}</span>
      <span class="nubi-progress-bar__percentage">{{ Math.round(value) }}%</span>
    </div>
    
    <div
      class="nubi-progress-bar__track"
      role="progressbar"
      :aria-valuenow="value"
      :aria-valuemin="0"
      :aria-valuemax="100"
      :aria-label="ariaLabel"
    >
      <div
        class="nubi-progress-bar__fill"
        :style="{ width: `${clampedValue}%` }"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * NubiProgressBar - Indicador visual de avance con animación suave
 * 
 * Características:
 * - Animación suave al cambiar valor
 * - Label opcional con porcentaje
 * - Accesibilidad con role="progressbar"
 * - Valor clampado entre 0 y 100
 */

import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

interface Props {
  /** Valor actual (0-100) */
  value: number
  /** Label opcional */
  label?: string
  /** Mostrar label con porcentaje */
  showLabel?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  label: '',
  showLabel: true
})

const { t } = useI18n()

const clampedValue = computed(() => Math.max(0, Math.min(100, props.value)))

const ariaLabel = computed(() => 
  props.label || t('components.progressBar.label', { value: Math.round(props.value) })
)
</script>

<style scoped>
.nubi-progress-bar {
  width: 100%;
}

.nubi-progress-bar__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--nubi-spacing-xs);
}

.nubi-progress-bar__label {
  font-size: var(--nubi-font-size-sm);
  font-weight: var(--nubi-font-weight-medium);
  color: var(--nubi-text-primary);
}

.nubi-progress-bar__percentage {
  font-size: var(--nubi-font-size-sm);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-secondary);
}

.nubi-progress-bar__track {
  width: 100%;
  height: 8px;
  background-color: var(--nubi-bg-surface-tertiary);
  border-radius: var(--nubi-radius-full);
  overflow: hidden;
}

.nubi-progress-bar__fill {
  height: 100%;
  background-color: var(--nubi-color-primary);
  border-radius: var(--nubi-radius-full);
  transition: width var(--nubi-duration-normal) var(--nubi-ease-in-out);
}
</style>
