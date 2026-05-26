<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { TopicResponse, CreateTopicRequest, CategoryResponse, ContentStatus } from '@/shared/types/api'

const { t } = useI18n()

interface Props {
  topic?: TopicResponse | null
  categories: CategoryResponse[]
  apiError?: string | null
}

const props = withDefaults(defineProps<Props>(), {
  topic: null,
  apiError: null
})

const emit = defineEmits<{
  (e: 'submit', payload: CreateTopicRequest): void
  (e: 'cancel'): void
}>()

const name = ref('')
const description = ref('')
const categoryId = ref<number | null>(null)
const status = ref<ContentStatus>('ACTIVE')
const minAge = ref<number | null>(null)
const maxAge = ref<number | null>(null)
const compatibleVariants = ref('')
const nameError = ref<string | null>(null)
const categoryError = ref<string | null>(null)

watch(() => props.topic, (topic) => {
  if (topic) {
    name.value = topic.name
    description.value = topic.description ?? ''
    categoryId.value = topic.categoryId
    status.value = topic.status
    minAge.value = topic.minAge ?? null
    maxAge.value = topic.maxAge ?? null
    compatibleVariants.value = topic.compatibleVariants?.join(', ') ?? ''
  } else {
    resetForm()
  }
}, { immediate: true })

function resetForm() {
  name.value = ''
  description.value = ''
  categoryId.value = null
  status.value = 'ACTIVE'
  minAge.value = null
  maxAge.value = null
  compatibleVariants.value = ''
  nameError.value = null
  categoryError.value = null
}

function validate(): boolean {
  nameError.value = null
  categoryError.value = null
  let valid = true

  if (!name.value.trim()) {
    nameError.value = t('devContent.form.required')
    valid = false
  }

  if (categoryId.value == null) {
    categoryError.value = t('devContent.form.required')
    valid = false
  }

  return valid
}

function handleSubmit() {
  if (!validate()) return

  const variants = compatibleVariants.value
    .split(',')
    .map(v => v.trim())
    .filter(v => v.length > 0)

  const payload: CreateTopicRequest = {
    name: name.value.trim(),
    description: description.value.trim() || null,
    categoryId: categoryId.value!,
    status: status.value,
    minAge: minAge.value,
    maxAge: maxAge.value,
    compatibleVariants: variants.length > 0 ? variants : null
  }

  emit('submit', payload)
}

function handleCancel() {
  resetForm()
  emit('cancel')
}
</script>

<template>
  <form class="topic-form" @submit.prevent="handleSubmit">
    <h3 class="topic-form__title">
      {{ topic ? t('devContent.topics.edit') : t('devContent.topics.create') }}
    </h3>

    <!-- API Error -->
    <div v-if="apiError" class="topic-form__api-error">
      {{ apiError }}
    </div>

    <!-- Name -->
    <div class="topic-form__field">
      <label class="topic-form__label" for="topic-name">
        {{ t('devContent.topics.name') }} *
      </label>
      <input
        id="topic-name"
        v-model="name"
        type="text"
        class="topic-form__input"
        :class="{ 'topic-form__input--error': nameError }"
        :placeholder="t('devContent.topics.namePlaceholder')"
        maxlength="200"
      />
      <span v-if="nameError" class="topic-form__error">{{ nameError }}</span>
    </div>

    <!-- Category -->
    <div class="topic-form__field">
      <label class="topic-form__label" for="topic-category">
        {{ t('devContent.topics.category') }} *
      </label>
      <select
        id="topic-category"
        v-model="categoryId"
        class="topic-form__select"
        :class="{ 'topic-form__input--error': categoryError }"
      >
        <option :value="null" disabled>{{ t('devContent.topics.selectCategory') }}</option>
        <option v-for="cat in categories" :key="cat.id" :value="cat.id">
          {{ cat.name }}
        </option>
      </select>
      <span v-if="categoryError" class="topic-form__error">{{ categoryError }}</span>
    </div>

    <!-- Description -->
    <div class="topic-form__field">
      <label class="topic-form__label" for="topic-description">
        {{ t('devContent.topics.description') }}
      </label>
      <textarea
        id="topic-description"
        v-model="description"
        class="topic-form__textarea"
        :placeholder="t('devContent.topics.descriptionPlaceholder')"
        rows="3"
      />
    </div>

    <!-- Status -->
    <div class="topic-form__field">
      <label class="topic-form__label" for="topic-status">
        {{ t('devContent.topics.status') }} *
      </label>
      <select
        id="topic-status"
        v-model="status"
        class="topic-form__select"
      >
        <option value="ACTIVE">{{ t('devContent.status.ACTIVE') }}</option>
        <option value="INACTIVE">{{ t('devContent.status.INACTIVE') }}</option>
        <option value="DRAFT">{{ t('devContent.status.DRAFT') }}</option>
      </select>
    </div>

    <!-- Age Range -->
    <div class="topic-form__row">
      <div class="topic-form__field">
        <label class="topic-form__label" for="topic-min-age">
          {{ t('devContent.topics.minAge') }}
        </label>
        <input
          id="topic-min-age"
          v-model.number="minAge"
          type="number"
          class="topic-form__input"
          min="0"
          max="18"
        />
      </div>

      <div class="topic-form__field">
        <label class="topic-form__label" for="topic-max-age">
          {{ t('devContent.topics.maxAge') }}
        </label>
        <input
          id="topic-max-age"
          v-model.number="maxAge"
          type="number"
          class="topic-form__input"
          min="0"
          max="18"
        />
      </div>
    </div>

    <!-- Compatible Variants -->
    <div class="topic-form__field">
      <label class="topic-form__label" for="topic-variants">
        {{ t('devContent.topics.compatibleVariants') }}
      </label>
      <input
        id="topic-variants"
        v-model="compatibleVariants"
        type="text"
        class="topic-form__input"
        placeholder="variant1, variant2"
      />
    </div>

    <!-- Actions -->
    <div class="topic-form__actions">
      <button type="button" class="topic-form__btn topic-form__btn--secondary" @click="handleCancel">
        {{ t('devContent.topics.cancel') }}
      </button>
      <button type="submit" class="topic-form__btn topic-form__btn--primary">
        {{ topic ? t('devContent.topics.save') : t('devContent.topics.create') }}
      </button>
    </div>
  </form>
</template>

<style scoped>
.topic-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding: var(--space-lg);
  background-color: #ffffff;
  border-radius: var(--radius-lg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  max-width: 500px;
}

.topic-form__title {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.topic-form__api-error {
  padding: var(--space-sm) var(--space-md);
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: var(--radius-md);
  color: #dc2626;
  font-size: var(--font-size-sm);
}

.topic-form__field {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
  flex: 1;
}

.topic-form__row {
  display: flex;
  gap: var(--space-md);
}

.topic-form__label {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: #374151;
}

.topic-form__input,
.topic-form__textarea,
.topic-form__select {
  padding: var(--space-sm) var(--space-md);
  border: 1px solid #d1d5db;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  transition: border-color var(--transition-base);
}

.topic-form__input:focus,
.topic-form__textarea:focus,
.topic-form__select:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(124, 58, 237, 0.1);
}

.topic-form__input--error {
  border-color: #dc2626;
}

.topic-form__error {
  font-size: var(--font-size-xs);
  color: #dc2626;
}

.topic-form__textarea {
  resize: vertical;
  min-height: 80px;
}

.topic-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
  margin-top: var(--space-sm);
}

.topic-form__btn {
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

.topic-form__btn--primary {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.topic-form__btn--primary:hover {
  background-color: var(--color-primary-dark);
}

.topic-form__btn--secondary {
  background-color: #e5e7eb;
  color: #374151;
}

.topic-form__btn--secondary:hover {
  background-color: #d1d5db;
}

@media (max-width: 480px) {
  .topic-form__row {
    flex-direction: column;
  }
}
</style>
