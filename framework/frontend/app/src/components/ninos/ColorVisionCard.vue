<template>
  <div
    :class="[
      'color-vision-card',
      {
        'color-vision-card--selected': selected,
        'color-vision-card--previewed': previewed
      }
    ]"
    role="radio"
    :aria-checked="selected"
    :aria-label="ariaLabel"
    tabindex="0"
    @pointerenter="emit('explore')"
    @pointerleave="emit('unexplore')"
    @focus="emit('explore')"
    @blur="emit('unexplore')"
    @pointerdown="emit('explore')"
    @click="emit('select')"
    @keydown.enter.prevent="emit('select')"
    @keydown.space.prevent="emit('select')"
  >
    <div class="color-vision-card__sample" aria-hidden="true">
      <svg
        v-for="sample in samples"
        :key="sample.index"
        class="color-vision-card__shape-icon"
        width="26"
        height="26"
        viewBox="0 0 26 26"
      >
        <template v-if="sample.shape === 'circle'">
          <circle
            cx="13"
            cy="13"
            r="11"
            :fill="sample.original"
            class="color-vision-card__layer"
            :class="{ 'color-vision-card__layer--visible': !showSimulated }"
          />
          <circle
            cx="13"
            cy="13"
            r="11"
            :fill="sample.adaptative"
            class="color-vision-card__layer"
            :class="{ 'color-vision-card__layer--visible': showSimulated }"
          />
        </template>
        <template v-else>
          <polygon
            :points="sample.points"
            :fill="sample.original"
            class="color-vision-card__layer"
            :class="{ 'color-vision-card__layer--visible': !showSimulated }"
          />
          <polygon
            :points="sample.points"
            :fill="sample.adaptative"
            class="color-vision-card__layer"
            :class="{ 'color-vision-card__layer--visible': showSimulated }"
          />
        </template>
      </svg>
    </div>

    <div class="color-vision-card__phase-label" aria-hidden="true">
      <span
        class="color-vision-card__phase-text"
        :class="{ 'color-vision-card__phase-text--visible': !showSimulated }"
      >
        {{ t('views.ninos.edit.sections.visualAccessibility.originalLabel') }}
      </span>
      <span
        class="color-vision-card__phase-text color-vision-card__phase-text--simulated"
        :class="{ 'color-vision-card__phase-text--visible': showSimulated }"
      >
        {{ t('views.ninos.edit.sections.visualAccessibility.simulatedLabel') }}
      </span>
    </div>

    <div class="color-vision-card__text">
      <span class="color-vision-card__name">{{ mode.label }}</span>
      <span class="color-vision-card__description">{{ mode.description }}</span>
    </div>

    <span v-if="selected" class="color-vision-card__indicator" aria-hidden="true">
      <svg width="18" height="18" viewBox="0 0 18 18" xmlns="http://www.w3.org/2000/svg">
        <circle cx="9" cy="9" r="8" fill="var(--nubi-color-primary)" />
        <path d="M5 9l3 3 5-5" stroke="#fff" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
    </span>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

interface ColorSample {
  original: string
  adaptative: string
}

interface Props {
  mode: {
    value: string
    label: string
    description: string
    colors: ColorSample[]
  }
  selected: boolean
  previewed: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'select'): void
  (e: 'explore'): void
  (e: 'unexplore'): void
}>()

const { t } = useI18n()

const SHAPES = ['circle', 'triangle', 'square', 'pentagon', 'rhombus'] as const
type Shape = typeof SHAPES[number]

function polygonPoints(sides: number, rotationDeg: number, r: number): string {
  const cx = 13
  const cy = 13
  const points: string[] = []
  for (let i = 0; i < sides; i++) {
    const angle = (rotationDeg + (360 / sides) * i) * (Math.PI / 180)
    points.push(`${(cx + r * Math.cos(angle)).toFixed(2)},${(cy + r * Math.sin(angle)).toFixed(2)}`)
  }
  return points.join(' ')
}

const SHAPE_POINTS: Partial<Record<Shape, string>> = {
  triangle: polygonPoints(3, -90, 11),
  square: polygonPoints(4, -45, 11),
  pentagon: polygonPoints(5, -90, 11),
  rhombus: polygonPoints(4, -90, 11)
}

const samples = computed(() =>
  props.mode.colors.map((color, index) => {
    const shape = SHAPES[index % SHAPES.length]
    return {
      index,
      shape,
      points: SHAPE_POINTS[shape],
      original: color.original,
      adaptative: color.adaptative
    }
  })
)

const ariaLabel = computed(() =>
  t('views.ninos.edit.sections.visualAccessibility.cardAriaLabel', {
    mode: props.mode.label,
    description: props.mode.description
  })
)

const showSimulated = ref(false)
let toggleTimer: ReturnType<typeof setInterval> | undefined

onMounted(() => {
  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (prefersReducedMotion) return

  toggleTimer = setInterval(() => {
    showSimulated.value = !showSimulated.value
  }, 1500)
})

onBeforeUnmount(() => {
  if (toggleTimer) clearInterval(toggleTimer)
})
</script>

<style scoped>
.color-vision-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--nubi-spacing-sm);
  padding: var(--nubi-spacing-md);
  min-height: 48px;
  min-width: 48px;
  background-color: var(--nubi-bg-surface);
  border: 2px solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-lg);
  cursor: pointer;
  user-select: none;
  position: relative;
  transition: border-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              box-shadow var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.color-vision-card:hover,
.color-vision-card--previewed {
  border-color: var(--nubi-border-hover);
}

.color-vision-card--selected {
  border-color: var(--nubi-color-primary);
}

.color-vision-card:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

.color-vision-card__sample {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 4px;
  max-width: 140px;
}

.color-vision-card__shape-icon {
  display: block;
  flex-shrink: 0;
}

.color-vision-card__layer {
  opacity: 0;
  transition: opacity 0.6s ease-in-out;
}

.color-vision-card__layer--visible {
  opacity: 1;
}

.color-vision-card__phase-label {
  position: relative;
  height: 1.1em;
  min-width: 4.5em;
  text-align: center;
}

.color-vision-card__phase-text {
  position: absolute;
  inset: 0;
  font-size: var(--nubi-font-size-xs);
  font-weight: var(--nubi-font-weight-medium);
  color: var(--nubi-text-tertiary);
  opacity: 0;
  transition: opacity 0.6s ease-in-out;
}

.color-vision-card__phase-text--visible {
  opacity: 1;
}

.color-vision-card__phase-text--simulated.color-vision-card__phase-text--visible {
  color: var(--nubi-color-primary);
}

@media (prefers-reduced-motion: reduce) {
  .color-vision-card__layer,
  .color-vision-card__phase-text {
    transition: none;
  }
}

.color-vision-card__text {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  text-align: center;
}

.color-vision-card__name {
  font-size: var(--nubi-font-size-sm);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-primary);
  line-height: var(--nubi-line-height-tight);
}

.color-vision-card__description {
  font-size: var(--nubi-font-size-xs);
  color: var(--nubi-text-secondary);
  line-height: var(--nubi-line-height-normal);
}

.color-vision-card__indicator {
  position: absolute;
  top: var(--nubi-spacing-xs);
  right: var(--nubi-spacing-xs);
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
