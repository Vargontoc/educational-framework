<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useDevContentStore } from '@/stores/useDevContentStore'
import type { TopicResponse, CreateTopicRequest } from '@/shared/types/api'
import TopicForm from './TopicForm.vue'

const { t } = useI18n()
const store = useDevContentStore()

const showForm = ref(false)
const editingTopic = ref<TopicResponse | null>(null)
const formError = ref<string | null>(null)
const fetchingEdit = ref(false)

const categoryFilter = ref<number | null>(null)

const filteredTopics = computed(() => {
  if (categoryFilter.value == null) {
    return store.topics
  }
  return store.topics.filter(topic => topic.categoryId === categoryFilter.value)
})

onMounted(() => {
  store.fetchTopics()
  if (store.categories.length === 0) {
    store.fetchCategories()
  }
})

function handleFilterChange() {
  store.setSelectedCategoryId(categoryFilter.value)
}

function handleCreate() {
  editingTopic.value = null
  formError.value = null
  showForm.value = true
}

async function handleEdit(topic: TopicResponse) {
  formError.value = null
  fetchingEdit.value = true
  try {
    const freshTopic = await store.getTopicById(topic.id)
    editingTopic.value = freshTopic
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
  editingTopic.value = null
  formError.value = null
}

async function handleSubmit(payload: CreateTopicRequest) {
  formError.value = null
  try {
    if (editingTopic.value) {
      await store.updateTopic(editingTopic.value.id, payload)
    } else {
      await store.createTopic(payload)
    }
    showForm.value = false
    editingTopic.value = null
  } catch (error: unknown) {
    const maybeAxiosError = error as {
      response?: { status?: number; data?: { message?: string; errors?: string[] } }
    }
    const status = maybeAxiosError.response?.status
    const apiErrors = maybeAxiosError.response?.data?.errors
    const apiMessage = maybeAxiosError.response?.data?.message

    if (status === 409) {
      formError.value = t('devContent.topics.conflictError')
    } else if (status === 404) {
      formError.value = t('devContent.topics.categoryNotFound')
    } else if (status === 400) {
      formError.value = apiErrors?.[0] ?? apiMessage ?? t('devContent.errors.badRequest')
    } else {
      formError.value = t('devContent.errors.serverError')
    }
  }
}

function handleRetry() {
  store.fetchTopics(categoryFilter.value ?? undefined)
}

function getCategoryName(categoryId: number): string {
  const category = store.categories.find(c => c.id === categoryId)
  return category?.name ?? `#${categoryId}`
}
</script>

<template>
  <div class="topic-list">
    <!-- Loading State -->
    <div v-if="store.topicsLoading" class="topic-list__state">
      <div class="spinner" aria-hidden="true"></div>
      <p>{{ t('devContent.loading') }}</p>
    </div>

    <!-- Error State -->
    <div v-else-if="store.topicsError" class="topic-list__state">
      <p class="topic-list__error">{{ store.topicsError }}</p>
      <button class="topic-list__retry-btn" @click="handleRetry">
        {{ t('devContent.retry') }}
      </button>
    </div>

    <!-- Empty State -->
    <div v-else-if="store.topics.length === 0 && !showForm" class="topic-list__state">
      <p class="topic-list__empty">{{ t('devContent.topics.noTopics') }}</p>
      <button class="topic-list__create-btn" @click="handleCreate">
        {{ t('devContent.topics.create') }}
      </button>
    </div>

    <!-- Content -->
    <template v-else>
      <div class="topic-list__header">
        <h2 class="topic-list__title">{{ t('devContent.topics.title') }}</h2>
        <div class="topic-list__actions">
          <select
            v-model="categoryFilter"
            class="topic-list__filter"
            @change="handleFilterChange"
          >
            <option :value="null">{{ t('devContent.topics.allCategories') }}</option>
            <option v-for="cat in store.categories" :key="cat.id" :value="cat.id">
              {{ cat.name }}
            </option>
          </select>
          <button
            v-if="!showForm"
            class="topic-list__create-btn"
            @click="handleCreate"
          >
            {{ t('devContent.topics.create') }}
          </button>
        </div>
      </div>

      <!-- Form -->
      <TopicForm
        v-if="showForm"
        :topic="editingTopic"
        :categories="store.categories"
        :api-error="formError"
        @submit="handleSubmit"
        @cancel="handleCancel"
      />

      <!-- Loading for edit fetch -->
      <div v-if="fetchingEdit" class="topic-list__state">
        <div class="spinner" aria-hidden="true"></div>
        <p>{{ t('devContent.loading') }}</p>
      </div>

      <!-- Table -->
      <div v-if="!showForm" class="topic-list__table-wrapper">
        <table class="topic-list__table">
          <thead>
            <tr>
              <th>{{ t('devContent.topics.name') }}</th>
              <th>{{ t('devContent.topics.category') }}</th>
              <th>{{ t('devContent.topics.status') }}</th>
              <th>{{ t('devContent.topics.minAge') }}</th>
              <th>{{ t('devContent.topics.maxAge') }}</th>
              <th>{{ t('devContent.form.edit') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="topic in filteredTopics" :key="topic.id">
              <td>{{ topic.name }}</td>
              <td>{{ getCategoryName(topic.categoryId) }}</td>
              <td>
                <span class="topic-list__status" :class="`topic-list__status--${topic.status.toLowerCase()}`">
                  {{ t(`devContent.status.${topic.status}`) }}
                </span>
              </td>
              <td>{{ topic.minAge ?? '-' }}</td>
              <td>{{ topic.maxAge ?? '-' }}</td>
              <td>
                <button class="topic-list__edit-btn" @click="handleEdit(topic)">
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
.topic-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding: var(--space-lg);
}

.topic-list__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-sm);
}

.topic-list__title {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.topic-list__actions {
  display: flex;
  gap: var(--space-sm);
  align-items: center;
}

.topic-list__filter {
  padding: var(--space-sm) var(--space-md);
  border: 1px solid #d1d5db;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  background-color: #ffffff;
  min-height: var(--touch-target-min);
}

.topic-list__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-xl);
  text-align: center;
}

.topic-list__empty {
  font-size: var(--font-size-md);
  color: #6b7280;
}

.topic-list__error {
  font-size: var(--font-size-md);
  color: #dc2626;
}

.topic-list__create-btn,
.topic-list__retry-btn,
.topic-list__edit-btn {
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

.topic-list__create-btn,
.topic-list__edit-btn {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.topic-list__create-btn:hover,
.topic-list__edit-btn:hover {
  background-color: var(--color-primary-dark);
}

.topic-list__retry-btn {
  background-color: transparent;
  color: var(--color-primary);
  border: 2px solid var(--color-primary);
}

.topic-list__retry-btn:hover {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.topic-list__table-wrapper {
  overflow-x: auto;
}

.topic-list__table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--font-size-sm);
}

.topic-list__table th,
.topic-list__table td {
  padding: var(--space-sm) var(--space-md);
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.topic-list__table th {
  font-weight: 600;
  color: #374151;
  background-color: #f9fafb;
}

.topic-list__status {
  display: inline-block;
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  font-weight: 600;
}

.topic-list__status--active {
  background-color: #d1fae5;
  color: #065f46;
}

.topic-list__status--inactive {
  background-color: #fee2e2;
  color: #991b1b;
}

.topic-list__status--draft {
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
  .topic-list__header {
    flex-direction: column;
    align-items: flex-start;
  }

  .topic-list__actions {
    width: 100%;
    flex-direction: column;
  }

  .topic-list__filter {
    width: 100%;
  }

  .topic-list__create-btn {
    width: 100%;
  }

  .topic-list__table th:nth-child(4),
  .topic-list__table td:nth-child(4),
  .topic-list__table th:nth-child(5),
  .topic-list__table td:nth-child(5) {
    display: none;
  }
}
</style>
