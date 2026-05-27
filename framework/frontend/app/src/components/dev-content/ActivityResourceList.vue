<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useDevContentStore } from '@/stores/useDevContentStore'
import type { ActivityResourceResponse, CreateActivityResourceRequest } from '@/shared/types/api'
import ActivityResourceForm from './ActivityResourceForm.vue'

const { t } = useI18n()
const store = useDevContentStore()

const showForm = ref(false)
const editingResource = ref<ActivityResourceResponse | null>(null)
const formError = ref<string | null>(null)

function handleCreate() {
  editingResource.value = null
  formError.value = null
  showForm.value = true
}

function handleEdit(resource: ActivityResourceResponse) {
  editingResource.value = resource
  formError.value = null
  showForm.value = true
}

function handleCancel() {
  showForm.value = false
  editingResource.value = null
  formError.value = null
}

async function handleSubmit(payload: CreateActivityResourceRequest) {
  formError.value = null
  try {
    if (editingResource.value) {
      await store.updateActivityResource(editingResource.value.id, payload)
    } else {
      await store.createActivityResource(payload)
    }
    showForm.value = false
    editingResource.value = null
  } catch (error: unknown) {
    const maybeAxiosError = error as {
      response?: { status?: number; data?: { message?: string; errors?: string[] } }
    }
    const status = maybeAxiosError.response?.status
    const apiErrors = maybeAxiosError.response?.data?.errors
    const apiMessage = maybeAxiosError.response?.data?.message

    if (status === 404) {
      formError.value = t('devContent.errors.notFound')
    } else if (status === 400) {
      formError.value = apiErrors?.[0] ?? apiMessage ?? t('devContent.errors.badRequest')
    } else {
      formError.value = t('devContent.errors.serverError')
    }
  }
}

function handleRetry() {
  if (store.selectedActivityId != null) {
    store.fetchActivityResources(store.selectedActivityId)
  }
}

function getTopicName(topicId: number | null | undefined): string {
  if (topicId == null) return '-'
  const topic = store.topics.find(t => t.id === topicId)
  return topic?.name ?? `#${topicId}`
}
</script>

<template>
  <div class="resource-list">
    <!-- No Activity Selected -->
    <div v-if="store.selectedActivityId == null" class="resource-list__state">
      <p class="resource-list__empty">{{ t('devContent.resources.selectActivity') }}</p>
    </div>

    <!-- Activity Selected -->
    <template v-else>
      <!-- Loading State -->
      <div v-if="store.activityResourcesLoading" class="resource-list__state">
        <div class="spinner" aria-hidden="true"></div>
        <p>{{ t('devContent.loading') }}</p>
      </div>

      <!-- Error State -->
      <div v-else-if="store.activityResourcesError" class="resource-list__state">
        <p class="resource-list__error">{{ store.activityResourcesError }}</p>
        <button class="resource-list__retry-btn" @click="handleRetry">
          {{ t('devContent.retry') }}
        </button>
      </div>

      <!-- Empty State -->
      <div v-else-if="store.activityResources.length === 0 && !showForm" class="resource-list__state">
        <p class="resource-list__empty">{{ t('devContent.resources.noResources') }}</p>
        <button class="resource-list__create-btn" @click="handleCreate">
          {{ t('devContent.resources.create') }}
        </button>
      </div>

      <!-- Content -->
      <template v-else>
        <div class="resource-list__header">
          <h2 class="resource-list__title">{{ t('devContent.resources.title') }}</h2>
          <button
            v-if="!showForm"
            class="resource-list__create-btn"
            @click="handleCreate"
          >
            {{ t('devContent.resources.create') }}
          </button>
        </div>

        <!-- V1 Notice -->
        <div class="resource-list__notice">
          {{ t('devContent.resources.v1Notice') }}
        </div>

        <!-- Form -->
        <ActivityResourceForm
          v-if="showForm"
          :activity-id="store.selectedActivityId"
          :topics="store.topics"
          :resource="editingResource"
          :api-error="formError"
          @submit="handleSubmit"
          @cancel="handleCancel"
        />

        <!-- Table -->
        <div v-if="!showForm" class="resource-list__table-wrapper">
          <table class="resource-list__table">
            <thead>
              <tr>
                <th>{{ t('devContent.resources.resourceType') }}</th>
                <th>{{ t('devContent.resources.path') }}</th>
                <th>{{ t('devContent.resources.topic') }}</th>
                <th>{{ t('devContent.resources.metadata') }}</th>
                <th>{{ t('devContent.form.edit') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="resource in store.activityResources" :key="resource.id">
                <td>
                  <span class="resource-list__type" :class="`resource-list__type--${resource.resourceType.toLowerCase()}`">
                    {{ t(`devContent.resourceType.${resource.resourceType}`) }}
                  </span>
                </td>
                <td class="resource-list__path-cell">{{ resource.path }}</td>
                <td>{{ getTopicName(resource.topicId) }}</td>
                <td class="resource-list__metadata-cell">{{ resource.metadata ?? '-' }}</td>
                <td>
                  <button class="resource-list__edit-btn" @click="handleEdit(resource)">
                    {{ t('devContent.form.edit') }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>
    </template>
  </div>
</template>

<style scoped>
.resource-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding: var(--space-lg);
}

.resource-list__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.resource-list__title {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.resource-list__notice {
  padding: var(--space-sm) var(--space-md);
  background-color: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: var(--radius-md);
  color: #1e40af;
  font-size: var(--font-size-sm);
}

.resource-list__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-xl);
  text-align: center;
}

.resource-list__empty {
  font-size: var(--font-size-md);
  color: #6b7280;
}

.resource-list__error {
  font-size: var(--font-size-md);
  color: #dc2626;
}

.resource-list__create-btn,
.resource-list__retry-btn,
.resource-list__edit-btn {
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

.resource-list__create-btn,
.resource-list__edit-btn {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.resource-list__create-btn:hover,
.resource-list__edit-btn:hover {
  background-color: var(--color-primary-dark);
}

.resource-list__retry-btn {
  background-color: transparent;
  color: var(--color-primary);
  border: 2px solid var(--color-primary);
}

.resource-list__retry-btn:hover {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.resource-list__table-wrapper {
  overflow-x: auto;
}

.resource-list__table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--font-size-sm);
}

.resource-list__table th,
.resource-list__table td {
  padding: var(--space-sm) var(--space-md);
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.resource-list__table th {
  font-weight: 600;
  color: #374151;
  background-color: #f9fafb;
}

.resource-list__type {
  display: inline-block;
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  font-weight: 600;
}

.resource-list__type--image {
  background-color: #dbeafe;
  color: #1e40af;
}

.resource-list__type--audio {
  background-color: #fce7f3;
  color: #9d174d;
}

.resource-list__type--video {
  background-color: #fef3c7;
  color: #92400e;
}

.resource-list__path-cell {
  font-family: monospace;
  font-size: var(--font-size-xs);
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resource-list__metadata-cell {
  font-family: monospace;
  font-size: var(--font-size-xs);
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  .resource-list__header {
    flex-direction: column;
    gap: var(--space-sm);
    align-items: flex-start;
  }

  .resource-list__table th:nth-child(4),
  .resource-list__table td:nth-child(4) {
    display: none;
  }
}
</style>
