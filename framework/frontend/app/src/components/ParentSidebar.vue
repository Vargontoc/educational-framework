<template>
  <aside
    :class="['parent-sidebar', { 'parent-sidebar--open': uiStore.sidebarOpen }]"
    role="navigation"
    :aria-label="t('sidebar.ariaLabels.mainNavigation')"
  >
    <div
      class="parent-sidebar__backdrop"
      @click="uiStore.closeSidebar()"
    />

    <div class="parent-sidebar__panel">
      <header class="parent-sidebar__header">
        <span class="parent-sidebar__brand">{{ t('views.panelCover.title') }}</span>
        <button
          class="parent-sidebar__close"
          :aria-label="t('sidebar.ariaLabels.closeMenu')"
          @click="uiStore.closeSidebar()"
        >
          <NubiIcon name="x" :size="20" />
        </button>
      </header>

      <nav class="parent-sidebar__nav">
        <div class="parent-sidebar__group">
          <span class="parent-sidebar__group-label">{{ t('sidebar.groups.panel') }}</span>
          <ul class="parent-sidebar__list">
            <li v-for="item in panelItems" :key="item.to">
              <router-link
                :to="item.to"
                :class="['parent-sidebar__link', { 'parent-sidebar__link--active': isActive(item.to) }]"
                :aria-current="isActive(item.to) ? 'page' : undefined"
                @click="onNavigate"
              >
                <NubiIcon :name="item.icon" :size="20" />
                <span class="parent-sidebar__label">{{ t(item.labelKey) }}</span>
              </router-link>
            </li>
            <li>
              <router-link
                :to="documentationRoute"
                :class="['parent-sidebar__link', { 'parent-sidebar__link--active': isDocumentationActive }]"
                :aria-current="isDocumentationActive ? 'page' : undefined"
                @click="onNavigate"
              >
                <NubiIcon name="file-text" :size="20" />
                <span class="parent-sidebar__label">{{ t('sidebar.sections.documentacion') }}</span>
              </router-link>
            </li>
          </ul>
        </div>

        <div class="parent-sidebar__group">
          <span class="parent-sidebar__group-label">{{ t('sidebar.groups.experiences') }}</span>
          <ul class="parent-sidebar__list">
            <li v-for="item in experienceItems" :key="item.to">
              <router-link
                :to="item.to"
                :class="['parent-sidebar__link', { 'parent-sidebar__link--active': isActive(item.to) }]"
                :aria-current="isActive(item.to) ? 'page' : undefined"
                @click="onNavigate"
              >
                <NubiIcon :name="item.icon" :size="20" />
                <span class="parent-sidebar__label">{{ t(item.labelKey) }}</span>
              </router-link>
            </li>
          </ul>
        </div>

        <div class="parent-sidebar__separator" role="separator" />

        <button
          class="parent-sidebar__logout"
          @click="handleLogout"
        >
          <NubiIcon name="log-out" :size="18" />
          <span class="parent-sidebar__logout-label">{{ t('sidebar.logout') }}</span>
        </button>
      </nav>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { computed } from 'vue'
import { useUIStore } from '../stores/ui'
import { useParentalSession } from '../composables/useParentalSession'
import NubiIcon from './base/NubiIcon.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const uiStore = useUIStore()
const { logout } = useParentalSession()

interface NavItem {
  to: string
  icon: string
  labelKey: string
}

const panelItems: NavItem[] = [
  { to: '/panel/configuracion', icon: 'settings', labelKey: 'sidebar.sections.configuracion' },
  { to: '/panel/ninos', icon: 'users', labelKey: 'sidebar.sections.ninos' },
  { to: '/panel/chatbot', icon: 'message-circle', labelKey: 'sidebar.sections.chatbot' }
]

const experienceItems: NavItem[] = [
  { to: '/panel/lectura-familiar', icon: 'book-open', labelKey: 'sidebar.sections.lecturaFamiliar' },
  { to: '/panel/relajacion-familiar', icon: 'wind', labelKey: 'sidebar.sections.relajacionFamiliar' }
]

const documentationRoute = computed(() => ({
  path: '/docs',
  query: { from: route.path }
}))

const isDocumentationActive = computed(() => route.path.startsWith('/docs'))

function isActive(path: string): boolean {
  return route.path === path
}

function onNavigate() {
  uiStore.closeSidebar()
}

async function handleLogout() {
  await logout()
  router.replace('/')
}
</script>

<style scoped>
.parent-sidebar {
  display: flex;
  flex-shrink: 0;
}

.parent-sidebar__backdrop {
  display: none;
}

.parent-sidebar__panel {
  display: flex;
  flex-direction: column;
  width: 280px;
  height: 100vh;
  background-color: var(--nubi-bg-surface-secondary);
  border-right: var(--nubi-border-width) solid var(--nubi-border-default);
  overflow: hidden;
}

.parent-sidebar__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--nubi-spacing-md) var(--nubi-spacing-lg);
  min-height: 64px;
  border-bottom: var(--nubi-border-width) solid var(--nubi-border-default);
}

.parent-sidebar__brand {
  font-size: var(--nubi-font-size-lg);
  font-weight: var(--nubi-font-weight-bold);
  color: var(--nubi-color-primary);
}

.parent-sidebar__close {
  display: none;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  min-height: 48px;
  padding: var(--nubi-spacing-xs);
  border: none;
  background: none;
  cursor: pointer;
  color: var(--nubi-text-secondary);
  border-radius: var(--nubi-radius-md);
  transition: background-color var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.parent-sidebar__close:hover {
  background-color: var(--nubi-bg-surface-tertiary);
}

.parent-sidebar__close:focus-visible {
  outline: none;
  box-shadow: 0 0 0 2px var(--nubi-color-focus);
}

.parent-sidebar__nav {
  flex: 1;
  overflow-y: auto;
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-sm);
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-md);
}

.parent-sidebar__group {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-xs);
}

.parent-sidebar__group-label {
  font-size: var(--nubi-font-size-xs);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-tertiary);
  text-transform: uppercase;
  letter-spacing: var(--nubi-letter-spacing-wide);
  padding: var(--nubi-spacing-xs) var(--nubi-spacing-sm);
}

.parent-sidebar__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.parent-sidebar__link {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-sm);
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
  min-height: 48px;
  color: var(--nubi-text-secondary);
  text-decoration: none;
  border-radius: var(--nubi-radius-md);
  border-left: 3px solid transparent;
  transition: color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              background-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              border-color var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.parent-sidebar__link:hover {
  color: var(--nubi-text-primary);
  background-color: var(--nubi-bg-surface-tertiary);
}

.parent-sidebar__link--active {
  color: var(--nubi-color-primary);
  background-color: var(--nubi-bg-surface);
  border-left-color: var(--nubi-color-primary);
  font-weight: var(--nubi-font-weight-medium);
}

.parent-sidebar__link:focus-visible {
  outline: none;
  box-shadow: 0 0 0 2px var(--nubi-color-focus);
}

.parent-sidebar__label {
  font-size: var(--nubi-font-size-base);
  line-height: var(--nubi-line-height-tight);
}

.parent-sidebar__separator {
  height: 1px;
  background-color: var(--nubi-border-default);
  margin: var(--nubi-spacing-sm) var(--nubi-spacing-md);
}

.parent-sidebar__logout {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-sm);
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
  min-height: 48px;
  margin: 0 var(--nubi-spacing-sm);
  border: none;
  background: none;
  cursor: pointer;
  color: var(--nubi-text-tertiary);
  font-family: var(--nubi-font-family-base);
  font-size: var(--nubi-font-size-sm);
  border-radius: var(--nubi-radius-md);
  transition: color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              background-color var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.parent-sidebar__logout:hover {
  color: var(--nubi-text-secondary);
  background-color: var(--nubi-bg-surface-tertiary);
}

.parent-sidebar__logout:focus-visible {
  outline: none;
  box-shadow: 0 0 0 2px var(--nubi-color-focus);
}

.parent-sidebar__logout-label {
  line-height: var(--nubi-line-height-tight);
}

@media (max-width: 1023px) {
  .parent-sidebar {
    position: fixed;
    inset: 0;
    z-index: 9000;
    pointer-events: none;
  }

  .parent-sidebar--open {
    pointer-events: auto;
  }

  .parent-sidebar__backdrop {
    display: block;
    position: absolute;
    inset: 0;
    background-color: var(--nubi-overlay-bg);
    opacity: 0;
    transition: opacity var(--nubi-duration-normal) var(--nubi-ease-in-out);
    pointer-events: none;
  }

  .parent-sidebar--open .parent-sidebar__backdrop {
    opacity: 1;
    pointer-events: auto;
  }

  .parent-sidebar__panel {
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    transform: translateX(-100%);
    transition: transform var(--nubi-duration-normal) var(--nubi-ease-in-out);
    z-index: 1;
  }

  .parent-sidebar--open .parent-sidebar__panel {
    transform: translateX(0);
  }

  .parent-sidebar__close {
    display: flex;
  }
}
</style>
