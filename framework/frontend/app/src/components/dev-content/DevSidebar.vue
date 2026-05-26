<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

interface Props {
  activeSection: string
}

defineProps<Props>()

const emit = defineEmits<{
  (e: 'navigate', section: string): void
}>()

const sections = [
  { id: 'categories', icon: '📁' },
  { id: 'topics', icon: '📚' },
  { id: 'activities', icon: '🎯' },
  { id: 'difficultyLevels', icon: '📊' },
  { id: 'resources', icon: '🔧' },
  { id: 'locales', icon: '🌍' },
  { id: 'curiosities', icon: '💡' },
  { id: 'avatarEvents', icon: '🎮' }
] as const
</script>

<template>
  <aside class="sidebar">
    <h2 class="sidebar__title">{{ t('devContent.title') }}</h2>
    <nav class="sidebar__nav">
      <button
        v-for="section in sections"
        :key="section.id"
        class="sidebar__item"
        :class="{ 'sidebar__item--active': activeSection === section.id }"
        @click="emit('navigate', section.id)"
      >
        <span class="sidebar__icon" aria-hidden="true">{{ section.icon }}</span>
        <span class="sidebar__label">{{ t(`devContent.nav.${section.id}`) }}</span>
      </button>
    </nav>
  </aside>
</template>

<style scoped>
.sidebar {
  width: 260px;
  min-width: 260px;
  background-color: #f8fafc;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.sidebar__title {
  padding: var(--space-lg);
  font-size: var(--font-size-md);
  font-weight: 700;
  color: var(--color-primary);
  border-bottom: 1px solid #e2e8f0;
}

.sidebar__nav {
  display: flex;
  flex-direction: column;
  padding: var(--space-sm);
  gap: var(--space-xs);
}

.sidebar__item {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-sm) var(--space-md);
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  cursor: pointer;
  transition: background-color var(--transition-base);
  text-align: left;
  min-height: var(--touch-target-min);
}

.sidebar__item:hover {
  background-color: #e2e8f0;
}

.sidebar__item--active {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.sidebar__item--active:hover {
  background-color: var(--color-primary-dark);
}

.sidebar__icon {
  font-size: var(--font-size-md);
  line-height: 1;
}

.sidebar__label {
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  font-weight: 500;
}

@media (max-width: 768px) {
  .sidebar {
    width: 100%;
    min-width: unset;
    border-right: none;
    border-bottom: 1px solid #e2e8f0;
  }

  .sidebar__nav {
    flex-direction: row;
    flex-wrap: wrap;
    gap: var(--space-xs);
    padding: var(--space-sm);
  }

  .sidebar__item {
    flex: 1 1 auto;
    min-width: 120px;
    justify-content: center;
  }

  .sidebar__title {
    text-align: center;
  }
}
</style>
