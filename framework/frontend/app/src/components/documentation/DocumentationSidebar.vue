<template>
  <aside
    :class="['documentation-sidebar', { 'documentation-sidebar--open': isOpen }]"
    role="navigation"
    :aria-label="t('views.docs.sidebar.label')"
  >
    <div
      class="documentation-sidebar__backdrop"
      @click="emit('close')"
    />

    <nav class="documentation-sidebar__panel">
      <ul class="documentation-sidebar__list">
        <li v-for="section in sections" :key="section.id">
          <router-link
            :to="section.route"
            :class="['documentation-sidebar__link', { 'documentation-sidebar__link--active': currentSection === section.id }]"
            :aria-current="currentSection === section.id ? 'page' : undefined"
            @click="onNavigate"
          >
            <NubiIcon :name="section.icon" :size="20" />
            <span class="documentation-sidebar__label">{{ section.label }}</span>
          </router-link>
        </li>
      </ul>
    </nav>
  </aside>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import NubiIcon from '../base/NubiIcon.vue'

interface Props {
  currentSection: string
  isOpen: boolean
}

defineProps<Props>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

const { t } = useI18n()

const sections = [
  { id: 'quien-soy', label: 'Quién soy', icon: 'smile', route: '/docs/quien-soy' },
  { id: 'primeros-pasos', label: 'Primeros pasos', icon: 'play', route: '/docs/primeros-pasos' },
  { id: 'agentes-ai', label: 'Agentes AI', icon: 'robot', route: '/docs/agentes-ai' },
  { id: 'minijuegos', label: 'Minijuegos', icon: 'gamepad-2', route: '/docs/minijuegos' },
  { id: 'contacto', label: 'Contacto', icon: 'mail', route: '/docs/contacto' }
]

function onNavigate() {
  emit('close')
}
</script>

<style scoped>
.documentation-sidebar {
  display: flex;
  flex-shrink: 0;
}

.documentation-sidebar__backdrop {
  display: none;
}

.documentation-sidebar__panel {
  display: flex;
  flex-direction: column;
  width: 280px;
  height: 100%;
  background-color: var(--nubi-bg-surface-secondary);
  border-right: var(--nubi-border-width) solid var(--nubi-border-default);
  overflow-y: auto;
  padding: var(--nubi-spacing-md) var(--nubi-spacing-sm);
}

.documentation-sidebar__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.documentation-sidebar__link {
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

.documentation-sidebar__link:hover {
  color: var(--nubi-text-primary);
  background-color: var(--nubi-bg-surface-tertiary);
}

.documentation-sidebar__link--active {
  color: var(--nubi-color-primary);
  background-color: var(--nubi-bg-surface);
  border-left-color: var(--nubi-color-primary);
  font-weight: var(--nubi-font-weight-medium);
}

.documentation-sidebar__link:focus-visible {
  outline: none;
  box-shadow: 0 0 0 2px var(--nubi-color-focus);
}

.documentation-sidebar__label {
  font-size: var(--nubi-font-size-base);
  line-height: var(--nubi-line-height-tight);
}

@media (max-width: 1023px) {
  .documentation-sidebar {
    position: fixed;
    inset: 0;
    z-index: 9000;
    pointer-events: none;
  }

  .documentation-sidebar--open {
    pointer-events: auto;
  }

  .documentation-sidebar__backdrop {
    display: block;
    position: absolute;
    inset: 0;
    background-color: var(--nubi-overlay-bg);
    opacity: 0;
    transition: opacity var(--nubi-duration-normal) var(--nubi-ease-in-out);
    pointer-events: none;
  }

  .documentation-sidebar--open .documentation-sidebar__backdrop {
    opacity: 1;
    pointer-events: auto;
  }

  .documentation-sidebar__panel {
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    transform: translateX(-100%);
    transition: transform var(--nubi-duration-normal) var(--nubi-ease-in-out);
    z-index: 1;
  }

  .documentation-sidebar--open .documentation-sidebar__panel {
    transform: translateX(0);
  }
}
</style>
