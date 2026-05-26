<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useDevContentStore } from '@/stores/useDevContentStore'
import type { CategoryResponse, CreateCategoryRequest } from '@/shared/types/api'
import CategoryForm from './CategoryForm.vue'

const { t } = useI18n()
const store = useDevContentStore()

const showForm = ref(false)
const editingCategory = ref<CategoryResponse | null>(null)
const formError = ref<string | null>(null)
const fetchingEdit = ref(false)

onMounted(() => {
  store.fetchCategories()
})

function handleCreate() {
  editingCategory.value = null
  formError.value = null
  showForm.value = true
}

async function handleEdit(category: CategoryResponse) {
  formError.value = null
  fetchingEdit.value = true
  try {
    const freshCategory = await store.getCategoryById(category.id)
    editingCategory.value = freshCategory
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
  editingCategory.value = null
  formError.value = null
}

async function handleSubmit(payload: CreateCategoryRequest) {
  formError.value = null
  try {
    if (editingCategory.value) {
      await store.updateCategory(editingCategory.value.id, payload)
    } else {
      await store.createCategory(payload)
    }
    showForm.value = false
    editingCategory.value = null
  } catch (error: unknown) {
    const maybeAxiosError = error as {
      response?: { status?: number; data?: { message?: string; errors?: string[] } }
    }
    const status = maybeAxiosError.response?.status
    const apiErrors = maybeAxiosError.response?.data?.errors
    const apiMessage = maybeAxiosError.response?.data?.message

    if (status === 409) {
      formError.value = t('devContent.categories.conflictError')
    } else if (status === 400) {
      formError.value = apiErrors?.[0] ?? apiMessage ?? t('devContent.errors.badRequest')
    } else {
      formError.value = t('devContent.errors.serverError')
    }
  }
}

function handleRetry() {
  store.fetchCategories()
}
</script>

<template>
  <div class="category-list">
    <!-- Loading State -->
    <div v-if="store.categoriesLoading" class="category-list__state">
      <div class="spinner" aria-hidden="true"></div>
      <p>{{ t('devContent.loading') }}</p>
    </div>

    <!-- Error State -->
    <div v-else-if="store.categoriesError" class="category-list__state">
      <p class="category-list__error">{{ store.categoriesError }}</p>
      <button class="category-list__retry-btn" @click="handleRetry">
        {{ t('devContent.retry') }}
      </button>
    </div>

    <!-- Empty State -->
    <div v-else-if="store.categories.length === 0 && !showForm" class="category-list__state">
      <p class="category-list__empty">{{ t('devContent.categories.noCategories') }}</p>
      <button class="category-list__create-btn" @click="handleCreate">
        {{ t('devContent.categories.create') }}
      </button>
    </div>

    <!-- Content -->
    <template v-else>
      <div class="category-list__header">
        <h2 class="category-list__title">{{ t('devContent.categories.title') }}</h2>
        <button
          v-if="!showForm"
          class="category-list__create-btn"
          @click="handleCreate"
        >
          {{ t('devContent.categories.create') }}
        </button>
      </div>

      <!-- Form -->
      <CategoryForm
        v-if="showForm"
        :category="editingCategory"
        :api-error="formError"
        @submit="handleSubmit"
        @cancel="handleCancel"
      />

      <!-- Loading for edit fetch -->
      <div v-if="fetchingEdit" class="category-list__state">
        <div class="spinner" aria-hidden="true"></div>
        <p>{{ t('devContent.loading') }}</p>
      </div>

      <!-- Table -->
      <div v-if="!showForm" class="category-list__table-wrapper">
        <table class="category-list__table">
          <thead>
            <tr>
              <th>{{ t('devContent.categories.name') }}</th>
              <th>{{ t('devContent.categories.status') }}</th>
              <th>{{ t('devContent.categories.displayOrder') }}</th>
              <th>{{ t('devContent.form.edit') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="category in store.categories" :key="category.id">
              <td class="category-list__name-cell">
                <span v-if="category.iconUrl" class="category-list__icon">{{ category.iconUrl }}</span>
                <span>{{ category.name }}</span>
              </td>
              <td>
                <span class="category-list__status" :class="`category-list__status--${category.status.toLowerCase()}`">
                  {{ t(`devContent.status.${category.status}`) }}
                </span>
              </td>
              <td>{{ category.displayOrder ?? '-' }}</td>
              <td>
                <button class="category-list__edit-btn" @click="handleEdit(category)">
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
.category-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding: var(--space-lg);
}

.category-list__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category-list__title {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.category-list__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-xl);
  text-align: center;
}

.category-list__empty {
  font-size: var(--font-size-md);
  color: #6b7280;
}

.category-list__error {
  font-size: var(--font-size-md);
  color: #dc2626;
}

.category-list__create-btn,
.category-list__retry-btn,
.category-list__edit-btn {
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

.category-list__create-btn,
.category-list__edit-btn {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.category-list__create-btn:hover,
.category-list__edit-btn:hover {
  background-color: var(--color-primary-dark);
}

.category-list__retry-btn {
  background-color: transparent;
  color: var(--color-primary);
  border: 2px solid var(--color-primary);
}

.category-list__retry-btn:hover {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.category-list__table-wrapper {
  overflow-x: auto;
}

.category-list__table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--font-size-sm);
}

.category-list__table th,
.category-list__table td {
  padding: var(--space-sm) var(--space-md);
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.category-list__table th {
  font-weight: 600;
  color: #374151;
  background-color: #f9fafb;
}

.category-list__name-cell {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.category-list__icon {
  font-size: var(--font-size-md);
}

.category-list__status {
  display: inline-block;
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  font-weight: 600;
}

.category-list__status--active {
  background-color: #d1fae5;
  color: #065f46;
}

.category-list__status--inactive {
  background-color: #fee2e2;
  color: #991b1b;
}

.category-list__status--draft {
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
  .category-list__header {
    flex-direction: column;
    gap: var(--space-sm);
    align-items: flex-start;
  }

  .category-list__table th:nth-child(3),
  .category-list__table td:nth-child(3) {
    display: none;
  }
}
</style>
