<template>
  <div class="documentation-layout">
    <header class="documentation-layout__header">
      <div class="documentation-layout__header-actions">
        <button
          v-if="showBackButton"
          class="documentation-layout__back-button"
          @click="goBack"
          :aria-label="t('views.docs.backToPanel')"
        >
          <NubiIcon name="arrow-left" :size="20" />
          <span class="documentation-layout__back-label">{{ t('views.docs.backToPanel') }}</span>
        </button>
        <button
          class="documentation-layout__menu-toggle"
          @click="sidebarOpen = !sidebarOpen"
          :aria-label="t('views.docs.menuToggle')"
        >
          <NubiIcon name="menu" :size="24" />
        </button>
      </div>
    </header>
    <div class="documentation-layout__body">
      <DocumentationSidebar
        :current-section="currentSection"
        :is-open="sidebarOpen"
        @close="sidebarOpen = false"
      />
      <main class="documentation-layout__content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import NubiIcon from '../components/base/NubiIcon.vue'
import DocumentationSidebar from '../components/documentation/DocumentationSidebar.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const VALID_PANEL_PATHS = [
  '/panel',
  '/panel/configuracion',
  '/panel/ninos',
  '/panel/chatbot',
  '/panel/lectura-familiar',
  '/panel/relajacion-familiar'
]

const sidebarOpen = ref(false)
const currentSection = computed(() => {
  if (route.name === 'DocumentationContact') return 'contacto'
  return route.params.section as string
})

const rawFrom = computed(() => {
  const from = route.query.from
  return typeof from === 'string' ? from : null
})

const isValidPanelPath = computed(() => {
  if (!rawFrom.value) return false
  return rawFrom.value.startsWith('/panel') && (
    VALID_PANEL_PATHS.includes(rawFrom.value) ||
    /^\/panel\/ninos\/\d+$/.test(rawFrom.value) ||
    /^\/panel\/ninos\/\d+\/dashboard$/.test(rawFrom.value)
  )
})

const showBackButton = computed(() => isValidPanelPath.value)

function goBack(): void {
  if (isValidPanelPath.value && rawFrom.value) {
    router.replace(rawFrom.value)
  }
}
</script>

<style scoped>
.documentation-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100%;
}

.documentation-layout__header {
  display: none;
  align-items: center;
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
  min-height: 56px;
  border-bottom: var(--nubi-border-width) solid var(--nubi-border-default);
  background-color: var(--nubi-bg-surface);
}

.documentation-layout__header-actions {
  display: flex;
  align-items: center;
}

.documentation-layout__menu-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  min-height: 48px;
  padding: var(--nubi-spacing-xs);
  border: none;
  background: none;
  cursor: pointer;
  color: var(--nubi-text-primary);
  border-radius: var(--nubi-radius-md);
  transition: background-color var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.documentation-layout__menu-toggle:hover {
  background-color: var(--nubi-bg-surface-tertiary);
}

.documentation-layout__menu-toggle:focus-visible {
  outline: none;
  box-shadow: 0 0 0 2px var(--nubi-color-focus);
}

.documentation-layout__body {
  display: flex;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.documentation-layout__content {
  flex: 1;
  overflow-y: auto;
  min-width: 0;
  min-height: 0;
}

.documentation-layout__back-button {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-xs);
  min-width: 48px;
  min-height: 48px;
  padding: var(--nubi-spacing-xs) var(--nubi-spacing-sm);
  border: none;
  background: none;
  cursor: pointer;
  color: var(--nubi-text-primary);
  border-radius: var(--nubi-radius-md);
  transition: background-color var(--nubi-duration-fast) var(--nubi-ease-in-out);
  margin-right: var(--nubi-spacing-sm);
}

.documentation-layout__back-button:hover {
  background-color: var(--nubi-bg-surface-tertiary);
}

.documentation-layout__back-button:focus-visible {
  outline: none;
  box-shadow: 0 0 0 2px var(--nubi-color-focus);
}

.documentation-layout__back-label {
  font-size: var(--nubi-font-size-sm);
  font-weight: var(--nubi-font-weight-medium);
  white-space: nowrap;
}

@media (max-width: 1023px) {
  .documentation-layout__header {
    display: flex;
  }
}
</style>
