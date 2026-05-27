<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { ActivityResponse, CreateActivityRequest, TopicResponse, ContentStatus } from '@/shared/types/api'

const { t } = useI18n()

interface Props {
  activity?: ActivityResponse | null
  topics: TopicResponse[]
  apiError?: string | null
}

const props = withDefaults(defineProps<Props>(), {
  activity: null,
  apiError: null
})

const emit = defineEmits<{
  (e: 'submit', payload: CreateActivityRequest): void
  (e: 'cancel'): void
}>()

const name = ref('')
const description = ref('')
const gameEngineType = ref('')
const status = ref<ContentStatus>('ACTIVE')
const minAge = ref<number | null>(null)
const maxAge = ref<number | null>(null)
const selectedTopicIds = ref<number[]>([])
const nameError = ref<string | null>(null)

watch(() => props.activity, (act) => {
  if (act) {
    name.value = act.name
    description.value = act.description ?? ''
    gameEngineType.value = act.gameEngineType ?? ''
    status.value = act.status
    minAge.value = act.minAge ?? null
    maxAge.value = act.maxAge ?? null
    selectedTopicIds.value = [...act.topicIds]
  } else {
    resetForm()
  }
}, { immediate: true })

function resetForm() {
  name.value = ''
  description.value = ''
  gameEngineType.value = ''
  status.value = 'ACTIVE'
  minAge.value = null
  maxAge.value = null
  selectedTopicIds.value = []
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

  const payload: CreateActivityRequest = {
    name: name.value.trim(),
    description: description.value.trim() || null,
    gameEngineType: gameEngineType.value.trim() || null,
    status: status.value,
    minAge: minAge.value,
    maxAge: maxAge.value,
    topicIds: selectedTopicIds.value.length > 0 ? selectedTopicIds.value : null
  }

  emit('submit', payload)
}

function handleCancel() {
  resetForm()
  emit('cancel')
}

function toggleTopic(topicId: number) {
  const index = selectedTopicIds.value.indexOf(topicId)
  if (index === -1) {
    selectedTopicIds.value.push(topicId)
  } else {
    selectedTopicIds.value.splice(index, 1)
  }
}
</script>

<template>
  <form class="activity-form" @submit.prevent="handleSubmit">
    <h3 class="activity-form__title">
      {{ activity ? t('devContent.activities.edit') : t('devContent.activities.create') }}
    </h3>

    <!-- API Error -->
    <div v-if="apiError" class="activity-form__api-error">
      {{ apiError }}
    </div>

    <!-- Name -->
    <div class="activity-form__field">
      <label class="activity-form__label" for="activity-name">
        {{ t('devContent.activities.name') }} *
      </label>
      <input
        id="activity-name"
        v-model="name"
        type="text"
        class="activity-form__input"
        :class="{ 'activity-form__input--error': nameError }"
        :placeholder="t('devContent.activities.namePlaceholder')"
        maxlength="200"
      />
      <span v-if="nameError" class="activity-form__error">{{ nameError }}</span>
    </div>

    <!-- Description -->
    <div class="activity-form__field">
      <label class="activity-form__label" for="activity-description">
        {{ t('devContent.activities.description') }}
      </label>
      <textarea
        id="activity-description"
        v-model="description"
        class="activity-form__textarea"
        :placeholder="t('devContent.activities.descriptionPlaceholder')"
        rows="3"
      />
    </div>

    <!-- Game Engine Type -->
    <div class="activity-form__field">
      <label class="activity-form__label" for="activity-engine">
        {{ t('devContent.activities.gameEngineType') }}
      </label>
      <input
        id="activity-engine"
        v-model="gameEngineType"
        type="text"
        class="activity-form__input"
        :placeholder="t('devContent.activities.gameEngineTypePlaceholder')"
      />
    </div>

    <!-- Status -->
    <div class="activity-form__field">
      <label class="activity-form__label" for="activity-status">
        {{ t('devContent.activities.status') }} *
      </label>
      <select
        id="activity-status"
        v-model="status"
        class="activity-form__select"
      >
        <option value="ACTIVE">{{ t('devContent.status.ACTIVE') }}</option>
        <option value="INACTIVE">{{ t('devContent.status.INACTIVE') }}</option>
        <option value="DRAFT">{{ t('devContent.status.DRAFT') }}</option>
      </select>
    </div>

    <!-- Age Range -->
    <div class="activity-form__row">
      <div class="activity-form__field">
        <label class="activity-form__label" for="activity-min-age">
          {{ t('devContent.activities.minAge') }}
        </label>
        <input
          id="activity-min-age"
          v-model.number="minAge"
          type="number"
          class="activity-form__input"
          min="0"
          max="18"
        />
      </div>

      <div class="activity-form__field">
        <label class="activity-form__label" for="activity-max-age">
          {{ t('devContent.activities.maxAge') }}
        </label>
        <input
          id="activity-max-age"
          v-model.number="maxAge"
          type="number"
          class="activity-form__input"
          min="0"
          max="18"
        />
      </div>
    </div>

    <!-- Topics Multi-Select -->
    <div class="activity-form__field">
      <label class="activity-form__label">
        {{ t('devContent.activities.topics') }}
      </label>
      <div class="activity-form__checkbox-group">
        <label
          v-for="topic in topics"
          :key="topic.id"
          class="activity-form__checkbox-label"
        >
          <input
            type="checkbox"
            :checked="selectedTopicIds.includes(topic.id)"
            @change="toggleTopic(topic.id)"
          />
          <span>{{ topic.name }}</span>
        </label>
      </div>
    </div>

    <!-- Actions -->
    <div class="activity-form__actions">
      <button type="button" class="activity-form__btn activity-form__btn--secondary" @click="handleCancel">
        {{ t('devContent.activities.cancel') }}
      </button>
      <button type="submit" class="activity-form__btn activity-form__btn--primary">
        {{ activity ? t('devContent.activities.save') : t('devContent.activities.create') }}
      </button>
    </div>
  </form>
</template>

<style scoped>
.activity-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding: var(--space-lg);
  background-color: #ffffff;
  border-radius: var(--radius-lg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  max-width: 500px;
}

.activity-form__title {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.activity-form__api-error {
  padding: var(--space-sm) var(--space-md);
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: var(--radius-md);
  color: #dc2626;
  font-size: var(--font-size-sm);
}

.activity-form__field {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
  flex: 1;
}

.activity-form__row {
  display: flex;
  gap: var(--space-md);
}

.activity-form__label {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: #374151;
}

.activity-form__input,
.activity-form__textarea,
.activity-form__select {
  padding: var(--space-sm) var(--space-md);
  border: 1px solid #d1d5db;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  transition: border-color var(--transition-base);
}

.activity-form__input:focus,
.activity-form__textarea:focus,
.activity-form__select:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(124, 58, 237, 0.1);
}

.activity-form__input--error {
  border-color: #dc2626;
}

.activity-form__error {
  font-size: var(--font-size-xs);
  color: #dc2626;
}

.activity-form__textarea {
  resize: vertical;
  min-height: 80px;
}

.activity-form__checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-sm);
  padding: var(--space-sm);
  border: 1px solid #d1d5db;
  border-radius: var(--radius-md);
  max-height: 150px;
  overflow-y: auto;
}

.activity-form__checkbox-label {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  font-size: var(--font-size-sm);
  cursor: pointer;
}

.activity-form__checkbox-label input[type="checkbox"] {
  width: 16px;
  height: 16px;
}

.activity-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
  margin-top: var(--space-sm);
}

.activity-form__btn {
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

.activity-form__btn--primary {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.activity-form__btn--primary:hover {
  background-color: var(--color-primary-dark);
}

.activity-form__btn--secondary {
  background-color: #e5e7eb;
  color: #374151;
}

.activity-form__btn--secondary:hover {
  background-color: #d1d5db;
}

@media (max-width: 480px) {
  .activity-form__row {
    flex-direction: column;
  }
}
</style>
