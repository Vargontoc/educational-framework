<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useSessionStore } from '@/stores/useSessionStore'
import RotationOverlay from '@/components/RotationOverlay.vue'
import ChildrenSection from '@/components/panel/ChildrenSection.vue'

const { t } = useI18n()
const router = useRouter()
const sessionStore = useSessionStore()

type Section = 'settings' | 'children' | 'agent' | 'documentation' | 'familyReading' | 'familyRelaxation'

const activeSection = ref<Section>('settings')

interface NavItem {
  id: Section
  labelKey: string
  ariaLabelKey: string
  group: 'management' | 'experiences'
}

const managementItems: NavItem[] = [
  { id: 'settings', labelKey: 'panel.nav.settings', ariaLabelKey: 'panel.nav.settingsAriaLabel', group: 'management' },
  { id: 'children', labelKey: 'panel.nav.children', ariaLabelKey: 'panel.nav.childrenAriaLabel', group: 'management' },
  { id: 'agent', labelKey: 'panel.nav.agent', ariaLabelKey: 'panel.nav.agentAriaLabel', group: 'management' },
  { id: 'documentation', labelKey: 'panel.nav.documentation', ariaLabelKey: 'panel.nav.documentationAriaLabel', group: 'management' }
]

const experienceItems: NavItem[] = [
  { id: 'familyReading', labelKey: 'panel.nav.familyReading', ariaLabelKey: 'panel.nav.familyReadingAriaLabel', group: 'experiences' },
  { id: 'familyRelaxation', labelKey: 'panel.nav.familyRelaxation', ariaLabelKey: 'panel.nav.familyRelaxationAriaLabel', group: 'experiences' }
]

async function handleLogout() {
  await sessionStore.logout()
  router.replace('/')
}

function handleNavClick(item: NavItem) {
  if (item.id === 'documentation') {
    router.push('/docs')
  } else {
    activeSection.value = item.id as Exclude<Section, 'documentation'>
  }
}
</script>

<template>
  <RotationOverlay>
    <div class="panel-shell">
      <aside class="sidebar" aria-label="Panel navigation">

        <nav class="sidebar__nav" aria-label="Panel sections">
          <ul class="sidebar__list" role="list">
            <li>
              <span class="sidebar__group-label" aria-hidden="true">{{ t('panel.nav.managementGroup') ?? 'Gestión' }}</span>
            </li>
            <li v-for="item in managementItems" :key="item.id">
              <button
                class="sidebar__item"
                :class="[`sidebar__item--${item.group}`, { 'sidebar__item--active': item.id !== 'documentation' && activeSection === item.id }]"
                :aria-current="item.id !== 'documentation' && activeSection === item.id ? 'page' : undefined"
                :aria-label="t(item.ariaLabelKey)"
                @click="handleNavClick(item)"
              >
                <span class="sidebar__item-icon" aria-hidden="true">
                  <svg v-if="item.id === 'settings'" width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <path d="M10 12.5a2.5 2.5 0 100-5 2.5 2.5 0 000 5z" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M16.5 10c0-.34-.03-.67-.08-1l1.6-1.2a.375.375 0 00.06-.53l-1.54-2.6a.375.375 0 00-.53-.06l-1.91 1.06c-.31-.23-.65-.44-1-.61l-.33-2A.375.375 0 0012.5 2h-5a.375.375 0 00-.37.32l-.33 2c-.36.17-.7.38-1 .61l-1.9-1.06a.375.375 0 00-.53.06L1.92 7.2a.375.375 0 00.06.53l1.6 1.2c-.05.33-.08.66-.08 1s.03.67.08 1l-1.6 1.2a.375.375 0 00-.06.53l1.54 2.6a.375.375 0 00.53.06l1.9-1.06c.31.23.65.44 1 .61l.33 2A.375.375 0 007.5 18h5a.375.375 0 00.37-.32l.33-2c.36-.17.7-.38 1-.61l1.9 1.06a.375.375 0 00.53-.06l1.54-2.6a.375.375 0 00-.06-.53l-1.6-1.2c.05-.33.08-.66.08-1z" stroke="currentColor" stroke-width="1.5"/>
                  </svg>
                  <svg v-else-if="item.id === 'children'" width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <circle cx="7" cy="6" r="3" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M2 17c0-2.21 2.24-4 5-4s5 1.79 5 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <circle cx="14" cy="7" r="2.5" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M18 17c0-1.66-1.34-3-3-3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  <svg v-else-if="item.id === 'agent'" width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <path d="M10 11a3 3 0 100-6 3 3 0 000 6z" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M3 17a7 7 0 0114 0" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  <svg v-else-if="item.id === 'documentation'" width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <path d="M4 4h8l4 4v8a1 1 0 01-1 1H4a1 1 0 01-1-1V4z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                    <path d="M12 4v4h4M7 10h6M7 13h4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </span>
                <span class="sidebar__item-label">{{ t(item.labelKey) }}</span>
              </button>
            </li>
          </ul>

          <ul class="sidebar__list sidebar__list--experiences" role="list">
            <li>
              <span class="sidebar__group-label" aria-hidden="true">{{ t('panel.nav.experiencesGroup') ?? 'Experiencias' }}</span>
            </li>
            <li v-for="item in experienceItems" :key="item.id">
              <button
                class="sidebar__item sidebar__item--experiences"
                :class="{ 'sidebar__item--active': activeSection === item.id }"
                :aria-current="activeSection === item.id ? 'page' : undefined"
                :aria-label="t(item.ariaLabelKey)"
                @click="activeSection = item.id"
              >
                <span class="sidebar__item-icon" aria-hidden="true">
                  <svg v-if="item.id === 'familyReading'" width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <path d="M3 5h14M3 10h10M3 15h12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  <svg v-else-if="item.id === 'familyRelaxation'" width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <path d="M5 10c0-1.1.9-2 2-2s2 .9 2 2-.9 2-2 2-2-.9-2-2z" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M3 17c2 0 3.5-1 5-2.5S12 12 14 12s3 1.5 3 3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </span>
                <span class="sidebar__item-label">{{ t(item.labelKey) }}</span>
              </button>
            </li>
          </ul>
        </nav>

        <button
          class="sidebar__logout"
          :aria-label="t('panel.logoutAriaLabel')"
          @click="handleLogout"
        >
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none" aria-hidden="true">
            <path d="M7 15H3a1 1 0 01-1-1V4a1 1 0 011-1h4M12 13l3-3-3-3M15 10H6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span class="sidebar__logout-label">{{ t('panel.logout') }}</span>
        </button>
      </aside>

      <main class="panel-content" :aria-label="t(activeSection ? `panel.section.${activeSection}.title` : 'panel.title')">
        <header class="panel-content__header">
          <h1 class="panel-content__title">{{ t(`panel.section.${activeSection}.title`) }}</h1>
        </header>

        <div class="panel-content__body">
          <ChildrenSection v-if="activeSection === 'children'" />
          <div v-else class="panel-placeholder">
            <div class="panel-placeholder__icon" aria-hidden="true">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <circle cx="24" cy="24" r="24" fill="#E8EDF5"/>
                <path d="M16 24h16M24 16v16" stroke="#2B5BE0" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <p class="panel-placeholder__description">{{ t(`panel.section.${activeSection}.description`) }}</p>
            <span class="panel-placeholder__badge">{{ t('panel.placeholder.comingSoon') }}</span>
          </div>
        </div>
      </main>
    </div>
  </RotationOverlay>
</template>

<style scoped>
.panel-shell {
  display: grid;
  grid-template-columns: 220px 1fr;
  min-height: 100vh;
  min-height: 100dvh;
  background: var(--color-panel-bg);
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding: var(--space-lg) var(--space-md);
  background: var(--color-surface);
  box-shadow: 4px 0 24px rgba(26, 35, 64, 0.06);
  overflow-y: auto;
}

.sidebar__brand {
  display: grid;
  width: 56px;
  height: 56px;
  place-items: center;
  border-radius: 16px;
  background: var(--color-primary);
  color: var(--color-text-on-primary);
  font-weight: 800;
  font-size: var(--font-size-sm);
  margin: 0 auto;
}

.sidebar__nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.sidebar__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.sidebar__list--experiences {
  margin-top: var(--space-sm);
  padding-top: var(--space-sm);
  border-top: 1px solid var(--color-neutral);
}

.sidebar__group-label {
  display: block;
  padding: 0 var(--space-sm) var(--space-xs);
  font-size: var(--font-size-caption);
  font-weight: 700;
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.sidebar__item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  min-height: var(--touch-target-adult);
  padding: var(--space-sm) var(--space-sm);
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-text-secondary);
  font-size: var(--font-size-button);
  font-family: var(--font-family-base);
  font-weight: 600;
  cursor: pointer;
  text-align: left;
  position: relative;
  transition: background-color var(--transition-base), color var(--transition-base);
}

.sidebar__item:hover {
  background: var(--color-neutral);
  color: var(--color-primary);
}

.sidebar__item--active {
  background: color-mix(in srgb, var(--color-primary) 10%, transparent);
  color: var(--color-primary);
  box-shadow: inset 3px 0 0 var(--color-primary);
}

.sidebar__item--experiences.sidebar__item--active {
  background: color-mix(in srgb, var(--color-celebration) 10%, transparent);
  color: var(--color-celebration-dark);
  box-shadow: inset 3px 0 0 var(--color-celebration);
}

.sidebar__item-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 20px;
  height: 20px;
}

.sidebar__item-label {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar__logout {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  min-height: var(--touch-target-adult);
  padding: var(--space-sm) var(--space-sm);
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-text-secondary);
  font-size: var(--font-size-button);
  font-family: var(--font-family-base);
  font-weight: 600;
  cursor: pointer;
  text-align: left;
  transition: background-color var(--transition-base), color var(--transition-base);
  margin-top: auto;
}

.sidebar__logout:hover {
  background: color-mix(in srgb, #e53935 8%, transparent);
  color: #e53935;
}

.sidebar__logout-label {
  white-space: nowrap;
}

.panel-content {
  display: flex;
  flex-direction: column;
  padding: var(--space-xl);
  overflow-y: auto;
}

.panel-content__header {
  margin-bottom: var(--space-xl);
}

.panel-content__eyebrow {
  margin: 0 0 var(--space-xs);
  color: var(--color-primary);
  font-size: var(--font-size-caption);
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.panel-content__title {
  margin: 0;
  font-size: var(--font-size-section-title);
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.3;
}

.panel-content__body {
  flex: 1;
}

.panel-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-md);
  min-height: 320px;
  padding: var(--space-xl);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  box-shadow: 0 8px 32px rgba(26, 35, 64, 0.08);
  text-align: center;
}

.panel-placeholder__icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.panel-placeholder__description {
  margin: 0;
  max-width: 360px;
  color: var(--color-text-secondary);
  font-size: var(--font-size-body);
  line-height: 1.5;
}

.panel-placeholder__badge {
  display: inline-flex;
  align-items: center;
  padding: var(--space-xs) var(--space-md);
  border-radius: var(--radius-pill);
  background: color-mix(in srgb, var(--color-primary) 10%, transparent);
  color: var(--color-primary);
  font-size: var(--font-size-caption);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

@media (max-width: 768px) {
  .panel-shell {
    grid-template-columns: 64px 1fr;
  }

  .sidebar {
    align-items: center;
    padding: var(--space-md) var(--space-xs);
    gap: var(--space-sm);
  }

  .sidebar__brand {
    width: 44px;
    height: 44px;
    font-size: var(--font-size-caption);
    margin: 0;
  }

  .sidebar__group-label {
    display: none;
  }

  .sidebar__list {
    width: 100%;
    gap: 2px;
  }

  .sidebar__list--experiences {
    padding-top: var(--space-sm);
    border-top: 1px solid var(--color-neutral);
  }

  .sidebar__item {
    justify-content: center;
    padding: var(--space-sm);
    width: 48px;
  }

  .sidebar__item--active {
    box-shadow: inset 2px 0 0 var(--color-primary);
  }

  .sidebar__item-label {
    display: none;
  }

  .sidebar__logout {
    justify-content: center;
    padding: var(--space-sm);
    width: 48px;
  }

  .sidebar__logout-label {
    display: none;
  }

  .panel-content {
    padding: var(--space-lg);
  }

  .panel-placeholder {
    min-height: 240px;
    padding: var(--space-lg);
  }
}
</style>
