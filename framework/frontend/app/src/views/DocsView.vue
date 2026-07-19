<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useSessionStore } from '@/stores/useSessionStore'
import RotationOverlay from '@/components/RotationOverlay.vue'

const { t } = useI18n()
const router = useRouter()
const sessionStore = useSessionStore()

type DocSection =
  | 'gettingStarted'
  | 'familyAndProfiles'
  | 'parentControl'
  | 'familyExperiences'
  | 'privacyAndSecurity'
  | 'support'
  | 'whoI'

interface SectionItem {
  id: DocSection
  labelKey: string
}

const activeSection = ref<DocSection>('gettingStarted')

const sections: SectionItem[] = [
  {id: 'whoI', labelKey : 'docs.sections.whoI.title'},
  { id: 'gettingStarted', labelKey: 'docs.sections.gettingStarted.title' },
  { id: 'familyAndProfiles', labelKey: 'docs.sections.familyAndProfiles.title' },
  { id: 'parentControl', labelKey: 'docs.sections.parentControl.title' },
  { id: 'familyExperiences', labelKey: 'docs.sections.familyExperiences.title' },
  { id: 'privacyAndSecurity', labelKey: 'docs.sections.privacyAndSecurity.title' },
  { id: 'support', labelKey: 'docs.sections.support.title' }
]
</script>

<template>
  <RotationOverlay>
    <div class="docs-shell">
      <div class="docs-layout">
        <aside class="docs-nav" aria-label="Documentación">
          <nav>
            <ul class="docs-nav__list" role="list">
              <li v-for="section in sections" :key="section.id">
                <button
                  class="docs-nav__item"
                  :class="{ 'docs-nav__item--active': activeSection === section.id }"
                  :aria-current="activeSection === section.id ? 'page' : undefined"
                  @click="activeSection = section.id"
                >
                  {{ t(section.labelKey) }}
                </button>
              </li>
            </ul>
          </nav>
        </aside>

        <main class="docs-content" tabindex="-1">
          <p class="docs-content__eyebrow">{{ t('docs.eyebrow') }}</p>
          <h1 class="docs-content__title">
            {{ t(`docs.sections.${activeSection}.title`) }}
          </h1>
          <p class="docs-content__description">
            {{ t(`docs.sections.${activeSection}.description`) }}
          </p>

          <div class="docs-placeholder" aria-hidden="true">
            <div class="docs-placeholder__icon">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <circle cx="24" cy="24" r="24" fill="#E8EDF5"/>
                <path d="M14 24h20M24 14v20" stroke="#2B5BE0" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <p class="docs-placeholder__text">
              {{ t(`docs.sections.${activeSection}.description`) }}
            </p>
          </div>

          <a
            v-if="sessionStore.isAuthenticated()"
            href="/panel"
            class="docs-back-panel"
            @click.prevent="router.push('/panel')"
          >
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
              <path d="M10 12L6 8L10 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            {{ t('docs.backPanel') }}
          </a>
        </main>
      </div>
    </div>
  </RotationOverlay>
</template>

<style scoped>
.docs-shell {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  min-height: 100dvh;
  background: var(--color-panel-bg);
}

.docs-header {
  background: var(--color-surface);
  box-shadow: 0 2px 12px rgba(26, 35, 64, 0.06);
  padding: var(--space-md) var(--space-lg);
}

.docs-header__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
}

.docs-header__back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: var(--touch-target-adult);
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-sm);
  color: var(--color-primary);
  font-size: var(--font-size-button);
  font-family: var(--font-family-base);
  font-weight: 600;
  text-decoration: none;
  transition: background-color var(--transition-base);
}

.docs-header__back:hover {
  background: color-mix(in srgb, var(--color-primary) 8%, transparent);
}

.docs-header__brand {
  display: grid;
  width: 44px;
  height: 44px;
  place-items: center;
  border-radius: 12px;
  background: var(--color-primary);
  color: var(--color-text-on-primary);
  font-weight: 800;
  font-size: var(--font-size-sm);
}

.docs-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: var(--space-xl);
  gap: var(--space-xl);
}

.docs-nav {
  display: flex;
  flex-direction: column;
}

.docs-nav__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.docs-nav__item {
  width: 100%;
  display: block;
  text-align: left;
  min-height: var(--touch-target-adult);
  padding: var(--space-sm) var(--space-md);
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-text-secondary);
  font-size: var(--font-size-button);
  font-family: var(--font-family-base);
  font-weight: 600;
  cursor: pointer;
  position: relative;
  transition: background-color var(--transition-base), color var(--transition-base);
}

.docs-nav__item:hover {
  background: color-mix(in srgb, var(--color-primary) 8%, transparent);
  color: var(--color-primary);
}

.docs-nav__item--active {
  background: color-mix(in srgb, var(--color-primary) 10%, transparent);
  color: var(--color-primary);
  box-shadow: inset 3px 0 0 var(--color-primary);
}

.docs-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: var(--space-xl);
  box-shadow: 0 8px 32px rgba(26, 35, 64, 0.08);
  outline: none;
}

.docs-content__eyebrow {
  margin: 0;
  color: var(--color-primary);
  font-size: var(--font-size-caption);
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.docs-content__title {
  margin: 0;
  font-size: var(--font-size-section-title);
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.3;
}

.docs-content__description {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-body);
  line-height: 1.6;
  max-width: 600px;
}

.docs-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-xl);
  margin-top: var(--space-md);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--color-neutral) 40%, transparent);
  text-align: center;
}

.docs-placeholder__icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.docs-placeholder__text {
  margin: 0;
  max-width: 400px;
  color: var(--color-text-secondary);
  font-size: var(--font-size-body);
  line-height: 1.5;
}

.docs-back-panel {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: var(--touch-target-adult);
  padding: var(--space-sm) var(--space-md);
  margin-top: var(--space-md);
  border-radius: var(--radius-pill);
  background: var(--color-primary);
  color: var(--color-text-on-primary);
  font-size: var(--font-size-button);
  font-family: var(--font-family-base);
  font-weight: 700;
  text-decoration: none;
  align-self: flex-start;
  transition: background-color var(--transition-base);
  box-shadow: 0 4px 0 var(--color-primary-dark);
}

.docs-back-panel:hover {
  background: var(--color-primary-dark);
}

.docs-back-panel:active {
  box-shadow: 0 2px 0 var(--color-primary-dark);
  transform: translateY(1px);
}

@media (max-width: 768px) {
  .docs-layout {
    grid-template-columns: 1fr;
    padding: var(--space-md);
    gap: var(--space-md);
  }

  .docs-nav {
    overflow-x: auto;
  }

  .docs-nav__list {
    flex-direction: row;
    flex-wrap: nowrap;
    gap: var(--space-xs);
  }

  .docs-nav__item {
    white-space: nowrap;
    min-height: 40px;
    padding: var(--space-xs) var(--space-sm);
  }

  .docs-nav__item--active {
    box-shadow: inset 0 -3px 0 var(--color-primary);
  }

  .docs-content {
    padding: var(--space-lg);
  }

  .docs-placeholder {
    padding: var(--space-lg);
  }
}
</style>
