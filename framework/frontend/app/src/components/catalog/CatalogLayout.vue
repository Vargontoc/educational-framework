<template>
  <div class="catalog-layout">
    <aside class="catalog-sidebar">
      <div class="catalog-header">
        <h1 class="catalog-title">Nubi Component Catalog</h1>
        <p class="catalog-subtitle">Design System & Components</p>
      </div>
      <nav class="catalog-nav">
        <div class="nav-section">
          <h2 class="nav-title">Foundation</h2>
          <ul class="nav-list">
            <li><a href="#colors" class="nav-link">Colors</a></li>
            <li><a href="#typography" class="nav-link">Typography</a></li>
            <li><a href="#spacing" class="nav-link">Spacing</a></li>
            <li><a href="#borders" class="nav-link">Borders & Shadows</a></li>
          </ul>
        </div>
        <div class="nav-section">
          <h2 class="nav-title">Components</h2>
          <ul class="nav-list">
            <li><a href="#buttons" class="nav-link">Buttons</a></li>
            <li><a href="#inputs" class="nav-link">Inputs</a></li>
            <li><a href="#cards" class="nav-link">Cards</a></li>
            <li><a href="#modals" class="nav-link">Modals</a></li>
          </ul>
        </div>
        <div class="nav-section">
          <h2 class="nav-title">Theme</h2>
          <ul class="nav-list">
            <li>
              <button @click="toggleTheme" class="nav-link theme-toggle">
                {{ getCurrentTheme() === 'light' ? '☀️ Light Mode' : '🌙 Dark Mode' }}
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
