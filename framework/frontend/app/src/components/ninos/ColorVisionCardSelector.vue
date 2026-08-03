<template>
  <div class="color-vision-card-selector" role="radiogroup" :aria-label="selectorLabel">
    <div class="color-vision-card-selector__grid">
      <ColorVisionCard
        v-for="mode in modes"
        :key="mode.value"
        :mode="mode"
        :selected="modelValue === mode.value"
        :previewed="previewedMode === mode.value || modelValue === mode.value"
        @select="$emit('update:modelValue', mode.value)"
        @explore="previewedMode = mode.value"
        @unexplore="previewedMode = null"
      />
    </div>
    <aside class="color-vision-card-selector__warning" role="note">
      {{ warningText }}
    </aside>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import ColorVisionCard from './ColorVisionCard.vue'

interface Props {
  modelValue: string
  modes: ReadonlyArray<{
    value: string
    label: string
    description: string
  }>
}

defineProps<Props>()

defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const { t } = useI18n()

const previewedMode = ref<string | null>(null)

const selectorLabel = t('views.ninos.edit.sections.visualAccessibility.selectorLabel')
const warningText = t('views.ninos.edit.sections.visualAccessibility.warning')
</script>

<style scoped>
.color-vision-card-selector {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-md);
}

.color-vision-card-selector__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: var(--nubi-spacing-md);
}

.color-vision-card-selector__warning {
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-secondary);
  background-color: var(--nubi-bg-surface-secondary);
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
  border-radius: var(--nubi-radius-md);
  border-left: 3px solid var(--nubi-color-primary);
  margin: 0;
  line-height: var(--nubi-line-height-relaxed);
}
</style>
