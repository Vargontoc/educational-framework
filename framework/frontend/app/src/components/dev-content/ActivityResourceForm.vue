<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { ActivityResourceResponse, CreateActivityResourceRequest, ResourceType, TopicResponse } from '@/shared/types/api'

const { t } = useI18n()

interface Props {
  activityId: number
  topics: TopicResponse[]
  resource?: ActivityResourceResponse | null
  apiError?: string | null
}

const props = withDefaults(defineProps<Props>(), {
  resource: null,
  apiError: null
})

const emit = defineEmits<{
  (e: 'submit', payload: CreateActivityResourceRequest): void
  (e: 'cancel'): void
}>()

const resourceType = ref<ResourceType>('IMAGE')
const path = ref('')
const topicId = ref<number | null>(null)
const metadata = ref('')
const pathError = ref<string | null>(null)
const metadataError = ref<string | null>(null)

watch(() => props.resource, (res) => {
  if (res) {
    resourceType.value = res.resourceType
    path.value = res.path
    topicId.value = res.topicId ?? null
    metadata.value = res.metadata ?? ''
  } else {
    resetForm()
  }
}, { immediate: true })

function resetForm() {
  resourceType.value = 'IMAGE'
  path.value = ''
  topicId.value = null
  metadata.value = ''
  pathError.value = null
  metadataError.value = null
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
  pathError.value = null
  metadataError.value = null
  let valid = true

  if (!path.value.trim()) {
    pathError.value = t('devContent.form.required')
    valid = false
  }

  if (metadata.value.trim() && !validateJson(metadata.value)) {
    metadataError.value = t('devContent.errors.invalidJson')
    valid = false
  }

  return valid
}

function handleSubmit() {
  if (!validate()) return

  const payload: CreateActivityResourceRequest = {
    activityId: props.activityId,
    topicId: topicId.value,
    resourceType: resourceType.value,
    path: path.value.trim(),
    metadata: metadata.value.trim() || null
  }

  emit('submit', payload)
}

function handleCancel() {
  resetForm()
  emit('cancel')
}
</script>

<template>
  <form class="resource-form" @submit.prevent="handleSubmit">
    <h3 class="resource-form__title">
      {{ resource ? t('devContent.resources.edit') : t('devContent.resources.create') }}
    </h3>

    <!-- V1 Notice -->
    <div class="resource-form__notice">
      {{ t('devContent.resources.v1Notice') }}
    </div>

    <!-- API Error -->
    <div v-if="apiError" class="resource-form__api-error">
      {{ apiError }}
    </div>

    <!-- Resource Type -->
    <div class="resource-form__field">
      <label class="resource-form__label" for="resource-type">
        {{ t('devContent.resources.resourceType') }} *
      </label>
      <select
        id="resource-type"
        v-model="resourceType"
        class="resource-form__select"
      >
        <option value="IMAGE">{{ t('devContent.resourceType.IMAGE') }}</option>
        <option value="AUDIO">{{ t('devContent.resourceType.AUDIO') }}</option>
        <option value="VIDEO">{{ t('devContent.resourceType.VIDEO') }}</option>
      </select>
    </div>

    <!-- Path -->
    <div class="resource-form__field">
      <label class="resource-form__label" for="resource-path">
        {{ t('devContent.resources.path') }} *
      </label>
      <input
        id="resource-path"
        v-model="path"
        type="text"
        class="resource-form__input"
        :class="{ 'resource-form__input--error': pathError }"
        :placeholder="t('devContent.resources.pathPlaceholder')"
        maxlength="500"
      />
      <span v-if="pathError" class="resource-form__error">{{ pathError }}</span>
    </div>

    <!-- Topic (optional) -->
    <div class="resource-form__field">
      <label class="resource-form__label" for="resource-topic">
        {{ t('devContent.resources.topic') }}
      </label>
      <select
        id="resource-topic"
        v-model="topicId"
        class="resource-form__select"
      >
        <option :value="null">{{ t('devContent.resources.selectTopic') }}</option>
        <option v-for="topic in topics" :key="topic.id" :value="topic.id">
          {{ topic.name }}
        </option>
      </select>
    </div>

    <!-- Metadata -->
    <div class="resource-form__field">
      <label class="resource-form__label" for="resource-metadata">
        {{ t('devContent.resources.metadata') }}
      </label>
      <textarea
        id="resource-metadata"
        v-model="metadata"
        class="resource-form__textarea"
        :class="{ 'resource-form__textarea--error': metadataError }"
        :placeholder="t('devContent.resources.metadataPlaceholder')"
        rows="4"
      />
      <span v-if="metadataError" class="resource-form__error">{{ metadataError }}</span>
    </div>

    <!-- Actions -->
    <div class="resource-form__actions">
      <button type="button" class="resource-form__btn resource-form__btn--secondary" @click="handleCancel">
        {{ t('devContent.resources.cancel') }}
      </button>
      <button type="submit" class="resource-form__btn resource-form__btn--primary">
        {{ resource ? t('devContent.resources.save') : t('devContent.resources.create') }}
      </button>
    </div>
  </form>
</template>

<style scoped>
.resource-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding: var(--space-lg);
  background-color: #ffffff;
  border-radius: var(--radius-lg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  max-width: 500px;
}

.resource-form__title {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.resource-form__notice {
  padding: var(--space-sm) var(--space-md);
  background-color: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: var(--radius-md);
  color: #1e40af;
  font-size: var(--font-size-sm);
}

.resource-form__api-error {
  padding: var(--space-sm) var(--space-md);
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: var(--radius-md);
  color: #dc2626;
  font-size: var(--font-size-sm);
}

.resource-form__field {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.resource-form__label {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: #374151;
}

.resource-form__input,
.resource-form__select,
.resource-form__textarea {
  padding: var(--space-sm) var(--space-md);
  border: 1px solid #d1d5db;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  transition: border-color var(--transition-base);
}

.resource-form__input:focus,
.resource-form__select:focus,
.resource-form__textarea:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(124, 58, 237, 0.1);
}

.resource-form__input--error,
.resource-form__textarea--error {
  border-color: #dc2626;
}

.resource-form__error {
  font-size: var(--font-size-xs);
  color: #dc2626;
}

.resource-form__textarea {
  resize: vertical;
  min-height: 80px;
  font-family: monospace;
}

.resource-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
  margin-top: var(--space-sm);
}

.resource-form__btn {
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

.resource-form__btn--primary {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.resource-form__btn--primary:hover {
  background-color: var(--color-primary-dark);
}

.resource-form__btn--secondary {
  background-color: #e5e7eb;
  color: #374151;
}

.resource-form__btn--secondary:hover {
  background-color: #d1d5db;
}
</style>
