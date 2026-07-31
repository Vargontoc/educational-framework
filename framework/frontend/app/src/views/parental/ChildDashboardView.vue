<template>
  <div class="child-dashboard-view">
    <NubiBreadcrumb :items="breadcrumbItems" />

    <div class="child-dashboard-view__placeholder">
      <p>{{ t('views.ninos.dashboard.placeholder') }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import NubiBreadcrumb from '../../components/base/NubiBreadcrumb.vue'

const { t } = useI18n()
const route = useRoute()

const profileName = computed(() => history.state?.name || '')

const breadcrumbItems = computed(() => [
  { label: t('views.ninos.title'), to: '/panel/ninos' },
  { label: profileName.value, to: `/panel/ninos/${route.params.id}` },
  { label: 'Dashboard' }
])
</script>

<style scoped>
.child-dashboard-view {
  padding: var(--nubi-spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-lg);
}

.child-dashboard-view__placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 30vh;
  color: var(--nubi-text-secondary);
  font-size: var(--nubi-font-size-lg);
}
</style>
