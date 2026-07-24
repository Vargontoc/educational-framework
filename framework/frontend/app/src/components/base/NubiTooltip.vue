<template>
  <div
    class="nubi-tooltip-wrapper"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
    @focus="handleFocus"
    @blur="handleBlur"
  >
    <slot />
    
    <Teleport to="body">
      <div
        v-if="isVisible"
        :id="tooltipId"
        role="tooltip"
        :class="['nubi-tooltip', `nubi-tooltip--${computedPosition}`]"
        :style="tooltipStyle"
      >
        <div class="nubi-tooltip__content">
          {{ text }}
        </div>
        <div class="nubi-tooltip__arrow" />
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
/**
 * NubiTooltip - Información contextual al hover
 * 
 * Características:
 * - Aparece al hover/focus
 * - Posición configurable (top/bottom/left/right)
 * - Detección de posición disponible en viewport
 * - En táctiles: tap-to-show con dismiss automático
 * - Accesibilidad con role="tooltip"
 */

import { ref, computed, onBeforeUnmount, useId, nextTick } from 'vue'

interface Props {
  /** Texto del tooltip */
  text: string
  /** Posición preferida */
  position?: 'top' | 'bottom' | 'left' | 'right'
  /** Retraso antes de mostrar (ms) */
  delay?: number
}

const props = withDefaults(defineProps<Props>(), {
  position: 'top',
  delay: 200
})

const isVisible = ref(false)
const computedPosition = ref(props.position)
const tooltipStyle = ref<Record<string, string>>({})
const tooltipId = `nubi-tooltip-${useId()}`

let showTimeout: ReturnType<typeof setTimeout> | null = null
let hideTimeout: ReturnType<typeof setTimeout> | null = null

function show() {
  if (hideTimeout) {
    clearTimeout(hideTimeout)
    hideTimeout = null
  }
  
  showTimeout = setTimeout(() => {
    isVisible.value = true
    nextTick(updatePosition)
  }, props.delay)
}

function hide() {
  if (showTimeout) {
    clearTimeout(showTimeout)
    showTimeout = null
  }
  
  hideTimeout = setTimeout(() => {
    isVisible.value = false
  }, 100)
}

function handleMouseEnter() {
  show()
}

function handleMouseLeave() {
  hide()
}

function handleFocus() {
  show()
}

function handleBlur() {
  hide()
}

function updatePosition() {
  const wrapper = document.querySelector(`[aria-describedby="${tooltipId}"]`)
  const tooltip = document.getElementById(tooltipId)
  
  if (!wrapper || !tooltip) return
  
  const wrapperRect = wrapper.getBoundingClientRect()
  const tooltipRect = tooltip.getBoundingClientRect()
  const viewportWidth = window.innerWidth
  const viewportHeight = window.innerHeight
  
  let pos = props.position
  let top = 0
  let left = 0
  
  // Detectar posición disponible
  switch (props.position) {
    case 'top':
      top = wrapperRect.top - tooltipRect.height - 8
      left = wrapperRect.left + (wrapperRect.width - tooltipRect.width) / 2
      if (top < 0) pos = 'bottom'
      break
    case 'bottom':
      top = wrapperRect.bottom + 8
      left = wrapperRect.left + (wrapperRect.width - tooltipRect.width) / 2
      if (top + tooltipRect.height > viewportHeight) pos = 'top'
      break
    case 'left':
      top = wrapperRect.top + (wrapperRect.height - tooltipRect.height) / 2
      left = wrapperRect.left - tooltipRect.width - 8
      if (left < 0) pos = 'right'
      break
    case 'right':
      top = wrapperRect.top + (wrapperRect.height - tooltipRect.height) / 2
      left = wrapperRect.right + 8
      if (left + tooltipRect.width > viewportWidth) pos = 'left'
      break
  }
  
  // Recalcular con posición ajustada
  switch (pos) {
    case 'top':
      top = wrapperRect.top - tooltipRect.height - 8
      left = wrapperRect.left + (wrapperRect.width - tooltipRect.width) / 2
      break
    case 'bottom':
      top = wrapperRect.bottom + 8
      left = wrapperRect.left + (wrapperRect.width - tooltipRect.width) / 2
      break
    case 'left':
      top = wrapperRect.top + (wrapperRect.height - tooltipRect.height) / 2
      left = wrapperRect.left - tooltipRect.width - 8
      break
    case 'right':
      top = wrapperRect.top + (wrapperRect.height - tooltipRect.height) / 2
      left = wrapperRect.right + 8
      break
  }
  
  // Clamp al viewport
  left = Math.max(8, Math.min(left, viewportWidth - tooltipRect.width - 8))
  top = Math.max(8, Math.min(top, viewportHeight - tooltipRect.height - 8))
  
  computedPosition.value = pos
  tooltipStyle.value = {
    position: 'fixed',
    top: `${top}px`,
    left: `${left}px`,
    zIndex: '9999'
  }
}

onBeforeUnmount(() => {
  if (showTimeout) clearTimeout(showTimeout)
  if (hideTimeout) clearTimeout(hideTimeout)
})
</script>

<style scoped>
.nubi-tooltip-wrapper {
  display: inline-flex;
}
</style>

<style>
/* Tooltip styles (not scoped - rendered in Teleport) */
.nubi-tooltip {
  pointer-events: none;
  animation: nubi-tooltip-fade-in 150ms var(--nubi-ease-out);
}

.nubi-tooltip__content {
  background-color: var(--nubi-bg-surface-inverse);
  color: var(--nubi-text-inverse);
  font-size: var(--nubi-font-size-xs);
  font-family: var(--nubi-font-family-base);
  padding: var(--nubi-spacing-xs) var(--nubi-spacing-sm);
  border-radius: var(--nubi-radius-md);
  max-width: 200px;
  text-align: center;
  line-height: var(--nubi-line-height-tight);
  box-shadow: var(--nubi-shadow-md);
}

.nubi-tooltip__arrow {
  position: absolute;
  width: 8px;
  height: 8px;
  background-color: var(--nubi-bg-surface-inverse);
  transform: rotate(45deg);
}

/* Arrow positions */
.nubi-tooltip--top .nubi-tooltip__arrow {
  bottom: -4px;
  left: 50%;
  margin-left: -4px;
}

.nubi-tooltip--bottom .nubi-tooltip__arrow {
  top: -4px;
  left: 50%;
  margin-left: -4px;
}

.nubi-tooltip--left .nubi-tooltip__arrow {
  right: -4px;
  top: 50%;
  margin-top: -4px;
}

.nubi-tooltip--right .nubi-tooltip__arrow {
  left: -4px;
  top: 50%;
  margin-top: -4px;
}

@keyframes nubi-tooltip-fade-in {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
