<template>
  <div class="catalog-layout">
    <aside class="catalog-sidebar">
      <div class="catalog-header">
        <h1 class="catalog-title">{{ $t('views.catalog.title') }}</h1>
        <p class="catalog-subtitle">{{ $t('views.catalog.subtitle') }}</p>
      </div>
      <nav class="catalog-nav">
        <div class="nav-section">
          <h2 class="nav-title">{{ $t('views.catalog.designTokens') }}</h2>
          <ul class="nav-list">
            <li><router-link to="/dev/components/tokens/colors" class="nav-link">{{ $t('views.catalog.colors') }}</router-link></li>
            <li><router-link to="/dev/components/tokens/typography" class="nav-link">{{ $t('views.catalog.typography') }}</router-link></li>
            <li><router-link to="/dev/components/tokens/spacing" class="nav-link">{{ $t('views.catalog.spacing') }}</router-link></li>
            <li><router-link to="/dev/components/tokens/borders" class="nav-link">{{ $t('views.catalog.borders') }}</router-link></li>
          </ul>
        </div>
        <div class="nav-section">
          <h2 class="nav-title">{{ $t('views.catalog.actionComponents') }}</h2>
          <ul class="nav-list">
            <li><router-link to="/dev/components/button" class="nav-link">{{ $t('views.catalog.buttons') }}</router-link></li>
            <li><router-link to="/dev/components/icon-button" class="nav-link">{{ $t('views.catalog.iconButtons') }}</router-link></li>
          </ul>
        </div>
        <div class="nav-section">
          <h2 class="nav-title">{{ $t('views.catalog.feedbackComponents') }}</h2>
          <ul class="nav-list">
            <li><router-link to="/dev/components/spinner" class="nav-link">{{ $t('views.catalog.spinners') }}</router-link></li>
            <li><router-link to="/dev/components/skeleton" class="nav-link">{{ $t('views.catalog.skeletons') }}</router-link></li>
            <li><router-link to="/dev/components/empty-state" class="nav-link">{{ $t('views.catalog.emptyStates') }}</router-link></li>
            <li><router-link to="/dev/components/error-state" class="nav-link">{{ $t('views.catalog.errorStates') }}</router-link></li>
            <li><router-link to="/dev/components/confirm-modal" class="nav-link">{{ $t('views.catalog.confirmModals') }}</router-link></li>
            <li><router-link to="/dev/components/info-modal" class="nav-link">{{ $t('views.catalog.infoModals') }}</router-link></li>
            <li><router-link to="/dev/components/toast" class="nav-link">{{ $t('views.catalog.toasts') }}</router-link></li>
            <li><router-link to="/dev/components/alert" class="nav-link">{{ $t('views.catalog.alerts') }}</router-link></li>
            <li><router-link to="/dev/components/tooltip" class="nav-link">{{ $t('views.catalog.tooltips') }}</router-link></li>
          </ul>
        </div>
        <div class="nav-section">
          <h2 class="nav-title">{{ $t('views.catalog.inputComponents') }}</h2>
          <ul class="nav-list">
            <li><router-link to="/dev/components/text-input" class="nav-link">{{ $t('views.catalog.textInputs') }}</router-link></li>
            <li><router-link to="/dev/components/number-input" class="nav-link">{{ $t('views.catalog.numberInputs') }}</router-link></li>
            <li><router-link to="/dev/components/pin-input" class="nav-link">{{ $t('views.catalog.pinInputs') }}</router-link></li>
            <li><router-link to="/dev/components/checkbox" class="nav-link">{{ $t('views.catalog.checkboxes') }}</router-link></li>
            <li><router-link to="/dev/components/toggle" class="nav-link">{{ $t('views.catalog.toggles') }}</router-link></li>
            <li><router-link to="/dev/components/select" class="nav-link">{{ $t('views.catalog.selects') }}</router-link></li>
            <li><router-link to="/dev/components/radio-group" class="nav-link">{{ $t('views.catalog.radioGroups') }}</router-link></li>
          </ul>
        </div>
        <div class="nav-section">
          <h2 class="nav-title">{{ $t('views.catalog.navigationComponents') }}</h2>
          <ul class="nav-list">
            <li><router-link to="/dev/components/sidebar" class="nav-link">{{ $t('views.catalog.sidebars') }}</router-link></li>
            <li><router-link to="/dev/components/tabs" class="nav-link">{{ $t('views.catalog.tabsComponent') }}</router-link></li>
            <li><router-link to="/dev/components/breadcrumb" class="nav-link">{{ $t('views.catalog.breadcrumbs') }}</router-link></li>
            <li><router-link to="/dev/components/back-button" class="nav-link">{{ $t('views.catalog.backButtons') }}</router-link></li>
          </ul>
        </div>
        <div class="nav-section">
          <h2 class="nav-title">{{ $t('views.catalog.contentComponents') }}</h2>
          <ul class="nav-list">
            <li><router-link to="/dev/components/card" class="nav-link">{{ $t('views.catalog.cards') }}</router-link></li>
            <li><router-link to="/dev/components/avatar" class="nav-link">{{ $t('views.catalog.avatars') }}</router-link></li>
            <li><router-link to="/dev/components/badge" class="nav-link">{{ $t('views.catalog.badges') }}</router-link></li>
            <li><router-link to="/dev/components/list" class="nav-link">{{ $t('views.catalog.lists') }}</router-link></li>
            <li><router-link to="/dev/components/grid" class="nav-link">{{ $t('views.catalog.grids') }}</router-link></li>
          </ul>
        </div>
        <div class="nav-section">
          <h2 class="nav-title">{{ $t('views.catalog.progressComponents') }}</h2>
          <ul class="nav-list">
            <li><router-link to="/dev/components/progress-bar" class="nav-link">{{ $t('views.catalog.progressBars') }}</router-link></li>
            <li><router-link to="/dev/components/stepper" class="nav-link">{{ $t('views.catalog.steppers') }}</router-link></li>
            <li><router-link to="/dev/components/counter" class="nav-link">{{ $t('views.catalog.counters') }}</router-link></li>
          </ul>
        </div>
        <div class="nav-section">
          <h2 class="nav-title">{{ $t('views.catalog.sessionComponents') }}</h2>
          <ul class="nav-list">
            <li><router-link to="/dev/components/auth-screen" class="nav-link">{{ $t('views.catalog.authScreens') }}</router-link></li>
            <li><router-link to="/dev/components/session-indicator" class="nav-link">{{ $t('views.catalog.sessionIndicators') }}</router-link></li>
            <li><router-link to="/dev/components/inactivity-overlay" class="nav-link">{{ $t('views.catalog.inactivityOverlays') }}</router-link></li>
          </ul>
        </div>
        <div class="nav-section">
          <h2 class="nav-title">{{ $t('views.catalog.theme') }}</h2>
          <ul class="nav-list">
            <li>
              <button @click="toggleTheme" class="nav-link theme-toggle">
                {{ getCurrentTheme() === 'light' ? $t('views.catalog.lightMode') : $t('views.catalog.darkMode') }}
              </button>
            </li>
          </ul>
        </div>
      </nav>
    </aside>
    <main class="catalog-content">
      <slot />
    </main>
  </div>
</template>

<script setup lang="ts">
/**
 * Layout base del catálogo de componentes
 * 
 * Proporciona navegación por categorías y alternancia de tema
 */

import { useTheme } from '../../composables/useTheme'

const { toggleTheme, getCurrentTheme } = useTheme()
</script>

<style scoped>
.catalog-layout {
  display: flex;
  min-height: 100vh;
  background-color: var(--nubi-bg-surface);
  color: var(--nubi-text-primary);
}

.catalog-sidebar {
  width: 280px;
  background-color: var(--nubi-bg-surface-secondary);
  border-right: 1px solid var(--nubi-border-default);
  padding: var(--nubi-spacing-lg);
  overflow-y: auto;
}

.catalog-header {
  margin-bottom: var(--nubi-spacing-xl);
}

.catalog-title {
  font-size: var(--nubi-font-size-xl);
  font-weight: var(--nubi-font-weight-bold);
  color: var(--nubi-text-primary);
  margin-bottom: var(--nubi-spacing-xs);
}

.catalog-subtitle {
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-secondary);
}

.catalog-nav {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-lg);
}

.nav-section {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-sm);
}

.nav-title {
  font-size: var(--nubi-font-size-xs);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-tertiary);
  text-transform: uppercase;
  letter-spacing: var(--nubi-letter-spacing-wide);
}

.nav-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-xs);
}

.nav-link {
  display: block;
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
  color: var(--nubi-text-secondary);
  text-decoration: none;
  border-radius: var(--nubi-radius-md);
  transition: var(--nubi-transition-colors);
  font-size: var(--nubi-font-size-sm);
}

.nav-link:hover {
  background-color: var(--nubi-bg-surface-tertiary);
  color: var(--nubi-text-primary);
}

.theme-toggle {
  background: none;
  border: none;
  cursor: pointer;
  width: 100%;
  text-align: left;
}

.catalog-content {
  flex: 1;
  padding: var(--nubi-spacing-2xl);
  overflow-y: auto;
}
</style>
