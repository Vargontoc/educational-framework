<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<template>
  <main class="panel" aria-labelledby="panel-title">
    <aside class="panel__sidebar" aria-label="Panel navigation">
      <div class="panel__brand">A+C</div>
      <nav class="panel__nav">
        <a href="#settings">{{ t('panel.nav.settings') }}</a>
        <a href="#children">{{ t('panel.nav.children') }}</a>
        <a href="#agent">{{ t('panel.nav.agent') }}</a>
        <a class="panel__experience" href="#reading">{{ t('panel.nav.familyReading') }}</a>
      </nav>
    </aside>
    <section class="panel__content">
      <p class="panel__eyebrow">{{ t('panel.eyebrow') }}</p>
      <h1 id="panel-title">{{ t('panel.title') }}</h1>
      <div class="panel__grid">
        <article class="panel__card">
          <span class="panel__icon" aria-hidden="true">S</span>
          <h2>{{ t('panel.cards.settingsTitle') }}</h2>
          <p>{{ t('panel.cards.settingsDescription') }}</p>
        </article>
        <article class="panel__card panel__card--warm">
          <span class="panel__icon" aria-hidden="true">N</span>
          <h2>{{ t('panel.cards.childrenTitle') }}</h2>
          <p>{{ t('panel.cards.childrenDescription') }}</p>
        </article>
      </div>
    </section>
  </main>
</template>

<style scoped>
.panel {
  display: grid;
  grid-template-columns: 220px 1fr;
  min-height: 100vh;
  min-height: 100dvh;
  background: var(--color-panel-bg);
}

.panel__sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
  padding: var(--space-lg) var(--space-md);
  background: var(--color-surface);
  box-shadow: 12px 0 36px rgba(26, 35, 64, 0.08);
}

.panel__brand {
  display: grid;
  width: 64px;
  height: 64px;
  place-items: center;
  border-radius: 20px;
  background: var(--color-primary);
  color: var(--color-text-on-primary);
  font-weight: 800;
}

.panel__nav {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.panel__nav a {
  min-height: var(--touch-target-adult);
  padding: 12px var(--space-md);
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  font-size: var(--font-size-button);
  font-weight: 700;
  text-decoration: none;
}

.panel__nav a:hover {
  background: var(--color-neutral);
  color: var(--color-primary);
}

.panel__experience {
  margin-top: var(--space-md);
  border-left: 4px solid var(--color-celebration);
  background: color-mix(in srgb, var(--color-celebration) 12%, white);
}

.panel__content {
  padding: var(--space-xl);
}

.panel__eyebrow {
  margin-bottom: var(--space-sm);
  color: var(--color-primary);
  font-size: var(--font-size-caption);
  font-weight: 800;
}

.panel h1 {
  margin-bottom: var(--space-lg);
  font-size: var(--font-size-section-title);
  line-height: 1.4;
}

.panel__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-md);
}

.panel__card {
  min-height: 210px;
  padding: var(--space-lg);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  box-shadow: 0 18px 40px rgba(26, 35, 64, 0.08);
}

.panel__card--warm {
  border-top: 6px solid var(--color-celebration);
}

.panel__icon {
  display: grid;
  width: 48px;
  height: 48px;
  margin-bottom: var(--space-md);
  place-items: center;
  border-radius: 16px;
  background: var(--color-neutral);
  color: var(--color-primary);
  font-size: 22px;
}

.panel__card h2 {
  margin-bottom: var(--space-sm);
  font-size: var(--font-size-section-title);
  line-height: 1.4;
}

.panel__card p {
  color: var(--color-text-secondary);
  font-size: var(--font-size-body);
}

@media (max-width: 768px) {
  .panel {
    grid-template-columns: 64px 1fr;
  }

  .panel__sidebar {
    align-items: center;
    padding: var(--space-md) var(--space-sm);
  }

  .panel__brand {
    width: 48px;
    height: 48px;
    font-size: var(--font-size-caption);
  }

  .panel__nav a {
    width: 48px;
    overflow: hidden;
    padding: 12px 0;
    text-align: center;
    white-space: nowrap;
  }

  .panel__content {
    padding: var(--space-lg);
  }

  .panel__grid {
    grid-template-columns: 1fr;
  }
}
</style>
