<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { CuriosityResponse, CreateCuriosityRequest, TopicResponse, ContentStatus } from '@/shared/types/api'

const { t } = useI18n()

interface Props {
  curiosity?: CuriosityResponse | null
  topics: TopicResponse[]
  apiError?: string | null
}

const props = withDefaults(defineProps<Props>(), {
  curiosity: null,
  apiError: null
})

const emit = defineEmits<{
  (e: 'submit', payload: CreateCuriosityRequest): void
  (e: 'cancel'): void
}>()

const text = ref('')
const topicId = ref<number | null>(null)
const minAge = ref<number | null>(null)
const maxAge = ref<number | null>(null)
const locale = ref('es-ES')
const tags = ref('')
const phoneticHint = ref('')
const status = ref<ContentStatus>('ACTIVE')
const textError = ref<string | null>(null)

const charCount = computed(() => text.value.length)

watch(() => props.curiosity, (cur) => {
  if (cur) {
    text.value = cur.text
    topicId.value = cur.topicId ?? null
    minAge.value = cur.minAge ?? null
    maxAge.value = cur.maxAge ?? null
    locale.value = cur.locale
    tags.value = cur.tags?.join(', ') ?? ''
    phoneticHint.value = cur.phoneticHint ?? ''
    status.value = cur.status
  } else {
    resetForm()
  }
}, { immediate: true })

function resetForm() {
  text.value = ''
  topicId.value = null
  minAge.value = null
  maxAge.value = null
  locale.value = 'es-ES'
  tags.value = ''
  phoneticHint.value = ''
  status.value = 'ACTIVE'
  textError.value = null
}

function validate(): boolean {
  textError.value = null
  let valid = true

  if (!text.value.trim()) {
    textError.value = t('devContent.form.required')
    valid = false
  } else if (text.value.length > 300) {
    textError.value = t('devContent.curiosities.charCount', { count: text.value.length })
    valid = false
  }

  return valid
}

function handleSubmit() {
  if (!validate()) return

  const tagsList = tags.value
    .split(',')
    .map(tag => tag.trim())
    .filter(tag => tag.length > 0)

  const payload: CreateCuriosityRequest = {
    text: text.value.trim(),
    topicId: topicId.value,
    minAge: minAge.value,
    maxAge: maxAge.value,
    tags: tagsList.length > 0 ? tagsList : null,
    locale: locale.value,
    phoneticHint: phoneticHint.value.trim() || null,
    status: status.value
  }

  emit('submit', payload)
}

function handleCancel() {
  resetForm()
  emit('cancel')
}
</script>

<template>
  <form class="curiosity-form" @submit.prevent="handleSubmit">
    <h3 class="curiosity-form__title">
      {{ curiosity ? t('devContent.curiosities.edit') : t('devContent.curiosities.create') }}
    </h3>

    <!-- TTS Helper -->
    <div class="curiosity-form__notice">
      {{ t('devContent.curiosities.ttsHelper') }}
    </div>

    <!-- API Error -->
    <div v-if="apiError" class="curiosity-form__api-error">
      {{ apiError }}
    </div>

    <!-- Text -->
    <div class="curiosity-form__field">
      <label class="curiosity-form__label" for="curiosity-text">
        {{ t('devContent.curiosities.text') }} *
      </label>
      <textarea
        id="curiosity-text"
        v-model="text"
        class="curiosity-form__textarea"
        :class="{ 'curiosity-form__textarea--error': textError }"
        :placeholder="t('devContent.curiosities.textPlaceholder')"
        rows="4"
        maxlength="300"
      />
      <div class="curiosity-form__char-count" :class="{ 'curiosity-form__char-count--warning': charCount > 280 }">
        {{ t('devContent.curiosities.charCount', { count: charCount }) }}
      </div>
      <span v-if="textError" class="curiosity-form__error">{{ textError }}</span>
    </div>

    <!-- Topic -->
    <div class="curiosity-form__field">
      <label class="curiosity-form__label" for="curiosity-topic">
        {{ t('devContent.curiosities.topic') }}
      </label>
      <select
        id="curiosity-topic"
        v-model="topicId"
        class="curiosity-form__select"
      >
        <option :value="null">{{ t('devContent.curiosities.selectTopic') }}</option>
        <option v-for="topic in topics" :key="topic.id" :value="topic.id">
          {{ topic.name }}
        </option>
      </select>
    </div>

    <!-- Age Range -->
    <div class="curiosity-form__row">
      <div class="curiosity-form__field">
        <label class="curiosity-form__label" for="curiosity-min-age">
          {{ t('devContent.curiosities.minAge') }}
        </label>
        <input
          id="curiosity-min-age"
          v-model.number="minAge"
          type="number"
          class="curiosity-form__input"
          min="0"
          max="18"
        />
      </div>

      <div class="curiosity-form__field">
        <label class="curiosity-form__label" for="curiosity-max-age">
          {{ t('devContent.curiosities.maxAge') }}
        </label>
        <input
          id="curiosity-max-age"
          v-model.number="maxAge"
          type="number"
          class="curiosity-form__input"
          min="0"
          max="18"
        />
      </div>
    </div>

    <!-- Locale -->
    <div class="curiosity-form__field">
      <label class="curiosity-form__label" for="curiosity-locale">
        {{ t('devContent.curiosities.locale') }} *
      </label>
      <input
        id="curiosity-locale"
        v-model="locale"
        type="text"
        class="curiosity-form__input"
      />
    </div>

    <!-- Tags -->
    <div class="curiosity-form__field">
      <label class="curiosity-form__label" for="curiosity-tags">
        {{ t('devContent.curiosities.tags') }}
      </label>
      <input
        id="curiosity-tags"
        v-model="tags"
        type="text"
        class="curiosity-form__input"
        :placeholder="t('devContent.curiosities.tagsPlaceholder')"
      />
    </div>

    <!-- Phonetic Hint -->
    <div class="curiosity-form__field">
      <label class="curiosity-form__label" for="curiosity-phonetic">
        {{ t('devContent.curiosities.phoneticHint') }}
      </label>
      <input
        id="curiosity-phonetic"
        v-model="phoneticHint"
        type="text"
        class="curiosity-form__input"
        :placeholder="t('devContent.curiosities.phoneticHintPlaceholder')"
      />
    </div>

    <!-- Status -->
    <div class="curiosity-form__field">
      <label class="curiosity-form__label" for="curiosity-status">
        {{ t('devContent.curiosities.status') }} *
      </label>
      <select
        id="curiosity-status"
        v-model="status"
        class="curiosity-form__select"
      >
        <option value="ACTIVE">{{ t('devContent.status.ACTIVE') }}</option>
        <option value="INACTIVE">{{ t('devContent.status.INACTIVE') }}</option>
        <option value="DRAFT">{{ t('devContent.status.DRAFT') }}</option>
      </select>
    </div>

    <!-- Actions -->
    <div class="curiosity-form__actions">
      <button type="button" class="curiosity-form__btn curiosity-form__btn--secondary" @click="handleCancel">
        {{ t('devContent.curiosities.cancel') }}
      </button>
      <button type="submit" class="curiosity-form__btn curiosity-form__btn--primary">
        {{ curiosity ? t('devContent.curiosities.save') : t('devContent.curiosities.create') }}
      </button>
    </div>
  </form>
</template>

<style scoped>
.curiosity-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding: var(--space-lg);
  background-color: #ffffff;
  border-radius: var(--radius-lg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  max-width: 500px;
}

.curiosity-form__title {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.curiosity-form__notice {
  padding: var(--space-sm) var(--space-md);
  background-color: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: var(--radius-md);
  color: #1e40af;
  font-size: var(--font-size-sm);
}

.curiosity-form__api-error {
  padding: var(--space-sm) var(--space-md);
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: var(--radius-md);
  color: #dc2626;
  font-size: var(--font-size-sm);
}

.curiosity-form__field {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
  flex: 1;
}

.curiosity-form__row {
  display: flex;
  gap: var(--space-md);
}

.curiosity-form__label {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: #374151;
}

.curiosity-form__input,
.curiosity-form__select,
.curiosity-form__textarea {
  padding: var(--space-sm) var(--space-md);
  border: 1px solid #d1d5db;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  transition: border-color var(--transition-base);
}

.curiosity-form__input:focus,
.curiosity-form__select:focus,
.curiosity-form__textarea:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(124, 58, 237, 0.1);
}

.curiosity-form__textarea {
  resize: vertical;
  min-height: 100px;
}

.curiosity-form__textarea--error {
  border-color: #dc2626;
}

.curiosity-form__char-count {
  font-size: var(--font-size-xs);
  color: #6b7280;
  text-align: right;
}

.curiosity-form__char-count--warning {
  color: #f59e0b;
}

.curiosity-form__error {
  font-size: var(--font-size-xs);
  color: #dc2626;
}

.curiosity-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
  margin-top: var(--space-sm);
}

.curiosity-form__btn {
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

.curiosity-form__btn--primary {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.curiosity-form__btn--primary:hover {
  background-color: var(--color-primary-dark);
}

.curiosity-form__btn--secondary {
  background-color: #e5e7eb;
  color: #374151;
}

.curiosity-form__btn--secondary:hover {
  background-color: #d1d5db;
}

@media (max-width: 480px) {
  .curiosity-form__row {
    flex-direction: column;
  }
}
</style>
