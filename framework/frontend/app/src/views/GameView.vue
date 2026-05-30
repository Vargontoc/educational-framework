<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'

const { t } = useI18n()
const route = useRoute()
const childId = route.params.childId
</script>

<template>
  <main class="game-view" aria-labelledby="game-title">
    <section class="game-view__world">
      <div class="game-view__cloud game-view__cloud--one" aria-hidden="true"></div>
      <div class="game-view__cloud game-view__cloud--two" aria-hidden="true"></div>
      <div class="game-view__path" aria-hidden="true">
        <span class="game-view__node game-view__node--done"></span>
        <span class="game-view__node game-view__node--available"></span>
        <span class="game-view__node game-view__node--locked"></span>
      </div>
      <div class="game-view__card">
        <p class="game-view__eyebrow">{{ t('game.mapEyebrow') }}</p>
        <h1 id="game-title">{{ t('game.title') }}</h1>
        <p>{{ t('game.placeholder', { childId }) }}</p>
      </div>
    </section>
  </main>
</template>

<style scoped>
.game-view {
  min-height: 100vh;
  min-height: 100dvh;
  background: linear-gradient(180deg, var(--color-sky) 0 66%, var(--color-grass) 67% 100%);
  overflow: hidden;
}

.game-view__world {
  position: relative;
  display: grid;
  min-height: 100vh;
  min-height: 100dvh;
  place-items: center;
  padding: var(--space-lg);
}

.game-view__cloud {
  position: absolute;
  width: 150px;
  height: 54px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.86);
  animation: drift 28s ease-in-out infinite alternate;
}

.game-view__cloud::before,
.game-view__cloud::after {
  content: '';
  position: absolute;
  bottom: 18px;
  border-radius: 50%;
  background: inherit;
}

.game-view__cloud::before {
  left: 22px;
  width: 62px;
  height: 62px;
}

.game-view__cloud::after {
  right: 28px;
  width: 78px;
  height: 78px;
}

.game-view__cloud--one {
  top: 12%;
  left: 8%;
}

.game-view__cloud--two {
  top: 20%;
  right: 10%;
  transform: scale(0.74);
  animation-duration: 34s;
}

.game-view__path {
  position: absolute;
  right: 10vw;
  bottom: 16vh;
  display: flex;
  align-items: center;
  gap: var(--space-lg);
}

.game-view__node {
  width: 82px;
  height: 82px;
  border-radius: 50%;
  border: 8px solid rgba(255, 255, 255, 0.72);
  box-shadow: 0 14px 30px rgba(26, 35, 64, 0.12);
}

.game-view__node--done {
  background: var(--color-success-child);
}

.game-view__node--available {
  min-width: var(--touch-target-child-primary);
  min-height: var(--touch-target-child-primary);
  background: var(--color-celebration);
  animation: call-attention 2s ease-in-out infinite;
}

.game-view__node--locked {
  background: var(--color-disabled);
  opacity: 0.5;
}

.game-view__card {
  position: relative;
  z-index: 1;
  max-width: 560px;
  padding: var(--space-lg);
  border-radius: 36px;
  background: rgba(255, 255, 255, 0.74);
  box-shadow: 0 22px 70px rgba(26, 35, 64, 0.12);
  text-align: center;
}

.game-view__card h1 {
  margin-bottom: var(--space-sm);
  font-size: var(--font-size-game-instruction);
  font-weight: 800;
  line-height: 1.5;
}

.game-view__card p {
  color: var(--color-text-secondary);
  font-size: var(--font-size-game-label);
  line-height: 1.5;
}

.game-view__eyebrow {
  color: var(--color-primary) !important;
  font-size: var(--font-size-button) !important;
  font-weight: 800;
}

@keyframes drift {
  to {
    transform: translateX(42px);
  }
}

@keyframes call-attention {
  0%,
  100% {
    transform: scale(1);
    box-shadow: 0 14px 30px rgba(26, 35, 64, 0.12), 0 0 0 0 rgba(245, 166, 35, 0.34);
  }

  50% {
    transform: scale(1.03);
    box-shadow: 0 14px 30px rgba(26, 35, 64, 0.12), 0 0 0 16px rgba(245, 166, 35, 0);
  }
}
</style>
