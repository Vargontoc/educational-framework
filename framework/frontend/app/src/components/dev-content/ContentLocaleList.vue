<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useDevContentStore } from '@/stores/useDevContentStore'
import type { ContentLocaleResponse, CreateContentLocaleRequest, LocaleEntityType } from '@/shared/types/api'
import ContentLocaleForm from './ContentLocaleForm.vue'

const { t } = useI18n()
const store = useDevContentStore()

const showForm = ref(false)
const editingLocale = ref<ContentLocaleResponse | null>(null)
const formError = ref<string | null>(null)

const entityTypes: LocaleEntityType[] = ['CATEGORY', 'TOPIC', 'ACTIVITY', 'DIFFICULTY_LEVEL', 'ACTIVITY_RESOURCE']

const selectedEntityType = ref<LocaleEntityType | null>(null)
const selectedEntityId = ref<number | null>(null)

function handleEntityChange() {
  if (selectedEntityType.value && selectedEntityId.value) {
    store.setSelectedLocaleEntity(selectedEntityType.value, selectedEntityId.value)
  } else {
    store.setSelectedLocaleEntity(null, null)
  }
}

function handleCreate() {
  editingLocale.value = null
  formError.value = null
  showForm.value = true
}

function handleEdit(locale: ContentLocaleResponse) {
  editingLocale.value = locale
  formError.value = null
  showForm.value = true
}

function handleCancel() {
  showForm.value = false
  editingLocale.value = null
  formError.value = null
}

async function handleSubmit(payload: CreateContentLocaleRequest) {
  formError.value = null
  try {
    if (editingLocale.value) {
      await store.updateContentLocale(editingLocale.value.id, payload)
    } else {
      await store.createContentLocale(payload)
    }
    showForm.value = false
    editingLocale.value = null
  } catch (error: unknown) {
    const maybeAxiosError = error as {
      response?: { status?: number; data?: { message?: string; errors?: string[] } }
    }
    const status = maybeAxiosError.response?.status
    const apiErrors = maybeAxiosError.response?.data?.errors
    const apiMessage = maybeAxiosError.response?.data?.message

    if (status === 409) {
      formError.value = t('devContent.locales.conflictError')
    } else if (status === 400) {
      formError.value = apiErrors?.[0] ?? apiMessage ?? t('devContent.errors.badRequest')
    } else if (status === 404) {
      formError.value = t('devContent.errors.notFound')
    } else {
      formError.value = t('devContent.errors.serverError')
    }
  }
}

function handleRetry() {
  if (selectedEntityType.value && selectedEntityId.value) {
    store.fetchContentLocales(selectedEntityType.value, selectedEntityId.value)
  }
}
</script>

<template>
  <div class="locale-list">
    <!-- i18n Notice -->
    <div class="locale-list__notice">
      {{ t('devContent.locales.i18nNotice') }}
    </div>

    <!-- Entity Selection -->
    <div class="locale-list__filters">
      <div class="locale-list__filter-field">
        <label class="locale-list__label" for="entity-type-filter">
          {{ t('devContent.locales.entityType') }}
        </label>
        <select
          id="entity-type-filter"
          v-model="selectedEntityType"
          class="locale-list__select"
          @change="handleEntityChange"
        >
          <option :value="null" disabled>{{ t('devContent.locales.selectEntityType') }}</option>
          <option v-for="type in entityTypes" :key="type" :value="type">
            {{ t(`devContent.localeEntityType.${type}`) }}
          </option>
        </select>
      </div>

      <div class="locale-list__filter-field">
        <label class="locale-list__label" for="entity-id-filter">
          {{ t('devContent.locales.entityId') }}
        </label>
        <input
          id="entity-id-filter"
          v-model.number="selectedEntityId"
          type="number"
          class="locale-list__input"
          :placeholder="t('devContent.locales.selectEntityId')"
          min="1"
          @change="handleEntityChange"
        />
      </div>
    </div>

    <!-- No Entity Selected -->
    <div v-if="!selectedEntityType || !selectedEntityId" class="locale-list__state">
      <p class="locale-list__empty">{{ t('devContent.locales.selectEntityType') }}</p>
    </div>

    <!-- Entity Selected -->
    <template v-else>
      <!-- Loading State -->
      <div v-if="store.contentLocalesLoading" class="locale-list__state">
        <div class="spinner" aria-hidden="true"></div>
        <p>{{ t('devContent.loading') }}</p>
      </div>

      <!-- Error State -->
      <div v-else-if="store.contentLocalesError" class="locale-list__state">
        <p class="locale-list__error">{{ store.contentLocalesError }}</p>
        <button class="locale-list__retry-btn" @click="handleRetry">
          {{ t('devContent.retry') }}
        </button>
      </div>

      <!-- Empty State -->
      <div v-else-if="store.contentLocales.length === 0 && !showForm" class="locale-list__state">
        <p class="locale-list__empty">{{ t('devContent.locales.noLocales') }}</p>
        <button class="locale-list__create-btn" @click="handleCreate">
          {{ t('devContent.locales.create') }}
        </button>
      </div>

      <!-- Content -->
      <template v-else>
        <div class="locale-list__header">
          <h2 class="locale-list__title">{{ t('devContent.locales.title') }}</h2>
          <button
            v-if="!showForm"
            class="locale-list__create-btn"
            @click="handleCreate"
          >
            {{ t('devContent.locales.create') }}
          </button>
        </div>

        <!-- Form -->
        <ContentLocaleForm
          v-if="showForm"
          :locale="editingLocale"
          :initial-entity-type="selectedEntityType"
          :initial-entity-id="selectedEntityId"
          :api-error="formError"
          @submit="handleSubmit"
          @cancel="handleCancel"
        />

        <!-- Table -->
        <div v-if="!showForm" class="locale-list__table-wrapper">
          <table class="locale-list__table">
            <thead>
              <tr>
                <th>{{ t('devContent.locales.localeCode') }}</th>
                <th>{{ t('devContent.locales.name') }}</th>
                <th>{{ t('devContent.locales.description') }}</th>
                <th>{{ t('devContent.form.edit') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="locale in store.contentLocales" :key="locale.id">
                <td>{{ locale.localeCode }}</td>
                <td>{{ locale.name }}</td>
                <td>{{ locale.description ?? '-' }}</td>
                <td>
                  <button class="locale-list__edit-btn" @click="handleEdit(locale)">
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
.locale-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding: var(--space-lg);
}

.locale-list__notice {
  padding: var(--space-sm) var(--space-md);
  background-color: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: var(--radius-md);
  color: #1e40af;
  font-size: var(--font-size-sm);
}

.locale-list__filters {
  display: flex;
  gap: var(--space-md);
  flex-wrap: wrap;
}

.locale-list__filter-field {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
  flex: 1;
  min-width: 200px;
}

.locale-list__label {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: #374151;
}

.locale-list__select,
.locale-list__input {
  padding: var(--space-sm) var(--space-md);
  border: 1px solid #d1d5db;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  background-color: #ffffff;
  min-height: var(--touch-target-min);
}

.locale-list__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.locale-list__title {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.locale-list__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-xl);
  text-align: center;
}

.locale-list__empty {
  font-size: var(--font-size-md);
  color: #6b7280;
}

.locale-list__error {
  font-size: var(--font-size-md);
  color: #dc2626;
}

.locale-list__create-btn,
.locale-list__retry-btn,
.locale-list__edit-btn {
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

.locale-list__create-btn,
.locale-list__edit-btn {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.locale-list__create-btn:hover,
.locale-list__edit-btn:hover {
  background-color: var(--color-primary-dark);
}

.locale-list__retry-btn {
  background-color: transparent;
  color: var(--color-primary);
  border: 2px solid var(--color-primary);
}

.locale-list__retry-btn:hover {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.locale-list__table-wrapper {
  overflow-x: auto;
}

.locale-list__table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--font-size-sm);
}

.locale-list__table th,
.locale-list__table td {
  padding: var(--space-sm) var(--space-md);
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.locale-list__table th {
  font-weight: 600;
  color: #374151;
  background-color: #f9fafb;
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
  .locale-list__filters {
    flex-direction: column;
  }

  .locale-list__header {
    flex-direction: column;
    gap: var(--space-sm);
    align-items: flex-start;
  }
}
</style>
