<template>
  <div class="panel-cover-view">

    <div class="panel-cover-view__groups">
      <section class="panel-cover-view__group">
        <h2 class="panel-cover-view__group-title">{{ t('views.panelCover.groups.panel') }}</h2>
        <div class="panel-cover-view__cards">
          <router-link
            v-for="card in panelCards"
            :key="card.key"
            :to="card.to"
            class="panel-cover-view__card"
            :aria-label="t(card.labelKey)"
          >
            <NubiIcon :name="card.icon" :size="28" />
            <span class="panel-cover-view__card-label">{{ t(card.labelKey) }}</span>
          </router-link>
        </div>
      </section>

      <section class="panel-cover-view__group">
        <h2 class="panel-cover-view__group-title">{{ t('views.panelCover.groups.experiences') }}</h2>
        <div class="panel-cover-view__cards">
          <router-link
            v-for="card in experienceCards"
            :key="card.key"
            :to="card.to"
            class="panel-cover-view__card"
            :aria-label="t(card.labelKey)"
          >
            <NubiIcon :name="card.icon" :size="28" />
            <span class="panel-cover-view__card-label">{{ t(card.labelKey) }}</span>
          </router-link>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import NubiIcon from '../components/base/NubiIcon.vue'

const { t } = useI18n()

interface CardItem {
  key: string
  icon: string
  labelKey: string
  to: string
}

const panelCards: CardItem[] = [
  { key: 'settings', icon: 'settings', labelKey: 'views.panelCover.sections.settings', to: '/panel/configuracion' },
  { key: 'children', icon: 'users', labelKey: 'views.panelCover.sections.children', to: '/panel/ninos' },
  { key: 'chatbot', icon: 'message-circle', labelKey: 'views.panelCover.sections.chatbot', to: '/panel/chatbot' },
  { key: 'documentation', icon: 'file-text', labelKey: 'views.panelCover.sections.documentation', to: '/docs' }
]

const experienceCards: CardItem[] = [
  { key: 'reading', icon: 'book-open', labelKey: 'views.panelCover.sections.readingFamily', to: '/panel/lectura-familiar' },
  { key: 'relaxation', icon: 'wind', labelKey: 'views.panelCover.sections.relaxationFamily', to: '/panel/relajacion-familiar' }
]
</script>

<style scoped>
.panel-cover-view {
  padding: var(--nubi-spacing-xl);
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-lg);
}

.panel-cover-view__title {
  font-size: var(--nubi-font-size-2xl);
  font-weight: var(--nubi-font-weight-bold);
  color: var(--nubi-text-primary);
  margin: 0;
  line-height: var(--nubi-line-height-tight);
}

.panel-cover-view__description {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-secondary);
  margin: 0;
  line-height: var(--nubi-line-height-relaxed);
}

.panel-cover-view__groups {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-xl);
}

.panel-cover-view__group {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-md);
}

.panel-cover-view__group-title {
  font-size: var(--nubi-font-size-lg);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-primary);
  margin: 0;
  padding-bottom: var(--nubi-spacing-xs);
  border-bottom: 1px solid var(--nubi-border-default);
}

.panel-cover-view__cards {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--nubi-spacing-md);
}

.panel-cover-view__card {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-md);
  padding: var(--nubi-spacing-md) var(--nubi-spacing-lg);
  min-height: 48px;
  background-color: var(--nubi-bg-surface);
  border: 1px solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-lg);
  cursor: pointer;
  text-decoration: none;
  color: inherit;
  transition: background-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              box-shadow var(--nubi-duration-fast) var(--nubi-ease-in-out);
  outline: none;
}

.panel-cover-view__card:hover {
  background-color: var(--nubi-bg-surface-secondary);
}

.panel-cover-view__card:focus-visible {
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

.panel-cover-view__card-label {
  font-size: var(--nubi-font-size-base);
  font-weight: var(--nubi-font-weight-medium);
  color: var(--nubi-text-primary);
}

@media (min-width: 768px) {
  .panel-cover-view__cards {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 640px) {
  .panel-cover-view {
    padding: var(--nubi-spacing-md);
  }

  .panel-cover-view__title {
    font-size: var(--nubi-font-size-xl);
  }
}

@media (max-height: 500px) and (orientation: landscape) {
  .panel-cover-view {
    padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
    gap: var(--nubi-spacing-md);
  }

  .panel-cover-view__title {
    font-size: var(--nubi-font-size-lg);
  }
}
</style>
