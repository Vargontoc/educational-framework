<template>
  <div class="color-vision-examples" :aria-label="ariaLabel" role="img">
    <svg width="200" height="100" viewBox="0 0 200 100" xmlns="http://www.w3.org/2000/svg">
      <circle cx="30" cy="50" r="20" :fill="circleColor1" stroke="#555" stroke-width="1" />
      <rect x="65" y="30" width="40" height="40" :fill="rectColor1" stroke="#555" stroke-width="1" />
      <circle cx="135" cy="50" r="20" :fill="circleColor2" stroke="#555" stroke-width="1" />
      <rect x="168" y="30" width="25" height="40" :fill="rectColor2" stroke="#555" stroke-width="1" />
    </svg>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  mode: string
}

const props = defineProps<Props>()

const ariaLabel = 'Ejemplos visuales de colores según el perfil seleccionado'

interface ColorSet {
  circle1: string
  rect1: string
  circle2: string
  rect2: string
}

const COLOR_SETS: Record<string, ColorSet> = {
  NONE: {
    circle1: '#e53e3e',
    rect1: '#38a169',
    circle2: '#3182ce',
    rect2: '#d69e2e'
  },
  PROTANOPIA: {
    circle1: '#5a5a5a',
    rect1: '#7a7a3a',
    circle2: '#3182ce',
    rect2: '#c8a820'
  },
  PROTANOMALY: {
    circle1: '#9a5a3e',
    rect1: '#5a8a4a',
    circle2: '#3182ce',
    rect2: '#c8a820'
  },
  DEUTERANOPIA: {
    circle1: '#c07a30',
    rect1: '#6a6a50',
    circle2: '#3182ce',
    rect2: '#b8a030'
  },
  DEUTERANOMALY: {
    circle1: '#d06a3a',
    rect1: '#5a8a50',
    circle2: '#3182ce',
    rect2: '#c0a028'
  },
  TRITANOPIA: {
    circle1: '#e53e3e',
    rect1: '#38a169',
    circle2: '#6a8aaa',
    rect2: '#aa6a8a'
  },
  TRITANOMALY: {
    circle1: '#e53e3e',
    rect1: '#38a169',
    circle2: '#4a7aaa',
    rect2: '#aa7a6a'
  },
  ACHROMATOMALY: {
    circle1: '#8a8a8a',
    rect1: '#6a6a6a',
    circle2: '#aaaaaa',
    rect2: '#505050'
  },
  ACHROMATOPSIA: {
    circle1: '#808080',
    rect1: '#606060',
    circle2: '#a0a0a0',
    rect2: '#404040'
  }
}

const activeColors = computed<ColorSet>(() => {
  return COLOR_SETS[props.mode] || COLOR_SETS.NONE
})

const circleColor1 = computed(() => activeColors.value.circle1)
const rectColor1 = computed(() => activeColors.value.rect1)
const circleColor2 = computed(() => activeColors.value.circle2)
const rectColor2 = computed(() => activeColors.value.rect2)
</script>

<style scoped>
.color-vision-examples {
  display: flex;
  justify-content: center;
  padding: var(--nubi-spacing-sm) 0;
}

.color-vision-examples svg {
  max-width: 100%;
  height: auto;
}
</style>
