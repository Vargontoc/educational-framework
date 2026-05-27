<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useDevContentStore } from '@/stores/useDevContentStore'
import type { ActivityResponse, CreateActivityRequest } from '@/shared/types/api'
import ActivityForm from './ActivityForm.vue'

const { t } = useI18n()
const store = useDevContentStore()

const showForm = ref(false)
const editingActivity = ref<ActivityResponse | null>(null)
const formError = ref<string | null>(null)
const fetchingEdit = ref(false)

const topicFilter = ref<number | null>(null)

onMounted(() => {
  store.fetchActivities()
  if (store.topics.length === 0) {
    store.fetchTopics()
  }
})

function handleFilterChange() {
  store.setSelectedTopicId(topicFilter.value)
}

function handleCreate() {
  editingActivity.value = null
  formError.value = null
  showForm.value = true
}

async function handleEdit(activity: ActivityResponse) {
  formError.value = null
  fetchingEdit.value = true
  try {
    const freshActivity = await store.getActivityById(activity.id)
    editingActivity.value = freshActivity
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
  editingActivity.value = null
  formError.value = null
}

async function handleSubmit(payload: CreateActivityRequest) {
  formError.value = null
  try {
    if (editingActivity.value) {
      await store.updateActivity(editingActivity.value.id, payload)
    } else {
      await store.createActivity(payload)
    }
    showForm.value = false
    editingActivity.value = null
  } catch (error: unknown) {
    const maybeAxiosError = error as {
      response?: { status?: number; data?: { message?: string; errors?: string[] } }
    }
    const status = maybeAxiosError.response?.status
    const apiErrors = maybeAxiosError.response?.data?.errors
    const apiMessage = maybeAxiosError.response?.data?.message

    if (status === 409) {
      formError.value = t('devContent.activities.conflictError')
    } else if (status === 400) {
      formError.value = apiErrors?.[0] ?? apiMessage ?? t('devContent.errors.badRequest')
    } else {
      formError.value = t('devContent.errors.serverError')
    }
  }
}

function handleRetry() {
  store.fetchActivities(topicFilter.value ?? undefined)
}

function getTopicNames(topicIds: number[]): string {
  if (!topicIds || topicIds.length === 0) return '-'
  return topicIds
    .map(id => {
      const topic = store.topics.find(t => t.id === id)
      return topic?.name ?? `#${id}`
    })
    .join(', ')
}
</script>

<template>
  <div class="activity-list">
    <!-- Loading State -->
    <div v-if="store.activitiesLoading" class="activity-list__state">
      <div class="spinner" aria-hidden="true"></div>
      <p>{{ t('devContent.loading') }}</p>
    </div>

    <!-- Error State -->
    <div v-else-if="store.activitiesError" class="activity-list__state">
      <p class="activity-list__error">{{ store.activitiesError }}</p>
      <button class="activity-list__retry-btn" @click="handleRetry">
        {{ t('devContent.retry') }}
      </button>
    </div>

    <!-- Empty State -->
    <div v-else-if="store.activities.length === 0 && !showForm" class="activity-list__state">
      <p class="activity-list__empty">{{ t('devContent.activities.noActivities') }}</p>
      <button class="activity-list__create-btn" @click="handleCreate">
        {{ t('devContent.activities.create') }}
      </button>
    </div>

    <!-- Content -->
    <template v-else>
      <div class="activity-list__header">
        <h2 class="activity-list__title">{{ t('devContent.activities.title') }}</h2>
        <div class="activity-list__actions">
          <select
            v-model="topicFilter"
            class="activity-list__filter"
            @change="handleFilterChange"
          >
            <option :value="null">{{ t('devContent.activities.allTopics') }}</option>
            <option v-for="topic in store.topics" :key="topic.id" :value="topic.id">
              {{ topic.name }}
            </option>
          </select>
          <button
            v-if="!showForm"
            class="activity-list__create-btn"
            @click="handleCreate"
          >
            {{ t('devContent.activities.create') }}
          </button>
        </div>
      </div>

      <!-- Form -->
      <ActivityForm
        v-if="showForm"
        :activity="editingActivity"
        :topics="store.topics"
        :api-error="formError"
        @submit="handleSubmit"
        @cancel="handleCancel"
      />

      <!-- Loading for edit fetch -->
      <div v-if="fetchingEdit" class="activity-list__state">
        <div class="spinner" aria-hidden="true"></div>
        <p>{{ t('devContent.loading') }}</p>
      </div>

      <!-- Table -->
      <div v-if="!showForm && !fetchingEdit" class="activity-list__table-wrapper">
        <table class="activity-list__table">
          <thead>
            <tr>
              <th>{{ t('devContent.activities.name') }}</th>
              <th>{{ t('devContent.activities.status') }}</th>
              <th>{{ t('devContent.activities.gameEngineType') }}</th>
              <th>{{ t('devContent.activities.minAge') }}</th>
              <th>{{ t('devContent.activities.maxAge') }}</th>
              <th>{{ t('devContent.activities.topics') }}</th>
              <th>{{ t('devContent.form.edit') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="activity in store.activities" :key="activity.id">
              <td>{{ activity.name }}</td>
              <td>
                <span class="activity-list__status" :class="`activity-list__status--${activity.status.toLowerCase()}`">
                  {{ t(`devContent.status.${activity.status}`) }}
                </span>
              </td>
              <td>{{ activity.gameEngineType ?? '-' }}</td>
              <td>{{ activity.minAge ?? '-' }}</td>
              <td>{{ activity.maxAge ?? '-' }}</td>
              <td>{{ getTopicNames(activity.topicIds) }}</td>
              <td>
                <button class="activity-list__edit-btn" @click="handleEdit(activity)">
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
.activity-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding: var(--space-lg);
}

.activity-list__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-sm);
}

.activity-list__title {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.activity-list__actions {
  display: flex;
  gap: var(--space-sm);
  align-items: center;
}

.activity-list__filter {
  padding: var(--space-sm) var(--space-md);
  border: 1px solid #d1d5db;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  background-color: #ffffff;
  min-height: var(--touch-target-min);
}

.activity-list__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-xl);
  text-align: center;
}

.activity-list__empty {
  font-size: var(--font-size-md);
  color: #6b7280;
}

.activity-list__error {
  font-size: var(--font-size-md);
  color: #dc2626;
}

.activity-list__create-btn,
.activity-list__retry-btn,
.activity-list__edit-btn {
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

.activity-list__create-btn,
.activity-list__edit-btn {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.activity-list__create-btn:hover,
.activity-list__edit-btn:hover {
  background-color: var(--color-primary-dark);
}

.activity-list__retry-btn {
  background-color: transparent;
  color: var(--color-primary);
  border: 2px solid var(--color-primary);
}

.activity-list__retry-btn:hover {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.activity-list__table-wrapper {
  overflow-x: auto;
}

.activity-list__table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--font-size-sm);
}

.activity-list__table th,
.activity-list__table td {
  padding: var(--space-sm) var(--space-md);
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.activity-list__table th {
  font-weight: 600;
  color: #374151;
  background-color: #f9fafb;
}

.activity-list__status {
  display: inline-block;
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  font-weight: 600;
}

.activity-list__status--active {
  background-color: #d1fae5;
  color: #065f46;
}

.activity-list__status--inactive {
  background-color: #fee2e2;
  color: #991b1b;
}

.activity-list__status--draft {
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
  .activity-list__header {
    flex-direction: column;
    align-items: flex-start;
  }

  .activity-list__actions {
    width: 100%;
    flex-direction: column;
  }

  .activity-list__filter {
    width: 100%;
  }

  .activity-list__create-btn {
    width: 100%;
  }

  .activity-list__table th:nth-child(4),
  .activity-list__table td:nth-child(4),
  .activity-list__table th:nth-child(5),
  .activity-list__table td:nth-child(5) {
    display: none;
  }
}
</style>
