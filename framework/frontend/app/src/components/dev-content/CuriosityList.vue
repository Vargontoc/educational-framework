<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useDevContentStore } from '@/stores/useDevContentStore'
import type { CuriosityResponse, CreateCuriosityRequest } from '@/shared/types/api'
import CuriosityForm from './CuriosityForm.vue'

const { t } = useI18n()
const store = useDevContentStore()

const showForm = ref(false)
const editingCuriosity = ref<CuriosityResponse | null>(null)
const formError = ref<string | null>(null)
const fetchingEdit = ref(false)

const topicFilter = ref<number | null>(null)
const ageFilter = ref<number | null>(null)
const localeFilter = ref<string | null>(null)

onMounted(() => {
  store.fetchCuriosities()
  if (store.topics.length === 0) {
    store.fetchTopics()
  }
})

function handleFilterChange() {
  store.setCuriosityFilters(topicFilter.value, ageFilter.value, localeFilter.value)
}

function handleCreate() {
  editingCuriosity.value = null
  formError.value = null
  showForm.value = true
}

async function handleEdit(curiosity: CuriosityResponse) {
  formError.value = null
  fetchingEdit.value = true
  try {
    const freshCuriosity = await store.getCuriosityById(curiosity.id)
    editingCuriosity.value = freshCuriosity
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
  editingCuriosity.value = null
  formError.value = null
}

async function handleSubmit(payload: CreateCuriosityRequest) {
  formError.value = null
  try {
    if (editingCuriosity.value) {
      await store.updateCuriosity(editingCuriosity.value.id, payload)
    } else {
      await store.createCuriosity(payload)
    }
    showForm.value = false
    editingCuriosity.value = null
  } catch (error: unknown) {
    const maybeAxiosError = error as {
      response?: { status?: number; data?: { message?: string; errors?: string[] } }
    }
    const status = maybeAxiosError.response?.status
    const apiErrors = maybeAxiosError.response?.data?.errors
    const apiMessage = maybeAxiosError.response?.data?.message

    if (status === 409) {
      formError.value = t('devContent.curiosities.conflictError')
    } else if (status === 400) {
      formError.value = apiErrors?.[0] ?? apiMessage ?? t('devContent.errors.badRequest')
    } else {
      formError.value = t('devContent.errors.serverError')
    }
  }
}

function handleRetry() {
  store.fetchCuriosities()
}

function getTopicName(topicId: number | null | undefined): string {
  if (topicId == null) return '-'
  const topic = store.topics.find(t => t.id === topicId)
  return topic?.name ?? `#${topicId}`
}
</script>

<template>
  <div class="curiosity-list">
    <!-- Filters -->
    <div class="curiosity-list__filters">
      <div class="curiosity-list__filter-field">
        <label class="curiosity-list__label" for="topic-filter">
          {{ t('devContent.curiosities.topic') }}
        </label>
        <select
          id="topic-filter"
          v-model="topicFilter"
          class="curiosity-list__select"
          @change="handleFilterChange"
        >
          <option :value="null">{{ t('devContent.curiosities.allTopics') }}</option>
          <option v-for="topic in store.topics" :key="topic.id" :value="topic.id">
            {{ topic.name }}
          </option>
        </select>
      </div>

      <div class="curiosity-list__filter-field">
        <label class="curiosity-list__label" for="age-filter">
          {{ t('devContent.curiosities.minAge') }}
        </label>
        <input
          id="age-filter"
          v-model.number="ageFilter"
          type="number"
          class="curiosity-list__input"
          min="0"
          max="18"
          placeholder="0-18"
          @change="handleFilterChange"
        />
      </div>

      <div class="curiosity-list__filter-field">
        <label class="curiosity-list__label" for="locale-filter">
          {{ t('devContent.curiosities.locale') }}
        </label>
        <input
          id="locale-filter"
          v-model="localeFilter"
          type="text"
          class="curiosity-list__input"
          placeholder="es-ES"
          @change="handleFilterChange"
        />
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="store.curiositiesLoading" class="curiosity-list__state">
      <div class="spinner" aria-hidden="true"></div>
      <p>{{ t('devContent.loading') }}</p>
    </div>

    <!-- Error State -->
    <div v-else-if="store.curiositiesError" class="curiosity-list__state">
      <p class="curiosity-list__error">{{ store.curiositiesError }}</p>
      <button class="curiosity-list__retry-btn" @click="handleRetry">
        {{ t('devContent.retry') }}
      </button>
    </div>

    <!-- Empty State -->
    <div v-else-if="store.curiosities.length === 0 && !showForm" class="curiosity-list__state">
      <p class="curiosity-list__empty">{{ t('devContent.curiosities.noCuriosities') }}</p>
      <button class="curiosity-list__create-btn" @click="handleCreate">
        {{ t('devContent.curiosities.create') }}
      </button>
    </div>

    <!-- Content -->
    <template v-else>
      <div class="curiosity-list__header">
        <h2 class="curiosity-list__title">{{ t('devContent.curiosities.title') }}</h2>
        <button
          v-if="!showForm"
          class="curiosity-list__create-btn"
          @click="handleCreate"
        >
          {{ t('devContent.curiosities.create') }}
        </button>
      </div>

      <!-- Form -->
      <CuriosityForm
        v-if="showForm"
        :curiosity="editingCuriosity"
        :topics="store.topics"
        :api-error="formError"
        @submit="handleSubmit"
        @cancel="handleCancel"
      />

      <!-- Loading for edit fetch -->
      <div v-if="fetchingEdit" class="curiosity-list__state">
        <div class="spinner" aria-hidden="true"></div>
        <p>{{ t('devContent.loading') }}</p>
      </div>

      <!-- Table -->
      <div v-if="!showForm && !fetchingEdit" class="curiosity-list__table-wrapper">
        <table class="curiosity-list__table">
          <thead>
            <tr>
              <th>{{ t('devContent.curiosities.text') }}</th>
              <th>{{ t('devContent.curiosities.topic') }}</th>
              <th>{{ t('devContent.curiosities.locale') }}</th>
              <th>{{ t('devContent.curiosities.minAge') }}</th>
              <th>{{ t('devContent.curiosities.maxAge') }}</th>
              <th>{{ t('devContent.curiosities.status') }}</th>
              <th>{{ t('devContent.form.edit') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="curiosity in store.curiosities" :key="curiosity.id">
              <td class="curiosity-list__text-cell">{{ curiosity.text }}</td>
              <td>{{ getTopicName(curiosity.topicId) }}</td>
              <td>{{ curiosity.locale }}</td>
              <td>{{ curiosity.minAge ?? '-' }}</td>
              <td>{{ curiosity.maxAge ?? '-' }}</td>
              <td>
                <span class="curiosity-list__status" :class="`curiosity-list__status--${curiosity.status.toLowerCase()}`">
                  {{ t(`devContent.status.${curiosity.status}`) }}
                </span>
              </td>
              <td>
                <button class="curiosity-list__edit-btn" @click="handleEdit(curiosity)">
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
.curiosity-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding: var(--space-lg);
}

.curiosity-list__filters {
  display: flex;
  gap: var(--space-md);
  flex-wrap: wrap;
}

.curiosity-list__filter-field {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
  flex: 1;
  min-width: 150px;
}

.curiosity-list__label {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: #374151;
}

.curiosity-list__select,
.curiosity-list__input {
  padding: var(--space-sm) var(--space-md);
  border: 1px solid #d1d5db;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  background-color: #ffffff;
  min-height: var(--touch-target-min);
}

.curiosity-list__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.curiosity-list__title {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.curiosity-list__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-xl);
  text-align: center;
}

.curiosity-list__empty {
  font-size: var(--font-size-md);
  color: #6b7280;
}

.curiosity-list__error {
  font-size: var(--font-size-md);
  color: #dc2626;
}

.curiosity-list__create-btn,
.curiosity-list__retry-btn,
.curiosity-list__edit-btn {
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

.curiosity-list__create-btn,
.curiosity-list__edit-btn {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.curiosity-list__create-btn:hover,
.curiosity-list__edit-btn:hover {
  background-color: var(--color-primary-dark);
}

.curiosity-list__retry-btn {
  background-color: transparent;
  color: var(--color-primary);
  border: 2px solid var(--color-primary);
}

.curiosity-list__retry-btn:hover {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.curiosity-list__table-wrapper {
  overflow-x: auto;
}

.curiosity-list__table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--font-size-sm);
}

.curiosity-list__table th,
.curiosity-list__table td {
  padding: var(--space-sm) var(--space-md);
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.curiosity-list__table th {
  font-weight: 600;
  color: #374151;
  background-color: #f9fafb;
}

.curiosity-list__text-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.curiosity-list__status {
  display: inline-block;
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  font-weight: 600;
}

.curiosity-list__status--active {
  background-color: #d1fae5;
  color: #065f46;
}

.curiosity-list__status--inactive {
  background-color: #fee2e2;
  color: #991b1b;
}

.curiosity-list__status--draft {
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
  .curiosity-list__filters {
    flex-direction: column;
  }

  .curiosity-list__header {
    flex-direction: column;
    gap: var(--space-sm);
    align-items: flex-start;
  }

  .curiosity-list__table th:nth-child(4),
  .curiosity-list__table td:nth-child(4),
  .curiosity-list__table th:nth-child(5),
  .curiosity-list__table td:nth-child(5) {
    display: none;
  }
}
</style>
