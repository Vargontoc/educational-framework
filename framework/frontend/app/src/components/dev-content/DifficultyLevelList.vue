<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useDevContentStore } from '@/stores/useDevContentStore'
import type { DifficultyLevelResponse, CreateDifficultyLevelRequest } from '@/shared/types/api'
import DifficultyLevelForm from './DifficultyLevelForm.vue'

const { t } = useI18n()
const store = useDevContentStore()

const showForm = ref(false)
const editingLevel = ref<DifficultyLevelResponse | null>(null)
const formError = ref<string | null>(null)

function handleCreate() {
  editingLevel.value = null
  formError.value = null
  showForm.value = true
}

function handleEdit(level: DifficultyLevelResponse) {
  editingLevel.value = level
  formError.value = null
  showForm.value = true
}

function handleCancel() {
  showForm.value = false
  editingLevel.value = null
  formError.value = null
}

async function handleSubmit(payload: CreateDifficultyLevelRequest) {
  formError.value = null
  try {
    if (editingLevel.value) {
      await store.updateDifficultyLevel(editingLevel.value.id, payload)
    } else {
      await store.createDifficultyLevel(payload)
    }
    showForm.value = false
    editingLevel.value = null
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
    store.fetchDifficultyLevels(store.selectedActivityId)
  }
}
</script>

<template>
  <div class="difficulty-list">
    <!-- No Activity Selected -->
    <div v-if="store.selectedActivityId == null" class="difficulty-list__state">
      <p class="difficulty-list__empty">{{ t('devContent.difficultyLevels.selectActivity') }}</p>
    </div>

    <!-- Activity Selected -->
    <template v-else>
      <!-- Loading State -->
      <div v-if="store.difficultyLevelsLoading" class="difficulty-list__state">
        <div class="spinner" aria-hidden="true"></div>
        <p>{{ t('devContent.loading') }}</p>
      </div>

      <!-- Error State -->
      <div v-else-if="store.difficultyLevelsError" class="difficulty-list__state">
        <p class="difficulty-list__error">{{ store.difficultyLevelsError }}</p>
        <button class="difficulty-list__retry-btn" @click="handleRetry">
          {{ t('devContent.retry') }}
        </button>
      </div>

      <!-- Empty State -->
      <div v-else-if="store.difficultyLevels.length === 0 && !showForm" class="difficulty-list__state">
        <p class="difficulty-list__empty">{{ t('devContent.difficultyLevels.noLevels') }}</p>
        <button class="difficulty-list__create-btn" @click="handleCreate">
          {{ t('devContent.difficultyLevels.create') }}
        </button>
      </div>

      <!-- Content -->
      <template v-else>
        <div class="difficulty-list__header">
          <h2 class="difficulty-list__title">{{ t('devContent.difficultyLevels.title') }}</h2>
          <button
            v-if="!showForm"
            class="difficulty-list__create-btn"
            @click="handleCreate"
          >
            {{ t('devContent.difficultyLevels.create') }}
          </button>
        </div>

        <!-- Form -->
        <DifficultyLevelForm
          v-if="showForm"
          :activity-id="store.selectedActivityId"
          :level="editingLevel"
          :api-error="formError"
          @submit="handleSubmit"
          @cancel="handleCancel"
        />

        <!-- Table -->
        <div v-if="!showForm" class="difficulty-list__table-wrapper">
          <table class="difficulty-list__table">
            <thead>
              <tr>
                <th>{{ t('devContent.difficultyLevels.difficultyCode') }}</th>
                <th>{{ t('devContent.difficultyLevels.engineParams') }}</th>
                <th>{{ t('devContent.difficultyLevels.adaptiveThresholdConfig') }}</th>
                <th>{{ t('devContent.form.edit') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="level in store.difficultyLevels" :key="level.id">
                <td>
                  <span class="difficulty-list__code" :class="`difficulty-list__code--${level.difficultyCode.toLowerCase()}`">
                    {{ t(`devContent.difficultyCode.${level.difficultyCode}`) }}
                  </span>
                </td>
                <td class="difficulty-list__json-cell">{{ level.engineParams ?? '-' }}</td>
                <td class="difficulty-list__json-cell">{{ level.adaptiveThresholdConfig ?? '-' }}</td>
                <td>
                  <button class="difficulty-list__edit-btn" @click="handleEdit(level)">
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
.difficulty-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding: var(--space-lg);
}

.difficulty-list__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.difficulty-list__title {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.difficulty-list__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-xl);
  text-align: center;
}

.difficulty-list__empty {
  font-size: var(--font-size-md);
  color: #6b7280;
}

.difficulty-list__error {
  font-size: var(--font-size-md);
  color: #dc2626;
}

.difficulty-list__create-btn,
.difficulty-list__retry-btn,
.difficulty-list__edit-btn {
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

.difficulty-list__create-btn,
.difficulty-list__edit-btn {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.difficulty-list__create-btn:hover,
.difficulty-list__edit-btn:hover {
  background-color: var(--color-primary-dark);
}

.difficulty-list__retry-btn {
  background-color: transparent;
  color: var(--color-primary);
  border: 2px solid var(--color-primary);
}

.difficulty-list__retry-btn:hover {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.difficulty-list__table-wrapper {
  overflow-x: auto;
}

.difficulty-list__table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--font-size-sm);
}

.difficulty-list__table th,
.difficulty-list__table td {
  padding: var(--space-sm) var(--space-md);
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.difficulty-list__table th {
  font-weight: 600;
  color: #374151;
  background-color: #f9fafb;
}

.difficulty-list__code {
  display: inline-block;
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  font-weight: 600;
}

.difficulty-list__code--easy {
  background-color: #d1fae5;
  color: #065f46;
}

.difficulty-list__code--medium {
  background-color: #fef3c7;
  color: #92400e;
}

.difficulty-list__code--hard {
  background-color: #fee2e2;
  color: #991b1b;
}

.difficulty-list__json-cell {
  font-family: monospace;
  font-size: var(--font-size-xs);
  max-width: 200px;
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
  .difficulty-list__header {
    flex-direction: column;
    gap: var(--space-sm);
    align-items: flex-start;
  }
}
</style>
