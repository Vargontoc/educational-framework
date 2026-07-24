<template>
  <span class="nubi-counter" :aria-label="ariaLabel">
    <NubiIcon v-if="icon" :name="icon" :size="16" class="nubi-counter__icon" />
    <span class="nubi-counter__value" :class="{ 'nubi-counter__value--animated': animated }">
      {{ displayValue }}
    </span>
    <span v-if="suffix" class="nubi-counter__suffix">{{ suffix }}</span>
  </span>
</template>

<script setup lang="ts">
/**
 * NubiCounter - Muestra cantidad con modo estático o animado
 * 
 * Características:
 * - Modo estático (valor fijo)
 * - Modo animado con transición numérica
 * - Icono opcional
 * - Sufijo opcional
 * - Accesibilidad con aria-label
 */

import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import NubiIcon from './NubiIcon.vue'

interface Props {
  /** Valor a mostrar */
  value: number
  /** Icono opcional */
  icon?: string
  /** Sufijo opcional (ej: "items", "users") */
  suffix?: string
  /** Activar animación */
  animated?: boolean
  /** Duración de la animación en ms */
  duration?: number
}

const props = withDefaults(defineProps<Props>(), {
  icon: '',
  suffix: '',
  animated: false,
  duration: 500
})

const { t } = useI18n()

const displayValue = ref(props.value)

const ariaLabel = computed(() => 
  t('components.counter.label', { value: props.value })
)

watch(() => props.value, (newValue) => {
  if (props.animated) {
    animateValue(newValue)
  } else {
    displayValue.value = newValue
  }
})

function animateValue(target: number) {
  const start = displayValue.value
  const diff = target - start
  const startTime = performance.now()
  
  function update(currentTime: number) {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / props.duration, 1)
    
    // Easing function (ease-out)
    const easeOut = 1 - Math.pow(1 - progress, 3)
    
    displayValue.value = Math.round(start + diff * easeOut)
    
    if (progress < 1) {
      requestAnimationFrame(update)
    }
  }
  
  requestAnimationFrame(update)
}
</script>

<style scoped>
.nubi-counter {
  display: inline-flex;
  align-items: center;
  gap: var(--nubi-spacing-xs);
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-primary);
}

.nubi-counter__icon {
  flex-shrink: 0;
  color: var(--nubi-text-secondary);
}

.nubi-counter__value {
  font-weight: var(--nubi-font-weight-semibold);
  font-variant-numeric: tabular-nums;
}

.nubi-counter__value--animated {
  transition: transform var(--nubi-duration-fast) var(--nubi-ease-out);
}

.nubi-counter__suffix {
  color: var(--nubi-text-secondary);
  font-weight: var(--nubi-font-weight-regular);
}
</style>
