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
    <svg
      class="color-vision-card__sample"
      width="120"
      height="56"
      viewBox="0 0 120 56"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
    >
      <defs>
        <filter :id="filterId">
          <feColorMatrix :values="matrixValues" />
        </filter>
      </defs>
      <g :filter="previewed ? `url(#${filterId})` : undefined">
        <circle cx="20" cy="28" r="16" fill="#e53e3e" stroke="#444" stroke-width="1" />
        <circle cx="20" cy="28" r="5" fill="none" stroke="#fff" stroke-width="1.5" opacity="0.85" />

        <polygon points="60,12 76,44 44,44" fill="#38a169" stroke="#444" stroke-width="1" />
        <polygon points="60,22 67,38 53,38" fill="none" stroke="#fff" stroke-width="1.5" opacity="0.85" />

        <rect x="84" y="12" width="32" height="32" fill="#3182ce" stroke="#444" stroke-width="1" />
        <rect x="92" y="20" width="16" height="16" fill="none" stroke="#fff" stroke-width="1.5" opacity="0.85" />
      </g>
    </svg>

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
import { computed, useId } from 'vue'
import { useI18n } from 'vue-i18n'
import { simulateColorVision } from '../../utils/simulateColorVision'

interface Props {
  mode: {
    value: string
    label: string
    description: string
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
const uniqueId = useId()
const filterId = `cvf-${uniqueId}`

const matrixValues = computed(() => simulateColorVision(props.mode.value))

const ariaLabel = computed(() =>
  t('views.ninos.edit.sections.visualAccessibility.cardAriaLabel', {
    mode: props.mode.label,
    description: props.mode.description
  })
)
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
  display: block;
  max-width: 100%;
  height: auto;
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
