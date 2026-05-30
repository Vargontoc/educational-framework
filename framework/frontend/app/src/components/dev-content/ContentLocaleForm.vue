<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { ContentLocaleResponse, CreateContentLocaleRequest, LocaleEntityType } from '@/shared/types/api'

const { t } = useI18n()

interface Props {
  locale?: ContentLocaleResponse | null
  initialEntityType?: LocaleEntityType | null
  initialEntityId?: number | null
  apiError?: string | null
}

const props = withDefaults(defineProps<Props>(), {
  locale: null,
  initialEntityType: null,
  initialEntityId: null,
  apiError: null
})

const emit = defineEmits<{
  (e: 'submit', payload: CreateContentLocaleRequest): void
  (e: 'cancel'): void
}>()

const entityTypes: LocaleEntityType[] = ['CATEGORY', 'TOPIC', 'ACTIVITY', 'DIFFICULTY_LEVEL', 'ACTIVITY_RESOURCE']

const entityType = ref<LocaleEntityType | null>(null)
const entityId = ref<number | null>(null)
const localeCode = ref('es-ES')
const name = ref('')
const description = ref('')
const nameError = ref<string | null>(null)
const entityError = ref<string | null>(null)

watch(() => props.locale, (loc) => {
  if (loc) {
    entityType.value = loc.entityType as LocaleEntityType
    entityId.value = loc.entityId
    localeCode.value = loc.localeCode
    name.value = loc.name
    description.value = loc.description ?? ''
  } else {
    resetForm()
  }
}, { immediate: true })

watch(() => props.initialEntityType, (type) => {
  if (type && !props.locale) {
    entityType.value = type
  }
}, { immediate: true })

watch(() => props.initialEntityId, (id) => {
  if (id != null && !props.locale) {
    entityId.value = id
  }
}, { immediate: true })

function resetForm() {
  entityType.value = props.initialEntityType
  entityId.value = props.initialEntityId
  localeCode.value = 'es-ES'
  name.value = ''
  description.value = ''
  nameError.value = null
  entityError.value = null
}

function validate(): boolean {
  nameError.value = null
  entityError.value = null
  let valid = true

  if (!entityType.value) {
    entityError.value = t('devContent.form.required')
    valid = false
  }

  if (entityId.value == null) {
    entityError.value = t('devContent.form.required')
    valid = false
  }

  if (!name.value.trim()) {
    nameError.value = t('devContent.form.required')
    valid = false
  }

  return valid
}

function handleSubmit() {
  if (!validate()) return

  const payload: CreateContentLocaleRequest = {
    entityType: entityType.value!,
    entityId: entityId.value!,
    localeCode: localeCode.value,
    name: name.value.trim(),
    description: description.value.trim() || null
  }

  emit('submit', payload)
}

function handleCancel() {
  resetForm()
  emit('cancel')
}
</script>

<template>
  <form class="locale-form" @submit.prevent="handleSubmit">
    <h3 class="locale-form__title">
      {{ locale ? t('devContent.locales.edit') : t('devContent.locales.create') }}
    </h3>

    <!-- i18n Notice -->
    <div class="locale-form__notice">
      {{ t('devContent.locales.i18nNotice') }}
    </div>

    <!-- API Error -->
    <div v-if="apiError" class="locale-form__api-error">
      {{ apiError }}
    </div>

    <!-- Entity Type -->
    <div class="locale-form__field">
      <label class="locale-form__label" for="locale-entity-type">
        {{ t('devContent.locales.entityType') }} *
      </label>
      <select
        id="locale-entity-type"
        v-model="entityType"
        class="locale-form__select"
        :class="{ 'locale-form__input--error': entityError }"
        :disabled="!!locale"
      >
        <option :value="null" disabled>{{ t('devContent.locales.selectEntityType') }}</option>
        <option v-for="type in entityTypes" :key="type" :value="type">
          {{ t(`devContent.localeEntityType.${type}`) }}
        </option>
      </select>
    </div>

    <!-- Entity ID -->
    <div class="locale-form__field">
      <label class="locale-form__label" for="locale-entity-id">
        {{ t('devContent.locales.entityId') }} *
      </label>
      <input
        id="locale-entity-id"
        v-model.number="entityId"
        type="number"
        class="locale-form__input"
        :class="{ 'locale-form__input--error': entityError }"
        :placeholder="t('devContent.locales.selectEntityId')"
        min="1"
        :disabled="!!locale"
      />
      <span v-if="entityError" class="locale-form__error">{{ entityError }}</span>
    </div>

    <!-- Locale Code -->
    <div class="locale-form__field">
      <label class="locale-form__label" for="locale-code">
        {{ t('devContent.locales.localeCode') }} *
      </label>
      <input
        id="locale-code"
        v-model="localeCode"
        type="text"
        class="locale-form__input"
        :disabled="!!locale"
      />
    </div>

    <!-- Name -->
    <div class="locale-form__field">
      <label class="locale-form__label" for="locale-name">
        {{ t('devContent.locales.name') }} *
      </label>
      <input
        id="locale-name"
        v-model="name"
        type="text"
        class="locale-form__input"
        :class="{ 'locale-form__input--error': nameError }"
        :placeholder="t('devContent.locales.namePlaceholder')"
        maxlength="200"
      />
      <span v-if="nameError" class="locale-form__error">{{ nameError }}</span>
    </div>

    <!-- Description -->
    <div class="locale-form__field">
      <label class="locale-form__label" for="locale-description">
        {{ t('devContent.locales.description') }}
      </label>
      <textarea
        id="locale-description"
        v-model="description"
        class="locale-form__textarea"
        :placeholder="t('devContent.locales.descriptionPlaceholder')"
        rows="3"
      />
    </div>

    <!-- Actions -->
    <div class="locale-form__actions">
      <button type="button" class="locale-form__btn locale-form__btn--secondary" @click="handleCancel">
        {{ t('devContent.locales.cancel') }}
      </button>
      <button type="submit" class="locale-form__btn locale-form__btn--primary">
        {{ locale ? t('devContent.locales.save') : t('devContent.locales.create') }}
      </button>
    </div>
  </form>
</template>

<style scoped>
.locale-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding: var(--space-lg);
  background-color: #ffffff;
  border-radius: var(--radius-lg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  max-width: 500px;
}

.locale-form__title {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.locale-form__notice {
  padding: var(--space-sm) var(--space-md);
  background-color: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: var(--radius-md);
  color: #1e40af;
  font-size: var(--font-size-sm);
}

.locale-form__api-error {
  padding: var(--space-sm) var(--space-md);
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: var(--radius-md);
  color: #dc2626;
  font-size: var(--font-size-sm);
}

.locale-form__field {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.locale-form__label {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: #374151;
}

.locale-form__input,
.locale-form__select,
.locale-form__textarea {
  padding: var(--space-sm) var(--space-md);
  border: 1px solid #d1d5db;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  transition: border-color var(--transition-base);
}

.locale-form__input:focus,
.locale-form__select:focus,
.locale-form__textarea:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(124, 58, 237, 0.1);
}

.locale-form__input--error {
  border-color: #dc2626;
}

.locale-form__error {
  font-size: var(--font-size-xs);
  color: #dc2626;
}

.locale-form__textarea {
  resize: vertical;
  min-height: 80px;
}

.locale-form__input:disabled,
.locale-form__select:disabled {
  background-color: #f3f4f6;
  cursor: not-allowed;
}

.locale-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
  margin-top: var(--space-sm);
}

.locale-form__btn {
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

.locale-form__btn--primary {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.locale-form__btn--primary:hover {
  background-color: var(--color-primary-dark);
}

.locale-form__btn--secondary {
  background-color: #e5e7eb;
  color: #374151;
}

.locale-form__btn--secondary:hover {
  background-color: #d1d5db;
}
</style>
