<script setup lang="ts">
import Button from '@/components/ui/Button.vue'
import Card from '@/components/ui/Card.vue'
import Badge from '@/components/ui/Badge.vue'
import { mockCards, mockBadges } from '@/mock/index'
</script>

<template>
  <main class="ds-page">
    <header class="ds-header">
      <h1>Design System</h1>
      <p>Component library preview — development only</p>
    </header>

    <!-- Color palette -->
    <section class="ds-section">
      <h2>Colors</h2>
      <div class="ds-row ds-row--wrap">
        <div class="ds-swatch ds-swatch--primary"><span>Primary</span></div>
        <div class="ds-swatch ds-swatch--primary-dark"><span>Primary Dark</span></div>
        <div class="ds-swatch ds-swatch--secondary"><span>Secondary</span></div>
        <div class="ds-swatch ds-swatch--neutral"><span>Neutral</span></div>
      </div>
    </section>

    <!-- Typography -->
    <section class="ds-section">
      <h2>Typography</h2>
      <p class="ds-type--sm">Small — 14px: The quick brown fox</p>
      <p class="ds-type--md">Medium — 18px: The quick brown fox</p>
      <p class="ds-type--lg">Large — 24px: The quick brown fox</p>
    </section>

    <!-- Buttons -->
    <section class="ds-section">
      <h2>Button</h2>
      <div class="ds-row">
        <Button variant="primary">Primary</Button>
        <Button variant="secondary">Secondary</Button>
        <Button variant="primary" :disabled="true">Disabled</Button>
      </div>
    </section>

    <!-- Badges -->
    <section class="ds-section">
      <h2>Badge</h2>
      <div class="ds-row">
        <Badge
          v-for="(badge, i) in mockBadges"
          :key="badge.id"
          :label="badge.label"
          :variant="i === 0 ? 'primary' : i === 1 ? 'secondary' : 'primary'"
          :disabled="i === 2"
        />
      </div>
    </section>

    <!-- Cards -->
    <section class="ds-section">
      <h2>Card</h2>
      <div class="ds-row ds-row--wrap">
        <Card
          v-for="(card, i) in mockCards"
          :key="card.id"
          :title="card.title"
          :description="card.description"
          :icon="card.icon"
          :variant="i % 2 === 0 ? 'primary' : 'secondary'"
          :disabled="i === 2"
        />
      </div>
    </section>
  </main>
</template>

<style scoped>
.ds-page {
  max-width: 900px;
  margin: 0 auto;
  padding: var(--space-lg);
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.ds-header h1 {
  font-size: var(--font-size-lg);
  color: var(--color-primary);
}

.ds-header p {
  font-size: var(--font-size-sm);
  color: #6b7280;
}

.ds-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.ds-section h2 {
  font-size: var(--font-size-md);
  font-weight: 700;
  border-bottom: 2px solid var(--color-neutral);
  padding-bottom: var(--space-sm);
}

.ds-row {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: var(--space-md);
}

.ds-row--wrap {
  flex-wrap: wrap;
}

/* Color swatches */
.ds-swatch {
  width: 120px;
  height: 80px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: flex-end;
  padding: var(--space-sm);
}

.ds-swatch span {
  font-size: var(--font-size-sm);
  font-weight: 700;
}

.ds-swatch--primary        { background-color: var(--color-primary); }
.ds-swatch--primary        span { color: #fff; }
.ds-swatch--primary-dark   { background-color: var(--color-primary-dark); }
.ds-swatch--primary-dark   span { color: #fff; }
.ds-swatch--secondary      { background-color: var(--color-secondary); }
.ds-swatch--secondary      span { color: var(--color-text-on-secondary); }
.ds-swatch--neutral        { background-color: var(--color-neutral); border: 1px solid #d1d5db; }
.ds-swatch--neutral        span { color: #1f2937; }

/* Typography specimens */
.ds-type--sm { font-size: var(--font-size-sm); }
.ds-type--md { font-size: var(--font-size-md); }
.ds-type--lg { font-size: var(--font-size-lg); font-weight: 700; }
</style>
