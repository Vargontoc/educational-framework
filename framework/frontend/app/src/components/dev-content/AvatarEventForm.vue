<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { AvatarEventCatalogResponse, CreateAvatarEventCatalogRequest, AvatarEventType, AvatarTone, ContentStatus } from '@/shared/types/api'

const { t } = useI18n()

interface Props {
  event?: AvatarEventCatalogResponse | null
  apiError?: string | null
}

const props = withDefaults(defineProps<Props>(), {
  event: null,
  apiError: null
})

const emit = defineEmits<{
  (e: 'submit', payload: CreateAvatarEventCatalogRequest): void
  (e: 'cancel'): void
}>()

const eventTypes: AvatarEventType[] = [
  'ACTIVITY_COMPLETED',
  'ACTIVITY_STARTED',
  'ACTIVITY_FAILED',
  'HELP_REQUESTED',
  'OUT_OF_SCOPE_QUERY',
  'CURIOSITY_REQUESTED'
]

const tones: AvatarTone[] = ['CALM', 'JOYFUL', 'ENTHUSIASTIC', 'SERIOUS', 'NEUTRAL']

const eventType = ref<AvatarEventType>('ACTIVITY_COMPLETED')
const tone = ref<AvatarTone>('NEUTRAL')
const locale = ref('es-ES')
const messageText = ref('')
const status = ref<ContentStatus>('ACTIVE')
const messageTextError = ref<string | null>(null)

const charCount = computed(() => messageText.value.length)

watch(() => props.event, (evt) => {
  if (evt) {
    eventType.value = evt.eventType
    tone.value = evt.tone
    locale.value = evt.locale
    messageText.value = evt.messageText
    status.value = evt.status
  } else {
    resetForm()
  }
}, { immediate: true })

function resetForm() {
  eventType.value = 'ACTIVITY_COMPLETED'
  tone.value = 'NEUTRAL'
  locale.value = 'es-ES'
  messageText.value = ''
  status.value = 'ACTIVE'
  messageTextError.value = null
}

function validate(): boolean {
  messageTextError.value = null
  let valid = true

  if (!messageText.value.trim()) {
    messageTextError.value = t('devContent.form.required')
    valid = false
  } else if (messageText.value.length > 300) {
    messageTextError.value = t('devContent.avatarEvents.charCount', { count: messageText.value.length })
    valid = false
  }

  return valid
}

function handleSubmit() {
  if (!validate()) return

  const payload: CreateAvatarEventCatalogRequest = {
    eventType: eventType.value,
    tone: tone.value,
    locale: locale.value,
    messageText: messageText.value.trim(),
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
  <form class="avatar-form" @submit.prevent="handleSubmit">
    <h3 class="avatar-form__title">
      {{ event ? t('devContent.avatarEvents.edit') : t('devContent.avatarEvents.create') }}
    </h3>

    <!-- TTS Helper -->
    <div class="avatar-form__notice">
      {{ t('devContent.avatarEvents.ttsHelper') }}
    </div>

    <!-- API Error -->
    <div v-if="apiError" class="avatar-form__api-error">
      {{ apiError }}
    </div>

    <!-- Event Type -->
    <div class="avatar-form__field">
      <label class="avatar-form__label" for="event-type">
        {{ t('devContent.avatarEvents.eventType') }} *
      </label>
      <select
        id="event-type"
        v-model="eventType"
        class="avatar-form__select"
      >
        <option v-for="type in eventTypes" :key="type" :value="type">
          {{ t(`devContent.avatarEventType.${type}`) }}
        </option>
      </select>
    </div>

    <!-- Tone -->
    <div class="avatar-form__field">
      <label class="avatar-form__label" for="event-tone">
        {{ t('devContent.avatarEvents.tone') }} *
      </label>
      <select
        id="event-tone"
        v-model="tone"
        class="avatar-form__select"
      >
        <option v-for="avatarTone in tones" :key="avatarTone" :value="avatarTone">
          {{ t(`devContent.avatarTone.${avatarTone}`) }}
        </option>
      </select>
    </div>

    <!-- Locale -->
    <div class="avatar-form__field">
      <label class="avatar-form__label" for="event-locale">
        {{ t('devContent.avatarEvents.locale') }} *
      </label>
      <input
        id="event-locale"
        v-model="locale"
        type="text"
        class="avatar-form__input"
      />
    </div>

    <!-- Message Text -->
    <div class="avatar-form__field">
      <label class="avatar-form__label" for="event-message">
        {{ t('devContent.avatarEvents.messageText') }} *
      </label>
      <textarea
        id="event-message"
        v-model="messageText"
        class="avatar-form__textarea"
        :class="{ 'avatar-form__textarea--error': messageTextError }"
        :placeholder="t('devContent.avatarEvents.messageTextPlaceholder')"
        rows="4"
        maxlength="300"
      />
      <div class="avatar-form__char-count" :class="{ 'avatar-form__char-count--warning': charCount > 280 }">
        {{ t('devContent.avatarEvents.charCount', { count: charCount }) }}
      </div>
      <span v-if="messageTextError" class="avatar-form__error">{{ messageTextError }}</span>
    </div>

    <!-- Status -->
    <div class="avatar-form__field">
      <label class="avatar-form__label" for="event-status">
        {{ t('devContent.avatarEvents.status') }} *
      </label>
      <select
        id="event-status"
        v-model="status"
        class="avatar-form__select"
      >
        <option value="ACTIVE">{{ t('devContent.status.ACTIVE') }}</option>
        <option value="INACTIVE">{{ t('devContent.status.INACTIVE') }}</option>
        <option value="DRAFT">{{ t('devContent.status.DRAFT') }}</option>
      </select>
    </div>

    <!-- Actions -->
    <div class="avatar-form__actions">
      <button type="button" class="avatar-form__btn avatar-form__btn--secondary" @click="handleCancel">
        {{ t('devContent.avatarEvents.cancel') }}
      </button>
      <button type="submit" class="avatar-form__btn avatar-form__btn--primary">
        {{ event ? t('devContent.avatarEvents.save') : t('devContent.avatarEvents.create') }}
      </button>
    </div>
  </form>
</template>

<style scoped>
.avatar-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding: var(--space-lg);
  background-color: #ffffff;
  border-radius: var(--radius-lg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  max-width: 500px;
}

.avatar-form__title {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.avatar-form__notice {
  padding: var(--space-sm) var(--space-md);
  background-color: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: var(--radius-md);
  color: #1e40af;
  font-size: var(--font-size-sm);
}

.avatar-form__api-error {
  padding: var(--space-sm) var(--space-md);
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: var(--radius-md);
  color: #dc2626;
  font-size: var(--font-size-sm);
}

.avatar-form__field {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.avatar-form__label {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: #374151;
}

.avatar-form__input,
.avatar-form__select,
.avatar-form__textarea {
  padding: var(--space-sm) var(--space-md);
  border: 1px solid #d1d5db;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  transition: border-color var(--transition-base);
}

.avatar-form__input:focus,
.avatar-form__select:focus,
.avatar-form__textarea:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(124, 58, 237, 0.1);
}

.avatar-form__textarea {
  resize: vertical;
  min-height: 100px;
}

.avatar-form__textarea--error {
  border-color: #dc2626;
}

.avatar-form__char-count {
  font-size: var(--font-size-xs);
  color: #6b7280;
  text-align: right;
}

.avatar-form__char-count--warning {
  color: #f59e0b;
}

.avatar-form__error {
  font-size: var(--font-size-xs);
  color: #dc2626;
}

.avatar-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
  margin-top: var(--space-sm);
}

.avatar-form__btn {
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

.avatar-form__btn--primary {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.avatar-form__btn--primary:hover {
  background-color: var(--color-primary-dark);
}

.avatar-form__btn--secondary {
  background-color: #e5e7eb;
  color: #374151;
}

.avatar-form__btn--secondary:hover {
  background-color: #d1d5db;
}
</style>
