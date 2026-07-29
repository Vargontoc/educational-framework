<template>
  <div class="parent-panel-layout" :data-theme="theme">
    <ParentSidebar />

    <div class="parent-panel-layout__main">
      <header class="parent-panel-layout__header">
        <button
          class="parent-panel-layout__hamburger"
          :aria-label="t('sidebar.ariaLabels.openMenu')"
          @click="uiStore.toggleSidebar()"
        >
          <NubiIcon name="menu" :size="24" />
        </button>
      </header>

      <main class="parent-panel-layout__content">
        <router-view />
      </main>
    </div>

    <ThemeToggle />

    <InactivityOverlay
      v-if="showInactivityOverlay"
      @cancel="handleCancel"
      @expired="handleExpired"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUIStore } from '../stores/ui'
import { useParentalSession } from '../composables/useParentalSession'
import { useInactivityTimer } from '../composables/useInactivityTimer'
import { useChatbotPendingResponse } from '../composables/useChatbotPendingResponse'
import { useTheme } from '../composables/useTheme'
import ParentSidebar from '../components/ParentSidebar.vue'
import NubiIcon from '../components/base/NubiIcon.vue'
import InactivityOverlay from '../components/InactivityOverlay.vue'
import ThemeToggle from '../components/ThemeToggle.vue'

const { t } = useI18n()
const uiStore = useUIStore()
const { logout } = useParentalSession()
const { isWaitingForChatbot } = useChatbotPendingResponse()
const { theme } = useTheme()

const showInactivityOverlay = ref(false)

function handleExpire() {
  showInactivityOverlay.value = true
}

function handleCancel() {
  showInactivityOverlay.value = false
  reset()
}

function handleExpired() {
  logout()
}

const { start, stop, pause, resume, reset } = useInactivityTimer(handleExpire)

watch(isWaitingForChatbot, (waiting) => {
  if (waiting) {
    pause()
  } else {
    resume()
  }
})

onMounted(() => {
  start()
})

onUnmounted(() => {
  stop()
})
</script>

<style scoped>
.parent-panel-layout {
  display: flex;
  min-height: 100vh;
  width: 100%;
}

.parent-panel-layout__main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.parent-panel-layout__header {
  display: none;
  align-items: center;
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
  min-height: 56px;
  border-bottom: var(--nubi-border-width) solid var(--nubi-border-default);
  background-color: var(--nubi-bg-surface);
}

.parent-panel-layout__hamburger {
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

.parent-panel-layout__hamburger:hover {
  background-color: var(--nubi-bg-surface-tertiary);
}

.parent-panel-layout__hamburger:focus-visible {
  outline: none;
  box-shadow: 0 0 0 2px var(--nubi-color-focus);
}

.parent-panel-layout__content {
  flex: 1;
  overflow-y: auto;
}

@media (max-width: 1023px) {
  .parent-panel-layout__header {
    display: flex;
  }
}
</style>
