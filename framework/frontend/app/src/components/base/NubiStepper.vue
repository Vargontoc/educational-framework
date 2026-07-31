<template>
  <div class="nubi-stepper">
    <div class="nubi-stepper__header">
      <div
        v-for="(step, index) in steps"
        :key="index"
        :class="[
          'nubi-stepper__step',
          {
            'nubi-stepper__step--active': index === currentStep,
            'nubi-stepper__step--completed': index < currentStep,
            'nubi-stepper__step--clickable': index < currentStep
          }
        ]"
        @click="handleStepClick(index)"
      >
        <div class="nubi-stepper__indicator">
          <NubiIcon v-if="index < currentStep" name="check" :size="16" />
          <span v-else>{{ index + 1 }}</span>
        </div>
        <span class="nubi-stepper__label">{{ step }}</span>
      </div>
    </div>
    
    <div class="nubi-stepper__content">
      <slot :current-step="currentStep" />
    </div>
    
    <div class="nubi-stepper__footer">
      <NubiButton
        v-if="currentStep > 0"
        variant="secondary"
        @click="previous"
      >
        {{ t('components.stepper.previous') }}
      </NubiButton>
      
      <NubiButton
        v-if="currentStep < steps.length - 1"
        variant="primary"
        @click="next"
      >
        {{ t('components.stepper.next') }}
      </NubiButton>
      
      <NubiButton
        v-if="currentStep === steps.length - 1"
        variant="primary"
        @click="complete"
      >
        {{ t('components.stepper.complete') }}
      </NubiButton>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * NubiStepper - Navegación entre pasos de un formulario
 * 
 * Características:
 * - Indicador visual de paso actual
 * - Pasos completados clickeables
 * - Navegación anterior/siguiente
 * - Slot para contenido del paso
 */

import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import NubiButton from './NubiButton.vue'
import NubiIcon from './NubiIcon.vue'

interface Props {
  /** Labels de los pasos */
  steps: string[]
  /** Paso actual (v-model) */
  modelValue?: number
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: 0
})

const emit = defineEmits<{
  'update:modelValue': [value: number]
  next: [step: number]
  previous: [step: number]
  complete: []
}>()

const { t } = useI18n()

const currentStep = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  currentStep.value = val
})

function next() {
  if (currentStep.value < props.steps.length - 1) {
    currentStep.value++
    emit('update:modelValue', currentStep.value)
    emit('next', currentStep.value)
  }
}

function previous() {
  if (currentStep.value > 0) {
    currentStep.value--
    emit('update:modelValue', currentStep.value)
    emit('previous', currentStep.value)
  }
}

function complete() {
  emit('complete')
}

function handleStepClick(index: number) {
  if (index < currentStep.value) {
    currentStep.value = index
    emit('update:modelValue', currentStep.value)
  }
}
</script>

<style scoped>
.nubi-stepper {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-lg);
}

.nubi-stepper__header {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-sm);
  overflow-x: auto;
  padding-bottom: var(--nubi-spacing-sm);
}

.nubi-stepper__step {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-sm);
  flex-shrink: 0;
}

.nubi-stepper__step:not(:last-child)::after {
  content: '';
  width: 40px;
  height: 2px;
  background-color: var(--nubi-border-default);
  margin: 0 var(--nubi-spacing-xs);
}

.nubi-stepper__step--completed:not(:last-child)::after {
  background-color: var(--nubi-color-primary);
}

.nubi-stepper__indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: var(--nubi-radius-full);
  background-color: var(--nubi-bg-surface-tertiary);
  color: var(--nubi-text-secondary);
  font-size: var(--nubi-font-size-sm);
  font-weight: var(--nubi-font-weight-semibold);
  flex-shrink: 0;
}

.nubi-stepper__step--active .nubi-stepper__indicator {
  background-color: var(--nubi-color-primary);
  color: var(--nubi-color-white);
}

.nubi-stepper__step--completed .nubi-stepper__indicator {
  background-color: var(--nubi-color-primary);
  color: var(--nubi-color-white);
}

.nubi-stepper__step--clickable {
  cursor: pointer;
}

.nubi-stepper__step--clickable:hover .nubi-stepper__indicator {
  box-shadow: 0 0 0 2px var(--nubi-color-focus);
}

.nubi-stepper__label {
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-secondary);
  white-space: nowrap;
}

.nubi-stepper__step--active .nubi-stepper__label {
  color: var(--nubi-text-primary);
  font-weight: var(--nubi-font-weight-medium);
}

.nubi-stepper__step--completed .nubi-stepper__label {
  color: var(--nubi-text-primary);
}

.nubi-stepper__content {
  min-height: 200px;
}

.nubi-stepper__footer {
  display: flex;
  gap: var(--nubi-spacing-sm);
  justify-content: flex-end;
  padding-top: var(--nubi-spacing-md);
  border-top: 1px solid var(--nubi-border-default);
}
</style>
