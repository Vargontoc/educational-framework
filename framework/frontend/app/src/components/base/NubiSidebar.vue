<template>
  <aside
    :class="[
      'nubi-sidebar',
      { 
        'nubi-sidebar--collapsed': isCollapsed,
        'nubi-sidebar--mobile-open': isMobileOpen
      }
    ]"
    :aria-label="t('components.sidebar.panel')"
  >
    <!-- Overlay móvil -->
    <div
      v-if="isMobile && isMobileOpen"
      class="nubi-sidebar__overlay"
      @click="closeMobile"
    />
    
    <!-- Contenido del sidebar -->
    <div class="nubi-sidebar__content">
      <!-- Header -->
      <div class="nubi-sidebar__header">
        <div v-if="!isCollapsed" class="nubi-sidebar__brand">
          <span class="nubi-sidebar__brand-text">My Friend Nubi</span>
        </div>
        
        <button
          class="nubi-sidebar__toggle"
          :aria-label="isCollapsed ? t('components.sidebar.expand') : t('components.sidebar.collapse')"
          @click="toggleCollapse"
        >
          <NubiIcon :name="isCollapsed ? 'chevron-right' : 'chevron-left'" :size="20" />
        </button>
      </div>
      
      <!-- Navegación -->
      <nav class="nubi-sidebar__nav">
        <div v-for="section in sections" :key="section.id" class="nubi-sidebar__section">
          <span v-if="!isCollapsed && section.title" class="nubi-sidebar__section-title">
            {{ section.title }}
          </span>
          
          <ul class="nubi-sidebar__items">
            <li v-for="item in section.items" :key="item.to">
              <router-link
                :to="item.to"
                :class="[
                  'nubi-sidebar__item',
                  { 'nubi-sidebar__item--active': isActive(item.to) }
                ]"
                @click="handleItemClick"
              >
                <NubiIcon :name="item.icon" :size="20" class="nubi-sidebar__item-icon" />
                <span v-if="!isCollapsed" class="nubi-sidebar__item-label">
                  {{ item.label }}
                </span>
              </router-link>
            </li>
          </ul>
        </div>
      </nav>
      
      <!-- Footer -->
      <div class="nubi-sidebar__footer">
        <button
          class="nubi-sidebar__theme-toggle"
          :aria-label="getCurrentTheme() === 'light' ? t('views.catalog.darkMode') : t('views.catalog.lightMode')"
          @click="toggleTheme"
        >
          <NubiIcon :name="getCurrentTheme() === 'light' ? 'moon' : 'sun'" :size="20" />
          <span v-if="!isCollapsed" class="nubi-sidebar__theme-label">
            {{ getCurrentTheme() === 'light' ? t('views.catalog.darkMode') : t('views.catalog.lightMode') }}
          </span>
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
/**
 * NubiSidebar - Menú lateral de navegación
 * 
 * Características:
 * - Iconos + texto expandido, solo iconos colapsado
 * - Animación suave (200-300ms)
 * - Responsive: overlay en móvil, lateral fijo en tablet
 * - Integración con Vue Router
 * - Secciones configurables
 * - Toggle de tema
 */

import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { useTheme } from '../../composables/useTheme'
import NubiIcon from './NubiIcon.vue'

export interface SidebarItem {
  label: string
  icon: string
  to: string
}

export interface SidebarSection {
  id: string
  title?: string
  items: SidebarItem[]
}

interface Props {
  /** Secciones del sidebar */
  sections?: SidebarSection[]
  /** Estado colapsado (v-model:collapsed) */
  collapsed?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  sections: () => [],
  collapsed: false
})

const emit = defineEmits<{
  'update:collapsed': [value: boolean]
  navigate: [item: SidebarItem]
}>()

const { t } = useI18n()
const route = useRoute()
const { toggleTheme, getCurrentTheme } = useTheme()

const isCollapsed = ref(props.collapsed)
const isMobileOpen = ref(false)
const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1024)

const isMobile = computed(() => windowWidth.value < 768)

function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value
  emit('update:collapsed', isCollapsed.value)
}

function openMobile() {
  isMobileOpen.value = true
}

function closeMobile() {
  isMobileOpen.value = false
}

function handleItemClick() {
  if (isMobile.value) {
    closeMobile()
  }
}

function isActive(path: string): boolean {
  return route.path === path || route.path.startsWith(path + '/')
}

function handleResize() {
  windowWidth.value = window.innerWidth
  if (!isMobile.value) {
    isMobileOpen.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})

defineExpose({ openMobile, closeMobile, isMobileOpen })
</script>

<style scoped>
.nubi-sidebar {
  position: relative;
  display: flex;
  flex-shrink: 0;
}

.nubi-sidebar__overlay {
  position: fixed;
  inset: 0;
  background-color: var(--nubi-overlay-bg);
  z-index: 8000;
}

.nubi-sidebar__content {
  display: flex;
  flex-direction: column;
  width: 260px;
  height: 100vh;
  background-color: var(--nubi-bg-surface-secondary);
  border-right: var(--nubi-border-width) solid var(--nubi-border-default);
  transition: width var(--nubi-duration-normal) var(--nubi-ease-in-out);
  overflow: hidden;
  position: relative;
  z-index: 8001;
}

.nubi-sidebar--collapsed .nubi-sidebar__content {
  width: 72px;
}

/* Mobile */
@media (max-width: 767px) {
  .nubi-sidebar__content {
    position: fixed;
    left: 0;
    top: 0;
    transform: translateX(-100%);
    transition: transform var(--nubi-duration-normal) var(--nubi-ease-in-out);
  }
  
  .nubi-sidebar--mobile-open .nubi-sidebar__content {
    transform: translateX(0);
  }
  
  .nubi-sidebar--collapsed .nubi-sidebar__content {
    width: 260px;
  }
}

/* Header */
.nubi-sidebar__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--nubi-spacing-md);
  min-height: 64px;
  border-bottom: var(--nubi-border-width) solid var(--nubi-border-default);
}

.nubi-sidebar__brand {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-sm);
  overflow: hidden;
}

.nubi-sidebar__brand-text {
  font-size: var(--nubi-font-size-lg);
  font-weight: var(--nubi-font-weight-bold);
  color: var(--nubi-color-primary);
  white-space: nowrap;
}

.nubi-sidebar__toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
  min-height: 40px;
  padding: var(--nubi-spacing-xs);
  border: none;
  background: none;
  cursor: pointer;
  color: var(--nubi-text-secondary);
  border-radius: var(--nubi-radius-md);
  transition: color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              background-color var(--nubi-duration-fast) var(--nubi-ease-in-out);
  flex-shrink: 0;
}

.nubi-sidebar__toggle:hover {
  color: var(--nubi-text-primary);
  background-color: var(--nubi-bg-surface-tertiary);
}

.nubi-sidebar__toggle:focus-visible {
  outline: none;
  box-shadow: 0 0 0 2px var(--nubi-color-focus);
}

/* Navigation */
.nubi-sidebar__nav {
  flex: 1;
  overflow-y: auto;
  padding: var(--nubi-spacing-sm);
}

.nubi-sidebar__section {
  margin-bottom: var(--nubi-spacing-md);
}

.nubi-sidebar__section-title {
  display: block;
  font-size: var(--nubi-font-size-xs);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-tertiary);
  text-transform: uppercase;
  letter-spacing: var(--nubi-letter-spacing-wide);
  padding: var(--nubi-spacing-xs) var(--nubi-spacing-sm);
  margin-bottom: var(--nubi-spacing-xs);
  white-space: nowrap;
  overflow: hidden;
}

.nubi-sidebar__items {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-xs);
}

.nubi-sidebar__item {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-sm);
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
  min-height: 48px;
  color: var(--nubi-text-secondary);
  text-decoration: none;
  border-radius: var(--nubi-radius-md);
  transition: color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              background-color var(--nubi-duration-fast) var(--nubi-ease-in-out);
  white-space: nowrap;
  overflow: hidden;
}

.nubi-sidebar__item:hover {
  color: var(--nubi-text-primary);
  background-color: var(--nubi-bg-surface-tertiary);
}

.nubi-sidebar__item--active {
  color: var(--nubi-color-primary);
  background-color: var(--nubi-bg-surface);
  font-weight: var(--nubi-font-weight-medium);
}

.nubi-sidebar__item:focus-visible {
  outline: none;
  box-shadow: 0 0 0 2px var(--nubi-color-focus);
}

.nubi-sidebar__item-icon {
  flex-shrink: 0;
}

.nubi-sidebar__item-label {
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Footer */
.nubi-sidebar__footer {
  padding: var(--nubi-spacing-sm);
  border-top: var(--nubi-border-width) solid var(--nubi-border-default);
}

.nubi-sidebar__theme-toggle {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-sm);
  width: 100%;
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
  min-height: 48px;
  border: none;
  background: none;
  cursor: pointer;
  color: var(--nubi-text-secondary);
  font-family: var(--nubi-font-family-base);
  font-size: var(--nubi-font-size-sm);
  border-radius: var(--nubi-radius-md);
  transition: color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              background-color var(--nubi-duration-fast) var(--nubi-ease-in-out);
  white-space: nowrap;
  overflow: hidden;
}

.nubi-sidebar__theme-toggle:hover {
  color: var(--nubi-text-primary);
  background-color: var(--nubi-bg-surface-tertiary);
}

.nubi-sidebar__theme-toggle:focus-visible {
  outline: none;
  box-shadow: 0 0 0 2px var(--nubi-color-focus);
}

.nubi-sidebar__theme-label {
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
