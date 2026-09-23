<template>
  <div class="diary-view">
    <NubiBreadcrumb :items="breadcrumbItems" />

    <h1 class="diary-view__title">{{ t('views.ninos.diary.title') }}</h1>

    <DiaryPeriodFilter :model-value="period" @update:model-value="handlePeriodChange" />

    <div v-if="loading" class="diary-view__loading">
      <NubiSpinner size="lg" :show-label="true" />
    </div>

    <NubiErrorState
      v-else-if="error"
      :message="t('views.ninos.diary.error')"
      @retry="handleRetry"
    />

    <template v-else>
      <DiarySummary :summary="summary" />
      <DiaryActivityList :activities="activities" :abandonment-signals="abandonmentSignals" />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import NubiBreadcrumb from '../../components/base/NubiBreadcrumb.vue'
import NubiSpinner from '../../components/base/NubiSpinner.vue'
import NubiErrorState from '../../components/base/NubiErrorState.vue'
import DiaryPeriodFilter from '../../components/diary/DiaryPeriodFilter.vue'
import DiarySummary from '../../components/diary/DiarySummary.vue'
import DiaryActivityList from '../../components/diary/DiaryActivityList.vue'
import { useDiary } from '../../composables/useDiary'
import type { DiaryPeriod } from '../../types/diary'

const { t } = useI18n()
const route = useRoute()

const childId = computed(() => Number(route.params.id))
const profileName = computed(() => (history.state?.name as string) || '')

const { period, summary, activities, abandonmentSignals, loading, error, load, setPeriod } = useDiary()

const breadcrumbItems = computed(() => [
  { label: t('views.ninos.title'), to: '/panel/ninos' },
  { label: profileName.value, to: `/panel/ninos/${route.params.id}` },
  { label: t('views.ninos.diary.title') }
])

onMounted(() => {
  load(childId.value)
})

function handlePeriodChange(newPeriod: DiaryPeriod): void {
  setPeriod(childId.value, newPeriod)
}

function handleRetry(): void {
  load(childId.value)
}
</script>

<style scoped>
.diary-view {
  padding: var(--nubi-spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-lg);
  max-width: 960px;
  margin: 0 auto;
}

.diary-view__title {
  margin: 0;
  font-size: var(--nubi-font-size-xl);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-primary);
}

.diary-view__loading {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 30vh;
}

@media (max-width: 640px) {
  .diary-view {
    padding: var(--nubi-spacing-md);
  }
}
</style>
