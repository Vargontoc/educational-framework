<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { DifficultyLevelResponse, CreateDifficultyLevelRequest, DifficultyCode } from '@/shared/types/api'

const { t } = useI18n()

interface Props {
  activityId: number
  level?: DifficultyLevelResponse | null
  apiError?: string | null
}

const props = withDefaults(defineProps<Props>(), {
  level: null,
  apiError: null
})

const emit = defineEmits<{
  (e: 'submit', payload: CreateDifficultyLevelRequest): void
  (e: 'cancel'): void
}>()

const difficultyCode = ref<DifficultyCode>('EASY')
const engineParams = ref('')
const adaptiveThresholdConfig = ref('')
const engineParamsError = ref<string | null>(null)
const adaptiveThresholdError = ref<string | null>(null)

watch(() => props.level, (lvl) => {
  if (lvl) {
    difficultyCode.value = lvl.difficultyCode
    engineParams.value = lvl.engineParams ?? ''
    adaptiveThresholdConfig.value = lvl.adaptiveThresholdConfig ?? ''
  } else {
    resetForm()
  }
}, { immediate: true })

function resetForm() {
  difficultyCode.value = 'EASY'
  engineParams.value = ''
  adaptiveThresholdConfig.value = ''
  engineParamsError.value = null
  adaptiveThresholdError.value = null
}

function validateJson(value: string): boolean {
  if (!value.trim()) return true
  try {
    JSON.parse(value)
    return true
  } catch {
    return false
  }
}

function validate(): boolean {
  engineParamsError.value = null
  adaptiveThresholdError.value = null
  let valid = true

  if (engineParams.value.trim() && !validateJson(engineParams.value)) {
    engineParamsError.value = t('devContent.errors.invalidJson')
    valid = false
  }

  if (adaptiveThresholdConfig.value.trim() && !validateJson(adaptiveThresholdConfig.value)) {
    adaptiveThresholdError.value = t('devContent.errors.invalidJson')
    valid = false
  }

  return valid
}

function handleSubmit() {
  if (!validate()) return

  const payload: CreateDifficultyLevelRequest = {
    activityId: props.activityId,
    difficultyCode: difficultyCode.value,
    engineParams: engineParams.value.trim() || null,
    adaptiveThresholdConfig: adaptiveThresholdConfig.value.trim() || null
  }

  emit('submit', payload)
}

function handleCancel() {
  resetForm()
  emit('cancel')
}
</script>

<template>
  <form class="difficulty-form" @submit.prevent="handleSubmit">
    <h3 class="difficulty-form__title">
      {{ level ? t('devContent.difficultyLevels.edit') : t('devContent.difficultyLevels.create') }}
    </h3>

    <!-- API Error -->
    <div v-if="apiError" class="difficulty-form__api-error">
      {{ apiError }}
    </div>

    <!-- Difficulty Code -->
    <div class="difficulty-form__field">
      <label class="difficulty-form__label" for="difficulty-code">
        {{ t('devContent.difficultyLevels.difficultyCode') }} *
      </label>
      <select
        id="difficulty-code"
        v-model="difficultyCode"
        class="difficulty-form__select"
      >
        <option value="EASY">{{ t('devContent.difficultyCode.EASY') }}</option>
        <option value="MEDIUM">{{ t('devContent.difficultyCode.MEDIUM') }}</option>
        <option value="HARD">{{ t('devContent.difficultyCode.HARD') }}</option>
      </select>
    </div>

    <!-- Engine Params -->
    <div class="difficulty-form__field">
      <label class="difficulty-form__label" for="engine-params">
        {{ t('devContent.difficultyLevels.engineParams') }}
      </label>
      <textarea
        id="engine-params"
        v-model="engineParams"
        class="difficulty-form__textarea"
        :class="{ 'difficulty-form__textarea--error': engineParamsError }"
        :placeholder="t('devContent.difficultyLevels.engineParamsPlaceholder')"
        rows="4"
      />
      <span v-if="engineParamsError" class="difficulty-form__error">{{ engineParamsError }}</span>
    </div>

    <!-- Adaptive Threshold Config -->
    <div class="difficulty-form__field">
      <label class="difficulty-form__label" for="adaptive-threshold">
        {{ t('devContent.difficultyLevels.adaptiveThresholdConfig') }}
      </label>
      <textarea
        id="adaptive-threshold"
        v-model="adaptiveThresholdConfig"
        class="difficulty-form__textarea"
        :class="{ 'difficulty-form__textarea--error': adaptiveThresholdError }"
        :placeholder="t('devContent.difficultyLevels.adaptiveThresholdPlaceholder')"
        rows="4"
      />
      <span v-if="adaptiveThresholdError" class="difficulty-form__error">{{ adaptiveThresholdError }}</span>
    </div>

    <!-- Actions -->
    <div class="difficulty-form__actions">
      <button type="button" class="difficulty-form__btn difficulty-form__btn--secondary" @click="handleCancel">
        {{ t('devContent.difficultyLevels.cancel') }}
      </button>
      <button type="submit" class="difficulty-form__btn difficulty-form__btn--primary">
        {{ level ? t('devContent.difficultyLevels.save') : t('devContent.difficultyLevels.create') }}
      </button>
    </div>
  </form>
</template>

<style scoped>
.difficulty-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding: var(--space-lg);
  background-color: #ffffff;
  border-radius: var(--radius-lg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  max-width: 500px;
}

.difficulty-form__title {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.difficulty-form__api-error {
  padding: var(--space-sm) var(--space-md);
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: var(--radius-md);
  color: #dc2626;
  font-size: var(--font-size-sm);
}

.difficulty-form__field {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.difficulty-form__label {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: #374151;
}

.difficulty-form__select,
.difficulty-form__textarea {
  padding: var(--space-sm) var(--space-md);
  border: 1px solid #d1d5db;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  transition: border-color var(--transition-base);
}

.difficulty-form__select:focus,
.difficulty-form__textarea:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(124, 58, 237, 0.1);
}

.difficulty-form__textarea {
  resize: vertical;
  min-height: 80px;
  font-family: monospace;
}

.difficulty-form__textarea--error {
  border-color: #dc2626;
}

.difficulty-form__error {
  font-size: var(--font-size-xs);
  color: #dc2626;
}

.difficulty-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
  margin-top: var(--space-sm);
}

.difficulty-form__btn {
  min-height: var(--touch-target-min);
  padding: var(--space-sm) var(--space-md);
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  font-weight: 700;
  cursor: pointer;
  transition: background-color var(--transition-base);
}

.difficulty-form__btn--primary {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.difficulty-form__btn--primary:hover {
  background-color: var(--color-primary-dark);
}

.difficulty-form__btn--secondary {
  background-color: #e5e7eb;
  color: #374151;
}

.difficulty-form__btn--secondary:hover {
  background-color: #d1d5db;
}
</style>
