<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useDevContentStore } from '@/stores/useDevContentStore'
import type { AvatarEventCatalogResponse, CreateAvatarEventCatalogRequest, AvatarEventType, AvatarTone } from '@/shared/types/api'
import AvatarEventForm from './AvatarEventForm.vue'

const { t } = useI18n()
const store = useDevContentStore()

const showForm = ref(false)
const editingEvent = ref<AvatarEventCatalogResponse | null>(null)
const formError = ref<string | null>(null)
const fetchingEdit = ref(false)

const eventTypes: AvatarEventType[] = [
  'ACTIVITY_COMPLETED',
  'ACTIVITY_STARTED',
  'ACTIVITY_FAILED',
  'HELP_REQUESTED',
  'OUT_OF_SCOPE_QUERY',
  'CURIOSITY_REQUESTED'
]

const tones: AvatarTone[] = ['CALM', 'JOYFUL', 'ENTHUSIASTIC', 'SERIOUS', 'NEUTRAL']

const eventTypeFilter = ref<AvatarEventType | null>(null)
const toneFilter = ref<AvatarTone | null>(null)
const localeFilter = ref<string | null>(null)

onMounted(() => {
  store.fetchAvatarEvents()
})

function handleFilterChange() {
  store.setAvatarEventFilters(eventTypeFilter.value, toneFilter.value, localeFilter.value)
}

function handleCreate() {
  editingEvent.value = null
  formError.value = null
  showForm.value = true
}

async function handleEdit(event: AvatarEventCatalogResponse) {
  formError.value = null
  fetchingEdit.value = true
  try {
    const freshEvent = await store.getAvatarEventById(event.id)
    editingEvent.value = freshEvent
    showForm.value = true
  } catch (error: unknown) {
    const maybeAxiosError = error as {
      response?: { status?: number }
    }
    if (maybeAxiosError.response?.status === 404) {
      formError.value = t('devContent.errors.notFound')
    } else {
      formError.value = t('devContent.errors.serverError')
    }
  } finally {
    fetchingEdit.value = false
  }
}

function handleCancel() {
  showForm.value = false
  editingEvent.value = null
  formError.value = null
}

async function handleSubmit(payload: CreateAvatarEventCatalogRequest) {
  formError.value = null
  try {
    if (editingEvent.value) {
      await store.updateAvatarEvent(editingEvent.value.id, payload)
    } else {
      await store.createAvatarEvent(payload)
    }
    showForm.value = false
    editingEvent.value = null
  } catch (error: unknown) {
    const maybeAxiosError = error as {
      response?: { status?: number; data?: { message?: string; errors?: string[] } }
    }
    const status = maybeAxiosError.response?.status
    const apiErrors = maybeAxiosError.response?.data?.errors
    const apiMessage = maybeAxiosError.response?.data?.message

    if (status === 409) {
      formError.value = t('devContent.avatarEvents.conflictError')
    } else if (status === 400) {
      formError.value = apiErrors?.[0] ?? apiMessage ?? t('devContent.errors.badRequest')
    } else {
      formError.value = t('devContent.errors.serverError')
    }
  }
}

function handleRetry() {
  store.fetchAvatarEvents()
}
</script>

<template>
  <div class="avatar-list">
    <!-- Filters -->
    <div class="avatar-list__filters">
      <div class="avatar-list__filter-field">
        <label class="avatar-list__label" for="event-type-filter">
          {{ t('devContent.avatarEvents.eventType') }}
        </label>
        <select
          id="event-type-filter"
          v-model="eventTypeFilter"
          class="avatar-list__select"
          @change="handleFilterChange"
        >
          <option :value="null">{{ t('devContent.avatarEvents.selectEventType') }}</option>
          <option v-for="type in eventTypes" :key="type" :value="type">
            {{ t(`devContent.avatarEventType.${type}`) }}
          </option>
        </select>
      </div>

      <div class="avatar-list__filter-field">
        <label class="avatar-list__label" for="tone-filter">
          {{ t('devContent.avatarEvents.tone') }}
        </label>
        <select
          id="tone-filter"
          v-model="toneFilter"
          class="avatar-list__select"
          @change="handleFilterChange"
        >
          <option :value="null">{{ t('devContent.avatarEvents.selectTone') }}</option>
          <option v-for="tone in tones" :key="tone" :value="tone">
            {{ t(`devContent.avatarTone.${tone}`) }}
          </option>
        </select>
      </div>

      <div class="avatar-list__filter-field">
        <label class="avatar-list__label" for="locale-filter">
          {{ t('devContent.avatarEvents.locale') }}
        </label>
        <input
          id="locale-filter"
          v-model="localeFilter"
          type="text"
          class="avatar-list__input"
          placeholder="es-ES"
          @change="handleFilterChange"
        />
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="store.avatarEventsLoading" class="avatar-list__state">
      <div class="spinner" aria-hidden="true"></div>
      <p>{{ t('devContent.loading') }}</p>
    </div>

    <!-- Error State -->
    <div v-else-if="store.avatarEventsError" class="avatar-list__state">
      <p class="avatar-list__error">{{ store.avatarEventsError }}</p>
      <button class="avatar-list__retry-btn" @click="handleRetry">
        {{ t('devContent.retry') }}
      </button>
    </div>

    <!-- Empty State -->
    <div v-else-if="store.avatarEvents.length === 0 && !showForm" class="avatar-list__state">
      <p class="avatar-list__empty">{{ t('devContent.avatarEvents.noEvents') }}</p>
      <button class="avatar-list__create-btn" @click="handleCreate">
        {{ t('devContent.avatarEvents.create') }}
      </button>
    </div>

    <!-- Content -->
    <template v-else>
      <div class="avatar-list__header">
        <h2 class="avatar-list__title">{{ t('devContent.avatarEvents.title') }}</h2>
        <button
          v-if="!showForm"
          class="avatar-list__create-btn"
          @click="handleCreate"
        >
          {{ t('devContent.avatarEvents.create') }}
        </button>
      </div>

      <!-- Form -->
      <AvatarEventForm
        v-if="showForm"
        :event="editingEvent"
        :api-error="formError"
        @submit="handleSubmit"
        @cancel="handleCancel"
      />

      <!-- Loading for edit fetch -->
      <div v-if="fetchingEdit" class="avatar-list__state">
        <div class="spinner" aria-hidden="true"></div>
        <p>{{ t('devContent.loading') }}</p>
      </div>

      <!-- Table -->
      <div v-if="!showForm && !fetchingEdit" class="avatar-list__table-wrapper">
        <table class="avatar-list__table">
          <thead>
            <tr>
              <th>{{ t('devContent.avatarEvents.eventType') }}</th>
              <th>{{ t('devContent.avatarEvents.tone') }}</th>
              <th>{{ t('devContent.avatarEvents.locale') }}</th>
              <th>{{ t('devContent.avatarEvents.messageText') }}</th>
              <th>{{ t('devContent.avatarEvents.status') }}</th>
              <th>{{ t('devContent.form.edit') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="event in store.avatarEvents" :key="event.id">
              <td>
                <span class="avatar-list__event-type">
                  {{ t(`devContent.avatarEventType.${event.eventType}`) }}
                </span>
              </td>
              <td>
                <span class="avatar-list__tone">
                  {{ t(`devContent.avatarTone.${event.tone}`) }}
                </span>
              </td>
              <td>{{ event.locale }}</td>
              <td class="avatar-list__message-cell">{{ event.messageText }}</td>
              <td>
                <span class="avatar-list__status" :class="`avatar-list__status--${event.status.toLowerCase()}`">
                  {{ t(`devContent.status.${event.status}`) }}
                </span>
              </td>
              <td>
                <button class="avatar-list__edit-btn" @click="handleEdit(event)">
                  {{ t('devContent.form.edit') }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>

<style scoped>
.avatar-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding: var(--space-lg);
}

.avatar-list__filters {
  display: flex;
  gap: var(--space-md);
  flex-wrap: wrap;
}

.avatar-list__filter-field {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
  flex: 1;
  min-width: 150px;
}

.avatar-list__label {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: #374151;
}

.avatar-list__select,
.avatar-list__input {
  padding: var(--space-sm) var(--space-md);
  border: 1px solid #d1d5db;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  background-color: #ffffff;
  min-height: var(--touch-target-min);
}

.avatar-list__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-list__title {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.avatar-list__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-xl);
  text-align: center;
}

.avatar-list__empty {
  font-size: var(--font-size-md);
  color: #6b7280;
}

.avatar-list__error {
  font-size: var(--font-size-md);
  color: #dc2626;
}

.avatar-list__create-btn,
.avatar-list__retry-btn,
.avatar-list__edit-btn {
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

.avatar-list__create-btn,
.avatar-list__edit-btn {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.avatar-list__create-btn:hover,
.avatar-list__edit-btn:hover {
  background-color: var(--color-primary-dark);
}

.avatar-list__retry-btn {
  background-color: transparent;
  color: var(--color-primary);
  border: 2px solid var(--color-primary);
}

.avatar-list__retry-btn:hover {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.avatar-list__table-wrapper {
  overflow-x: auto;
}

.avatar-list__table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--font-size-sm);
}

.avatar-list__table th,
.avatar-list__table td {
  padding: var(--space-sm) var(--space-md);
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.avatar-list__table th {
  font-weight: 600;
  color: #374151;
  background-color: #f9fafb;
}

.avatar-list__event-type,
.avatar-list__tone {
  display: inline-block;
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  font-weight: 600;
  background-color: #e5e7eb;
  color: #374151;
}

.avatar-list__message-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.avatar-list__status {
  display: inline-block;
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  font-weight: 600;
}

.avatar-list__status--active {
  background-color: #d1fae5;
  color: #065f46;
}

.avatar-list__status--inactive {
  background-color: #fee2e2;
  color: #991b1b;
}

.avatar-list__status--draft {
  background-color: #fef3c7;
  color: #92400e;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--color-neutral);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 768px) {
  .avatar-list__filters {
    flex-direction: column;
  }

  .avatar-list__header {
    flex-direction: column;
    gap: var(--space-sm);
    align-items: flex-start;
  }
}
</style>
