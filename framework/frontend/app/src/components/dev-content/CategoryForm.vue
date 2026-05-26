<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { CategoryResponse, CreateCategoryRequest, ContentStatus } from '@/shared/types/api'

const { t } = useI18n()

interface Props {
  category?: CategoryResponse | null
  apiError?: string | null
}

const props = withDefaults(defineProps<Props>(), {
  category: null,
  apiError: null
})

const emit = defineEmits<{
  (e: 'submit', payload: CreateCategoryRequest): void
  (e: 'cancel'): void
}>()

const name = ref('')
const description = ref('')
const status = ref<ContentStatus>('ACTIVE')
const displayOrder = ref<number | null>(null)
const iconUrl = ref('')
const nameError = ref<string | null>(null)

watch(() => props.category, (cat) => {
  if (cat) {
    name.value = cat.name
    description.value = cat.description ?? ''
    status.value = cat.status
    displayOrder.value = cat.displayOrder ?? null
    iconUrl.value = cat.iconUrl ?? ''
  } else {
    resetForm()
  }
}, { immediate: true })

function resetForm() {
  name.value = ''
  description.value = ''
  status.value = 'ACTIVE'
  displayOrder.value = null
  iconUrl.value = ''
  nameError.value = null
}

function validate(): boolean {
  nameError.value = null
  if (!name.value.trim()) {
    nameError.value = t('devContent.form.required')
    return false
  }
  return true
}

function handleSubmit() {
  if (!validate()) return

  const payload: CreateCategoryRequest = {
    name: name.value.trim(),
    description: description.value.trim() || null,
    status: status.value,
    displayOrder: displayOrder.value,
    iconUrl: iconUrl.value.trim() || null
  }

  emit('submit', payload)
}

function handleCancel() {
  resetForm()
  emit('cancel')
}
</script>

<template>
  <form class="category-form" @submit.prevent="handleSubmit">
    <h3 class="category-form__title">
      {{ category ? t('devContent.categories.edit') : t('devContent.categories.create') }}
    </h3>

    <!-- API Error -->
    <div v-if="apiError" class="category-form__api-error">
      {{ apiError }}
    </div>

    <!-- Name -->
    <div class="category-form__field">
      <label class="category-form__label" for="category-name">
        {{ t('devContent.categories.name') }} *
      </label>
      <input
        id="category-name"
        v-model="name"
        type="text"
        class="category-form__input"
        :class="{ 'category-form__input--error': nameError }"
        :placeholder="t('devContent.categories.namePlaceholder')"
        maxlength="200"
      />
      <span v-if="nameError" class="category-form__error">{{ nameError }}</span>
    </div>

    <!-- Description -->
    <div class="category-form__field">
      <label class="category-form__label" for="category-description">
        {{ t('devContent.categories.description') }}
      </label>
      <textarea
        id="category-description"
        v-model="description"
        class="category-form__textarea"
        :placeholder="t('devContent.categories.descriptionPlaceholder')"
        rows="3"
      />
    </div>

    <!-- Status -->
    <div class="category-form__field">
      <label class="category-form__label" for="category-status">
        {{ t('devContent.categories.status') }} *
      </label>
      <select
        id="category-status"
        v-model="status"
        class="category-form__select"
      >
        <option value="ACTIVE">{{ t('devContent.status.ACTIVE') }}</option>
        <option value="INACTIVE">{{ t('devContent.status.INACTIVE') }}</option>
        <option value="DRAFT">{{ t('devContent.status.DRAFT') }}</option>
      </select>
    </div>

    <!-- Display Order -->
    <div class="category-form__field">
      <label class="category-form__label" for="category-order">
        {{ t('devContent.categories.displayOrder') }}
      </label>
      <input
        id="category-order"
        v-model.number="displayOrder"
        type="number"
        class="category-form__input"
        min="0"
      />
    </div>

    <!-- Icon URL -->
    <div class="category-form__field">
      <label class="category-form__label" for="category-icon">
        {{ t('devContent.categories.iconUrl') }}
      </label>
      <input
        id="category-icon"
        v-model="iconUrl"
        type="text"
        class="category-form__input"
        :placeholder="t('devContent.categories.iconUrlPlaceholder')"
      />
    </div>

    <!-- Actions -->
    <div class="category-form__actions">
      <button type="button" class="category-form__btn category-form__btn--secondary" @click="handleCancel">
        {{ t('devContent.categories.cancel') }}
      </button>
      <button type="submit" class="category-form__btn category-form__btn--primary">
        {{ category ? t('devContent.categories.save') : t('devContent.categories.create') }}
      </button>
    </div>
  </form>
</template>

<style scoped>
.category-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding: var(--space-lg);
  background-color: #ffffff;
  border-radius: var(--radius-lg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  max-width: 500px;
}

.category-form__title {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.category-form__api-error {
  padding: var(--space-sm) var(--space-md);
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: var(--radius-md);
  color: #dc2626;
  font-size: var(--font-size-sm);
}

.category-form__field {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.category-form__label {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: #374151;
}

.category-form__input,
.category-form__textarea,
.category-form__select {
  padding: var(--space-sm) var(--space-md);
  border: 1px solid #d1d5db;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  transition: border-color var(--transition-base);
}

.category-form__input:focus,
.category-form__textarea:focus,
.category-form__select:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(124, 58, 237, 0.1);
}

.category-form__input--error {
  border-color: #dc2626;
}

.category-form__error {
  font-size: var(--font-size-xs);
  color: #dc2626;
}

.category-form__textarea {
  resize: vertical;
  min-height: 80px;
}

.category-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
  margin-top: var(--space-sm);
}

.category-form__btn {
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

.category-form__btn--primary {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.category-form__btn--primary:hover {
  background-color: var(--color-primary-dark);
}

.category-form__btn--secondary {
  background-color: #e5e7eb;
  color: #374151;
}

.category-form__btn--secondary:hover {
  background-color: #d1d5db;
}
</style>
